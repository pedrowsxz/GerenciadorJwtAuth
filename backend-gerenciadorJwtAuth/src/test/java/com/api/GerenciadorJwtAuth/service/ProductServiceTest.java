package com.api.GerenciadorJwtAuth.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CityRepository cityRepository;

    @InjectMocks
    private ProductService productService;

    private Product product;
    private ProductDTO productDTO;
    private User user;
    private City city;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setRole(Role.USER);

        city = new City();
        city.setId(1L);

        product = new Product();
        product.setId(1L);
        product.setProductCode("CODE123");
        product.setProductName("Test Product");
        product.setProductValue(100.0);
        product.setStock(10);
        product.setUser(user);
        product.setCity(city);

        productDTO = new ProductDTO();
        productDTO.setId(1L);
        productDTO.setProductCode("CODE123");
        productDTO.setProductName("Test Product");
        productDTO.setProductValue(100.0);
        productDTO.setStock(10);
        productDTO.setUserId(1L);
        productDTO.setCityId(1L);
    }

    @Test
    void getAllProducts_ShouldReturnListOfProducts() {
        when(productRepository.findAll()).thenReturn(Collections.singletonList(product));

        List<ProductDTO> result = productService.getAllProducts();

        assertEquals(1, result.size());
        assertEquals("CODE123", result.get(0).getProductCode());
        verify(productRepository).findAll();
    }

    @Test
    void getProductById_WhenExists_ShouldReturnProductDTO() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        ProductDTO result = productService.getProductById(1L);

        assertNotNull(result);
        assertEquals("Test Product", result.getProductName());
        verify(productRepository).findById(1L);
    }

    @Test
    void getProductById_WhenNotExists_ShouldThrowException() {
        when(productRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                productService.getProductById(1L));
    }

    @Test
    void createProduct_WhenProductCodeExists_ShouldThrowException() {
        when(productRepository.existsByProductCode("CODE123")).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () ->
                productService.createProduct(productDTO, 1L));
    }

    @Test
    void createProduct_WhenUserNotFound_ShouldThrowException() {
        when(productRepository.existsByProductCode("CODE123")).thenReturn(false);
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                productService.createProduct(productDTO, 1L));
    }

    @Test
    void createProduct_WhenCityNotFound_ShouldThrowException() {
        when(productRepository.existsByProductCode("CODE123")).thenReturn(false);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(cityRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                productService.createProduct(productDTO, 1L));
    }

    @Test
    void createProduct_WithValidData_ShouldReturnCreatedProduct() {
        when(productRepository.existsByProductCode("CODE123")).thenReturn(false);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(cityRepository.findById(1L)).thenReturn(Optional.of(city));
        when(productRepository.save(any(Product.class))).thenReturn(product);

        ProductDTO result = productService.createProduct(productDTO, 1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(productRepository).save(any(Product.class));
    }

    @Test
    void updateProduct_WhenUnauthorizedUser_ShouldThrowException() {
        User otherUser = new User();
        otherUser.setId(2L);
        otherUser.setRole(Role.USER);

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(userRepository.findById(2L)).thenReturn(Optional.of(otherUser));

        assertThrows(UnauthorizedException.class, () ->
                productService.updateProduct(1L, productDTO, 2L));
    }

    @Test
    void updateProduct_WhenAdminUser_ShouldAllowUpdate() {
        User adminUser = new User();
        adminUser.setId(2L);
        adminUser.setRole(Role.ADMIN);

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(userRepository.findById(2L)).thenReturn(Optional.of(adminUser));
        when(cityRepository.findById(1L)).thenReturn(Optional.of(city));
        when(productRepository.save(any(Product.class))).thenReturn(product);

        ProductDTO result = productService.updateProduct(1L, productDTO, 2L);

        assertNotNull(result);
        verify(productRepository).save(any(Product.class));
    }

    @Test
    void updateProduct_WhenProductCodeConflict_ShouldThrowException() {
        ProductDTO newProductDTO = new ProductDTO();
        newProductDTO.setProductCode("NEWCODE");

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productRepository.existsByProductCode("NEWCODE")).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () ->
                productService.updateProduct(1L, newProductDTO, 1L));
    }

    @Test
    void deleteProduct_WhenUnauthorizedUser_ShouldThrowException() {
        User otherUser = new User();
        otherUser.setId(2L);
        otherUser.setRole(Role.USER);

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(userRepository.findById(2L)).thenReturn(Optional.of(otherUser));

        assertThrows(UnauthorizedException.class, () ->
                productService.deleteProduct(1L, 2L));
    }

    @Test
    void deleteProduct_WhenAuthorized_ShouldDeleteProduct() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        productService.deleteProduct(1L, 1L);

        verify(productRepository).deleteById(1L);
    }

    @Test
    void deleteProduct_WhenAdmin_ShouldAllowDelete() {
        User adminUser = new User();
        adminUser.setId(2L);
        adminUser.setRole(Role.ADMIN);

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(userRepository.findById(2L)).thenReturn(Optional.of(adminUser));

        productService.deleteProduct(1L, 2L);

        verify(productRepository).deleteById(1L);
    }
}