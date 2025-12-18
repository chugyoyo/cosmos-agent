package com.chugyoyo.cosmosagent.mcp.server;

import io.agentscope.core.tool.Tool;
import io.agentscope.core.tool.ToolParam;
import lombok.extern.slf4j.Slf4j;
import com.fasterxml.jackson.databind.ObjectMapper; // 假设使用 Jackson 处理 JSON

import javax.sql.DataSource;
import java.sql.*;
import java.util.*;
import java.util.regex.Pattern;

@Slf4j
public class DatabaseTools {

    // 实际项目中，建议通过依赖注入(Spring)或构造函数传入 DataSource
    private static DataSource dataSource;
    private static final ObjectMapper jsonMapper = new ObjectMapper();

    // 简单的 setter 用于演示初始化
    public static void setDataSource(DataSource ds) {
        dataSource = ds;
    }

    /**
     * 工具 1: 获取数据库所有表的设计结构
     * LLM 需要这个信息来理解数据库中有哪些字段，以便编写 JOIN 或 WHERE 条件
     */
    @Tool(name = "get_database_schema", description = "Fetch metadata of all tables, including table names, column names, and column types. Use this before generating SQL.")
    public String getDatabaseSchema() {
        if (dataSource == null) return "Error: DataSource is not initialized.";

        Map<String, List<Map<String, String>>> schema = new HashMap<>();

        try (Connection conn = dataSource.getConnection()) {
            DatabaseMetaData metaData = conn.getMetaData();

            // 1. 获取所有表
            try (ResultSet tables = metaData.getTables(null, null, "%", new String[]{"TABLE"})) {
                while (tables.next()) {
                    String tableName = tables.getString("TABLE_NAME");
                    List<Map<String, String>> columnsList = new ArrayList<>();

                    // 2. 获取该表的所有列
                    try (ResultSet columns = metaData.getColumns(null, null, tableName, "%")) {
                        while (columns.next()) {
                            Map<String, String> columnInfo = new HashMap<>();
                            columnInfo.put("name", columns.getString("COLUMN_NAME"));
                            columnInfo.put("type", columns.getString("TYPE_NAME"));
                            columnInfo.put("remarks", columns.getString("REMARKS")); // 如果有注释，对 AI 很有用
                            columnsList.add(columnInfo);
                        }
                    }
                    schema.put(tableName, columnsList);
                }
            }
            // 返回 JSON 字符串，节省 Token 且结构清晰
            return jsonMapper.writeValueAsString(schema);

        } catch (Exception e) {
            log.error("Failed to fetch schema", e);
            return "Error fetching schema: " + e.getMessage();
        }
    }

    /**
     * 工具 2: 执行自定义 SQL
     * 包含基础的安全校验（规则级）。
     * 虽然你提到了 "AI 校验"，但在 Tool 层面建议加上硬性的正则校验作为兜底。
     */
    @Tool(name = "execute_sql", description = "Execute a SELECT SQL query. The SQL must be a read-only statement. DDL and DML (INSERT, UPDATE, DELETE, DROP) are strictly forbidden.")
    public String executeSql(
            @ToolParam(name = "sql", description = "The SQL query string to execute. Must be valid SQL.") String sql) {

        log.info("Agent requesting to execute SQL: {}", sql);

        if (dataSource == null) return "Error: DataSource is not initialized.";

        // --- 安全校验层 (Safety Guardrail) ---
        // 这里的校验是为了防止 AI 产生幻觉或被 Prompt 注入攻击。
        // 规则：必须以 SELECT 开头，且不包含敏感关键字
        String normalizedSql = sql.trim().toUpperCase();
        if (!normalizedSql.startsWith("SELECT") && !normalizedSql.startsWith("WITH")) {
            return "Security Alert: Only SELECT queries are allowed. Your query must start with SELECT or WITH.";
        }

        // 简单的黑名单
        // DROP|DELETE|UPDATE|INSERT|ALTER|TRUNCATE|GRANT|REVOKE|EXEC|SHOW
        Set<String> blackSet = new HashSet<>(Arrays.asList(
                "DROP", "DELETE", "UPDATE", "INSERT", "ALTER", "TRUNCATE",
                "GRANT", "REVOKE", "EXEC", "SHOW"
        ));
        if (blackSet.stream().anyMatch(normalizedSql::contains)) {
            return "Security Alert: The SQL contains forbidden DDL/DML keywords or multiple statements. Query rejected.";
        }

        // --- 执行查询 ---
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();
            List<Map<String, Object>> resultList = new ArrayList<>();

            // 限制最大返回行数，防止 Context Window 爆炸
            int rowLimit = 50;
            int rowCount = 0;

            while (rs.next()) {
                if (rowCount >= rowLimit) break;
                Map<String, Object> row = new HashMap<>();
                for (int i = 1; i <= columnCount; i++) {
                    row.put(metaData.getColumnLabel(i), rs.getObject(i));
                }
                resultList.add(row);
                rowCount++;
            }

            if (rowCount >= rowLimit) {
                log.warn("Query result truncated at {} rows", rowLimit);
            }

            String jsonResult = jsonMapper.writeValueAsString(resultList);
            return jsonResult.isEmpty() ? "No results found." : jsonResult;

        } catch (SQLException e) {
            // 将 SQL 错误原样返回给 AI，AI 具有很强的自我修正能力 (Self-Correction)，
            // 看到错误信息后，它通常会尝试重写 SQL。
            log.warn("SQL Execution failed: {}", e.getMessage());
            return "SQL Execution Error: " + e.getMessage();
        } catch (Exception e) {
            return "System Error: " + e.getMessage();
        }
    }
}
