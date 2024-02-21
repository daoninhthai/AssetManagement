package com.warehouse.inventory.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AiQueryRequest {

    @NotBlank(message = "Câu hỏi không được để trống")
    private String question;

    @Builder.Default
    private String language = "vi";
}
