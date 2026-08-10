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

import java.util.List;

/**
 * Mapper 方法执行结果。
 *
 * <p>封装预编译 SQL 语句及其占位符参数列表，
 * 由持久化层直接绑定执行。</p>
 *
 * @author hyx
 **/

public class MapperResult {
    
    public MapperResult() {
    }
    
    /**
     * 构造映射结果。
     *
     * @param sql       预编译 SQL
     * @param paramList 占位符参数列表
     */
    public MapperResult(String sql, List<Object> paramList) {
        this.sql = sql;
        this.paramList = paramList;
    }
    
    /** 预编译 SQL 语句。 */
    private String sql;
    
    /** 占位符绑定参数列表。 */
    private List<Object> paramList;
    
    /** @return 预编译 SQL 语句 */
    public String getSql() {
        return sql;
    }
    
    /** @param sql 预编译 SQL 语句 */
    public void setSql(String sql) {
        this.sql = sql;
    }
    
    /** @return 占位符参数列表 */
    public List<Object> getParamList() {
        return paramList;
    }
    
    /** @param paramList 占位符参数列表 */
    public void setParamList(List<Object> paramList) {
        this.paramList = paramList;
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
        return "MapperResult{" + "sql='" + sql + '\'' + ", paramList=" + paramList + '}';
    }
}
