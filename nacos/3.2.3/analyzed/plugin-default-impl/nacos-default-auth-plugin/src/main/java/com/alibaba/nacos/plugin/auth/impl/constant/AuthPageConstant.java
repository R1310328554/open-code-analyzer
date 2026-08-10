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

package com.alibaba.nacos.plugin.auth.impl.constant;

/**
 * 鉴权插件分页 SQL 片段常量。
 *
 * <p>兼容 SQL Server {@code OFFSET/FETCH} 与 MySQL {@code LIMIT} 两种分页语法。</p>
 *
 * @author huangKeming
 **/

public class AuthPageConstant {
    
    /** SQL Server 分页关键字：OFFSET。 */
    public static final String OFFSET = "OFFSET";
    
    /** SQL Server 分页：跳过行数占位符片段。 */
    public static final String OFFSET_ROWS = "OFFSET ? ROWS";
    
    /** SQL Server 分页：取下一批行数片段。 */
    public static final String FETCH_NEXT = "FETCH NEXT ? ROWS ONLY";
    
    /** MySQL 分页关键字：LIMIT。 */
    public static final String LIMIT = "LIMIT";
    
    /** MySQL 分页：偏移与条数占位符片段。 */
    public static final String LIMIT_SIZE = "LIMIT ?,?";
    
}
