package com.api.GerenciadorJwtAuth.controller;

import com.api.GerenciadorJwtAuth.dto.ProductDTO;
import com.api.GerenciadorJwtAuth.exception.ResourceNotFoundException;
import com.api.GerenciadorJwtAuth.exception.UnauthorizedException;
import com.api.GerenciadorJwtAuth.security.JwtTokenProvider;
import com.api.GerenciadorJwtAuth.service.ProductService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class ProductControllerTest {

    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private ProductService productService;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    private ProductController productController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(productController).build();
    }

    @Test
    void getAllProducts_ReturnsProductsList() throws Exception {
        ProductDTO product1 = createProductDTO(1L, "P001");
        ProductDTO product2 = createProductDTO(2L, "P002");
        List<ProductDTO> products = Arrays.asList(product1, product2);

        when(productService.getAllProducts()).thenReturn(products);

        mockMvc.perform(get("/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].productCode").value("P001"))
                .andExpect(jsonPath("$[1].productCode").value("P002"));
    }

    @Test
    void getProductById_ProductExists_ReturnsProduct() throws Exception {
        ProductDTO product = createProductDTO(1L, "P001");
        when(productService.getProductById(1L)).thenReturn(product);

        mockMvc.perform(get("/products/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.productCode").value("P001"));
    }

    @Test
    void getProductById_ProductNotFound_ReturnsNotFound() throws Exception {
        when(productService.getProductById(1L))
                .thenThrow(new ResourceNotFoundException("Product", "id", 1L));

        mockMvc.perform(get("/products/1"))
                .andExpect(status().isNotFound());
    }

    @Test
    void createProduct_ValidRequest_ReturnsCreatedProduct() throws Exception {
        ProductDTO inputDTO = createProductDTO(null, "P001");
        ProductDTO savedDTO = createProductDTO(1L, "P001");

        when(jwtTokenProvider.extractUserId("valid.token")).thenReturn(123L);
        when(productService.createProduct(eq(inputDTO), eq(123L))).thenReturn(savedDTO);

        mockMvc.perform(post("/products")
                        .header("Authorization", "Bearer valid.token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.productCode").value("P001"));
    }

    @Test
    void createProduct_DuplicateProductCode_ReturnsBadRequest() throws Exception {
        ProductDTO inputDTO = createProductDTO(null, "P001");

        when(jwtTokenProvider.extractUserId("valid.token")).thenReturn(123L);
        when(productService.createProduct(eq(inputDTO), eq(123L)))
                .thenThrow(new IllegalArgumentException("Product code already exists"));

        mockMvc.perform(post("/products")
                        .header("Authorization", "Bearer valid.token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateProduct_ValidRequest_ReturnsUpdatedProduct() throws Exception {
        ProductDTO inputDTO = createProductDTO(1L, "P001");
        ProductDTO updatedDTO = createProductDTO(1L, "P001");

        when(jwtTokenProvider.extractUserId("valid.token")).thenReturn(123L);
        when(productService.updateProduct(eq(1L), eq(inputDTO), eq(123L))).thenReturn(updatedDTO);

        mockMvc.perform(put("/products/1")
                        .header("Authorization", "Bearer valid.token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    void updateProduct_UnauthorizedUser_ReturnsForbidden() throws Exception {
        ProductDTO inputDTO = createProductDTO(1L, "P001");

        when(jwtTokenProvider.extractUserId("valid.token")).thenReturn(456L);
        when(productService.updateProduct(eq(1L), eq(inputDTO), eq(456L)))
                .thenThrow(new UnauthorizedException("Permission denied"));

        mockMvc.perform(put("/products/1")
                        .header("Authorization", "Bearer valid.token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputDTO)))
                .andExpect(status().isForbidden());
    }

    @Test
    void deleteProduct_ValidRequest_ReturnsNoContent() throws Exception {
        when(jwtTokenProvider.extractUserId("valid.token")).thenReturn(123L);
        doNothing().when(productService).deleteProduct(1L, 123L);

        mockMvc.perform(delete("/products/1")
                        .header("Authorization", "Bearer valid.token"))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteProduct_UnauthorizedUser_ReturnsForbidden() throws Exception {
        when(jwtTokenProvider.extractUserId("valid.token")).thenReturn(456L);
        doThrow(new UnauthorizedException("Permission denied"))
                .when(productService).deleteProduct(1L, 456L);

        mockMvc.perform(delete("/products/1")
                        .header("Authorization", "Bearer valid.token"))
                .andExpect(status().isForbidden());
    }

    private ProductDTO createProductDTO(Long id, String productCode) {
        ProductDTO dto = new ProductDTO();
        dto.setId(id);
        dto.setProductCode(productCode);
        dto.setProductName("Product Name");
        dto.setProductValue(100.0);
        dto.setStock(10);
        dto.setCityId(1L);
        dto.setUserId(1L);
        return dto;
    }
}