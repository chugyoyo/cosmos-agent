package com.chugyoyo.cosmosagent.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chugyoyo.cosmosagent.entity.ChatMessage;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ChatMessageMapper extends BaseMapper<ChatMessage> {
}