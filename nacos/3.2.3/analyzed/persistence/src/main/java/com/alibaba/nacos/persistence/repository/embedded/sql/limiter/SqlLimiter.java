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

package com.alibaba.nacos.persistence.repository.embedded.sql.limiter;

import com.alibaba.nacos.persistence.repository.embedded.sql.ModifyRequest;
import com.alibaba.nacos.persistence.repository.embedded.sql.SelectRequest;

import java.sql.SQLException;
import java.util.List;

/**
 * SQL 类型白名单限制器接口。
 *
 * <p>在嵌入式 Derby 场景下拦截非法 DML/DDL，防止导入或共识复制执行危险语句。</p>
 *
 * @author xiweng.yy
 */
public interface SqlLimiter {
    
    /** 校验单条 {@link ModifyRequest} 的 SQL 类型。 */
    void doLimitForModifyRequest(ModifyRequest modifyRequest) throws SQLException;
    
    /** 批量校验修改请求列表。 */
    void doLimitForModifyRequest(List<ModifyRequest> modifyRequests) throws SQLException;
    
    /** 校验单条 {@link SelectRequest}。 */
    void doLimitForSelectRequest(SelectRequest selectRequest) throws SQLException;
    
    /** 批量校验查询请求列表。 */
    void doLimitForSelectRequest(List<SelectRequest> selectRequests) throws SQLException;
    
    /** 校验原始 SQL 字符串。 */
    void doLimit(String sql) throws SQLException;
    
    /** 批量校验 SQL 字符串列表。 */
    void doLimit(List<String> sql) throws SQLException;
}
