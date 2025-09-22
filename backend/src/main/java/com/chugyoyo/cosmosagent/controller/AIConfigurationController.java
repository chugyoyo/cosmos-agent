package com.chugyoyo.cosmosagent.controller;

import com.chugyoyo.cosmosagent.common.HttpStatus;
import com.chugyoyo.cosmosagent.dto.AIConfigurationDTO;
import com.chugyoyo.cosmosagent.service.AIConfigurationService;
import com.chugyoyo.cosmosagent.common.ApiResp;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/configurations")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class AIConfigurationController {

    private final AIConfigurationService configurationService;

    @GetMapping("/getAllConfigurations")
    public ApiResp<List<AIConfigurationDTO>> getAllConfigurations() {
        return ApiResp.success(configurationService.getAllConfigurations());
    }

    @GetMapping("/{provider}/getConfigurationByProvider")
    public ApiResp<AIConfigurationDTO> getConfigurationByProvider(@PathVariable String provider) {
        AIConfigurationDTO config = configurationService.getConfigurationByProvider(provider);
        return config != null ? ApiResp.success(config) : ApiResp.fail(HttpStatus.NOT_FOUND, "Configuration not found");
    }

    @PostMapping("/createConfiguration")
    public ApiResp<AIConfigurationDTO> createConfiguration(@Valid @RequestBody AIConfigurationDTO dto) {
        AIConfigurationDTO created = configurationService.createConfiguration(dto);
        return ApiResp.success(created);
    }

    @PutMapping("/{id}/updateConfiguration")
    public ApiResp<AIConfigurationDTO> updateConfiguration(@PathVariable Long id, @Valid @RequestBody AIConfigurationDTO dto) {
        AIConfigurationDTO updated = configurationService.updateConfiguration(id, dto);
        return ApiResp.success(updated);
    }

    @DeleteMapping("/{id}/deleteConfiguration")
    public ApiResp<Void> deleteConfiguration(@PathVariable Long id) {
        configurationService.deleteConfiguration(id);
        return ApiResp.success();
    }

    @GetMapping("/testConnectionByProvider")
    public ApiResp<Map<String, Object>> testConnection(@RequestParam("provider") String provider) {
        Map<String, Object> result = configurationService.testConnection(provider);
        return ApiResp.success(result);
    }
}