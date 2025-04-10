package com.api.GerenciadorJwtAuth.repository;

import com.api.GerenciadorJwtAuth.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    List<Product> findByUserId(Long userId);

    List<Product> findByCityId(Long cityId);

    Optional<Product> findByProductCode(String productCode);

    boolean existsByProductCode(String productCode);
}