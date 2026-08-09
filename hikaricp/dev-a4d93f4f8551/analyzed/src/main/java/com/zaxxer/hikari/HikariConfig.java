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

package com.zaxxer.hikari;

import com.codahale.metrics.health.HealthCheckRegistry;
import com.zaxxer.hikari.metrics.MetricsTrackerFactory;
import com.zaxxer.hikari.util.Credentials;
import com.zaxxer.hikari.util.PropertyElf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.io.*;
import java.lang.reflect.Modifier;
import java.security.AccessControlException;
import java.sql.Connection;
import java.util.Properties;
import java.util.TreeSet;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicReference;

import static com.zaxxer.hikari.util.UtilityElf.*;
import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * HikariCP 连接池配置类，实现 {@link HikariConfigMXBean}。
 * <p>
 * 可通过属性文件、{@link Properties} 或 setter 配置；
 * 连接池启动后配置被“封存”，运行时仅可通过 MXBean 修改部分属性。
 *
 * @author Brett Wooldridge
 */
@SuppressWarnings({"SameParameterValue", "unused"})
public class HikariConfig implements HikariConfigMXBean
{
   private static final Logger LOGGER = LoggerFactory.getLogger(HikariConfig.class);

   private static final char[] ID_CHARACTERS = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();
   private static final long CONNECTION_TIMEOUT = SECONDS.toMillis(30);
   private static final long VALIDATION_TIMEOUT = SECONDS.toMillis(5);
   private static final long SOFT_TIMEOUT_FLOOR = Long.getLong("com.zaxxer.hikari.timeoutMs.floor", 250L);
   private static final long IDLE_TIMEOUT = MINUTES.toMillis(10);
   private static final long MAX_LIFETIME = MINUTES.toMillis(30);
   private static final long DEFAULT_KEEPALIVE_TIME = MINUTES.toMillis(2);
   private static final int DEFAULT_POOL_SIZE = 10;

   private static boolean unitTest = false;

   // 可通过 HikariConfigMXBean 在运行时修改的属性
   //
   private volatile String catalog;
   private volatile long connectionTimeout;
   private volatile long validationTimeout;
   private volatile long idleTimeout;
   private volatile long leakDetectionThreshold;
   private volatile long maxLifetime;
   private volatile int maxPoolSize;
   private volatile int minIdle;
   private final AtomicReference<Credentials> credentials = new AtomicReference<>(Credentials.of(null, null));

   // 运行时不可修改的属性
   //
   private long initializationFailTimeout;
   private String connectionInitSql;
   private String connectionTestQuery;
   private String credentialsProviderClassName;
   private String dataSourceClassName;
   private String dataSourceJndiName;
   private String driverClassName;
   private String exceptionOverrideClassName;
   private SQLExceptionOverride exceptionOverride;
   private String jdbcUrl;
   private String poolName;
   private String schema;
   private String transactionIsolationName;
   private boolean isAutoCommit;
   private boolean isReadOnly;
   private boolean isIsolateInternalQueries;
   private boolean isRegisterMbeans;
   private boolean isAllowPoolSuspension;
   private HikariCredentialsProvider credentialsProvider;
   private DataSource dataSource;
   private Properties dataSourceProperties;
   private ThreadFactory threadFactory;
   private ScheduledExecutorService scheduledExecutor;
   private MetricsTrackerFactory metricsTrackerFactory;
   private Object metricRegistry;
   private Object healthCheckRegistry;
   private Properties healthCheckProperties;

   private long keepaliveTime;

   private volatile boolean sealed;

   /**
    * 默认构造器。
    * <p>
    * 若系统属性 {@code hikari.configurationFile} 已设置，
    * 将尝试加载指定配置文件。
    * <p>
    * 也可使用 {@link #HikariConfig(String propertyFileName)} 代替系统属性。
    */
   public HikariConfig()
   {
      dataSourceProperties = new Properties();
      healthCheckProperties = new Properties();

      minIdle = -1;
      maxPoolSize = DEFAULT_POOL_SIZE;
      maxLifetime = MAX_LIFETIME;
      connectionTimeout = CONNECTION_TIMEOUT;
      validationTimeout = VALIDATION_TIMEOUT;
      idleTimeout = IDLE_TIMEOUT;
      initializationFailTimeout = 1;
      isAutoCommit = true;
      keepaliveTime = DEFAULT_KEEPALIVE_TIME;

      var systemProp = System.getProperty("hikaricp.configurationFile");
      if (systemProp != null) {
         loadProperties(systemProp);
      }
   }

   /**
    * 从指定 {@link Properties} 对象构造配置。
    *
    * @param properties 属性集合
    */
   public HikariConfig(Properties properties)
   {
      this();
      PropertyElf.setTargetFromProperties(this, properties);
   }

