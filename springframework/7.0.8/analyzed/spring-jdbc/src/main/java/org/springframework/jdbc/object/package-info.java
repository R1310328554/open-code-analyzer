/**
 * 该包中的类将 RDBMS 查询、更新和存储过程表示为线程安全、可重用的对象。这种方法是由 JDO 建模的，尽管查询返回的对象当然与数据库“断开连接”。
 * <p> 这个较高级别的 JDBC 抽象依赖于 {@code org.springframework.jdbc.core} 包中的较低级别的抽象。抛出的异常与 {@code
 * org.springframework.dao} 包中一样，这意味着使用此包的代码不需要实现 JDBC 或 RDBMS 特定的错误处理。
 * <p> 这个包和相关包在 Rod Johnson 的 <a
 * href="https://www.amazon.com/exec/obidos/tg/detail/-/0764543857/">Expert 一对一 J2EE 设计和开发
 * </a>（Wrox，2002）的第 9 章中讨论。
 */
@NullMarked
package org.springframework.jdbc.object;

import org.jspecify.annotations.NullMarked;
