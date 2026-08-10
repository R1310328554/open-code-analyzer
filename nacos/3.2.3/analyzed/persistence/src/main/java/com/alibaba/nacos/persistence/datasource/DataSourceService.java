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

import java.io.IOException;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * 数据源服务接口。
 *
 * <p>抽象 Nacos 持久层 JDBC 访问：初始化、重载、主库可写检测、 {@link JdbcTemplate} 与 {@link TransactionTemplate} 获取及健康状态查询。</p>
 *
 * @author Nacos
 */
public interface DataSourceService {
    
    /**
     * 初始化数据源及相关 JDBC 资源。
     *
     * @throws Exception 初始化失败时抛出
     */
    void init() throws Exception;
    
    /**
     * 重新加载数据源配置（如外部 MySQL 多数据源切换）。
     *
     * @throws IOException 重载失败时抛出
     */
    void reload() throws IOException;
    
    /**
     * 检测当前主库是否可写。
     *
     * @return 主库可写返回 true
     */
    boolean checkMasterWritable();
    
    /**
     * 获取用于 SQL 操作的 {@link JdbcTemplate}。
     *
     * @return JDBC 模板
     */
    JdbcTemplate getJdbcTemplate();
    
    /**
     * 获取事务模板，用于编程式事务。
     *
     * @return 事务模板
     */
    TransactionTemplate getTransactionTemplate();
    
    /**
     * 返回当前活跃数据源的 JDBC URL。
     *
     * @return 数据库连接 URL
     */
    String getCurrentDbUrl();
    
    /**
     * 返回数据源健康状态摘要（UP/DOWN/WARN）。
     *
     * @return 健康信息字符串
     */
    String getHealth();
    
    /**
     * 返回当前数据源平台类型（如 mysql、derby）。
     *
     * @return 数据源类型
     */
    String getDataSourceType();
    
}
