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

package com.alibaba.nacos.persistence.repository.embedded.sql;

import org.springframework.jdbc.core.RowMapper;

/**
 * 查询类型常量，对应 {@link org.springframework.jdbc.core.JdbcTemplate} 的不同查询方法。
 *
 * <p>供 {@link SelectRequest} 在 Raft 复制查询时选择正确的 JdbcTemplate 调用路径。</p>
 *
 * @author <a href="mailto:liaochuntao@live.com">liaochuntao</a>
 */
public class QueryType {
    
    /** 带 RowMapper 与参数的单条查询。 */
    public static final byte QUERY_ONE_WITH_MAPPER_WITH_ARGS = 0;
    
    /** 无参数、按 Class 映射的单条查询。 */
    public static final byte QUERY_ONE_NO_MAPPER_NO_ARGS = 1;
    
    /** 带参数、按 Class 映射的单条查询。 */
    public static final byte QUERY_ONE_NO_MAPPER_WITH_ARGS = 2;
    
    /** 带 RowMapper 的多条查询。 */
    public static final byte QUERY_MANY_WITH_MAPPER_WITH_ARGS = 3;
    
    /** 返回 List&lt;Map&gt; 的多条查询。 */
    public static final byte QUERY_MANY_WITH_LIST_WITH_ARGS = 4;
    
    /** 按 Class 映射的多条列表查询。 */
    public static final byte QUERY_MANY_NO_MAPPER_WITH_ARGS = 5;
    
}