   /**
    * 从指定属性文件名构造配置。{@code propertyFileName} 先按文件系统路径解析，
    * 失败则尝试 {@code Class.getResourceAsStream(propertyFileName)}。
    *
    * @param propertyFileName 属性文件名或路径
    */
   public HikariConfig(String propertyFileName)
   {
      this();

      loadProperties(propertyFileName);
   }

   // ***********************************************************************
   //                       HikariConfigMXBean 方法
   // ***********************************************************************

   /** {@inheritDoc} */
   @Override
   public String getCatalog()
   {
      return catalog;
   }

   /** {@inheritDoc} */
   @Override
   public void setCatalog(String catalog)
   {
      this.catalog = catalog;
   }


   /** {@inheritDoc} */
   @Override
   public long getConnectionTimeout()
   {
      return connectionTimeout;
   }

   /** {@inheritDoc} */
   @Override
   public void setConnectionTimeout(long connectionTimeoutMs)
   {
      if (connectionTimeoutMs == 0) {
         this.connectionTimeout = Integer.MAX_VALUE;
      }
      else if (connectionTimeoutMs < SOFT_TIMEOUT_FLOOR) {
         throw new IllegalArgumentException("connectionTimeout cannot be less than " + SOFT_TIMEOUT_FLOOR + "ms");
      }
      else {
         this.connectionTimeout = connectionTimeoutMs;
      }
   }

   /** {@inheritDoc} */
   @Override
   public long getIdleTimeout()
   {
      return idleTimeout;
   }

   /** {@inheritDoc} */
   @Override
   public void setIdleTimeout(long idleTimeoutMs)
   {
      if (idleTimeoutMs < 0) {
         throw new IllegalArgumentException("idleTimeout cannot be negative");
      }
      this.idleTimeout = idleTimeoutMs;
   }

   /** {@inheritDoc} */
   @Override
   public long getLeakDetectionThreshold()
   {
      return leakDetectionThreshold;
   }

   /** {@inheritDoc} */
   @Override
   public void setLeakDetectionThreshold(long leakDetectionThresholdMs)
   {
      this.leakDetectionThreshold = leakDetectionThresholdMs;
   }

   /** {@inheritDoc} */
   @Override
   public long getMaxLifetime()
   {
      return maxLifetime;
   }

   /** {@inheritDoc} */
   @Override
   public void setMaxLifetime(long maxLifetimeMs)
   {
      this.maxLifetime = maxLifetimeMs;
   }

   /** {@inheritDoc} */
   @Override
   public int getMaximumPoolSize()
   {
      return maxPoolSize;
   }

   /** {@inheritDoc} */
   @Override
   public void setMaximumPoolSize(int maxPoolSize)
   {
      if (maxPoolSize < 1) {
         throw new IllegalArgumentException("maxPoolSize cannot be less than 1");
      }
      this.maxPoolSize = maxPoolSize;
   }

   /** {@inheritDoc} */
   @Override
   public int getMinimumIdle()
   {
      return minIdle;
   }

   /** {@inheritDoc} */
   @Override
   public void setMinimumIdle(int minIdle)
   {
      if (minIdle < 0) {
         throw new IllegalArgumentException("minimumIdle cannot be negative");
      }
      this.minIdle = minIdle;
   }

   /**
    * 获取 {@code DataSource.getConnection(username, password)} 的默认密码。
    *
    * @return 密码
    */
   public String getPassword()
   {
      return credentials.get().getPassword();
   }

   /**
    * 设置 {@code DataSource.getConnection(username, password)} 的默认密码。
    *
    * @param password 密码
    */
   @Override
   public void setPassword(String password)
   {
      credentials.updateAndGet(current -> Credentials.of(current.getUsername(), password));
   }

   /**
    * 获取 {@code DataSource.getConnection(username, password)} 的默认用户名。
    *
    * @return 用户名
    */
   public String getUsername()
   {
      return credentials.get().getUsername();
   }

   /**
    * 设置 {@code DataSource.getConnection(username, password)} 的默认用户名。
    *
    * @param username 用户名
    */
   @Override
   public void setUsername(String username)
   {
      credentials.updateAndGet(current -> Credentials.of(username, current.getPassword()));
   }

   /**
    * 原子地设置 {@code DataSource.getConnection(username, password)} 的默认用户名与密码。
    *
    * @param credentials 用户名与密码对
    */
   @Override
   public void setCredentials(final Credentials credentials)
   {
      this.credentials.set(credentials);
   }

   /**
    * 原子地获取 {@code DataSource.getConnection(username, password)} 的默认用户名与密码。
    *
    * @return 用户名与密码对
    */
   public Credentials getCredentials()
   {
      return credentials.get();
   }

   /** {@inheritDoc} */
   @Override
   public long getValidationTimeout()
   {
      return validationTimeout;
   }

