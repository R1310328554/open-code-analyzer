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

import com.zaxxer.hikari.metrics.MetricsTrackerFactory;
import com.zaxxer.hikari.pool.HikariPool;
import com.zaxxer.hikari.pool.HikariPool.PoolInitializationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.io.Closeable;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.zaxxer.hikari.pool.HikariPool.POOL_NORMAL;

/**
 * HikariCP 池化 {@link javax.sql.DataSource} 实现。
 *
 * @author Brett Wooldridge
 */
public class HikariDataSource extends HikariConfig implements DataSource, Closeable
{
   private static final Logger LOGGER = LoggerFactory.getLogger(HikariDataSource.class);

   private final AtomicBoolean isShutdown = new AtomicBoolean();

   private final HikariPool fastPathPool;
   private volatile HikariPool pool;

   /**
    * 默认构造器，通过 setter 配置连接池。
    * 相比 {@link #HikariDataSource(HikariConfig)}，{@link #getConnection()} 性能略低，
    * 因为需要执行懒初始化检查。
    * <p>
    * 首次调用 {@link #getConnection()} 时启动连接池；启动后配置被“封存”，
    * 除 {@link HikariConfigMXBean} 方法外不可再修改。
    */
   public HikariDataSource()
   {
      fastPathPool = null;
   }

   /**
    * 使用指定配置构造 {@code HikariDataSource}。
    * 会复制 {@link HikariConfig} 并立即启动连接池。
    * <p>
    * 传入的 {@link HikariConfig} 可继续修改，不影响本实例，也可用于初始化另一个 {@code HikariDataSource}。
    *
    * @param configuration {@link HikariConfig} 实例
    */
   public HikariDataSource(HikariConfig configuration)
   {
      configuration.validate();
      configuration.copyStateTo(this);

      LOGGER.info("{} - Starting...", configuration.getPoolName());
      pool = fastPathPool = new HikariPool(this);
      LOGGER.info("{} - Start completed.", configuration.getPoolName());

      this.seal();
   }

   // ***********************************************************************
   //                          DataSource methods
   // ***********************************************************************

   /** {@inheritDoc} */
   @Override
   public Connection getConnection() throws SQLException
   {
      if (isClosed()) {
         throw new SQLException("HikariDataSource " + this + " has been closed.");
      }

      if (fastPathPool != null) {
         return fastPathPool.getConnection();
      }

      // 双重检查锁定，参见 http://en.wikipedia.org/wiki/Double-checked_locking#Usage_in_Java
      HikariPool result = pool;
      if (result == null) {
         synchronized (this) {
            result = pool;
            if (result == null) {
               validate();
               LOGGER.info("{} - Starting...", getPoolName());
               try {
                  pool = result = new HikariPool(this);
                  this.seal();
               }
               catch (PoolInitializationException pie) {
                  if (pie.getCause() instanceof SQLException) {
                     throw (SQLException) pie.getCause();
                  }
                  else {
                     throw pie;
                  }
               }
               LOGGER.info("{} - Start completed.", getPoolName());
            }
         }
      }

      return result.getConnection();
   }

   /** {@inheritDoc} */
   @Override
   public Connection getConnection(String username, String password) throws SQLException
   {
      throw new SQLFeatureNotSupportedException();
   }

   /** {@inheritDoc} */
   @Override
   public PrintWriter getLogWriter() throws SQLException
   {
      HikariPool p = pool;
      return (p != null ? p.getUnwrappedDataSource().getLogWriter() : null);
   }

   /** {@inheritDoc} */
   @Override
   public void setLogWriter(PrintWriter out) throws SQLException
   {
      var p = pool;
      if (p != null) {
         p.getUnwrappedDataSource().setLogWriter(out);
      }
   }

   /** {@inheritDoc} */
   @Override
   public void setLoginTimeout(int seconds) throws SQLException
   {
      var p = pool;
      if (p != null) {
         p.getUnwrappedDataSource().setLoginTimeout(seconds);
      }
   }

   /** {@inheritDoc} */
   @Override
   public int getLoginTimeout() throws SQLException
   {
      var p = pool;
      return (p != null ? p.getUnwrappedDataSource().getLoginTimeout() : 0);
   }

   /** {@inheritDoc} */
   @Override
   public java.util.logging.Logger getParentLogger() throws SQLFeatureNotSupportedException
   {
      throw new SQLFeatureNotSupportedException();
   }

   /** {@inheritDoc} */
   @Override
   @SuppressWarnings("unchecked")
   public <T> T unwrap(Class<T> iface) throws SQLException
   {
      if (iface.isInstance(this)) {
         return (T) this;
      }

      var p = pool;
      if (p != null) {
         final var unwrappedDataSource = p.getUnwrappedDataSource();
         if (iface.isInstance(unwrappedDataSource)) {
            return (T) unwrappedDataSource;
         }

         if (unwrappedDataSource != null) {
            return unwrappedDataSource.unwrap(iface);
         }
      }

      throw new SQLException("Wrapped DataSource is not an instance of " + iface);
   }

