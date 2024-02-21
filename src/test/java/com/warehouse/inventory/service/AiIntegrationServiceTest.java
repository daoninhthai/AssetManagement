package com.warehouse.inventory.service;

import com.warehouse.inventory.dto.ForecastResponse;
import com.warehouse.inventory.dto.ReorderSuggestion;
import com.warehouse.inventory.exception.AiServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AiIntegrationService Unit Tests")
class AiIntegrationServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private AiIntegrationService aiIntegrationService;

    private static final String AI_SERVICE_URL = "http://localhost:8000";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(aiIntegrationService, "aiServiceUrl", AI_SERVICE_URL);
        ReflectionTestUtils.setField(aiIntegrationService, "timeout", 5000);
    }

    @Test
    @DisplayName("getForecast should return ForecastResponse on successful AI service call")
    void test_getForecast_success() {
        ForecastResponse expectedResponse = new ForecastResponse();
        expectedResponse.setProductId(1L);
        expectedResponse.setProductName("Wireless Mouse");
        expectedResponse.setForecastDays(30);
        expectedResponse.setPredictedDemand(450);
        expectedResponse.setConfidenceScore(0.87);
        expectedResponse.setSeasonalFactor(1.12);
        expectedResponse.setTrend("INCREASING");

        ResponseEntity<ForecastResponse> responseEntity =
                new ResponseEntity<>(expectedResponse, HttpStatus.OK);

        when(restTemplate.postForEntity(
                eq(AI_SERVICE_URL + "/api/v1/forecast"),
                any(),
                eq(ForecastResponse.class)
        )).thenReturn(responseEntity);

        ForecastResponse result = aiIntegrationService.getForecast(1L, 30);

        assertThat(result).isNotNull();
        assertThat(result.getProductId()).isEqualTo(1L);
        assertThat(result.getPredictedDemand()).isEqualTo(450);
        assertThat(result.getConfidenceScore()).isEqualTo(0.87);
        assertThat(result.getTrend()).isEqualTo("INCREASING");

        verify(restTemplate, times(1)).postForEntity(
                eq(AI_SERVICE_URL + "/api/v1/forecast"),
                any(),
                eq(ForecastResponse.class)
        );
    }

    @Test
    @DisplayName("getForecast should throw AiServiceException when AI service is unavailable")
    void test_getForecast_serviceDown() {
        when(restTemplate.postForEntity(
                eq(AI_SERVICE_URL + "/api/v1/forecast"),
                any(),
                eq(ForecastResponse.class)
        )).thenThrow(new ResourceAccessException("Connection refused"));

        assertThatThrownBy(() -> aiIntegrationService.getForecast(1L, 30))
                .isInstanceOf(AiServiceException.class)
                .hasMessageContaining("AI service")
                .hasMessageContaining("unavailable");

        verify(restTemplate, times(1)).postForEntity(
                eq(AI_SERVICE_URL + "/api/v1/forecast"),
                any(),
                eq(ForecastResponse.class)
        );
    }

    @Test
    @DisplayName("getReorderSuggestions should return a list of reorder suggestions")
    void test_getReorderSuggestions() {
        ReorderSuggestion suggestion1 = new ReorderSuggestion();
        suggestion1.setProductId(1L);
        suggestion1.setProductName("Wireless Mouse");
        suggestion1.setCurrentStock(8);
        suggestion1.setReorderQuantity(50);
        suggestion1.setEstimatedCost(1499.50);
        suggestion1.setUrgency("HIGH");

        ReorderSuggestion suggestion2 = new ReorderSuggestion();
        suggestion2.setProductId(2L);
        suggestion2.setProductName("Mechanical Keyboard");
        suggestion2.setCurrentStock(15);
        suggestion2.setReorderQuantity(30);
        suggestion2.setEstimatedCost(2399.70);
        suggestion2.setUrgency("MEDIUM");

        ReorderSuggestion[] suggestionsArray = {suggestion1, suggestion2};

        ResponseEntity<ReorderSuggestion[]> responseEntity =
                new ResponseEntity<>(suggestionsArray, HttpStatus.OK);

        when(restTemplate.getForEntity(
                eq(AI_SERVICE_URL + "/api/v1/reorder-suggestions"),
                eq(ReorderSuggestion[].class)
        )).thenReturn(responseEntity);

        List<ReorderSuggestion> result = aiIntegrationService.getReorderSuggestions();

        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getProductName()).isEqualTo("Wireless Mouse");
        assertThat(result.get(0).getUrgency()).isEqualTo("HIGH");
        assertThat(result.get(0).getReorderQuantity()).isEqualTo(50);
        assertThat(result.get(1).getProductName()).isEqualTo("Mechanical Keyboard");
        assertThat(result.get(1).getUrgency()).isEqualTo("MEDIUM");

        verify(restTemplate, times(1)).getForEntity(
                eq(AI_SERVICE_URL + "/api/v1/reorder-suggestions"),
                eq(ReorderSuggestion[].class)
        );
    }
}