   /** {@inheritDoc} */
   @Override
   public void setValidationTimeout(long validationTimeoutMs)
   {
      if (validationTimeoutMs < SOFT_TIMEOUT_FLOOR) {
         throw new IllegalArgumentException("validationTimeout cannot be less than " + SOFT_TIMEOUT_FLOOR + "ms");
      }

      this.validationTimeout = validationTimeoutMs;
   }

   // ***********************************************************************
   //                     其余配置方法
   // ***********************************************************************

   /**
    * 获取用于测试连接有效性的 SQL 查询。
    *
    * @return SQL 查询字符串，或 {@code null}
    */
   public String getConnectionTestQuery()
   {
      return connectionTestQuery;
   }

   /**
    * 设置用于测试连接有效性的 SQL 查询。
    * 部分数据库上使用 JDBC4 {@code Connection.isValid()} 更高效，推荐使用。
    *
    * @param connectionTestQuery SQL 查询字符串
    */
   public void setConnectionTestQuery(String connectionTestQuery)
   {
      checkIfSealed();
      this.connectionTestQuery = connectionTestQuery;
   }

   /**
    * 获取新建连接加入池之前执行的初始化 SQL。
    *
    * @return 新连接初始化 SQL，或 {@code null}
    */
   public String getConnectionInitSql()
   {
      return connectionInitSql;
   }

   /**
    * 设置新建连接加入池之前执行的初始化 SQL；若执行失败视为连接创建失败。
    *
    * @param connectionInitSql 新连接初始化 SQL
    */
   public void setConnectionInitSql(String connectionInitSql)
   {
      checkIfSealed();
      this.connectionInitSql = connectionInitSql;
   }

   /**
    * 获取显式指定由池包装的 {@link DataSource}。
    *
    * @return {@link DataSource} 实例，或 {@code null}
    */
   public DataSource getDataSource()
   {
      return dataSource;
   }

   /**
    * 设置池显式包装的 {@link DataSource}；此 setter 不可通过属性文件初始化。
    *
    * @param dataSource 要由池包装的 {@link DataSource}
    */
   public void setDataSource(DataSource dataSource)
   {
      checkIfSealed();
      this.dataSource = dataSource;
   }

   /**
    * 获取用于创建连接的 JDBC {@link DataSource} 类全名。
    *
    * @return JDBC {@link DataSource} 类全限定名
    */
   public String getDataSourceClassName()
   {
      return dataSourceClassName;
   }

   /**
    * 设置用于创建连接的 JDBC {@link DataSource} 类全限定名。
    *
    * @param className JDBC {@link DataSource} 类全限定名
    */
   public void setDataSourceClassName(String className)
   {
      checkIfSealed();
      this.dataSourceClassName = className;
   }

   /**
    * 添加用于配置 {@link DataSource}/{@link java.sql.Driver} 的属性（名/值对）。
    * <p>
    * 对 {@link DataSource}，属性名按 Java Bean 约定映射为 setter，
    * 例如 {@code cachePrepStmts} 对应 {@code setCachePrepStmts(value)}。
    * <p>
    * 对 {@link java.sql.Driver}，属性加入 {@link Properties}，
    * 在 {@link java.sql.Driver#connect(String, Properties)} 时传给驱动。
    *
    * @param propertyName 属性名
    * @param value 传给 DataSource/Driver 的值
    */
   public void addDataSourceProperty(String propertyName, Object value)
   {
      checkIfSealed();
      dataSourceProperties.put(propertyName, value);
   }

   public String getDataSourceJNDI()
   {
      return this.dataSourceJndiName;
   }

   public void setDataSourceJNDI(String jndiDataSource)
   {
      checkIfSealed();
      this.dataSourceJndiName = jndiDataSource;
   }

   public Properties getDataSourceProperties()
   {
      return dataSourceProperties;
   }

   public void setDataSourceProperties(Properties dsProperties)
   {
      checkIfSealed();
      dataSourceProperties.putAll(dsProperties);
   }

   public String getDriverClassName()
   {
      return driverClassName;
   }

   public void setDriverClassName(String driverClassName)
   {
      checkIfSealed();

      try {
         createInstance(driverClassName, java.sql.Driver.class);
         this.driverClassName = driverClassName;
      }
      catch (Exception e) {
         throw new RuntimeException("Failed to load driver class " + driverClassName, e);
      }
   }

   public String getJdbcUrl()
   {
      return jdbcUrl;
   }

   public void setJdbcUrl(String jdbcUrl)
   {
      checkIfSealed();
      this.jdbcUrl = jdbcUrl;
   }

   /**
    * 获取池中连接的默认自动提交行为。
    *
    * @return 默认自动提交设置
    */
   public boolean isAutoCommit()
   {
      return isAutoCommit;
   }

   /**
    * 设置池中连接的默认自动提交行为。
    *
    * @param isAutoCommit 期望的自动提交默认值
    */
   public void setAutoCommit(boolean isAutoCommit)
   {
      checkIfSealed();
      this.isAutoCommit = isAutoCommit;
   }