   /** {@inheritDoc} */
   @Override
   public boolean isWrapperFor(Class<?> iface) throws SQLException
   {
      if (iface.isInstance(this)) {
         return true;
      }

      var p = pool;
      if (p != null) {
         final var unwrappedDataSource = p.getUnwrappedDataSource();
         if (iface.isInstance(unwrappedDataSource)) {
            return true;
         }

         if (unwrappedDataSource != null) {
            return unwrappedDataSource.isWrapperFor(iface);
         }
      }

      return false;
   }

   // ***********************************************************************
   //                        HikariConfigMXBean methods
   // ***********************************************************************

   /** {@inheritDoc} */
   @Override
   public void setMetricRegistry(Object metricRegistry)
   {
      var isAlreadySet = getMetricRegistry() != null;
      super.setMetricRegistry(metricRegistry);

      var p = pool;
      if (p != null) {
         if (isAlreadySet) {
            throw new IllegalStateException("MetricRegistry can only be set one time");
         }
         else {
            p.setMetricRegistry(super.getMetricRegistry());
         }
      }
   }

   /** {@inheritDoc} */
   @Override
   public void setMetricsTrackerFactory(MetricsTrackerFactory metricsTrackerFactory)
   {
      var isAlreadySet = getMetricsTrackerFactory() != null;
      super.setMetricsTrackerFactory(metricsTrackerFactory);

      var p = pool;
      if (p != null) {
         if (isAlreadySet) {
            throw new IllegalStateException("MetricsTrackerFactory can only be set one time");
         }
         else {
            p.setMetricsTrackerFactory(super.getMetricsTrackerFactory());
         }
      }
   }

   /** {@inheritDoc} */
   @Override
   public void setHealthCheckRegistry(Object healthCheckRegistry)
   {
      var isAlreadySet = getHealthCheckRegistry() != null;
      super.setHealthCheckRegistry(healthCheckRegistry);

      var p = pool;
      if (p != null) {
         if (isAlreadySet) {
            throw new IllegalStateException("HealthCheckRegistry can only be set one time");
         }
         else {
            p.setHealthCheckRegistry(super.getHealthCheckRegistry());
         }
      }
   }

   // ***********************************************************************
   //                        HikariCP-specific methods
   // ***********************************************************************

   /**
    * 若连接池已启动且未挂起或关闭，则返回 {@code true}。
    *
    * @return 连接池是否处于正常运行状态
    */
   public boolean isRunning()
   {
      return pool != null && pool.poolState == POOL_NORMAL;
   }

   /**
    * 获取本 {@code HikariDataSource} 实例的 {@code HikariPoolMXBean}。
    * 若在未传入 {@code HikariConfig} 且尚未首次调用 {@code #getConnection()} 的情况下构造，
    * 则返回 {@code null}。
    *
    * @return {@code HikariPoolMXBean} 实例，或 {@code null}
    */
   public HikariPoolMXBean getHikariPoolMXBean()
   {
      return pool;
   }

   /**
    * 获取本 {@code HikariDataSource} 实例的 {@code HikariConfigMXBean}。
    *
    * @return {@code HikariConfigMXBean} 实例
    */
   public HikariConfigMXBean getHikariConfigMXBean()
   {
      return this;
   }

   /**
    * 从池中驱逐指定连接。若连接已关闭（已归还池中），可能触发“软”驱逐，
    * 即连接若仍在使用则稍后驱逐；若尚未关闭则立即驱逐。
    *
    * @param connection 要从池中驱逐的连接
    */
   public void evictConnection(Connection connection)
   {
      HikariPool p;
      if (!isClosed() && (p = pool) != null && connection.getClass().getName().startsWith("com.zaxxer.hikari")) {
         p.evictConnection(connection);
      }
   }

   /**
    * 关闭数据源及其关联的连接池。
    */
   @Override
   public void close()
   {
      if (isShutdown.getAndSet(true)) {
         return;
      }

      var p = pool;
      if (p != null) {
         try {
            LOGGER.info("{} - Shutdown initiated...", getPoolName());
            p.shutdown();
            LOGGER.info("{} - Shutdown completed.", getPoolName());
         }
         catch (InterruptedException e) {
            LOGGER.warn("{} - Interrupted during closing", getPoolName(), e);
            Thread.currentThread().interrupt();
         }
      }
   }

   /**
    * 判断 {@code HikariDataSource} 是否已关闭。
    *
    * @return 已关闭返回 {@code true}，否则返回 {@code false}
    */
   public boolean isClosed()
   {
      return isShutdown.get();
   }

   /** {@inheritDoc} */
   @Override
   public String toString()
   {
      return "HikariDataSource (" + pool + ")";
   }
}
