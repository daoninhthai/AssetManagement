package com.warehouse.inventory.service;

import com.warehouse.inventory.dto.response.AiQueryResponse;
import com.warehouse.inventory.dto.response.AnomalyResult;
import com.warehouse.inventory.dto.response.ForecastResponse;
import com.warehouse.inventory.dto.response.ReorderSuggestion;
import com.warehouse.inventory.entity.Product;
import com.warehouse.inventory.entity.StockMovement;
import com.warehouse.inventory.exception.AiServiceException;
import com.warehouse.inventory.repository.WarehouseStockRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AiIntegrationService {

    private final RestTemplate restTemplate;
    private final ProductService productService;
    private final StockMovementService stockMovementService;
    private final WarehouseStockRepository warehouseStockRepository;

    @Value("${ai.service.url:http://localhost:8000}")
    private String aiServiceUrl;

    /**
     * Get demand forecast for a specific product.
     * Sends historical movement data to Python FastAPI service.
     */
    @Cacheable(value = "forecasts", key = "#productId", unless = "#result == null")
    public ForecastResponse getForecast(Long productId) {
        log.info("Getting AI forecast for product: {}", productId);

        try {
            Product product = productService.findById(productId);
            Integer currentStock = warehouseStockRepository.getTotalStockByProductId(productId);

            // Prepare historical data
            List<StockMovement> movements = stockMovementService.findByProduct(productId);
            List<Map<String, Object>> historicalData = movements.stream()
                    .map(m -> {
                        Map<String, Object> entry = new HashMap<>();
                        entry.put("date", m.getCreatedAt().format(DateTimeFormatter.ISO_DATE));
                        entry.put("quantity", m.getQuantity());
                        entry.put("type", m.getType().name());
                        return entry;
                    })
                    .collect(Collectors.toList());

            // Build request payload
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("productId", productId);
            requestBody.put("productName", product.getName());
            requestBody.put("currentStock", currentStock != null ? currentStock : 0);
            requestBody.put("historicalData", historicalData);
            requestBody.put("days", 30);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            ResponseEntity<ForecastResponse> response = restTemplate.exchange(
                    aiServiceUrl + "/api/ai/forecast",
                    HttpMethod.POST,
                    entity,
                    ForecastResponse.class);

            ForecastResponse forecast = response.getBody();
            if (forecast != null) {
                forecast.setProductId(productId);
                forecast.setProductName(product.getName());
                forecast.setCurrentStock(currentStock != null ? currentStock : 0);
            }

            return forecast;

        } catch (RestClientException e) {
            log.error("Failed to get forecast from AI service: {}", e.getMessage());
            throw new AiServiceException("Không thể kết nối đến dịch vụ AI để dự báo nhu cầu", e);
        } catch (AiServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error getting forecast: {}", e.getMessage(), e);
            throw new AiServiceException("Lỗi không mong đợi khi gọi dịch vụ AI", e);
        }
    }

    /**
     * Get reorder suggestions for low stock products.
     */
    public List<ReorderSuggestion> getReorderSuggestions() {
        log.info("Getting AI reorder suggestions");

        try {
            List<Product> lowStockProducts = productService.findLowStock();

            if (lowStockProducts.isEmpty()) {
                log.debug("No low stock products found for reorder suggestions");
                return Collections.emptyList();
            }

            List<Map<String, Object>> productsData = lowStockProducts.stream()
                    .map(p -> {
                        Integer stock = warehouseStockRepository.getTotalStockByProductId(p.getId());
                        Map<String, Object> data = new HashMap<>();
                        data.put("productId", p.getId());
                        data.put("productName", p.getName());
                        data.put("currentStock", stock != null ? stock : 0);
                        data.put("minStockLevel", p.getMinStockLevel());
                        data.put("maxStockLevel", p.getMaxStockLevel());
                        data.put("reorderPoint", p.getReorderPoint());
                        data.put("unitPrice", p.getUnitPrice());
                        data.put("costPrice", p.getCostPrice());
                        return data;
                    })
                    .collect(Collectors.toList());

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("products", productsData);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            ResponseEntity<List<ReorderSuggestion>> response = restTemplate.exchange(
                    aiServiceUrl + "/api/ai/reorder",
                    HttpMethod.POST,
                    entity,
                    new ParameterizedTypeReference<List<ReorderSuggestion>>() {});

            return response.getBody() != null ? response.getBody() : Collections.emptyList();

        } catch (RestClientException e) {
            log.error("Failed to get reorder suggestions from AI service: {}", e.getMessage());
            // Fallback: generate basic suggestions without AI
            return generateBasicReorderSuggestions();
        } catch (Exception e) {
            log.error("Unexpected error getting reorder suggestions: {}", e.getMessage(), e);
            return generateBasicReorderSuggestions();
        }
    }

    /**
     * Detect anomalies in stock movement data.
     */
    public List<AnomalyResult> detectAnomalies() {
        log.info("Detecting anomalies via AI service");

        try {
            List<StockMovement> recentMovements = stockMovementService.getDailyStats(
                    LocalDateTime.now().minusDays(90), LocalDateTime.now());

            List<Map<String, Object>> movementData = recentMovements.stream()
                    .map(m -> {
                        Map<String, Object> data = new HashMap<>();
                        data.put("id", m.getId());
                        data.put("productId", m.getProduct().getId());
                        data.put("productName", m.getProduct().getName());
                        data.put("quantity", m.getQuantity());
                        data.put("type", m.getType().name());
                        data.put("createdAt", m.getCreatedAt().format(DateTimeFormatter.ISO_DATE_TIME));
                        return data;
                    })
                    .collect(Collectors.toList());

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("movements", movementData);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            ResponseEntity<List<AnomalyResult>> response = restTemplate.exchange(
                    aiServiceUrl + "/api/ai/anomaly",
                    HttpMethod.POST,
                    entity,
                    new ParameterizedTypeReference<List<AnomalyResult>>() {});

            return response.getBody() != null ? response.getBody() : Collections.emptyList();

        } catch (RestClientException e) {
            log.error("Failed to detect anomalies from AI service: {}", e.getMessage());
            throw new AiServiceException("Không thể kết nối đến dịch vụ AI để phát hiện bất thường", e);
        } catch (Exception e) {
            log.error("Unexpected error detecting anomalies: {}", e.getMessage(), e);
            throw new AiServiceException("Lỗi không mong đợi khi gọi dịch vụ AI", e);
        }
    }

    /**
     * Query the AI service with natural language.
     */
    public AiQueryResponse queryNaturalLanguage(String question, String language) {
        log.info("Processing NL query: '{}' (language: {})", question, language);

        try {
            // Build context with current inventory data
            long totalProducts = productService.count();
            List<Product> lowStock = productService.findLowStock();
            BigDecimal totalValue = warehouseStockRepository.getTotalStockValue();

            Map<String, Object> context = new HashMap<>();
            context.put("totalProducts", totalProducts);
            context.put("lowStockCount", lowStock.size());
            context.put("totalStockValue", totalValue != null ? totalValue : BigDecimal.ZERO);
            context.put("lowStockProducts", lowStock.stream()
                    .map(Product::getName)
                    .limit(10)
                    .collect(Collectors.toList()));

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("question", question);
            requestBody.put("language", language);
            requestBody.put("context", context);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            ResponseEntity<AiQueryResponse> response = restTemplate.exchange(
                    aiServiceUrl + "/api/ai/query",
                    HttpMethod.POST,
                    entity,
                    AiQueryResponse.class);

            AiQueryResponse result = response.getBody();
            if (result != null) {
                result.setQuestion(question);
            }
            return result;

        } catch (RestClientException e) {
            log.error("Failed to query AI service: {}", e.getMessage());
            throw new AiServiceException("Không thể kết nối đến dịch vụ AI để xử lý câu hỏi", e);
        } catch (Exception e) {
            log.error("Unexpected error querying AI: {}", e.getMessage(), e);
            throw new AiServiceException("Lỗi không mong đợi khi gọi dịch vụ AI", e);
        }
    }

    /**
     * Fallback: generate basic reorder suggestions without AI service.
     */
    private List<ReorderSuggestion> generateBasicReorderSuggestions() {
        log.info("Generating basic reorder suggestions (AI fallback)");

        List<Product> lowStockProducts = productService.findLowStock();
        List<ReorderSuggestion> suggestions = new ArrayList<>();

        for (Product product : lowStockProducts) {
            Integer currentStock = warehouseStockRepository.getTotalStockByProductId(product.getId());
            int stock = currentStock != null ? currentStock : 0;
            int reorderPoint = product.getReorderPoint() != null ? product.getReorderPoint() : 0;
            int maxLevel = product.getMaxStockLevel() != null ? product.getMaxStockLevel() : reorderPoint * 3;
            int reorderQty = maxLevel - stock;

            BigDecimal costPrice = product.getCostPrice() != null ? product.getCostPrice() : BigDecimal.ZERO;
            BigDecimal estimatedCost = costPrice.multiply(BigDecimal.valueOf(reorderQty));

            String urgency;
            int daysUntilStockout;
            if (stock == 0) {
                urgency = "CRITICAL";
                daysUntilStockout = 0;
            } else if (stock <= reorderPoint / 2) {
                urgency = "HIGH";
                daysUntilStockout = 3;
            } else {
                urgency = "MEDIUM";
                daysUntilStockout = 7;
            }

            suggestions.add(ReorderSuggestion.builder()
                    .productId(product.getId())
                    .productName(product.getName())
                    .currentStock(stock)
                    .reorderPoint(reorderPoint)
                    .reorderQuantity(reorderQty)
                    .estimatedCost(estimatedCost)
                    .urgency(urgency)
                    .daysUntilStockout(daysUntilStockout)
                    .build());
        }

        return suggestions;
    }
}
