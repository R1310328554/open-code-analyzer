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

package com.alibaba.nacos.plugin.auth.impl.model;

/**
 * 分页查询 SQL 与参数封装结果。
 *
 * <p>由分页助手生成带 OFFSET/LIMIT 的 fetch SQL 及合并后的绑定参数数组。</p>
 *
 * @author huangKeMing
 */
public class OffsetFetchResult {
    
    /** 分页数据查询 SQL。 */
    String fetchSql;
    
    /** 与 fetchSql 对应的绑定参数数组。 */
    Object[] newArgs;
    
    /** 无参构造。 */
    public OffsetFetchResult() {
    }
    
    /** 指定 fetch SQL 与参数数组。 */
    public OffsetFetchResult(String fetchSql, Object[] newArgs) {
        this.fetchSql = fetchSql;
        this.newArgs = newArgs;
    }
    
    /** 获取分页查询 SQL。 */
    public String getFetchSql() {
        return fetchSql;
    }
    
    /** 设置分页查询 SQL。 */
    public void setFetchSql(String fetchSql) {
        this.fetchSql = fetchSql;
    }
    
    /** 获取绑定参数数组。 */
    public Object[] getNewArgs() {
        return newArgs;
    }
    
    /** 设置绑定参数数组。 */
    public void setNewArgs(Object[] newArgs) {
        this.newArgs = newArgs;
    }
    
}
