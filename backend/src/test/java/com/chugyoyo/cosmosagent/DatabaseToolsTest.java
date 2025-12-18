package com.chugyoyo.cosmosagent;

import com.chugyoyo.cosmosagent.mcp.server.DatabaseTools;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.sql.DataSource;

@Slf4j
@SpringBootTest
public class DatabaseToolsTest {
    @Autowired
    private DataSource dataSource;

    @Test
    public void test() {
        DatabaseTools.setDataSource(dataSource);
        DatabaseTools databaseTools = new DatabaseTools();
        String databaseSchema = databaseTools.getDatabaseSchema();
        log.info("Database schema: {}", databaseSchema);
        String executeSql = databaseTools.executeSql("CREATE DATABASE IF NOT EXISTS test");
        log.info("Execute sql: {}", executeSql);
        String executeSql2 = databaseTools.executeSql("select * from chat_message limit 1");
        log.info("Execute sql2: {}", executeSql2);
    }
}
