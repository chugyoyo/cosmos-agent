package com.chugyoyo.cosmosagent.controller;

import com.chugyoyo.cosmosagent.common.HttpStatus;
import com.chugyoyo.cosmosagent.dto.AIConfigurationDTO;
import com.chugyoyo.cosmosagent.service.AIConfigurationService;
import com.chugyoyo.cosmosagent.common.ApiResp;
import lombok.RequiredArgsConstructor;
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
    public ApiResp<List<AIConfigurationDTO>> getAllConfigurations() {
        return ApiResp.success(configurationService.getAllConfigurations());
    }

    @GetMapping("/{provider}")
    public ApiResp<AIConfigurationDTO> getConfigurationByProvider(@PathVariable String provider) {
        AIConfigurationDTO config = configurationService.getConfigurationByProvider(provider);
        return config != null ? ApiResp.success(config) : ApiResp.fail(HttpStatus.NOT_FOUND, "Configuration not found");
    }

    @PostMapping
    public ApiResp<AIConfigurationDTO> createConfiguration(@Valid @RequestBody AIConfigurationDTO dto) {
        AIConfigurationDTO created = configurationService.createConfiguration(dto);
        return ApiResp.success(created);
    }

    @PutMapping("/{id}")
    public ApiResp<AIConfigurationDTO> updateConfiguration(@PathVariable Long id, @Valid @RequestBody AIConfigurationDTO dto) {
        AIConfigurationDTO updated = configurationService.updateConfiguration(id, dto);
        return ApiResp.success(updated);
    }

    @DeleteMapping("/{id}")
    public ApiResp<Void> deleteConfiguration(@PathVariable Long id) {
        configurationService.deleteConfiguration(id);
        return ApiResp.success();
    }

    @PostMapping("/{provider}/test")
    public ApiResp<Boolean> testConnection(@PathVariable String provider) {
        boolean isConnected = configurationService.testConnection(provider);
        return ApiResp.success(isConnected);
    }
}