   /**
    * 获取是否允许挂起连接池。
    *
    * @return 是否允许挂起
    */
   public boolean isAllowPoolSuspension()
   {
      return isAllowPoolSuspension;
   }

   /**
    * 设置是否允许挂起连接池。启用挂起有性能开销；
    * 除非需要（例如冗余系统），否则不要启用。
    *
    * @param isAllowPoolSuspension 是否允许挂起
    */
   public void setAllowPoolSuspension(boolean isAllowPoolSuspension)
   {
      checkIfSealed();
      this.isAllowPoolSuspension = isAllowPoolSuspension;
   }

   /**
    * 获取池初始化失败超时。详见 {@code #setInitializationFailTimeout(long)}。
    *
    * @return 初始化失败前的等待毫秒数
    * @see HikariConfig#setInitializationFailTimeout(long)
    */
   public long getInitializationFailTimeout()
   {
      return initializationFailTimeout;
   }

   /**
    * 设置池初始化失败超时。适用于以 {@link HikariConfig} 构造 {@link HikariDataSource}，
    * 或无参构造后首次调用 {@link HikariDataSource#getConnection()} 的场景。
    * <ul>
    *   <li>大于 0：作为初始化超时；调用线程阻塞直至成功连库或超时，
    *       超时则抛出 {@code PoolInitializationException}。</li>
    *   <li>等于 0：无法获取连接时不阻止启动；启动时尝试获取连接并校验
    *       {@code connectionTestQuery} 与 {@code connectionInitSql}，校验失败则抛异常；
    *       无法获取连接则跳过校验，池在后台继续尝试，
    *       {@code DataSource#getConnection()} 调用方可能遇到异常。</li>
    *   <li>小于 0：启动时不尝试连接与校验，池立即启动并在后台获取连接，
    *       调用方可能遇到异常。</li>
    * </ul>
    * 当该值 ≥ 0 且执行初始连接校验时，不覆盖 {@code connectionTimeout} 或 {@code validationTimeout}；
    * 默认值为 1 毫秒。
    *
    * @param initializationFailTimeout 初始化失败超时毫秒数；0 表示校验后继续启动；
    *        小于 0 表示跳过所有初始化检查并立即启动
    */
   public void setInitializationFailTimeout(long initializationFailTimeout)
   {
      checkIfSealed();
      this.initializationFailTimeout = initializationFailTimeout;
   }

   /**
    * 判断内部池查询（主要是存活检测）是否通过 {@link Connection#rollback()} 隔离在独立事务中。
    * 默认 {@code false}。
    *
    * @return 是否隔离内部查询
    */
   public boolean isIsolateInternalQueries()
   {
      return isIsolateInternalQueries;
   }

   /**
    * 配置内部池查询是否通过 {@link Connection#rollback()} 隔离在独立事务中。
    * 默认 {@code false}。
    *
    * @param isolate 是否隔离内部查询
    */
   public void setIsolateInternalQueries(boolean isolate)
   {
      checkIfSealed();
      this.isIsolateInternalQueries = isolate;
   }

   public MetricsTrackerFactory getMetricsTrackerFactory()
   {
      return metricsTrackerFactory;
   }

   public void setMetricsTrackerFactory(MetricsTrackerFactory metricsTrackerFactory)
   {
      if (metricRegistry != null) {
         throw new IllegalStateException("cannot use setMetricsTrackerFactory() and setMetricRegistry() together");
      }

      this.metricsTrackerFactory = metricsTrackerFactory;
   }

   /**
    * 获取 HikariCP 注册指标所用的 MetricRegistry 实例。默认 {@code null}。
    *
    * @return 将使用的 MetricRegistry 实例
    */
   public Object getMetricRegistry()
   {
      return metricRegistry;
   }

   /**
    * 设置 HikariCP 注册指标所用的 MetricRegistry 实例。
    *
    * @param metricRegistry MetricRegistry 实例
    */
   public void setMetricRegistry(Object metricRegistry)
   {
      if (metricsTrackerFactory != null) {
         throw new IllegalStateException("cannot use setMetricRegistry() and setMetricsTrackerFactory() together");
      }

      if (metricRegistry != null) {
         metricRegistry = getObjectOrPerformJndiLookup(metricRegistry);

         if (!safeIsAssignableFrom(metricRegistry, "com.codahale.metrics.MetricRegistry")
             && !(safeIsAssignableFrom(metricRegistry, "io.dropwizard.metrics5.MetricRegistry"))
             && !(safeIsAssignableFrom(metricRegistry, "io.micrometer.core.instrument.MeterRegistry"))) {
            throw new IllegalArgumentException("Class must be instance of com.codahale.metrics.MetricRegistry, " +
               "io.dropwizard.metrics5.MetricRegistry, or io.micrometer.core.instrument.MeterRegistry");
         }
      }

      this.metricRegistry = metricRegistry;
   }

