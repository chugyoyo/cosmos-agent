package com.chugyoyo.cosmosagent.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chugyoyo.cosmosagent.entity.ChatSession;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ChatSessionMapper extends BaseMapper<ChatSession> {
}