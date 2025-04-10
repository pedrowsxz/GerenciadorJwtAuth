package com.api.GerenciadorJwtAuth.service;

import com.api.GerenciadorJwtAuth.dto.ProductDTO;
import com.api.GerenciadorJwtAuth.exception.ResourceNotFoundException;
import com.api.GerenciadorJwtAuth.exception.UnauthorizedException;
import com.api.GerenciadorJwtAuth.model.City;
import com.api.GerenciadorJwtAuth.model.Product;
import com.api.GerenciadorJwtAuth.model.Role;
import com.api.GerenciadorJwtAuth.model.User;
import com.api.GerenciadorJwtAuth.repository.CityRepository;
import com.api.GerenciadorJwtAuth.repository.ProductRepository;
import com.api.GerenciadorJwtAuth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final CityRepository cityRepository;

    @Transactional(readOnly = true)
    public List<ProductDTO> getAllProducts() {
        return productRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ProductDTO getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));
        return mapToDTO(product);
    }

    @Transactional
    public ProductDTO createProduct(ProductDTO productDTO, Long userId) {
        if (productRepository.existsByProductCode(productDTO.getProductCode())) {
            throw new IllegalArgumentException("Product code already exists");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        City city = cityRepository.findById(productDTO.getCityId())
                .orElseThrow(() -> new ResourceNotFoundException("City", "id", productDTO.getCityId()));

        Product product = new Product();
        product.setProductCode(productDTO.getProductCode());
        product.setProductName(productDTO.getProductName());
        product.setProductValue(productDTO.getProductValue());
        product.setStock(productDTO.getStock());
        product.setCity(city);
        product.setUser(user);

        Product savedProduct = productRepository.save(product);
        return mapToDTO(savedProduct);
    }

    @Transactional
    public ProductDTO updateProduct(Long id, ProductDTO productDTO, Long userId) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));

        if (!product.getUser().getId().equals(userId) &&
                !userRepository.findById(userId).orElseThrow().getRole().equals(Role.ADMIN)) {
            throw new UnauthorizedException("You don't have permission to update this product");
        }

        if (!product.getProductCode().equals(productDTO.getProductCode()) &&
                productRepository.existsByProductCode(productDTO.getProductCode())) {
            throw new IllegalArgumentException("Product code already exists");
        }

        City city = cityRepository.findById(productDTO.getCityId())
                .orElseThrow(() -> new ResourceNotFoundException("City", "id", productDTO.getCityId()));

        product.setProductCode(productDTO.getProductCode());
        product.setProductName(productDTO.getProductName());
        product.setProductValue(productDTO.getProductValue());
        product.setStock(productDTO.getStock());
        product.setCity(city);

        Product updatedProduct = productRepository.save(product);
        return mapToDTO(updatedProduct);
    }

    @Transactional
    public void deleteProduct(Long id, Long userId) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));

        if (!product.getUser().getId().equals(userId) &&
                !userRepository.findById(userId).orElseThrow().getRole().equals(Role.ADMIN)) {
            throw new UnauthorizedException("You don't have permission to delete this product");
        }

        productRepository.deleteById(id);
    }

    private ProductDTO mapToDTO(Product product) {
        ProductDTO dto = new ProductDTO();
        dto.setId(product.getId());
        dto.setProductCode(product.getProductCode());
        dto.setProductName(product.getProductName());
        dto.setProductValue(product.getProductValue());
        dto.setStock(product.getStock());
        dto.setCityId(product.getCity().getId());
        dto.setUserId(product.getUser().getId());
        return dto;
    }
}