package com.chugyoyo.cosmosagent.controller;

import com.chugyoyo.cosmosagent.dto.AIConfigurationDTO;
import com.chugyoyo.cosmosagent.service.AIConfigurationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/configurations")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AIConfigurationController {
    
    private final AIConfigurationService configurationService;
    
    @GetMapping
    public ResponseEntity<List<AIConfigurationDTO>> getAllConfigurations() {
        return ResponseEntity.ok(configurationService.getAllConfigurations());
    }
    
    @GetMapping("/{provider}")
    public ResponseEntity<AIConfigurationDTO> getConfigurationByProvider(@PathVariable String provider) {
        AIConfigurationDTO config = configurationService.getConfigurationByProvider(provider);
        return config != null ? ResponseEntity.ok(config) : ResponseEntity.notFound().build();
    }
    
    @PostMapping
    public ResponseEntity<AIConfigurationDTO> createConfiguration(@Valid @RequestBody AIConfigurationDTO dto) {
        try {
            AIConfigurationDTO created = configurationService.createConfiguration(dto);
            return ResponseEntity.ok(created);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<AIConfigurationDTO> updateConfiguration(@PathVariable Long id, @Valid @RequestBody AIConfigurationDTO dto) {
        try {
            AIConfigurationDTO updated = configurationService.updateConfiguration(id, dto);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteConfiguration(@PathVariable Long id) {
        configurationService.deleteConfiguration(id);
        return ResponseEntity.ok().build();
    }
    
    @PostMapping("/{provider}/test")
    public ResponseEntity<Boolean> testConnection(@PathVariable String provider) {
        boolean isConnected = configurationService.testConnection(provider);
        return ResponseEntity.ok(isConnected);
    }
}