   /**
    * 获取 HikariCP 注册健康检查所用的 HealthCheckRegistry。
    * 目前仅支持 Codahale/DropWizard。
    *
    * @return 将使用的 HealthCheckRegistry 实例
    */
   public Object getHealthCheckRegistry()
   {
      return healthCheckRegistry;
   }

   /**
    * 设置 HikariCP 注册健康检查所用的 HealthCheckRegistry。
    * 目前仅支持 Codahale/DropWizard。默认 {@code null}。
    *
    * @param healthCheckRegistry 要使用的 HealthCheckRegistry
    */
   public void setHealthCheckRegistry(Object healthCheckRegistry)
   {
      checkIfSealed();

      if (healthCheckRegistry != null) {
         healthCheckRegistry = getObjectOrPerformJndiLookup(healthCheckRegistry);

         if (!(healthCheckRegistry instanceof HealthCheckRegistry)) {
            throw new IllegalArgumentException("Class must be an instance of com.codahale.metrics.health.HealthCheckRegistry");
         }
      }

      this.healthCheckRegistry = healthCheckRegistry;
   }

   public Properties getHealthCheckProperties()
   {
      return healthCheckProperties;
   }

   public void setHealthCheckProperties(Properties healthCheckProperties)
   {
      checkIfSealed();
      this.healthCheckProperties.putAll(healthCheckProperties);
   }

   public void addHealthCheckProperty(String key, String value)
   {
      checkIfSealed();
      healthCheckProperties.setProperty(key, value);
   }

   /**
    * 控制池中连接的 keepalive 检测间隔。使用中的连接不会被 keepalive 线程检测，仅空闲时检测。
    *
    * @return 存活检测间隔（毫秒），默认 0（禁用）
    */
   public long getKeepaliveTime() {
      return keepaliveTime;
   }

   /**
    * 控制池中连接的 keepalive 检测间隔。使用中的连接不会被 keepalive 线程检测，仅空闲时检测。
    *
    * @param keepaliveTimeMs 存活检测间隔（毫秒），默认 0（禁用）
    */
   public void setKeepaliveTime(long keepaliveTimeMs) {
      this.keepaliveTime = keepaliveTimeMs;
   }

   /**
    * 判断池中连接是否为只读模式。
    *
    * @return 只读返回 {@code true}
    */
   public boolean isReadOnly()
   {
      return isReadOnly;
   }

   /**
    * 配置加入池的连接是否为只读。
    *
    * @param readOnly 是否只读
    */
   public void setReadOnly(boolean readOnly)
   {
      checkIfSealed();
      this.isReadOnly = readOnly;
   }

   /**
    * 判断 HikariCP 是否在 JMX 中自注册 {@link HikariConfigMXBean} 与 {@link HikariPoolMXBean}。
    *
    * @return 是否注册 MXBean
    */
   public boolean isRegisterMbeans()
   {
      return isRegisterMbeans;
   }

   /**
    * 配置 HikariCP 是否在 JMX 中自注册 {@link HikariConfigMXBean} 与 {@link HikariPoolMXBean}。
    *
    * @param register 是否注册 MXBean
    */
   public void setRegisterMbeans(boolean register)
   {
      checkIfSealed();
      this.isRegisterMbeans = register;
   }

   /** {@inheritDoc} */
   @Override
   public String getPoolName()
   {
      return poolName;
   }

   /**
    * 设置连接池名称，主要用于日志与 JMX 管理控制台识别池及配置。
    *
    * @param poolName 连接池名称
    */
   public void setPoolName(String poolName)
   {
      checkIfSealed();
      this.poolName = poolName;
   }

   /**
    * 获取用于 housekeeping 的 {@link ScheduledExecutorService}。
    *
    * @return 调度执行器
    */
   public ScheduledExecutorService getScheduledExecutor()
   {
      return scheduledExecutor;
   }

   /**
    * 设置用于 housekeeping 的 {@link ScheduledExecutorService}。
    *
    * @param executor 调度执行器
    */
   public void setScheduledExecutor(ScheduledExecutorService executor)
   {
      checkIfSealed();
      this.scheduledExecutor = executor;
   }

   public String getTransactionIsolation()
   {
      return transactionIsolationName;
   }

   /**
    * 获取连接上设置的默认 schema 名称。
    *
    * @return 默认 schema 名称
    */
   public String getSchema()
   {
      return schema;
   }

   /**
    * 设置连接上使用的默认 schema 名称。
    *
    * @param schema 默认 schema 名称
    */
   public void setSchema(String schema)
   {
      checkIfSealed();
      this.schema = schema;
   }

   /**
    * 获取运行时获取凭据的 {@link HikariCredentialsProvider} 类名。
    *
    * @return 凭据提供者类名
    * @see HikariCredentialsProvider
    */
   public String getCredentialsProviderClassName()
   {
      return credentialsProviderClassName;
   }

