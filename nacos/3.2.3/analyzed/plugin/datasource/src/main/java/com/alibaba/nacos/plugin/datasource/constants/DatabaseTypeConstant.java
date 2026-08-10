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

package com.alibaba.nacos.plugin.datasource.constants;

/**
 * 数据库类型标识常量。
 *
 * <p>与 {@link com.alibaba.nacos.plugin.datasource.dialect.DatabaseDialect#getType()} 及方言插件注册键一致。</p>
 *
 * @author Long Yu
 **/
public class DatabaseTypeConstant {
    
    /** PostgreSQL 数据库类型。 */
    public static final String POSTGRESQL = "postgresql";
    
    /** 华为 GaussDB 数据库类型。 */
    public static final String GUASSDB = "gaussdb";
    
    /** MySQL 数据库类型。 */
    public static final String MYSQL = "mysql";
    
    /** Oracle 数据库类型。 */
    public static final String ORACLE = "oracle";
    
    /** 达梦（DM）数据库类型。 */
    public static final String DM = "dm";
    
    /** Microsoft SQL Server 数据库类型。 */
    public static final String SQLSERVER = "sqlserver";
    
    /** 人大金仓 Kingbase 数据库类型。 */
    public static final String KINGBASE = "kingbase";
    
    /** 崖山数据库（YashanDB）类型。 */
    public static final String YASDB = "yasdb";
    
    /** Apache Derby 内嵌数据库类型。 */
    public static final String DERBY = "derby";
    
}
