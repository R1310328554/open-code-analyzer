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

import com.alibaba.nacos.persistence.configuration.DatasourceConfiguration;

/**
 * 动态数据源适配器（单例）。
 *
 * <p>根据 {@link DatasourceConfiguration#isEmbeddedStorage()} 在 {@link LocalDataSourceServiceImpl}（Derby 嵌入式）与 {@link ExternalDataSourceServiceImpl}（外部 MySQL 等） 之间切换，单机默认嵌入式、集群默认外部库。</p>
 *
 * @author Nacos
 */
public class DynamicDataSource {
    
    /** 本地 Derby 嵌入式数据源服务。 */
    private DataSourceService localDataSourceService = null;
    
    /** 外部数据库数据源服务。 */
    private DataSourceService basicDataSourceService = null;
    
    /** 单例实例。 */
    private static final DynamicDataSource INSTANCE = new DynamicDataSource();
    
    private DynamicDataSource() {
    }
    
    /** 获取动态数据源单例。 */
    public static DynamicDataSource getInstance() {
        return INSTANCE;
    }
    
    /** 懒加载并返回当前模式对应的数据源服务。 */
    public synchronized DataSourceService getDataSource() {
        try {
            
            // 单机模式默认使用嵌入式存储
            // 集群模式默认使用外部数据库
            
            if (DatasourceConfiguration.isEmbeddedStorage()) {
                if (localDataSourceService == null) {
                    localDataSourceService = new LocalDataSourceServiceImpl();
                    localDataSourceService.init();
                }
                return localDataSourceService;
            } else {
                if (basicDataSourceService == null) {
                    basicDataSourceService = new ExternalDataSourceServiceImpl();
                    basicDataSourceService.init();
                }
                return basicDataSourceService;
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
}
