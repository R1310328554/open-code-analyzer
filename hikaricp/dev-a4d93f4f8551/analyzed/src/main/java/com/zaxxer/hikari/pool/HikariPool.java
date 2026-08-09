/*
 * Copyright (C) 2013,2014 Brett Wooldridge
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

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.HikariPoolMXBean;
import com.zaxxer.hikari.metrics.MetricsTrackerFactory;
import com.zaxxer.hikari.metrics.PoolStats;
import com.zaxxer.hikari.util.ConcurrentBag;
import com.zaxxer.hikari.util.ConcurrentBag.IBagStateListener;
import com.zaxxer.hikari.util.SuspendResumeLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLTransientConnectionException;
import java.util.Optional;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

import static com.zaxxer.hikari.util.ClockSource.*;
import static com.zaxxer.hikari.util.ConcurrentBag.IConcurrentBagEntry.STATE_IN_USE;
import static com.zaxxer.hikari.util.ConcurrentBag.IConcurrentBagEntry.STATE_NOT_IN_USE;
import static com.zaxxer.hikari.util.UtilityElf.*;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * HikariCP 核心连接池实现，提供借还连接、维护空闲连接、
 * 驱逐/关闭连接及池生命周期管理等基本池化行为。
 *
 * @author Brett Wooldridge
 * @hidden
 */
public final class HikariPool extends PoolBase implements HikariPoolMXBean, IBagStateListener
{
   private final Logger logger = LoggerFactory.getLogger(HikariPool.class);

   public static final int POOL_NORMAL = 0;
   public static final int POOL_SUSPENDED = 1;
   public static final int POOL_SHUTDOWN = 2;

   public volatile int poolState;

   private final long aliveBypassWindowMs = Long.getLong("com.zaxxer.hikari.aliveBypassWindowMs", MILLISECONDS.toMillis(500));
   private final long housekeepingPeriodMs = Long.getLong("com.zaxxer.hikari.housekeeping.periodMs", SECONDS.toMillis(30));
   private final long lifeTimeVarianceFactor = Math.min(40, Math.max(2, Long.getLong("com.zaxxer.hikari.lifeTimeVarianceFactor", 4))); // 方差百分比 = 100 / factor
   private final boolean isRequestBoundariesEnabled = Boolean.getBoolean("com.zaxxer.hikari.enableRequestBoundaries");

   private static final String EVICTED_CONNECTION_MESSAGE = "(connection was evicted)";
   private static final String DEAD_CONNECTION_MESSAGE = "(connection is dead)";

   private final PoolEntryCreator poolEntryCreator = new PoolEntryCreator();
   private final PoolEntryCreator postFillPoolEntryCreator = new PoolEntryCreator("After adding ");
   private final ThreadPoolExecutor addConnectionExecutor;
   private final ThreadPoolExecutor closeConnectionExecutor;

   private final ConcurrentBag<PoolEntry> connectionBag;

   private final ProxyLeakTaskFactory leakTaskFactory;
   private final SuspendResumeLock suspendResumeLock;

   private final ScheduledExecutorService houseKeepingExecutorService;
   private ScheduledFuture<?> houseKeeperTask;

