package com.api.GerenciadorJwtAuth.service;

import com.api.GerenciadorJwtAuth.dto.CityDTO;
import com.api.GerenciadorJwtAuth.exception.ResourceNotFoundException;
import com.api.GerenciadorJwtAuth.model.City;
import com.api.GerenciadorJwtAuth.repository.CityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CityService {

    private final CityRepository cityRepository;

    @Transactional(readOnly = true)
    public List<CityDTO> getAllCities() {
        return cityRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CityDTO getCityById(Long id) {
        City city = cityRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("City", "id", id));
        return mapToDTO(city);
    }

    @Transactional
    public CityDTO createCity(CityDTO cityDTO) {
        if (cityRepository.existsByName(cityDTO.getName())) {
            throw new IllegalArgumentException("City name already exists");
        }

        City city = new City();
        city.setName(cityDTO.getName());
        city.setState(cityDTO.getState());

        City savedCity = cityRepository.save(city);
        return mapToDTO(savedCity);
    }

    @Transactional
    public CityDTO updateCity(Long id, CityDTO cityDTO) {
        City city = cityRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("City", "id", id));

        city.setName(cityDTO.getName());
        city.setState(cityDTO.getState());

        City updatedCity = cityRepository.save(city);
        return mapToDTO(updatedCity);
    }

    @Transactional
    public void deleteCity(Long id) {
        if (!cityRepository.existsById(id)) {
            throw new ResourceNotFoundException("City", "id", id);
        }
        cityRepository.deleteById(id);
    }

    private CityDTO mapToDTO(City city) {
        CityDTO dto = new CityDTO();
        dto.setId(city.getId());
        dto.setName(city.getName());
        dto.setState(city.getState());
        return dto;
    }
}