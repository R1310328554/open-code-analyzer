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

import javax.sql.DataSource;

/**
 * Hikari 连接池实例的 javax.management MBean 接口。
 *
 * @author Brett Wooldridge
 */
public interface HikariPoolMXBean
{
   /**
    * 获取当前池中空闲连接数。
    * <p>
    * 返回值极为瞬时，仅为某一时刻的快照。因此，由于与 {@link #getActiveConnections()} 调用存在时间差，
    * 空闲连接数与活跃连接数之和可能小于或大于 {@link #getTotalConnections()} 的返回值。
    *
    * @return 当前空闲连接数
    */
   int getIdleConnections();

   /**
    * 获取当前池中活跃（在用）连接数。
    * <p>
    * 返回值极为瞬时，仅为某一时刻的快照。因此，由于与 {@link #getIdleConnections()} 调用存在时间差，
    * 空闲连接数与活跃连接数之和可能小于或大于 {@link #getTotalConnections()} 的返回值。
    *
    * @return 当前活跃（在用）连接数
    */
   int getActiveConnections();

   /**
    * 获取当前池中连接总数。返回值瞬时，仅为某一时刻的快照。
    *
    * @return 池中连接总数
    */
   int getTotalConnections();

   /**
    * 获取正在等待从池中获取连接的线程数。返回值极为瞬时，仅为某一时刻的快照。
    *
    * @return 等待连接的线程数
    */
   int getThreadsAwaitingConnection();

   /**
    * 驱逐当前所有空闲连接，并标记活跃（在用）连接在归还池时予以驱逐。
    */
   void softEvictConnections();

   /**
    * 挂起连接池。挂起后，调用 {@link DataSource#getConnection()} 的线程将<i>无限期阻塞</i>，
    * 直至通过 {@link #resumePool()} 恢复连接池。
    * <br>
    * 除非已将 {@link HikariConfig#setAllowPoolSuspension(boolean)} 或等价属性设为 {@code true}，
    * 否则此方法无效。
    */
   void suspendPool();

   /**
    * 恢复连接池，使此前通过 {@link #suspendPool()} 挂起的池可再次借出连接。
    * <br>
    * 除非已将 {@link HikariConfig#setAllowPoolSuspension(boolean)} 或等价属性设为 {@code true}，
    * 否则此方法无效。
    */
   void resumePool();
}
