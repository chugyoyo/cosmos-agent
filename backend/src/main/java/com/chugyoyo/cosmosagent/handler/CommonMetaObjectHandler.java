package com.chugyoyo.cosmosagent.handler;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class CommonMetaObjectHandler implements MetaObjectHandler {

    @Override
    public void insertFill(MetaObject metaObject) {
        this.strictInsertFill(metaObject, "createdAt", LocalDateTime.class, LocalDateTime.now());
        this.strictInsertFill(metaObject, "updatedAt", LocalDateTime.class, LocalDateTime.now());
        
        // 为Agent实体设置默认值
        if (metaObject.hasGetter("callCount")) {
            this.strictInsertFill(metaObject, "callCount", Long.class, 0L);
        }
        if (metaObject.hasGetter("status")) {
            this.strictInsertFill(metaObject, "status", String.class, "CREATED");
        }
        if (metaObject.hasGetter("isDeployed")) {
            this.strictInsertFill(metaObject, "isDeployed", Boolean.class, false);
        }
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        this.strictUpdateFill(metaObject, "updatedAt", LocalDateTime.class, LocalDateTime.now());
    }
}