   /**
    * 使用指定配置构造连接池。
    *
    * @param config {@link HikariConfig} 实例
    */
   public HikariPool(final HikariConfig config)
   {
      super(config);

      // 步骤1：创建 ConcurrentBag 作为连接存储
      this.connectionBag = new ConcurrentBag<>(this);
      this.suspendResumeLock = config.isAllowPoolSuspension() ? new SuspendResumeLock() : SuspendResumeLock.FAUX_LOCK;

      this.houseKeepingExecutorService = initializeHouseKeepingExecutorService();

      // 步骤2：按 initializationFailTimeout 快速失败校验
      checkFailFast();

      // 步骤3：配置指标追踪
      if (config.getMetricsTrackerFactory() != null) {
         setMetricsTrackerFactory(config.getMetricsTrackerFactory());
      }
      else {
         setMetricRegistry(config.getMetricRegistry());
      }

      setHealthCheckRegistry(config.getHealthCheckRegistry());

      handleMBeans(this, true);

      ThreadFactory threadFactory = config.getThreadFactory();

      final int maxPoolSize = config.getMaximumPoolSize();
      this.addConnectionExecutor = createThreadPoolExecutor(maxPoolSize, poolName + ":connection-adder", threadFactory, new CustomDiscardPolicy());
      this.closeConnectionExecutor = createThreadPoolExecutor(maxPoolSize, poolName + ":connection-closer", threadFactory, new ThreadPoolExecutor.CallerRunsPolicy());

      this.leakTaskFactory = new ProxyLeakTaskFactory(config.getLeakDetectionThreshold(), houseKeepingExecutorService);

      this.houseKeeperTask = houseKeepingExecutorService.scheduleWithFixedDelay(new HouseKeeper(), 100L, housekeepingPeriodMs, MILLISECONDS);

      if (Boolean.getBoolean("com.zaxxer.hikari.blockUntilFilled") && config.getInitializationFailTimeout() > 1) {
         addConnectionExecutor.setMaximumPoolSize(Math.min(16, Runtime.getRuntime().availableProcessors()));
         addConnectionExecutor.setCorePoolSize(Math.min(16, Runtime.getRuntime().availableProcessors()));

         final long startTime = currentTime();
         while (elapsedMillis(startTime) < config.getInitializationFailTimeout() && getTotalConnections() < config.getMinimumIdle()) {
            quietlySleep(MILLISECONDS.toMillis(100));
         }

         addConnectionExecutor.setCorePoolSize(1);
         addConnectionExecutor.setMaximumPoolSize(1);
      }
   }

   /**
    * 从池中获取连接，超时时间为 {@code connectionTimeout} 毫秒。
    *
    * @return {@link java.sql.Connection} 实例
    * @throws SQLException 获取连接超时
    */
   public Connection getConnection() throws SQLException
   {
      return getConnection(connectionTimeout);
   }

   /**
    * 从池中获取连接，在指定毫秒内超时。
    *
    * @param hardTimeout 最大等待毫秒数
    * @return {@link java.sql.Connection} 实例
    * @throws SQLException 获取连接超时
    */
   public Connection getConnection(final long hardTimeout) throws SQLException
   {
      // 步骤1：获取挂起/恢复锁，防止池挂起期间借连接
      suspendResumeLock.acquire();
      final var startTime = currentTime();

      try {
         var timeout = hardTimeout;
         // 步骤2：循环从 bag 借出 PoolEntry，直到成功或超时
         do {
            var poolEntry = connectionBag.borrow(timeout, MILLISECONDS);
            if (poolEntry == null) {
               break; // 等待超时，跳出循环并抛出异常
            }

            final var now = currentTime();
            // 步骤3：若已标记驱逐或（超过存活绕过窗口且连接已死）则关闭并重试
            if (poolEntry.isMarkedEvicted() || (elapsedMillis(poolEntry.lastAccessed, now) > aliveBypassWindowMs && isConnectionDead(poolEntry.connection))) {
               closeConnection(poolEntry, poolEntry.isMarkedEvicted() ? EVICTED_CONNECTION_MESSAGE : DEAD_CONNECTION_MESSAGE);
               timeout = hardTimeout - elapsedMillis(startTime);
            }
            else {
               metricsTracker.recordBorrowStats(poolEntry, startTime);
               if (isRequestBoundariesEnabled) {
                  try {
                     poolEntry.connection.beginRequest();
                  } catch (SQLException e) {
                     logger.warn("beginRequest Failed for: {}, ({})", poolEntry.connection, e.getMessage());
                  }
               }
               // 步骤4：连接可用，包装为 ProxyConnection 并调度泄漏检测
               return poolEntry.createProxyConnection(leakTaskFactory.schedule(poolEntry));
            }
         } while (timeout > 0L);

         metricsTracker.recordBorrowTimeoutStats(startTime);
         throw createTimeoutException(startTime);
      }
      catch (InterruptedException e) {
         Thread.currentThread().interrupt();
         throw new SQLException(poolName + " - Interrupted during connection acquisition", e);
      }
      finally {
         suspendResumeLock.release();
      }
   }

