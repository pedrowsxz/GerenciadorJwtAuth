package com.api.GerenciadorJwtAuth.controller;

import com.api.GerenciadorJwtAuth.dto.CityDTO;
import com.api.GerenciadorJwtAuth.service.CityService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class CityController {

    private final CityService cityService;

    @GetMapping("/cities")
    public ResponseEntity<List<CityDTO>> getAllCities() {
        return ResponseEntity.ok(cityService.getAllCities());
    }

    @GetMapping("/cities/{id}")
    public ResponseEntity<CityDTO> getCityById(@PathVariable Long id) {
        return ResponseEntity.ok(cityService.getCityById(id));
    }

    @PostMapping("/cities")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<CityDTO> createCity(@Valid @RequestBody CityDTO cityDTO) {
        return new ResponseEntity<>(cityService.createCity(cityDTO), HttpStatus.CREATED);
    }

    @PutMapping("/cities/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<CityDTO> updateCity(@PathVariable Long id, @Valid @RequestBody CityDTO cityDTO) {
        return ResponseEntity.ok(cityService.updateCity(id, cityDTO));
    }

    @DeleteMapping("/cities/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Void> deleteCity(@PathVariable Long id) {
        cityService.deleteCity(id);
        return ResponseEntity.noContent().build();
    }
}