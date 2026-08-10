/*
 * Copyright 1999-2023 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alibaba.nacos.persistence.datasource;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.core.env.Environment;

import java.util.concurrent.TimeUnit;

/**
 * 数据源连接池属性封装。
 *
 * <p>Nacos 使用 HikariCP 作为连接池，本类基于 {@link com.zaxxer.hikari.HikariDataSource} 提供默认超时、池大小等配置，并支持从 {@code db.pool.config} 绑定外部属性。</p>
 *
 * @author xiweng.yy
 */
public class DataSourcePoolProperties {
    
    /** 默认连接超时：3 秒。 */
    public static final long DEFAULT_CONNECTION_TIMEOUT = TimeUnit.SECONDS.toMillis(3L);
    
    public static final long DEFAULT_VALIDATION_TIMEOUT = TimeUnit.SECONDS.toMillis(10L);
    
    public static final long DEFAULT_IDLE_TIMEOUT = TimeUnit.MINUTES.toMillis(10L);
    
    /** 默认最大连接池大小。 */
    public static final int DEFAULT_MAX_POOL_SIZE = 20;
    
    public static final int DEFAULT_MINIMUM_IDLE = 2;
    
    /** 内部 HikariCP 数据源实例。 */
    private final HikariDataSource dataSource;
    
    private DataSourcePoolProperties() {
        dataSource = new HikariDataSource();
        dataSource.setIdleTimeout(DEFAULT_IDLE_TIMEOUT);
        dataSource.setConnectionTimeout(DEFAULT_CONNECTION_TIMEOUT);
        dataSource.setValidationTimeout(DEFAULT_VALIDATION_TIMEOUT);
        dataSource.setMaximumPoolSize(DEFAULT_MAX_POOL_SIZE);
        dataSource.setMinimumIdle(DEFAULT_MINIMUM_IDLE);
    }
    
    /**
     * 从 Spring {@link Environment} 构建 Hikari 连接池配置。
     *
     * @return 已绑定 {@code db.pool.config} 的池属性对象
     */
    /** 创建实例并绑定环境变量中的池参数。 */
    public static DataSourcePoolProperties build(Environment environment) {
        DataSourcePoolProperties result = new DataSourcePoolProperties();
        Binder.get(environment).bind("db.pool.config", Bindable.ofInstance(result.getDataSource()));
        return result;
    }
    
    public void setDriverClassName(final String driverClassName) {
        dataSource.setDriverClassName(driverClassName);
    }
    
    public void setJdbcUrl(final String jdbcUrl) {
        dataSource.setJdbcUrl(jdbcUrl);
    }
    
    public void setUsername(final String username) {
        dataSource.setUsername(username);
    }
    
    public void setPassword(final String password) {
        dataSource.setPassword(password);
    }
    
    /** 返回底层 HikariCP 数据源。 */
    public HikariDataSource getDataSource() {
        return dataSource;
    }
}
