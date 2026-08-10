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
 * 数据源插件 SPI 标识常量。
 *
 * <p>与 {@link com.alibaba.nacos.plugin.datasource.MapperManager} 注册的数据源实现名称对应。</p>
 *
 * @author hyx
 **/

public class DataSourceConstant {
    
    /** MySQL 数据源插件标识。 */
    public static final String MYSQL = "mysql";
    
    /** 内嵌 Derby 数据源插件标识。 */
    public static final String DERBY = "derby";
    
    /** Oracle 数据源插件标识。 */
    public static final String ORACLE = "oracle";
}
