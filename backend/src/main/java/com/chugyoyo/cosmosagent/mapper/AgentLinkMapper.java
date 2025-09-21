package com.chugyoyo.cosmosagent.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chugyoyo.cosmosagent.entity.AgentLink;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Delete;

import java.util.List;

@Mapper
public interface AgentLinkMapper extends BaseMapper<AgentLink> {
    
    /**
     * 根据代理ID查询所有连线
     */
    @Select("SELECT * FROM agent_link WHERE agent_id = #{agentId} ORDER BY sort_order ASC, created_at ASC")
    List<AgentLink> findByAgentId(@Param("agentId") Long agentId);
    
    /**
     * 根据源节点ID查询所有出线连线
     */
    @Select("SELECT * FROM agent_link WHERE source_node_id = #{sourceNodeId} ORDER BY sort_order ASC")
    List<AgentLink> findBySourceNodeId(@Param("sourceNodeId") Long sourceNodeId);
    
    /**
     * 根据目标节点ID查询所有入线连线
     */
    @Select("SELECT * FROM agent_link WHERE target_node_id = #{targetNodeId} ORDER BY sort_order ASC")
    List<AgentLink> findByTargetNodeId(@Param("targetNodeId") Long targetNodeId);
    
    /**
     * 根据节点ID查询所有相关连线（包括入线和出线）
     */
    @Select("SELECT * FROM agent_link WHERE source_node_id = #{nodeId} OR target_node_id = #{nodeId} ORDER BY sort_order ASC")
    List<AgentLink> findByNodeId(@Param("nodeId") Long nodeId);
    
    /**
     * 删除代理的所有连线
     */
    @Delete("DELETE FROM agent_link WHERE agent_id = #{agentId}")
    int deleteByAgentId(@Param("agentId") Long agentId);
    
    /**
     * 删除节点的所有相关连线
     */
    @Delete("DELETE FROM agent_link WHERE source_node_id = #{nodeId} OR target_node_id = #{nodeId}")
    int deleteByNodeId(@Param("nodeId") Long nodeId);
    
    /**
     * 检查两个节点之间是否已存在连线
     */
    @Select("SELECT COUNT(*) FROM agent_link WHERE agent_id = #{agentId} AND source_node_id = #{sourceNodeId} AND target_node_id = #{targetNodeId}")
    int countByNodes(@Param("agentId") Long agentId, @Param("sourceNodeId") Long sourceNodeId, @Param("targetNodeId") Long targetNodeId);
}