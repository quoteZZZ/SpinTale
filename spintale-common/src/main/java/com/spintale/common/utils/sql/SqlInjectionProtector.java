package com.spintale.common.utils.sql;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * SQL注入防护工具类
 * 用于验证dataScope等动态SQL片段的安全性
 */
public class SqlInjectionProtector {

    private static final Logger log = LoggerFactory.getLogger(SqlInjectionProtector.class);

    private static final Pattern SQL_INJECTION_PATTERN = Pattern.compile(
        "(?i)(--|;|'|\"|\\/\\*|\\*\\/|xp_|sp_|exec|execute|insert|update|delete|drop|create|alter|truncate)"
    );

    private static final Set<String> ALLOWED_DATA_SCOPE_KEYWORDS = new HashSet<>(Arrays.asList(
        "OR", "AND", "IN", "NOT", "NULL", "IS", "LIKE", "BETWEEN", "EXISTS"
    ));

    private static final Pattern VALID_DATA_SCOPE_PATTERN = Pattern.compile(
        "^\\s*AND\\s+[\\w.]+\\s+(IN|NOT\\s+IN|IS\\s+NULL|IS\\s+NOT\\s+NULL|=|!=|<|>|<=|>=|LIKE)\\s*\\([^)]*\\)\\s*$",
        Pattern.CASE_INSENSITIVE
    );

    /**
     * 验证dataScope是否安全
     * 
     * @param dataScope 数据范围SQL片段
     * @return true表示安全，false表示存在风险
     */
    public static boolean isDataScopeSafe(String dataScope) {
        if (dataScope == null || dataScope.trim().isEmpty()) {
            return true;
        }

        String trimmed = dataScope.trim();

        if (SQL_INJECTION_PATTERN.matcher(trimmed).find()) {
            log.warn("SQL injection pattern detected in dataScope: {}", maskSensitive(trimmed));
            return false;
        }

        if (!trimmed.toUpperCase().startsWith("AND")) {
            log.warn("dataScope must start with 'AND': {}", maskSensitive(trimmed));
            return false;
        }

        if (!VALID_DATA_SCOPE_PATTERN.matcher(trimmed).matches()) {
            log.warn("dataScope format is invalid: {}", maskSensitive(trimmed));
            return false;
        }

        return true;
    }

    /**
     * 清理并验证dataScope
     * 如果不安全则返回空字符串
     */
    public static String sanitizeDataScope(String dataScope) {
        if (dataScope == null || dataScope.trim().isEmpty()) {
            return "";
        }

        if (!isDataScopeSafe(dataScope)) {
            log.error("Unsafe dataScope blocked: {}", maskSensitive(dataScope));
            return "";
        }

        return dataScope.trim();
    }

    /**
     * 验证列名是否安全
     */
    public static boolean isColumnNameSafe(String columnName) {
        if (columnName == null || columnName.trim().isEmpty()) {
            return false;
        }

        String trimmed = columnName.trim();

        if (SQL_INJECTION_PATTERN.matcher(trimmed).find()) {
            return false;
        }

        return trimmed.matches("^[a-zA-Z_][a-zA-Z0-9_.]*$");
    }

    /**
     * 验证表名是否安全
     */
    public static boolean isTableNameSafe(String tableName) {
        if (tableName == null || tableName.trim().isEmpty()) {
            return false;
        }

        String trimmed = tableName.trim();

        if (SQL_INJECTION_PATTERN.matcher(trimmed).find()) {
            return false;
        }

        return trimmed.matches("^[a-zA-Z_][a-zA-Z0-9_]*$");
    }

    /**
     * 遮蔽敏感信息
     */
    private static String maskSensitive(String input) {
        if (input == null) {
            return "null";
        }
        if (input.length() <= 20) {
            return input;
        }
        return input.substring(0, 10) + "..." + input.substring(input.length() - 10);
    }

    /**
     * 验证ORDER BY子句是否安全
     */
    public static boolean isOrderBySafe(String orderBy) {
        if (orderBy == null || orderBy.trim().isEmpty()) {
            return true;
        }

        String trimmed = orderBy.trim();

        if (SQL_INJECTION_PATTERN.matcher(trimmed).find()) {
            return false;
        }

        return trimmed.matches("^[a-zA-Z0-9_,\\s]+(ASC|DESC|asc|desc)?$");
    }

    /**
     * 验证GROUP BY子句是否安全
     */
    public static boolean isGroupBySafe(String groupBy) {
        if (groupBy == null || groupBy.trim().isEmpty()) {
            return true;
        }

        String trimmed = groupBy.trim();

        if (SQL_INJECTION_PATTERN.matcher(trimmed).find()) {
            return false;
        }

        return trimmed.matches("^[a-zA-Z0-9_,\\s]+$");
    }
}