   /**
    * 设置运行时获取凭据的 {@link HikariCredentialsProvider} 类名。
    * 也可通过 {@link #setCredentialsProvider(HikariCredentialsProvider)} 直接提供实例。
    *
    * @param credentialsProviderClassName 凭据提供者类名
    * @see HikariCredentialsProvider
    */
   public void setCredentialsProviderClassName(String credentialsProviderClassName) {
      checkIfSealed();

      try {
         this.credentialsProvider = createInstance(credentialsProviderClassName, HikariCredentialsProvider.class);
         this.exceptionOverrideClassName = credentialsProviderClassName;
      }
      catch (Exception e) {
         throw new RuntimeException("Failed to instantiate class " + credentialsProviderClassName, e);
      }
   }

   /**
    * 获取由 {@link #setCredentialsProviderClassName(String)} 创建
    * 或 {@link #setCredentialsProvider(HikariCredentialsProvider)} 指定的实例。
    *
    * @return {@link HikariCredentialsProvider} 实例，或 {@code null}
    * @see HikariCredentialsProvider
    */
   public HikariCredentialsProvider getCredentialsProvider() {
      return credentialsProvider;
   }

   /**
    * 设置用户提供的 {@link HikariCredentialsProvider} 实例；
    * 使用此方法时不应再调用 {@link #setCredentialsProviderClassName(String)}。
    *
    * @param credentialsProvider 用户提供的凭据提供者
    * @see HikariCredentialsProvider
    */
   public void setCredentialsProvider(HikariCredentialsProvider credentialsProvider) {
      checkIfSealed();
      this.credentialsProvider = credentialsProvider;
   }

   /**
    * 获取用户提供的 {@link SQLExceptionOverride} 类名。
    *
    * @return 类名
    * @see SQLExceptionOverride
    */
   public String getExceptionOverrideClassName()
   {
      return this.exceptionOverrideClassName;
   }

   /**
    * 设置用户提供的 {@link SQLExceptionOverride} 类名。
    *
    * @param exceptionOverrideClassName 类名
    * @see SQLExceptionOverride
    */
   public void setExceptionOverrideClassName(String exceptionOverrideClassName)
   {
      checkIfSealed();

      try {
         this.exceptionOverride = createInstance(exceptionOverrideClassName, SQLExceptionOverride.class);
         this.exceptionOverrideClassName = exceptionOverrideClassName;
      }
      catch (Exception e) {
         throw new RuntimeException("Failed to instantiate class " + exceptionOverrideClassName, e);
      }
   }

   /**
    * 获取由 {@link #setExceptionOverrideClassName(String)} 创建
    * 或 {@link #setExceptionOverride(SQLExceptionOverride)} 指定的实例。
    *
    * @return {@link SQLExceptionOverride} 实例，或 {@code null}
    * @see SQLExceptionOverride
    */
   public SQLExceptionOverride getExceptionOverride()
   {
      return this.exceptionOverride;
   }

   /**
    * 设置用户提供的 {@link SQLExceptionOverride} 实例。
    *
    * @param exceptionOverride 异常覆盖实现
    * @see SQLExceptionOverride
    */
   public void setExceptionOverride(SQLExceptionOverride exceptionOverride) {
      checkIfSealed();
      this.exceptionOverride = exceptionOverride;
   }

   /**
    * 设置默认事务隔离级别。值为 {@code Connection} 类中的常量名，
    * 例如 {@code TRANSACTION_REPEATABLE_READ}。
    *
    * @param isolationLevel 隔离级别名称
    */
   public void setTransactionIsolation(String isolationLevel)
   {
      checkIfSealed();
      this.transactionIsolationName = isolationLevel;
   }

   /**
    * 获取创建线程的 {@link ThreadFactory}。
    *
    * @return 线程工厂，{@code null} 时使用默认工厂
    */
   public ThreadFactory getThreadFactory()
   {
      return threadFactory;
   }

   /**
    * 设置创建线程的 {@link ThreadFactory}。
    *
    * @param threadFactory 线程工厂，{@code null} 时使用默认工厂
    */
   public void setThreadFactory(ThreadFactory threadFactory)
   {
      checkIfSealed();
      this.threadFactory = threadFactory;
   }

   void seal()
   {
      this.sealed = true;
   }

   /**
    * 将 {@code this} 的状态复制到 {@code other}。
    *
    * @param other 目标 {@link HikariConfig}
    */
   @SuppressWarnings({"rawtypes", "unchecked"})
   public void copyStateTo(HikariConfig other)
   {
      for (var field : HikariConfig.class.getDeclaredFields()) {
         try {
            if (!Modifier.isFinal(field.getModifiers())) {
               field.setAccessible(true);
               field.set(other, field.get(this));
            } else if (field.getType().isAssignableFrom(AtomicReference.class)) {
               ((AtomicReference) field.get(other)).set(((AtomicReference) field.get(this)).get());
            }
         }
         catch (Exception e) {
            throw new RuntimeException("Failed to copy HikariConfig state: " + e.getMessage(), e);
         }
      }

      other.sealed = false;
   }

