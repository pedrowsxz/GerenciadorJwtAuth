package com.api.GerenciadorJwtAuth.controller;

import com.api.GerenciadorJwtAuth.security.JwtTokenProvider;
import com.api.GerenciadorJwtAuth.dto.ProductDTO;
import com.api.GerenciadorJwtAuth.service.ProductService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    private final JwtTokenProvider jwtTokenProvider;

    private Long getUserIdFromToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            return jwtTokenProvider.extractUserId(token);
        }
        return null;
    }

    @GetMapping("/products")
    public ResponseEntity<List<ProductDTO>> getAllProducts() {
        return ResponseEntity.ok(productService.getAllProducts());
    }

    @GetMapping("/products/{id}")
    public ResponseEntity<ProductDTO> getProductById(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getProductById(id));
    }

    @PostMapping("/products")
    public ResponseEntity<ProductDTO> createProduct(
            @Valid @RequestBody ProductDTO productDTO,
            HttpServletRequest request) {
        Long userId = getUserIdFromToken(request);
        return new ResponseEntity<>(productService.createProduct(productDTO, userId), HttpStatus.CREATED);
    }

    @PutMapping("/products/{id}")
    public ResponseEntity<ProductDTO> updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody ProductDTO productDTO,
            HttpServletRequest request) {
        Long userId = getUserIdFromToken(request);
        return ResponseEntity.ok(productService.updateProduct(id, productDTO, userId));
    }

    @DeleteMapping("/products/{id}")
    public ResponseEntity<Void> deleteProduct(
            @PathVariable Long id,
            HttpServletRequest request) {
        Long userId = getUserIdFromToken(request);
        productService.deleteProduct(id, userId);
        return ResponseEntity.noContent().build();
    }
}