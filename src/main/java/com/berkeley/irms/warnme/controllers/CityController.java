package com.berkeley.irms.warnme.controllers;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import com.berkeley.irms.warnme.models.City;
import com.berkeley.irms.warnme.services.CityService;

import java.util.List;

@RestController
@RequestMapping("/api/cities")
public class CityController {

    private final CityService CityService;

    @Autowired
    public CityController(CityService CityService) {
        this.CityService = CityService;
    }

    @GetMapping
    public List<City> getAllCities() {
        return CityService.getAllCities();
    }

    @GetMapping("/{id}")
    public City getCityById(@PathVariable String id) {
        return CityService.getCityById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "City not found with id: " + id));
    }

     @GetMapping("/")
    public City getCityByTitle(@RequestParam String name) {
        return CityService.getCityByName(name)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "City not found with name: " + name));
    }

    @PostMapping
    public City createCity(@RequestBody City City) {
        return CityService.createCity(City);
    }

    @DeleteMapping("/{id}")
    public void deleteCity(@PathVariable String id) {
        CityService.deleteCity(id);
    }
}
