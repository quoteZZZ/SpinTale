package com.spintale.common.utils.sql;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * SQL注入防护工具类测试
 */
class SqlInjectionProtectorTest {

    @Test
    @DisplayName("空dataScope是安全的")
    void testEmptyDataScopeSafe() {
        assertTrue(SqlInjectionProtector.isDataScopeSafe(null));
        assertTrue(SqlInjectionProtector.isDataScopeSafe(""));
        assertTrue(SqlInjectionProtector.isDataScopeSafe("   "));
    }

    @Test
    @DisplayName("合法的dataScope通过验证")
    void testValidDataScope() {
        assertTrue(SqlInjectionProtector.isDataScopeSafe(
            "AND dept_id IN (100, 101, 102)"
        ));
        assertTrue(SqlInjectionProtector.isDataScopeSafe(
            "AND user_id = 1"
        ));
    }

    @Test
    @DisplayName("包含SQL注入关键字的dataScope被拒绝")
    void testSqlInjectionRejected() {
        assertFalse(SqlInjectionProtector.isDataScopeSafe(
            "AND dept_id IN (100); DROP TABLE user--"
        ));
        assertFalse(SqlInjectionProtector.isDataScopeSafe(
            "AND 1=1 OR 1=1;--"
        ));
    }

    @Test
    @DisplayName("不以AND开头的dataScope被拒绝")
    void testNotStartWithAnd() {
        assertFalse(SqlInjectionProtector.isDataScopeSafe(
            "dept_id IN (100, 101)"
        ));
    }

    @Test
    @DisplayName("清理不安全的dataScope返回空字符串")
    void testSanitizeUnsafe() {
        String result = SqlInjectionProtector.sanitizeDataScope(
            "AND dept_id IN (100); DROP TABLE user--"
        );
        assertEquals("", result);
    }

    @Test
    @DisplayName("清理安全的dataScope返回原值")
    void testSanitizeSafe() {
        String result = SqlInjectionProtector.sanitizeDataScope(
            "AND dept_id IN (100, 101)"
        );
        assertEquals("AND dept_id IN (100, 101)", result);
    }

    @Test
    @DisplayName("验证列名安全性")
    void testColumnNameSafety() {
        assertTrue(SqlInjectionProtector.isColumnNameSafe("dept_id"));
        assertTrue(SqlInjectionProtector.isColumnNameSafe("user.name"));
        assertFalse(SqlInjectionProtector.isColumnNameSafe("dept_id; DROP TABLE"));
        assertFalse(SqlInjectionProtector.isColumnNameSafe("1invalid"));
    }

    @Test
    @DisplayName("验证表名安全性")
    void testTableNameSafety() {
        assertTrue(SqlInjectionProtector.isTableNameSafe("sys_user"));
        assertTrue(SqlInjectionProtector.isTableNameSafe("user"));
        assertFalse(SqlInjectionProtector.isTableNameSafe("sys_user; DROP"));
        assertFalse(SqlInjectionProtector.isTableNameSafe(""));
    }

    @Test
    @DisplayName("验证ORDER BY安全性")
    void testOrderBySafety() {
        assertTrue(SqlInjectionProtector.isOrderBySafe("create_time DESC"));
        assertTrue(SqlInjectionProtector.isOrderBySafe("name ASC, id DESC"));
        assertFalse(SqlInjectionProtector.isOrderBySafe("id; DROP TABLE user"));
    }

    @Test
    @DisplayName("验证GROUP BY安全性")
    void testGroupBySafety() {
        assertTrue(SqlInjectionProtector.isGroupBySafe("dept_id"));
        assertTrue(SqlInjectionProtector.isGroupBySafe("dept_id, user_id"));
        assertFalse(SqlInjectionProtector.isGroupBySafe("dept_id; DROP TABLE"));
    }
}
