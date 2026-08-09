/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.rocketmq.auth.authorization.provider;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import org.apache.rocketmq.auth.authentication.model.Subject;
import org.apache.rocketmq.auth.authorization.model.Acl;
import org.apache.rocketmq.auth.config.AuthConfig;

/**
 * 授权元数据提供者：负责 ACL 的持久化 CRUD 与查询。
 */
public interface AuthorizationMetadataProvider {

    /** 初始化存储后端，绑定 {@link AuthConfig} 与可选元数据服务。 */
    void initialize(AuthConfig authConfig, Supplier<?> metadataService);

    /** 关闭 RocksDB 与缓存线程等资源。 */
    void shutdown();

    /** 持久化新建 ACL。 */
    CompletableFuture<Void> createAcl(Acl acl);

    /** 删除主体的 ACL 记录。 */
    CompletableFuture<Void> deleteAcl(Subject subject);

    /** 更新已有 ACL 记录。 */
    CompletableFuture<Void> updateAcl(Acl acl);

    /** 按主体查询 ACL。 */
    CompletableFuture<Acl> getAcl(Subject subject);

    /** 列出 ACL，支持主体与资源子串过滤。 */
    CompletableFuture<List<Acl>> listAcl(String subjectFilter, String resourceFilter);
}
