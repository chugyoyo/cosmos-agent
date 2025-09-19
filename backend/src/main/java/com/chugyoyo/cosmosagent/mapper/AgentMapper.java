package com.chugyoyo.cosmosagent.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chugyoyo.cosmosagent.entity.Agent;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AgentMapper extends BaseMapper<Agent> {
}