package com.warehouse.inventory.controller;

import com.warehouse.inventory.dto.request.AiQueryRequest;
import com.warehouse.inventory.dto.request.ForecastRequest;
import com.warehouse.inventory.dto.response.AiQueryResponse;
import com.warehouse.inventory.dto.response.AnomalyResult;
import com.warehouse.inventory.dto.response.ApiResponse;
import com.warehouse.inventory.dto.response.ForecastResponse;
import com.warehouse.inventory.dto.response.ReorderSuggestion;
import com.warehouse.inventory.exception.AiServiceException;
import com.warehouse.inventory.service.AiIntegrationService;
import com.warehouse.inventory.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Collections;
import java.util.List;

@Slf4j
@Controller
@RequiredArgsConstructor
public class AiController {

    private final AiIntegrationService aiIntegrationService;
    private final ProductService productService;

    @GetMapping("/ai")
    public String insights(Model model) {
        log.debug("Loading AI insights page");
        model.addAttribute("products", productService.findAll());
        return "ai/insights";
    }

    @GetMapping("/ai/forecast")
    public String forecastPage(Model model) {
        log.debug("Loading AI forecast page");
        model.addAttribute("forecastRequest", new ForecastRequest());
        model.addAttribute("products", productService.findAll());
        return "ai/forecast";
    }

    @PostMapping("/ai/forecast")
    public String runForecast(@ModelAttribute ForecastRequest forecastRequest, Model model) {
        log.debug("Running AI forecast for product: {}", forecastRequest.getProductId());
        model.addAttribute("forecastRequest", forecastRequest);
        model.addAttribute("products", productService.findAll());

        try {
            ForecastResponse forecast = aiIntegrationService.getForecast(forecastRequest.getProductId());
            model.addAttribute("forecast", forecast);
        } catch (AiServiceException e) {
            model.addAttribute("errorMessage", e.getMessage());
        }

        return "ai/forecast";
    }

    @GetMapping("/ai/query")
    public String queryPage(Model model) {
        log.debug("Loading AI query page");
        model.addAttribute("queryRequest", new AiQueryRequest());
        return "ai/query";
    }

    @PostMapping("/ai/query")
    public String processQuery(@ModelAttribute AiQueryRequest queryRequest, Model model) {
        log.debug("Processing AI query: {}", queryRequest.getQuestion());
        model.addAttribute("queryRequest", queryRequest);

        try {
            String language = queryRequest.getLanguage() != null ? queryRequest.getLanguage() : "vi";
            AiQueryResponse response = aiIntegrationService.queryNaturalLanguage(
                    queryRequest.getQuestion(), language);
            model.addAttribute("queryResponse", response);
        } catch (AiServiceException e) {
            model.addAttribute("errorMessage", e.getMessage());
        }

        return "ai/query";
    }

    @GetMapping("/api/ai/suggestions")
    @ResponseBody
    public ResponseEntity<ApiResponse<List<ReorderSuggestion>>> getReorderSuggestions() {
        log.debug("REST: Getting AI reorder suggestions");
        try {
            List<ReorderSuggestion> suggestions = aiIntegrationService.getReorderSuggestions();
            return ResponseEntity.ok(ApiResponse.success("Gợi ý đặt hàng lại", suggestions));
        } catch (Exception e) {
            log.error("Error getting reorder suggestions: {}", e.getMessage());
            return ResponseEntity.ok(ApiResponse.success("Không có gợi ý", Collections.emptyList()));
        }
    }

    @GetMapping("/api/ai/anomalies")
    @ResponseBody
    public ResponseEntity<ApiResponse<List<AnomalyResult>>> getAnomalies() {
        log.debug("REST: Getting AI anomaly detection results");
        try {
            List<AnomalyResult> anomalies = aiIntegrationService.detectAnomalies();
            return ResponseEntity.ok(ApiResponse.success("Phát hiện bất thường", anomalies));
        } catch (AiServiceException e) {
            log.error("Error detecting anomalies: {}", e.getMessage());
            return ResponseEntity.ok(ApiResponse.error(e.getMessage()));
        }
    }
}
