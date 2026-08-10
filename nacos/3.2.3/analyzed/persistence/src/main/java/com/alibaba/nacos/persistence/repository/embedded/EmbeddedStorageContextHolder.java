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

package com.alibaba.nacos.persistence.repository.embedded;

import com.alibaba.nacos.persistence.repository.embedded.sql.ModifyRequest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 嵌入式存储线程上下文持有者。
 *
 * <p>使用 {@link ThreadLocal} 暂存待批量执行的 {@link ModifyRequest} 列表及扩展信息，供 {@link DatabaseOperate#blockUpdate()} 在同一事务中提交。</p>
 *
 * @author xiweng.yy
 */
public class EmbeddedStorageContextHolder {
    
    private static final ThreadLocal<ArrayList<ModifyRequest>> SQL_CONTEXT =
        ThreadLocal.withInitial(ArrayList::new);
    
    private static final ThreadLocal<Map<String, String>> EXTEND_INFO_CONTEXT =
        ThreadLocal.withInitial(HashMap::new);
    
    /**
     * 向当前线程追加一条待执行的修改 SQL。
     *
     * @param sql  sql
     * @param args argument list
     */
    public static void addSqlContext(String sql, Object... args) {
        ArrayList<ModifyRequest> requests = SQL_CONTEXT.get();
        ModifyRequest context = new ModifyRequest();
        context.setExecuteNo(requests.size());
        context.setSql(sql);
        context.setArgs(args);
        requests.add(context);
        SQL_CONTEXT.set(requests);
    }
    
    /**
     * 追加修改 SQL，并指定更新影响行数为 0 时是否回滚事务。
     *
     * @param rollbackOnUpdateFail  roll back when update fail
     * @param sql  sql
     * @param args argument list
     */
    public static void addSqlContext(boolean rollbackOnUpdateFail, String sql, Object... args) {
        ArrayList<ModifyRequest> requests = SQL_CONTEXT.get();
        ModifyRequest context = new ModifyRequest();
        context.setExecuteNo(requests.size());
        context.setSql(sql);
        context.setArgs(args);
        context.setRollBackOnUpdateFail(rollbackOnUpdateFail);
        requests.add(context);
        SQL_CONTEXT.set(requests);
    }
    
    /** 写入单条扩展上下文信息（如业务追踪键）。 */
    public static void putExtendInfo(String key, String value) {
        Map<String, String> old = EXTEND_INFO_CONTEXT.get();
        old.put(key, value);
        EXTEND_INFO_CONTEXT.set(old);
    }
    
    /** 批量合并扩展上下文信息。 */
    public static void putAllExtendInfo(Map<String, String> map) {
        Map<String, String> old = EXTEND_INFO_CONTEXT.get();
        old.putAll(map);
        EXTEND_INFO_CONTEXT.set(old);
    }
    
    /** 判断扩展上下文中是否包含指定键。 */
    public static boolean containsExtendInfo(String key) {
        Map<String, String> extendInfo = EXTEND_INFO_CONTEXT.get();
        boolean exist = extendInfo.containsKey(key);
        EXTEND_INFO_CONTEXT.set(extendInfo);
        return exist;
    }
    
    /** 获取当前线程累积的 SQL 修改请求列表。 */
    public static List<ModifyRequest> getCurrentSqlContext() {
        return SQL_CONTEXT.get();
    }
    
    /** 获取当前线程的扩展信息映射。 */
    public static Map<String, String> getCurrentExtendInfo() {
        return EXTEND_INFO_CONTEXT.get();
    }
    
    /** 清理当前线程全部 SQL 与扩展上下文，防止 ThreadLocal 泄漏。 */
    public static void cleanAllContext() {
        SQL_CONTEXT.remove();
        EXTEND_INFO_CONTEXT.remove();
    }
}
