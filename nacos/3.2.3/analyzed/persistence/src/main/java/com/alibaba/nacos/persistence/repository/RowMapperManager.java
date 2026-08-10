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

package com.alibaba.nacos.persistence.repository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * {@link RowMapper} 注册与管理器。
 *
 * <p>维护类全名到 RowMapper 实例的映射，内置 {@link MapRowMapper} 将结果集转为 Map， 支持运行时注册自定义映射器。</p>
 *
 * @author <a href="mailto:liaochuntao@live.com">liaochuntao</a>
 */
public final class RowMapperManager {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(RowMapperManager.class);
    
    /** 默认 Map 行映射器单例。 */
    public static final MapRowMapper MAP_ROW_MAPPER = new MapRowMapper();
    
    /** 类全名到 RowMapper 的注册表。 */
    public static Map<String, RowMapper> mapperMap = new HashMap<>(16);
    
    static {
        // 注册内置 MAP_ROW_MAPPER
        mapperMap.put(MAP_ROW_MAPPER.getClass().getCanonicalName(), MAP_ROW_MAPPER);
    }
    
    /** 按类全名查找已注册的 RowMapper。 */
    public static <D> RowMapper<D> getRowMapper(String classFullName) {
        return (RowMapper<D>) mapperMap.get(classFullName);
    }
    
    /**
     * 注册自定义 RowMapper 到管理器。
     *
     * @param classFullName 映射器处理的类全名
     * @param rowMapper RowMapper 实例
     * @param <D> 映射目标类型
     */
    /** 注册或覆盖 RowMapper，冲突时记录警告。 */
    public static synchronized <D> void registerRowMapper(String classFullName,
        RowMapper<D> rowMapper) {
        if (mapperMap.containsKey(classFullName)) {
            LOGGER.warn("row mapper {} conflicts, {} will be replaced by {}", classFullName,
                mapperMap.get(classFullName).getClass().getCanonicalName(),
                rowMapper.getClass().getCanonicalName());
        }
        mapperMap.put(classFullName, rowMapper);
    }
    
    /** 将 ResultSet 每行转为 LinkedHashMap 的 RowMapper 实现。 */
    public static final class MapRowMapper implements RowMapper<Map<String, Object>> {
        
        @Override
        /** 按列标签填充 Map，保持列顺序。 */
        public Map<String, Object> mapRow(ResultSet resultSet, int rowNum) throws SQLException {
            ResultSetMetaData metaData = resultSet.getMetaData();
            int columnCount = metaData.getColumnCount();
            Map<String, Object> map = new LinkedHashMap<>(columnCount);
            for (int i = 1; i <= columnCount; i++) {
                map.put(metaData.getColumnLabel(i), resultSet.getObject(i));
            }
            return map;
        }
    }
    
}
