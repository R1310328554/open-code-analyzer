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

package com.alibaba.nacos.persistence.repository.embedded.operate;

import com.alibaba.nacos.common.model.RestResult;
import com.alibaba.nacos.persistence.repository.embedded.EmbeddedStorageContextHolder;
import com.alibaba.nacos.persistence.repository.embedded.sql.ModifyRequest;
import org.springframework.jdbc.core.RowMapper;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

/**
 * 嵌入式 Derby 数据库操作门面接口。
 *
 * <p>对外暴露查询、批量更新、数据导入及基于 {@link EmbeddedStorageContextHolder} 的 blockUpdate 能力，屏蔽单机/集群实现差异。</p>
 *
 * @author <a href="mailto:liaochuntao@live.com">liaochuntao</a>
 */
public interface DatabaseOperate {
    
    /** 无参数单条查询。 */
    <R> R queryOne(String sql, Class<R> cls);
    
    /** 带参数单条查询。 */
    <R> R queryOne(String sql, Object[] args, Class<R> cls);
    
    /** 使用 RowMapper 的单条查询。 */
    <R> R queryOne(String sql, Object[] args, RowMapper<R> mapper);
    
    /** 使用 RowMapper 的多条查询。 */
    <R> List<R> queryMany(String sql, Object[] args, RowMapper<R> mapper);
    
    /** 按 Class 类型查询列表。 */
    <R> List<R> queryMany(String sql, Object[] args, Class<R> rClass);
    
    /** 查询多行 Map 结果。 */
    List<Map<String, Object>> queryMany(String sql, Object[] args);
    
    /** 批量修改数据，支持结果回调。 */
    Boolean update(List<ModifyRequest> modifyRequests, BiConsumer<Boolean, Throwable> consumer);
    
    /** 批量修改数据（无回调）。 */
    default Boolean update(List<ModifyRequest> modifyRequests) {
        return update(modifyRequests, null);
    }
    
    /**
     * 从外部 SQL 文件异步导入嵌入式 Derby。
     *
     * @param file {@link File}
     * @return {@link CompletableFuture}
     */
    CompletableFuture<RestResult<String>> dataImport(File file);
    
    /** 提交并清空当前线程 {@link EmbeddedStorageContextHolder} 中的 SQL 上下文。 */
    default Boolean blockUpdate() {
        return blockUpdate(null);
    }
    
    /**
     * 提交当前线程 SQL 上下文，并在 finally 中清理 ThreadLocal。
     *
     * @author klw(213539@qq.com)
     * 2020/8/24 18:16
     * @param consumer the consumer
     * @return java.lang.Boolean
     */
    default Boolean blockUpdate(BiConsumer<Boolean, Throwable> consumer) {
        try {
            return update(EmbeddedStorageContextHolder.getCurrentSqlContext(), consumer);
        } finally {
            EmbeddedStorageContextHolder.cleanAllContext();
        }
    }
    
}
