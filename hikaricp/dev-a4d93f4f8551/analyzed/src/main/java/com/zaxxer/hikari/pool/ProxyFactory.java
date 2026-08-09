/*
 * Copyright (C) 2013, 2014 Brett Wooldridge
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.zaxxer.hikari.pool;

import java.sql.*;

import com.zaxxer.hikari.util.FastList;

/**
 * 为标准 JDBC 接口实例创建代理对象的工厂。
 *
 * @author Brett Wooldridge
 */
public final class ProxyFactory
{
   private ProxyFactory()
   {
      // 不可实例化
   }

   /**
    * 为指定 {@link Connection} 创建代理。
    * @param poolEntry 持有池状态的 {@link PoolEntry}
    * @param connection 原始数据库连接
    * @param openStatements 跟踪已打开 {@link Statement} 的可复用列表
    * @param leakTask 本连接的 {@link ProxyLeakTask}
    * @param isReadOnly 连接默认只读状态
    * @param isAutoCommit 连接默认自动提交状态
    * @return 包装指定 {@link Connection} 的代理
    */
   static ProxyConnection getProxyConnection(final PoolEntry poolEntry, final Connection connection, final FastList<Statement> openStatements, final ProxyLeakTask leakTask, final boolean isReadOnly, final boolean isAutoCommit)
   {
      // 方法体由 JavassistProxyFactory 在构建时注入
      throw new IllegalStateException("You need to run the CLI build and you need target/classes in your classpath to run.");
   }

   static Statement getProxyStatement(final ProxyConnection connection, final Statement statement)
   {
      // 方法体由 JavassistProxyFactory 在构建时注入
      throw new IllegalStateException("You need to run the CLI build and you need target/classes in your classpath to run.");
   }

   static CallableStatement getProxyCallableStatement(final ProxyConnection connection, final CallableStatement statement)
   {
      // 方法体由 JavassistProxyFactory 在构建时注入
      throw new IllegalStateException("You need to run the CLI build and you need target/classes in your classpath to run.");
   }

   static PreparedStatement getProxyPreparedStatement(final ProxyConnection connection, final PreparedStatement statement)
   {
      // 方法体由 JavassistProxyFactory 在构建时注入
      throw new IllegalStateException("You need to run the CLI build and you need target/classes in your classpath to run.");
   }

   static ResultSet getProxyResultSet(final ProxyConnection connection, final ProxyStatement statement, final ResultSet resultSet)
   {
      // 方法体由 JavassistProxyFactory 在构建时注入
      throw new IllegalStateException("You need to run the CLI build and you need target/classes in your classpath to run.");
   }

   static DatabaseMetaData getProxyDatabaseMetaData(final ProxyConnection connection, final DatabaseMetaData metaData)
   {
      // 方法体由 JavassistProxyFactory 在构建时注入
      throw new IllegalStateException("You need to run the CLI build and you need target/classes in your classpath to run.");
   }
}