   // ***********************************************************************
   //                          私有方法
   // ***********************************************************************

   @SuppressWarnings("StatementWithEmptyBody")
   /**
    * 校验并规范化配置，在启动池之前调用。
    */
   public void validate()
   {
      // 步骤1：生成默认池名并校验 JMX 池名
      if (poolName == null) {
         poolName = generatePoolName();
      }
      else if (isRegisterMbeans && poolName.contains(":")) {
         throw new IllegalArgumentException("poolName cannot contain ':' when used with JMX");
      }

      // 空字符串属性视为 null
      //noinspection NonAtomicOperationOnVolatileField
      catalog = getNullIfEmpty(catalog);
      connectionInitSql = getNullIfEmpty(connectionInitSql);
      connectionTestQuery = getNullIfEmpty(connectionTestQuery);
      transactionIsolationName = getNullIfEmpty(transactionIsolationName);
      dataSourceClassName = getNullIfEmpty(dataSourceClassName);
      dataSourceJndiName = getNullIfEmpty(dataSourceJndiName);
      driverClassName = getNullIfEmpty(driverClassName);
      jdbcUrl = getNullIfEmpty(jdbcUrl);

      // 校验数据源配置选项
      if (dataSource != null) {
         if (dataSourceClassName != null) {
            LOGGER.warn("{} - using dataSource and ignoring dataSourceClassName.", poolName);
         }
      }
      else if (dataSourceClassName != null) {
         if (driverClassName != null) {
            LOGGER.error("{} - cannot use driverClassName and dataSourceClassName together.", poolName);
            // 注意：此异常文本被 Spring Boot FailureAnalyzer 引用，不应
            // 在未通知 Spring Boot 开发者的情况下修改。
            throw new IllegalStateException("cannot use driverClassName and dataSourceClassName together.");
         }
         else if (jdbcUrl != null) {
            LOGGER.warn("{} - using dataSourceClassName and ignoring jdbcUrl.", poolName);
         }
      }
      else if (jdbcUrl != null || dataSourceJndiName != null) {
         // 配置有效
      }
      else if (driverClassName != null) {
         LOGGER.error("{} - jdbcUrl is required with driverClassName.", poolName);
         throw new IllegalArgumentException("jdbcUrl is required with driverClassName.");
      }
      else {
         LOGGER.error("{} - dataSource or dataSourceClassName or jdbcUrl is required.", poolName);
         throw new IllegalArgumentException("dataSource or dataSourceClassName or jdbcUrl is required.");
      }

      // 步骤2：校验数值型超时/池大小等参数
      validateNumerics();

      if (LOGGER.isDebugEnabled() || unitTest) {
         logConfiguration();
      }
   }

   private void validateNumerics()
   {
      if (maxLifetime != 0 && maxLifetime < SECONDS.toMillis(30)) {
         LOGGER.warn("{} - maxLifetime is less than 30000ms, setting to default {}ms.", poolName, MAX_LIFETIME);
         maxLifetime = MAX_LIFETIME;
      }

      // keepalive 间隔必须大于 30 秒
      if (keepaliveTime != 0 && keepaliveTime < SECONDS.toMillis(30)) {
         LOGGER.warn("{} - keepaliveTime is less than 30000ms, disabling it.", poolName);
         keepaliveTime = 0L;
      }

      // keepalive 必须小于 maxLifetime（若已启用 maxLifetime）
      if (keepaliveTime != 0 && maxLifetime != 0 && keepaliveTime >= maxLifetime) {
         LOGGER.warn("{} - keepaliveTime is greater than or equal to maxLifetime, disabling it.", poolName);
         keepaliveTime = 0L;
      }

      if (leakDetectionThreshold > 0 && !unitTest) {
         if (leakDetectionThreshold < SECONDS.toMillis(2) || (leakDetectionThreshold > maxLifetime && maxLifetime > 0)) {
            LOGGER.warn("{} - leakDetectionThreshold is less than 2000ms or more than maxLifetime, disabling it.", poolName);
            leakDetectionThreshold = 0;
         }
      }

      if (connectionTimeout < SOFT_TIMEOUT_FLOOR) {
         LOGGER.warn("{} - connectionTimeout is less than {}ms, setting to {}ms.", poolName, SOFT_TIMEOUT_FLOOR, CONNECTION_TIMEOUT);
         connectionTimeout = CONNECTION_TIMEOUT;
      }

      if (validationTimeout < SOFT_TIMEOUT_FLOOR) {
         LOGGER.warn("{} - validationTimeout is less than {}ms, setting to {}ms.", poolName, SOFT_TIMEOUT_FLOOR, VALIDATION_TIMEOUT);
         validationTimeout = VALIDATION_TIMEOUT;
      }

      if (minIdle < 0 || minIdle > maxPoolSize) {
         minIdle = maxPoolSize;
      }

      if (idleTimeout + SECONDS.toMillis(1) > maxLifetime && maxLifetime > 0 && minIdle < maxPoolSize) {
         LOGGER.warn("{} - idleTimeout is close to or more than maxLifetime, disabling it.", poolName);
         idleTimeout = 0;
      }
      else if (idleTimeout != 0 && idleTimeout < SECONDS.toMillis(10) && minIdle < maxPoolSize) {
         LOGGER.warn("{} - idleTimeout is less than 10000ms, setting to default {}ms.", poolName, IDLE_TIMEOUT);
         idleTimeout = IDLE_TIMEOUT;
      }
      else  if (idleTimeout != IDLE_TIMEOUT && idleTimeout != 0 && minIdle == maxPoolSize) {
         LOGGER.warn("{} - idleTimeout has been set but has no effect because the pool is operating as a fixed size pool.", poolName);
      }
   }

