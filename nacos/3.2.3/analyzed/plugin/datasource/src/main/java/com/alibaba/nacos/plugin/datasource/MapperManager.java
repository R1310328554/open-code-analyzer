/*
 * Copyright 1999-2022 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.plugin.datasource;

import com.alibaba.nacos.api.exception.runtime.NacosRuntimeException;
import com.alibaba.nacos.common.spi.NacosServiceLoader;
import com.alibaba.nacos.common.utils.StringUtils;
import com.alibaba.nacos.plugin.datasource.mapper.Mapper;
import com.alibaba.nacos.plugin.datasource.proxy.MapperProxy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static com.alibaba.nacos.api.common.Constants.Exception.FIND_DATASOURCE_ERROR_CODE;
import static com.alibaba.nacos.api.common.Constants.Exception.FIND_TABLE_ERROR_CODE;

/**
 * 数据源插件 Mapper 管理器。
 *
 * <p>通过 SPI 加载各数据库方言的 {@link Mapper} 实现，按数据源类型与表名索引，
 * 供持久化层按表查找对应 SQL 映射器。</p>
 *
 * @author hyx
 **/

public class MapperManager {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(MapperManager.class);
    
    /** 数据源类型 →（表名 → Mapper）二级索引。 */
    public static final Map<String, Map<String, Mapper>> MAPPER_SPI_MAP = new HashMap<>();
    
    /** 单例实例。 */
    private static final MapperManager INSTANCE = new MapperManager();
    
    /** 是否启用 Mapper 调用日志代理。 */
    private boolean dataSourceLogEnable;
    
    private MapperManager() {
        loadInitial();
    }
    
    /**
     * 获取 MapperManager 单例并设置日志开关。
     *
     * @param isDataSourceLogEnable 是否启用数据源 SQL 日志
     * @return MapperManager 实例
     */
    public static MapperManager instance(boolean isDataSourceLogEnable) {
        INSTANCE.dataSourceLogEnable = isDataSourceLogEnable;
        return INSTANCE;
    }
    
    /**
     * 初始化加载所有 SPI 注册的 Mapper 实现。
     */
    public synchronized void loadInitial() {
        Collection<Mapper> mappers = NacosServiceLoader.load(Mapper.class);
        for (Mapper mapper : mappers) {
            putMapper(mapper);
            LOGGER.info(
                "[MapperManager] Load Mapper({}) datasource({}) tableName({}) successfully.",
                mapper.getClass(), mapper.getDataSource(), mapper.getTableName());
        }
    }
    
    /**
     * 动态注册 Mapper 到 SPI 映射表。
     *
     * @param mapper 待注册的 Mapper 实现
     */
    public static synchronized void join(Mapper mapper) {
        if (Objects.isNull(mapper)) {
            return;
        }
        putMapper(mapper);
        LOGGER.info("[MapperManager] join successfully.");
    }
    
    /** 将 Mapper 写入二级索引，同表名已存在则跳过。 */
    private static void putMapper(Mapper mapper) {
        Map<String, Mapper> mapperMap =
            MAPPER_SPI_MAP.computeIfAbsent(mapper.getDataSource(), key -> new HashMap<>(16));
        mapperMap.putIfAbsent(mapper.getTableName(), mapper);
    }
    
    /**
     * 按数据源类型与表名查找 Mapper。
     *
     * @param dataSource 数据源类型（如 mysql、derby）
     * @param tableName  表名
     * @param <R>        Mapper 子类型
     * @return 匹配的 Mapper，启用日志时返回代理包装
     */
    public <R extends Mapper> R findMapper(String dataSource, String tableName) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("[MapperManager] findMapper dataSource: {}, tableName: {}", dataSource,
                tableName);
        }
        if (StringUtils.isBlank(dataSource) || StringUtils.isBlank(tableName)) {
            throw new NacosRuntimeException(FIND_DATASOURCE_ERROR_CODE,
                "dataSource or tableName is null");
        }
        Map<String, Mapper> tableMapper = MAPPER_SPI_MAP.get(dataSource);
        if (Objects.isNull(tableMapper)) {
            throw new NacosRuntimeException(FIND_DATASOURCE_ERROR_CODE,
                "[MapperManager] Failed to find the datasource,dataSource:" + dataSource);
        }
        Mapper mapper = tableMapper.get(tableName);
        if (Objects.isNull(mapper)) {
            throw new NacosRuntimeException(FIND_TABLE_ERROR_CODE,
                "[MapperManager] Failed to find the table ,tableName:" + tableName);
        }
        if (dataSourceLogEnable) {
            return MapperProxy.createSingleProxy(mapper);
        }
        return (R) mapper;
    }
    
    /**
     * 获取全部已注册 Mapper 的只读视图。
     *
     * @return 不可修改的二级映射表
     */
    public Map<String, Map<String, Mapper>> getAllMappers() {
        return Collections.unmodifiableMap(MAPPER_SPI_MAP);
    }
}