   /**
    * 关闭连接池：关闭所有空闲连接，中止或关闭活跃连接。
    *
    * @throws InterruptedException 关闭过程中线程被中断
    */
   public synchronized void shutdown() throws InterruptedException
   {
      try {
         // 步骤1：标记池为关闭状态
         poolState = POOL_SHUTDOWN;

         if (addConnectionExecutor == null) { // 池从未启动
            return;
         }

         logPoolState("Before shutdown ");

         if (houseKeeperTask != null) {
            houseKeeperTask.cancel(false);
            houseKeeperTask = null;
         }

         // 步骤2：软驱逐所有连接
         softEvictConnections();

         // 步骤3：停止添加连接的线程池
         addConnectionExecutor.shutdown();
         if (!addConnectionExecutor.awaitTermination(getLoginTimeout(), SECONDS)) {
            logger.warn("Timed-out waiting for add connection executor to shutdown");
         }

         destroyHouseKeepingExecutorService();

         connectionBag.close();

         final var assassinExecutor = createThreadPoolExecutor(config.getMaximumPoolSize(), poolName + ":connection-assassinator",
                                                                           config.getThreadFactory(), new ThreadPoolExecutor.CallerRunsPolicy());
         try {
            final var start = currentTime();
            do {
               abortActiveConnections(assassinExecutor);
               softEvictConnections();
            } while (getTotalConnections() > 0 && elapsedMillis(start) < SECONDS.toMillis(10));
         }
         finally {
            assassinExecutor.shutdown();
            if (!assassinExecutor.awaitTermination(10L, SECONDS)) {
               logger.warn("Timed-out waiting for connection assassin to shutdown");
            }
         }

         shutdownNetworkTimeoutExecutor();
         closeConnectionExecutor.shutdown();
         if (!closeConnectionExecutor.awaitTermination(10L, SECONDS)) {
            logger.warn("Timed-out waiting for close connection executor to shutdown");
         }
      }
      finally {
         logPoolState("After  shutdown ");
         handleMBeans(this, false);
         metricsTracker.close();
      }
   }

   /**
    * 从池中驱逐连接。
    *
    * @param connection 要驱逐的连接（实际为 {@link ProxyConnection}）
    */
   public void evictConnection(Connection connection)
   {
      var proxyConnection = (ProxyConnection) connection;
      proxyConnection.cancelLeakTask();

      try {
         softEvictConnection(proxyConnection.getPoolEntry(), "(connection evicted by user)", !connection.isClosed() /* owner */);
      }
      catch (SQLException e) {
         // HikariCP 中不会到达，但接口仍要求捕获
      }
   }

   /**
    * 设置注册指标收集器所用的 metrics 注册表；{@link HikariDataSource} 禁止多次调用。
    *
    * @param metricRegistry 指标注册表实例
    */
   public void setMetricRegistry(Object metricRegistry)
   {
      if (metricRegistry != null && safeIsAssignableFrom(metricRegistry, "com.codahale.metrics.MetricRegistry")) {
         setMetricsTrackerFactory(createInstance("com.zaxxer.hikari.metrics.dropwizard.CodahaleMetricsTrackerFactory",
                                                  MetricsTrackerFactory.class, metricRegistry));
      }
      else if (metricRegistry != null && safeIsAssignableFrom(metricRegistry, "io.dropwizard.metrics5.MetricRegistry")) {
         setMetricsTrackerFactory(createInstance("com.zaxxer.hikari.metrics.dropwizard.Dropwizard5MetricsTrackerFactory",
                                                  MetricsTrackerFactory.class, metricRegistry));
      }
      else if (metricRegistry != null && safeIsAssignableFrom(metricRegistry, "io.micrometer.core.instrument.MeterRegistry")) {
         setMetricsTrackerFactory(createInstance("com.zaxxer.hikari.metrics.micrometer.MicrometerMetricsTrackerFactory",
                                                  MetricsTrackerFactory.class, metricRegistry));
      }
      else {
         setMetricsTrackerFactory(null);
      }
   }

