/*
 * Copyright (C) 2013 Brett Wooldridge
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

package com.zaxxer.hikari;

import com.zaxxer.hikari.util.Credentials;

/**
 * Hikari 连接池配置的 javax.management MBean 接口。
 *
 * @author Brett Wooldridge
 */
public interface HikariConfigMXBean
{
   /**
    * 获取客户端从连接池获取连接时允许等待的最大毫秒数。
    * 若超时仍无可用连接，{@link javax.sql.DataSource#getConnection()} 将抛出 {@link java.sql.SQLException}。
    *
    * @return 连接超时时间（毫秒）
    */
   long getConnectionTimeout();

   /**
    * 设置客户端从连接池获取连接时允许等待的最大毫秒数。
    * 若超时仍无可用连接，{@link javax.sql.DataSource#getConnection()} 将抛出 {@link java.sql.SQLException}。
    *
    * @param connectionTimeoutMs 连接超时时间（毫秒）
    */
   void setConnectionTimeout(long connectionTimeoutMs);

   /**
    * 获取连接池等待连接通过存活校验的最大毫秒数。
    *
    * @return 校验超时时间（毫秒）
    */
   long getValidationTimeout();

   /**
    * 设置连接池等待连接通过存活校验的最大毫秒数。
    *
    * @param validationTimeoutMs 校验超时时间（毫秒）
    */
   void setValidationTimeout(long validationTimeoutMs);

   /**
    * 控制连接在池中允许空闲的最长时间（毫秒）。
    * 是否因空闲而退役存在最多约 +30 秒、平均约 +15 秒的波动；
    * 连接绝不会在此超时之前被当作空闲连接移除。
    * 值为 0 表示永不因空闲而移除连接。
    *
    * @return 空闲超时时间（毫秒）
    */
   long getIdleTimeout();

   /**
    * 控制连接在池中允许空闲的最长时间（毫秒）。
    * 是否因空闲而退役存在最多约 +30 秒、平均约 +15 秒的波动；
    * 连接绝不会在此超时之前被当作空闲连接移除。
    * 值为 0 表示永不因空闲而移除连接。
    *
    * @param idleTimeoutMs 空闲超时时间（毫秒）
    */
   void setIdleTimeout(long idleTimeoutMs);

   /**
    * 控制连接借出池外多久后记录可能泄漏的日志。值为 0 表示禁用泄漏检测。
    *
    * @return 连接泄漏检测阈值（毫秒）
    */
   long getLeakDetectionThreshold();

   /**
    * 控制连接借出池外多久后记录可能泄漏的日志。值为 0 表示禁用泄漏检测。
    *
    * @param leakDetectionThresholdMs 连接泄漏检测阈值（毫秒）
    */
   void setLeakDetectionThreshold(long leakDetectionThresholdMs);

   /**
    * 控制池中连接的最大存活时间（毫秒）。
    * 连接达到该时限后将被退役，即使近期仍在使用；
    * 正在使用的连接不会被立即退役，仅在归还空闲后移除。
    *
    * @return 连接最大存活时间（毫秒）
    */
   long getMaxLifetime();

   /**
    * 控制池中连接的最大存活时间（毫秒）。
    * 连接达到该时限后将被退役，即使近期仍在使用；
    * 正在使用的连接不会被立即退役，仅在归还空闲后移除。
    *
    * @param maxLifetimeMs 连接最大存活时间（毫秒）
    */
   void setMaxLifetime(long maxLifetimeMs);

   /**
    * 控制 HikariCP 试图维持的最小空闲连接数（含空闲与在用连接）。
    * 若空闲连接数低于该值，连接池会尽快补充。
    *
    * @return 池中连接数下限
    */
   int getMinimumIdle();

   /**
    * 控制 HikariCP 试图维持的最小空闲连接数（含空闲与在用连接）。
    * 若空闲连接数低于该值，连接池会尽快补充。
    *
    * @param minIdle 要维持的最小空闲连接数
    */
   void setMinimumIdle(int minIdle);

   /**
    * 控制 HikariCP 在池中保留的最大连接数（含空闲与在用连接）。
    *
    * @return 池中最大连接数
    */
   int getMaximumPoolSize();

   /**
    * 控制连接池允许达到的最大规模（含空闲与在用连接），
    * 实质上决定了与数据库后端的最大实际连接数。
    * <p>
    * 当池达到该规模且无空闲连接时，{@code getConnection()} 调用将阻塞，
    * 直至 {@code connectionTimeout} 毫秒后超时。
    *
    * @param maxPoolSize 池中最大连接数
    */
   void setMaximumPoolSize(int maxPoolSize);

   /**
    * 设置认证用密码。运行时修改仅对新建立的连接生效。
    * 运行时修改仅适用于基于 {@link javax.sql.DataSource} 的连接，
    * 不适用于 Driver 类或 JDBC URL 方式建立的连接。
    *
    * @param password 数据库密码
    */
   void setPassword(String password);

   /**
    * 设置认证用用户名。运行时修改仅对新建立的连接生效。
    * 运行时修改仅适用于基于 {@link javax.sql.DataSource} 的连接，
    * 不适用于 Driver 类或 JDBC URL 方式建立的连接。
    *
    * @param username 数据库用户名
    */
   void setUsername(String username);

   /**
    * 设置认证用用户名与密码。运行时修改仅对新建立的连接生效。
    * 运行时修改仅适用于基于 {@link javax.sql.DataSource} 的连接，
    * 不适用于 Driver 类或 JDBC URL 方式建立的连接。
    *
    * @param credentials 数据库用户名与密码对
    */
   void setCredentials(Credentials credentials);

   /**
    * 连接池名称。
    *
    * @return 连接池名称
    */
   String getPoolName();

   /**
    * 获取要在连接上设置的默认 catalog 名称。
    *
    * @return 默认 catalog 名称
    */
   String getCatalog();

   /**
    * 设置要在连接上设置的默认 catalog 名称。
    * <p>
    * 警告：仅应在连接池已挂起且连接已驱逐后修改此值。
    *
    * @param catalog catalog 名称，或 {@code null}
    */
   void setCatalog(String catalog);
}
