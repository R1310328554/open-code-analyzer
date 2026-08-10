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

package com.alibaba.nacos.plugin.datasource.mapper;

import java.util.List;

/**
 * 所有 Mapper 的父接口。
 *
 * <p>定义通用 CRUD SQL 片段生成方法，以及表名、数据源类型、
 * 主键生成策略与数据库函数映射等元信息访问契约。</p>
 *
 * @author hyx
 **/

public interface Mapper {
    
    /**
     * 生成 SELECT 语句，包含列名与 WHERE 条件参数。
     *
     * @param columns 待查询列名列表
     * @param where   WHERE 条件参数列表
     * @return SELECT SQL 字符串
     */
    String select(List<String> columns, List<String> where);
    
    /**
     * 生成 INSERT 语句，包含待插入列名。
     *
     * @param columns 待插入列名列表
     * @return INSERT SQL 字符串
     */
    String insert(List<String> columns);
    
    /**
     * 生成 UPDATE 语句，包含 SET 列与 WHERE 条件参数。
     *
     * @param columns SET 子句列名列表
     * @param where   WHERE 条件参数列表
     * @return UPDATE SQL 字符串
     */
    String update(List<String> columns, List<String> where);
    
    /**
     * 生成 DELETE 语句。
     *
     * @param params DELETE 条件参数列表
     * @return DELETE SQL 字符串
     */
    String delete(List<String> params);
    
    /**
     * 生成 COUNT 语句，包含 WHERE 条件参数。
     *
     * @param where WHERE 条件参数列表
     * @return COUNT SQL 字符串
     */
    String count(List<String> where);
    
    /**
     * 获取映射表名。
     *
     * @return 表名
     */
    String getTableName();
    
    /**
     * 获取数据源类型标识。
     *
     * @return 数据源名称（如 mysql、derby）
     */
    String getDataSource();
    
    /**
     * 获取 config_info 表主键生成策略列名。
     * 旧默认值：Statement.RETURN_GENERATED_KEYS
     * 新默认值：new String[]{"id"}
     *
     * @return 主键列名数组
     */
    String[] getPrimaryKeyGeneratedKeys();
    
    /**
     * 按函数名获取当前数据库方言的函数表达式。
     *
     * @param functionName 函数名
     * @return 方言化函数 SQL 片段
     */
    String getFunction(String functionName);
}
