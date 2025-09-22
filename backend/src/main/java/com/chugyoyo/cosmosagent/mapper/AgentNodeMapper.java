package com.chugyoyo.cosmosagent.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chugyoyo.cosmosagent.entity.AgentLink;
import com.chugyoyo.cosmosagent.entity.AgentNode;
import jakarta.validation.constraints.NotNull;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface AgentNodeMapper extends BaseMapper<AgentNode> {

    /**
     * 根据代理ID查询所有连线
     */
    @Select("SELECT * FROM agent_node WHERE agent_id = #{agentId} ORDER BY created_at ASC")
    List<AgentNode> findByAgentId(@Param("agentId") Long agentId);
}