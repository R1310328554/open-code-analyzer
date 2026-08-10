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

package com.alibaba.nacos.plugin.datasource.dialect;

/**
 * 数据库方言 SPI 接口。
 *
 * <p>封装分页 SQL 拼接、主键回填列名及可信 SQL 函数解析，由 {@link com.alibaba.nacos.plugin.datasource.manager.DatabaseDialectManager} 按类型加载。</p>
 *
 * @author Long Yu
 */
public interface DatabaseDialect {
    
    /**
     * 返回方言对应的数据库类型标识。
     * @return return database type name
     */
    public String getType();
    
    /**
     * 计算分页第一个参数（通常为偏移量 offset）。
     * @param page current pageNo
     * @param pageSize current pageSize
     * @return offset val or maxRange
     */
    public int getPagePrevNum(int page, int pageSize);
    
    /**
     * 计算分页第二个参数（通常为 LIMIT 条数）。
     * @param page current pageNo
     * @param pageSize current pageSize
     * @return limit val or minRange
     */
    public int getPageLastNum(int page, int pageSize);
    
    /**
     * 为 SQL 追加取前 N 条的 LIMIT 占位符子句。
     * @param sql orign sql
     * @return append limit sql
     */
    public String getLimitTopSqlWithMark(String sql);
    
    /**
     * 为 SQL 追加标准分页 LIMIT 占位符（offset + size）。
     * @param sql orign sql
     * @return append limit sql
     */
    public String getLimitPageSqlWithMark(String sql);
    
    /**
     * 将页码与页大小直接拼入 LIMIT 子句。
     * @param sql orign sql
     * @param pageNo current pageNo
     * @param pageSize current pageSize
     * @return contain page number param sql
     */
    public String getLimitPageSql(String sql, int pageNo, int pageSize);
    
    /**
     * 按绝对偏移量拼接 LIMIT 分页 SQL。
     * @param sql orign sql
     * @param startOffset current offset row
     * @param pageSize current pageSize
     * @return contain page number param sql
     */
    public String getLimitPageSqlWithOffset(String sql, int startOffset, int pageSize);
    
    /**
     * 返回 INSERT 后需回填的主键列名数组。
     * @return
     */
    public String[] getReturnPrimaryKeys();
    
    /**
     * 按函数名解析当前方言的可信 SQL 函数片段
     * @author Mr.Muzhi
     * @since 2025/1/7 16:30
     * @param functionName functionName
     * @return function
     */
    String getFunction(String functionName);
}
