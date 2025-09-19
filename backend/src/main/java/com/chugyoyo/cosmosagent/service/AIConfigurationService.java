package com.chugyoyo.cosmosagent.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chugyoyo.cosmosagent.dto.AIConfigurationDTO;
import com.chugyoyo.cosmosagent.entity.AIConfiguration;
import com.chugyoyo.cosmosagent.mapper.AIConfigurationMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class AIConfigurationService extends ServiceImpl<AIConfigurationMapper, AIConfiguration> {

    @Autowired
    private SpringAiTestService springAiTestService;

    public List<AIConfigurationDTO> getAllConfigurations() {
        return list().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    public AIConfigurationDTO getConfigurationByProvider(String provider) {
        LambdaQueryWrapper<AIConfiguration> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AIConfiguration::getProvider, provider);
        AIConfiguration entity = getOne(wrapper);
        return entity != null ? convertToDTO(entity) : null;
    }
    
    @Transactional
    public AIConfigurationDTO createConfiguration(AIConfigurationDTO dto) {
        LambdaQueryWrapper<AIConfiguration> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AIConfiguration::getProvider, dto.getProvider());
        if (count(wrapper) > 0) {
            throw new RuntimeException("Configuration for provider " + dto.getProvider() + " already exists");
        }
        
        AIConfiguration entity = convertToEntity(dto);
        entity.setIsActive(true);
        
        boolean result = save(entity);
        return result ? convertToDTO(entity) : null;
    }
    
    @Transactional
    public AIConfigurationDTO updateConfiguration(Long id, AIConfigurationDTO dto) {
        AIConfiguration entity = getById(id);
        if (entity == null) {
            throw new RuntimeException("Configuration not found");
        }
        
        entity.setApiKey(dto.getApiKey());
        entity.setBaseUrl(dto.getBaseUrl());
        entity.setModel(dto.getModel());
        entity.setIsActive(dto.getIsActive());
        
        boolean result = updateById(entity);
        return result ? convertToDTO(entity) : null;
    }
    
    @Transactional
    public void deleteConfiguration(Long id) {
        removeById(id);
    }
    
    public Map<String, Object> testConnection(String provider) {
        try {
            LambdaQueryWrapper<AIConfiguration> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(AIConfiguration::getProvider, provider);
            AIConfiguration config = getOne(wrapper);
            if (config == null) {
                throw new RuntimeException("Configuration not found");
            }
            
            // 首先验证基本配置
            if (!validateConfiguration(config)) {
                Map<String, Object> result = new java.util.HashMap<>();
                result.put("success", false);
                result.put("message", "配置不完整，请检查 API Key");
                return result;
            }
            
            // 使用 Spring AI 进行真实连接测试
            return springAiTestService.testConnection(provider);
        } catch (Exception e) {
            log.error("测试连接失败", e);
            Map<String, Object> result = new java.util.HashMap<>();
            result.put("success", false);
            result.put("message", "测试失败: " + e.getMessage());
            return result;
        }
    }
    
    private boolean validateConfiguration(AIConfiguration config) {
        return config.getApiKey() != null && !config.getApiKey().isEmpty();
    }
    
    private AIConfigurationDTO convertToDTO(AIConfiguration entity) {
        AIConfigurationDTO dto = new AIConfigurationDTO();
        dto.setId(entity.getId());
        dto.setProvider(entity.getProvider());
        dto.setApiKey(entity.getApiKey());
        dto.setBaseUrl(entity.getBaseUrl());
        dto.setModel(entity.getModel());
        dto.setIsActive(entity.getIsActive());
        return dto;
    }
    
    private AIConfiguration convertToEntity(AIConfigurationDTO dto) {
        AIConfiguration entity = new AIConfiguration();
        entity.setProvider(dto.getProvider());
        entity.setApiKey(dto.getApiKey());
        entity.setBaseUrl(dto.getBaseUrl());
        entity.setModel(dto.getModel());
        entity.setIsActive(dto.getIsActive());
        return entity;
    }
}