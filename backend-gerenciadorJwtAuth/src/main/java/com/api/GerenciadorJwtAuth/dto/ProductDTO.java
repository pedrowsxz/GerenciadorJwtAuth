package com.api.GerenciadorJwtAuth.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductDTO {

    private Long id;

    @NotBlank(message = "Product code is required")
    @Size(min = 3, max = 20, message = "Product code must be between 3 and 20 characters")
    private String productCode;

    @NotBlank(message = "Product name is required")
    @Size(min = 3, max = 100, message = "Product name must be between 3 and 100 characters")
    private String productName;

    @NotNull(message = "Product value is required")
    @Min(value = 0, message = "Product value must be greater than or equal to zero")
    private Double productValue;

    @NotNull(message = "Stock is required")
    @Min(value = 0, message = "Stock must be greater than or equal to zero")
    private Integer stock;

    @NotNull(message = "City ID is required")
    private Long cityId;

    private Long userId;
}