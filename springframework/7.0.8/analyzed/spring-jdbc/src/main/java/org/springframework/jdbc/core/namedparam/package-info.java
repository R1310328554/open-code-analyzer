/**
 * 具有命名参数支持的 JdbcTemplate 变体。
 * <p>NamedParameterJdbcTemplate 是 JdbcTemplate 的包装器，添加了对命名参数解析的支持。它不实现 JdbcOperations 接口或扩
 * 展 JdbcTemplate，而是实现专用的 NamedParameterJdbcOperations 接口。
 * <P>如果您需要 Spring JDBC 的全部功能来执行不太常见的操作，请使用 NamedParameterJdbcTemplate 的 {@code
 * getJdbcOperations()} 方法并使用返回的经典模板，或者直接使用 JdbcTemplate 实例。
 */
@NullMarked
package org.springframework.jdbc.core.namedparam;

import org.jspecify.annotations.NullMarked;
