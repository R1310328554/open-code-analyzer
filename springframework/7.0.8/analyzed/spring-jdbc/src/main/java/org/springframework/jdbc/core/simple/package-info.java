/**
 * 常见 JDBC 交互的简化层。
 * <p>{@code JdbcClient} 为 JDBC 查询和更新操作提供流畅的 API，支持 JDBC 样式的位置以及 Spring 样式的命名参数。
 * <p>{@code SimpleJdbcInsert} 和 {@code SimpleJdbcCall} 利用 JDBC 驱动程序提供的数据库元数据来简化应用程序代码。许多参数
 * 规范变得不必要，因为可以在元数据中查找它们。
 */
@NullMarked
package org.springframework.jdbc.core.simple;

import org.jspecify.annotations.NullMarked;