   private void checkIfSealed()
   {
      if (sealed) throw new IllegalStateException("The configuration of the pool is sealed once started. Use HikariConfigMXBean for runtime changes.");
   }

   private void logConfiguration()
   {
      LOGGER.debug("{} - configuration:", poolName);
      final var propertyNames = new TreeSet<>(PropertyElf.getPropertyNames(HikariConfig.class));
      for (var prop : propertyNames) {
         try {
            var value = PropertyElf.getProperty(prop, this);
            if ("dataSourceProperties".equals(prop)) {
               var dsProps = PropertyElf.copyProperties(dataSourceProperties);
               dsProps.setProperty("password", "<masked>");
               value = dsProps;
            }

            if ("initializationFailTimeout".equals(prop) && initializationFailTimeout == Long.MAX_VALUE) {
               value = "infinite";
            }
            else if ("transactionIsolation".equals(prop) && transactionIsolationName == null) {
               value = "default";
            }
            else if (prop.matches("scheduledExecutorService|threadFactory") && value == null) {
               value = "internal";
            }
            else if (prop.contains("jdbcUrl") && value instanceof String) {
               value = maskPasswordInJdbcUrl((String) value);
            }
            else if (prop.contains("password")) {
               value = "<masked>";
            }
            else if (value instanceof String) {
               value = "\"" + value + "\""; // quote to see lead/trailing spaces is any
            }
            else if (value == null) {
               value = "none";
            }
            LOGGER.debug("{}{}", (prop + "................................................").substring(0, 32), value);
         }
         catch (Exception e) {
            // 忽略并继续
         }
      }
   }

   private void loadProperties(String propertyFileName)
   {
      try (final var is = openPropertiesInputStream(propertyFileName)) {
         if (is != null) {
            var props = new Properties();
            props.load(is);
            PropertyElf.setTargetFromProperties(this, props);
         }
         else {
            throw new IllegalArgumentException("Cannot find property file: " + propertyFileName);
         }
      }
      catch (IOException io) {
         throw new RuntimeException("Failed to read property file", io);
      }
   }

   private InputStream openPropertiesInputStream(String propertyFileName) throws FileNotFoundException {
      final var propFile = new File(propertyFileName);
      if (propFile.isFile()) {
         return new FileInputStream(propFile);
      }
      var propertiesInputStream = this.getClass().getResourceAsStream(propertyFileName);
      if (propertiesInputStream == null) {
        propertiesInputStream = this.getClass().getClassLoader().getResourceAsStream(propertyFileName);
      }
      return propertiesInputStream;
   }

   private String generatePoolName()
   {
      final var prefix = "HikariPool-";
      try {
         // 池编号在 JVM 内全局递增，避免类加载器隔离环境下编号冲突
         synchronized (System.getProperties()) {
            final var next = String.valueOf(Integer.getInteger("com.zaxxer.hikari.pool_number", 0) + 1);
            System.setProperty("com.zaxxer.hikari.pool_number", next);
            return prefix + next;
         }
      } catch (AccessControlException e) {
         // SecurityManager 禁止读写系统属性
         // 改为生成随机池名
         final var random = ThreadLocalRandom.current();
         final var buf = new StringBuilder(prefix);

         for (var i = 0; i < 4; i++) {
            buf.append(ID_CHARACTERS[random.nextInt(62)]);
         }

         LOGGER.info("assigned random pool name '{}' (security manager prevented access to system properties)", buf);

         return buf.toString();
      }
   }

   private Object getObjectOrPerformJndiLookup(Object object)
   {
      if (object instanceof String) {
         try {
            var initCtx = new InitialContext();
            return initCtx.lookup((String) object);
         }
         catch (NamingException e) {
            throw new IllegalArgumentException(e);
         }
      }
      return object;
   }
}