   /**
    * 设置用于创建池内 {@link IMetricsTracker} 的 {@link MetricsTrackerFactory}。
    *
    * @param metricsTrackerFactory {@link MetricsTrackerFactory} 子类实例
    */
   public void setMetricsTrackerFactory(MetricsTrackerFactory metricsTrackerFactory)
   {
      if (metricsTrackerFactory != null) {
         this.metricsTracker = new MetricsTrackerDelegate(metricsTrackerFactory.create(config.getPoolName(), getPoolStats()));
      }
      else {
         this.metricsTracker = new NopMetricsTrackerDelegate();
      }
   }

   /**
    * 设置注册健康检查所用的注册表；目前仅支持 Codahale 健康检查。
    *
    * @param healthCheckRegistry 健康检查注册表实例
    */
   public void setHealthCheckRegistry(Object healthCheckRegistry)
   {
      if (healthCheckRegistry != null) {
         try {
            Class<?> checkerClazz = HikariPool.class.getClassLoader().loadClass("com.zaxxer.hikari.metrics.dropwizard.CodahaleHealthChecker");
            Class<?> healthCheckRegistryClazz = HikariPool.class.getClassLoader().loadClass("com.codahale.metrics.health.HealthCheckRegistry");
            checkerClazz.getMethod("registerHealthChecks", HikariPool.class, HikariConfig.class, healthCheckRegistryClazz)
                 .invoke(null, this, config, healthCheckRegistry);
         } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
         }
      }
   }

   // ***********************************************************************
   //                        IBagStateListener 回调
   // ***********************************************************************

   /** {@inheritDoc} */
   @Override
   public void addBagItem(final int waiting)
   {
      if (waiting > addConnectionExecutor.getQueue().size())
         addConnectionExecutor.submit(poolEntryCreator);
   }

   // ***********************************************************************
   //                        HikariPoolMXBean 方法
   // ***********************************************************************

   /** {@inheritDoc} */
   @Override
   public int getActiveConnections()
   {
      return connectionBag.getCount(STATE_IN_USE);
   }

   /** {@inheritDoc} */
   @Override
   public int getIdleConnections()
   {
      return connectionBag.getCount(STATE_NOT_IN_USE);
   }

   /** {@inheritDoc} */
   @Override
   public int getTotalConnections()
   {
      return connectionBag.size();
   }

   /** {@inheritDoc} */
   @Override
   public int getThreadsAwaitingConnection()
   {
      return connectionBag.getWaitingThreadCount();
   }

   /** {@inheritDoc} */
   @Override
   public void softEvictConnections()
   {
      connectionBag.values().forEach(poolEntry -> softEvictConnection(poolEntry, "(connection evicted)", false /* not owner */));
   }

   /** {@inheritDoc} */
   @Override
   public synchronized void suspendPool()
   {
      if (suspendResumeLock == SuspendResumeLock.FAUX_LOCK) {
         throw new IllegalStateException(poolName + " - is not suspendable");
      }
      else if (poolState != POOL_SUSPENDED) {
         suspendResumeLock.suspend();
         poolState = POOL_SUSPENDED;
      }
   }

   /** {@inheritDoc} */
   @Override
   public synchronized void resumePool()
   {
      if (poolState == POOL_SUSPENDED) {
         poolState = POOL_NORMAL;
         fillPool(false);
         suspendResumeLock.resume();
      }
   }

   // ***********************************************************************
   //                           包级方法
   // ***********************************************************************

   /**
    * 在 DEBUG 级别记录当前池状态。
    *
    * @param prefix 日志消息可选前缀
    */
   void logPoolState(String... prefix)
   {
      if (logger.isDebugEnabled()) {
         logger.debug("{} - {}stats (total={}/{}, idle={}/{}, active={}, waiting={})",
                      poolName, (prefix.length > 0 ? prefix[0] : ""),
                      getTotalConnections(), config.getMaximumPoolSize(), getIdleConnections(), config.getMinimumIdle(), getActiveConnections(), getThreadsAwaitingConnection());
      }
   }

   /**
    * 回收 {@link PoolEntry}（归还到池中）。
    *
    * @param poolEntry 要回收的条目
    */
   @Override
   void recycle(final PoolEntry poolEntry)
   {
      metricsTracker.recordConnectionUsage(poolEntry);
      if (poolEntry.isMarkedEvicted()) {
         closeConnection(poolEntry, EVICTED_CONNECTION_MESSAGE);
      } else {
         if (isRequestBoundariesEnabled) {
            try {
               poolEntry.connection.endRequest();
            } catch (SQLException e) {
               logger.warn("endRequest Failed for: {},({})", poolEntry.connection, e.getMessage());
            }
         }
         connectionBag.requite(poolEntry);
      }
   }

   /**
    * 永久关闭底层真实连接（吞掉异常）。
    *
    * @param poolEntry 持有待关闭连接的条目
    * @param closureReason 关闭原因
    */
   void closeConnection(final PoolEntry poolEntry, final String closureReason)
   {
      if (connectionBag.remove(poolEntry)) {
         final var connection = poolEntry.close();
         closeConnectionExecutor.execute(() -> {
            quietlyCloseConnection(connection, closureReason);
            if (poolState == POOL_NORMAL) {
               fillPool(false);
            }
         });
      }
   }

   @SuppressWarnings("unused")
   int[] getPoolStateCounts()
   {
      return connectionBag.getStateCounts();
   }


   // ***********************************************************************
   //                           私有方法
   // ***********************************************************************

   /**
    * 创建新的 {@link PoolEntry}。若配置了 maxLifetime，
    * 会按带随机方差的寿命调度退役任务，避免池中连接同时大量失效。
    */
   private PoolEntry createPoolEntry()
   {
      try {
         final var poolEntry = newPoolEntry(getTotalConnections() == 0);

         final var maxLifetime = config.getMaxLifetime();
         if (maxLifetime > 0) {
            // 默认在 maxLifetime 内随机方差（最多约 25%）
            final var variance = maxLifetime > 10_000L ? ThreadLocalRandom.current().nextLong( maxLifetime / lifeTimeVarianceFactor ) : 0L;
            final var lifetime = maxLifetime - variance;
            poolEntry.setFutureEol(houseKeepingExecutorService.schedule(new MaxLifetimeTask(poolEntry), lifetime, MILLISECONDS));
         }

         final long keepaliveTime = config.getKeepaliveTime();
         if (keepaliveTime > 0) {
            // 心跳间隔最多 20% 随机方差
            final var variance = ThreadLocalRandom.current().nextLong(keepaliveTime / 5);
            final var heartbeatTime = keepaliveTime - variance;
            poolEntry.setKeepalive(houseKeepingExecutorService.scheduleWithFixedDelay(new KeepaliveTask(poolEntry), heartbeatTime, heartbeatTime, MILLISECONDS));
         }

         return poolEntry;
      }
      catch (ConnectionSetupException e) {
         if (poolState == POOL_NORMAL) { // 检查 POOL_NORMAL，避免与 shutdown() 并发时刷屏日志
            logger.debug("{} - Error thrown while acquiring connection from data source", poolName, e.getCause());
         }
      }
      catch (Throwable e) {
         if (poolState == POOL_NORMAL) { // 检查 POOL_NORMAL，避免与 shutdown() 并发时刷屏日志
            logger.debug("{} - Cannot acquire connection from data source", poolName, e);
         }
      }

      return null;
   }

   /**
    * 根据当前空闲连接数补充到 {@code minimumIdle}。
    */
   private synchronized void fillPool(final boolean isAfterAdd)
   {
      final var idle = getIdleConnections();
      final var shouldAdd = getTotalConnections() < config.getMaximumPoolSize() && idle < config.getMinimumIdle();

      if (shouldAdd) {
         final var countToAdd = config.getMinimumIdle() - idle;
         for (int i = 0; i < countToAdd; i++)
            addConnectionExecutor.submit(isAfterAdd ? postFillPoolEntryCreator : poolEntryCreator);
      }
      else if (isAfterAdd) {
         logger.debug("{} - Fill pool skipped, pool has sufficient level or currently being filled.", poolName);
      }
   }

   /**
    * 尝试中止或关闭所有活跃连接。
    *
    * @param assassinExecutor 传给 {@link Connection#abort(Executor)} 的执行器
    */
   private void abortActiveConnections(final ExecutorService assassinExecutor)
   {
      for (var poolEntry : connectionBag.values(STATE_IN_USE)) {
         Connection connection = poolEntry.close();
         try {
            connection.abort(assassinExecutor);
         }
         catch (Throwable e) {
            quietlyCloseConnection(connection, "(connection aborted during shutdown)");
         }
         finally {
            connectionBag.remove(poolEntry);
         }
      }
   }

   /**
    * 若配置了 {@code initializationFailTimeout}，校验数据库连通性。
    *
    * @throws PoolInitializationException 创建或校验连接失败
    * @see HikariConfig#setInitializationFailTimeout(long)
    */
   private void checkFailFast()
   {
      final var initializationFailTimeout = config.getInitializationFailTimeout();
      if (initializationFailTimeout < 0) {
         return;
      }

      // 在超时窗口内重试创建首条连接
      final var startTime = currentTime();
      do {
         final var poolEntry = createPoolEntry();
         if (poolEntry != null) {
            if (config.getMinimumIdle() > 0) {
               connectionBag.add(poolEntry);
               logger.info("{} - Added connection {}", poolName, poolEntry.connection);
            }
            else {
               quietlyCloseConnection(poolEntry.close(), "(initialization check complete and minimumIdle is zero)");
            }

            return;
         }

         if (getLastConnectionFailure() instanceof ConnectionSetupException) {
            throwPoolInitializationException(getLastConnectionFailure().getCause());
         }

         quietlySleep(SECONDS.toMillis(1));
      } while (elapsedMillis(startTime) < initializationFailTimeout);

      if (initializationFailTimeout > 0) {
         throwPoolInitializationException(getLastConnectionFailure());
      }
   }

   /**
    * 记录导致池初始化失败的异常，并抛出附带原因的 {@link PoolInitializationException}。
    *
    * @param t 失败原因（可为 {@code null}）
    */
   private void throwPoolInitializationException(Throwable t)
   {
      destroyHouseKeepingExecutorService();
      throw new PoolInitializationException(t);
   }

   /**
    * 对连接/{@link PoolEntry} 执行“软”驱逐。
    * 若由用户经 {@link HikariDataSource#evictConnection(Connection)} 调用，则 {@code owner} 为 {@code true}。
    * <p>
    * 若调用方是持有者，或连接空闲（可在 {@link ConcurrentBag} 中 reserve），则立即关闭；
    * 否则仅标记待驱逐，下次借出时再关闭。
    * <p>
    * @param poolEntry 要软驱逐的条目
    * @param reason 驱逐原因
    * @param owner 调用方是否为连接持有者
    * @return 已关闭返回 {@code true}，仅标记返回 {@code false}
    */
   private boolean softEvictConnection(final PoolEntry poolEntry, final String reason, final boolean owner)
   {
      // 步骤1：标记条目待驱逐
      poolEntry.markEvicted();
      // 步骤2：持有者可立即关闭；空闲连接可 reserve 后关闭
      if (owner || connectionBag.reserve(poolEntry)) {
         closeConnection(poolEntry, reason);
         return true;
      }

      return false;
   }

   /**
    * 创建/初始化 housekeeping 用的 {@link ScheduledExecutorService}。
    * 若 {@link HikariConfig} 中已指定则使用用户提供的，否则创建并配置默认实例。
    *
    * @return 用户指定或内部创建的调度执行器
    */
   private ScheduledExecutorService initializeHouseKeepingExecutorService()
   {
      if (config.getScheduledExecutor() == null) {
         final var threadFactory = Optional.ofNullable(config.getThreadFactory()).orElseGet(() -> new DefaultThreadFactory(poolName + ":housekeeper"));
         final var executor = new ScheduledThreadPoolExecutor(1, threadFactory, new ThreadPoolExecutor.DiscardPolicy());
         executor.setExecuteExistingDelayedTasksAfterShutdownPolicy(false);
         executor.setRemoveOnCancelPolicy(true);
         return executor;
      }
      else {
         return config.getScheduledExecutor();
      }
   }

   /**
    * 销毁（shutdown）housekeeping 执行器（仅当由本池创建时）。
    */
   private void destroyHouseKeepingExecutorService()
   {
      if (config.getScheduledExecutor() == null) {
         houseKeepingExecutorService.shutdownNow();
      }
   }

   /**
    * 创建指标追踪用的 {@link PoolStats} 实例，采样分辨率 1 秒。
    *
    * @return {@link PoolStats} 实例
    */
   private PoolStats getPoolStats()
   {
      return new PoolStats(SECONDS.toMillis(1)) {
         @Override
         protected void update() {
            this.pendingThreads = HikariPool.this.getThreadsAwaitingConnection();
            this.idleConnections = HikariPool.this.getIdleConnections();
            this.totalConnections = HikariPool.this.getTotalConnections();
            this.activeConnections = HikariPool.this.getActiveConnections();
            this.maxConnections = config.getMaximumPoolSize();
            this.minConnections = config.getMinimumIdle();
         }
      };
   }

   /**
    * 创建获取连接超时异常（{@link SQLTransientConnectionException}）。
    * 若有底层原因（如驱动创建连接时抛出的 {@link SQLException}），
    * 复用其 SQLState 并链为 nextException。
    * <p>
    * 副作用：DEBUG 记录超时，并向指标追踪器上报。
    *
    * @param startTime 获取尝试的起始时间戳
    * @return 从 {@link #getConnection()} 抛出的 {@link SQLException}
    */
   private SQLException createTimeoutException(long startTime)
   {
      logPoolState("Timeout failure ");
      metricsTracker.recordConnectionTimeout();

      String sqlState = null;
      int errorCode = 0;
      final var originalException = getLastConnectionFailure();
      if (originalException instanceof SQLException) {
         sqlState = ((SQLException) originalException).getSQLState();
         errorCode = ((SQLException) originalException).getErrorCode();
      }
      final var connectionException = new SQLTransientConnectionException(
         poolName + " - Connection is not available, request timed out after " + elapsedMillis(startTime) + "ms " +
            "(total=" + getTotalConnections() + ", active=" + getActiveConnections() + ", idle=" + getIdleConnections() + ", waiting=" + getThreadsAwaitingConnection() + ")",
         sqlState, errorCode, originalException);
      if (originalException instanceof SQLException) {
         connectionException.setNextException((SQLException) originalException);
      }

      return connectionException;
   }


   // ***********************************************************************
   //                      具名内部类
   // ***********************************************************************

   /**
    * 创建 {@link PoolEntry}（连接）并加入池。
    */
   private final class PoolEntryCreator implements Callable<Boolean>
   {
      private final String loggingPrefix;

      PoolEntryCreator()
      {
         this(null);
      }

      PoolEntryCreator(String loggingPrefix)
      {
         this.loggingPrefix = loggingPrefix;
      }

      @Override
      public Boolean call()
      {
         var backoffMs = 10L;
         var added = false;
         try {
            // 仅在池正常且仍需要连接时循环创建
            while (shouldContinueCreating()) {
               final var poolEntry = createPoolEntry();
               if (poolEntry != null) {
                  added = true;
                  connectionBag.add(poolEntry);
                  logger.debug("{} - Added connection {}", poolName, poolEntry.connection);
                  quietlySleep(30L);
                  break;
               } else {  // 从数据库获取连接失败，退避后重试
                  if (loggingPrefix != null && backoffMs % 50 == 0)
                     logger.debug("{} - Connection add failed, sleeping with backoff: {}ms", poolName, backoffMs);
                  quietlySleep(backoffMs);
                  backoffMs = Math.min(SECONDS.toMillis(5), backoffMs * 2);
               }
            }
         }
         finally {
            if (added && loggingPrefix != null)
               logPoolState(loggingPrefix);
            else if (!added)
               logPoolState("Connection not added, ");
         }

         // 池已挂起、关闭或已达最大容量
         return Boolean.FALSE;
      }

      /**
       * 仅当需要更多空闲连接或有线程等待时才继续创建；否则退出。
       *
       * @return 是否应继续创建连接
       */
      private synchronized boolean shouldContinueCreating() {
         return poolState == POOL_NORMAL && !Thread.interrupted() && getTotalConnections() < config.getMaximumPoolSize() &&
            (getIdleConnections() < config.getMinimumIdle() || connectionBag.getWaitingThreadCount() > getIdleConnections());
      }
   }

   /**
    * Housekeeping 任务：退役过期空闲连接并维持最小空闲数。
    */
   private final class HouseKeeper implements Runnable
   {
      private volatile long previous = plusMillis(currentTime(), -housekeepingPeriodMs);
      @SuppressWarnings("AtomicFieldUpdaterNotStaticFinal")
      private final AtomicReferenceFieldUpdater<PoolBase, String> catalogUpdater = AtomicReferenceFieldUpdater.newUpdater(PoolBase.class, String.class, "catalog");

      @Override
      public void run()
      {
         try {
            // 刷新可能经 MBean 修改的运行时配置
            connectionTimeout = config.getConnectionTimeout();
            validationTimeout = config.getValidationTimeout();
            leakTaskFactory.updateLeakDetectionThreshold(config.getLeakDetectionThreshold());

            if (config.getCatalog() != null && !config.getCatalog().equals(catalog)) {
               catalogUpdater.set(HikariPool.this, config.getCatalog());
            }

            final var idleTimeout = config.getIdleTimeout();
            final var now = currentTime();

            // 检测时钟回拨（按 NTP 规范允许 +128ms 容差）
            if (plusMillis(now, 128) < plusMillis(previous, housekeepingPeriodMs)) {
               logger.warn("{} - Retrograde clock change detected (housekeeper delta={}), soft-evicting connections from pool.",
                           poolName, elapsedDisplayString(previous, now));
               previous = now;
               softEvictConnections();
               return;
            }
            else if (now > plusMillis(previous, (3 * housekeepingPeriodMs) / 2)) {
               // 时钟前跳无需驱逐，只会加速连接退役
               logger.warn("{} - Thread starvation or clock leap detected (housekeeper delta={}).", poolName, elapsedDisplayString(previous, now));
            }

            previous = now;

            if (idleTimeout > 0L && config.getMinimumIdle() < config.getMaximumPoolSize()) {
               logPoolState("Before cleanup ");
               final var notInUse = connectionBag.values(STATE_NOT_IN_USE);
               var maxToRemove = notInUse.size() - config.getMinimumIdle();
               for (PoolEntry entry : notInUse) {
                  if (maxToRemove > 0 && elapsedMillis(entry.lastAccessed, now) > idleTimeout && connectionBag.reserve(entry)) {
                     closeConnection(entry, "(connection has passed idleTimeout)");
                     maxToRemove--;
                  }
               }
               logPoolState("After  cleanup ");
            }
            else
               logPoolState("Pool ");

            fillPool(true); // 尝试维持最小空闲连接数
         }
         catch (Exception e) {
            logger.error("Unexpected exception in housekeeping task", e);
         }
      }
   }

   private final class MaxLifetimeTask implements Runnable
   {
      private final PoolEntry poolEntry;

      MaxLifetimeTask(final PoolEntry poolEntry)
      {
         this.poolEntry = poolEntry;
      }

      public void run()
      {
         if (softEvictConnection(poolEntry, "(connection has passed maxLifetime)", false /* not owner */)) {
            addBagItem(connectionBag.getWaitingThreadCount());
         }
      }
   }

   private final class KeepaliveTask implements Runnable
   {
      private final PoolEntry poolEntry;

      KeepaliveTask(final PoolEntry poolEntry)
      {
         this.poolEntry = poolEntry;
      }

      public void run()
      {
         if (connectionBag.reserve(poolEntry)) {
            if (isConnectionDead(poolEntry.connection)) {
               softEvictConnection(poolEntry, DEAD_CONNECTION_MESSAGE, true);
               addBagItem(connectionBag.getWaitingThreadCount());
            }
            else {
               connectionBag.unreserve(poolEntry);
               logger.debug("{} - keepalive: connection {} is alive", poolName, poolEntry.connection);
            }
         }
      }
   }

   /**
    * 池初始化失败时抛出，通常因无法创建或校验连接。
    * @hidden
    */
   public static class PoolInitializationException extends RuntimeException
   {
      private static final long serialVersionUID = 929872118275916520L;

      /**
       * 构造异常，可将给定 {@link Throwable} 作为原因。
       * @param t 要包装的原因
       */
      public PoolInitializationException(Throwable t)
      {
         super("Failed to initialize pool: " + t.getMessage(), t);
      }
   }
}
