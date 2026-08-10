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

package com.alibaba.nacos.plugin.datasource.model;

import java.util.HashMap;
import java.util.Map;

/**
 * Mapper 方法的统一入参容器。
 *
 * <p>将 WHERE、UPDATE 及上下文参数与分页信息封装为独立映射，
 * 供各 Mapper 方法生成预编译 SQL。</p>
 *
 * @author hyx
 **/

public class MapperContext {
    
    /** WHERE 子句参数映射。 */
    private final Map<String, Object> whereParamMap;
    
    /** UPDATE SET 子句参数映射。 */
    private final Map<String, Object> updateParamMap;
    
    /** 辅助上下文参数（不参与占位符绑定）。 */
    private final Map<String, String> contextParamMap;
    
    /** 分页起始行。 */
    private int startRow;
    
    /** 每页条数。 */
    private int pageSize;
    
    public MapperContext() {
        this.whereParamMap = new HashMap<>();
        this.updateParamMap = new HashMap<>();
        this.contextParamMap = new HashMap<>();
    }
    
    public MapperContext(int startRow, int pageSize) {
        this();
        this.startRow = startRow;
        this.pageSize = pageSize;
    }
    
    /**
     * 获取 WHERE 子句参数值。
     *
     * @param key 参数键
     * @return 键对应的参数值
     */
    public Object getWhereParameter(String key) {
        return whereParamMap.get(key);
    }
    
    /**
     * 设置 WHERE 子句参数。
     *
     * @param key   参数键
     * @param value 参数值
     */
    public void putWhereParameter(String key, Object value) {
        this.whereParamMap.put(key, value);
    }
    
    /**
     * 获取上下文辅助参数（如灰度名等逻辑开关）。
     *
     * @param key 参数键
     * @return 键对应的字符串值
     */
    public String getContextParameter(String key) {
        return contextParamMap.get(key);
    }
    
    /**
     * 设置上下文辅助参数。
     *
     * @param key   参数键
     * @param value 参数值
     */
    public void putContextParameter(String key, String value) {
        this.contextParamMap.put(key, value);
    }
    
    /**
     * 获取 UPDATE SET 子句参数值。
     *
     * @param key 参数键
     * @return 键对应的参数值
     */
    public Object getUpdateParameter(String key) {
        return updateParamMap.get(key);
    }
    
    /**
     * 设置 UPDATE SET 子句参数。
     *
     * @param key   参数键
     * @param value 参数值
     */
    public void putUpdateParameter(String key, Object value) {
        this.updateParamMap.put(key, value);
    }
    
    @Override
    public boolean equals(Object o) {
        return super.equals(o);
    }
    
    @Override
    public int hashCode() {
        return super.hashCode();
    }
    
    @Override
    public String toString() {
        return "MapperContext{" + "whereParamMap=" + whereParamMap + '}';
    }
    
    /** @return 分页起始行 */
    public int getStartRow() {
        return startRow;
    }
    
    /** @param startRow 分页起始行 */
    public void setStartRow(int startRow) {
        this.startRow = startRow;
    }
    
    /** @return 每页条数 */
    public int getPageSize() {
        return pageSize;
    }
    
    /** @param pageSize 每页条数 */
    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }
}
