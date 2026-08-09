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
package org.apache.rocketmq.auth.authorization.manager;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.apache.rocketmq.auth.authentication.model.Subject;
import org.apache.rocketmq.auth.authorization.enums.PolicyType;
import org.apache.rocketmq.auth.authorization.model.Acl;
import org.apache.rocketmq.auth.authorization.model.Resource;

/**
 * 授权元数据管理器：对外暴露 ACL 的 CRUD 与查询接口，供管理 API 调用。
 */
public interface AuthorizationMetadataManager {

    /** 关闭底层认证与授权元数据提供者。 */
    void shutdown();

    /** 创建 ACL；主体已存在时合并策略。 */
    CompletableFuture<Void> createAcl(Acl acl);

    /** 更新 ACL；不存在时等同创建。 */
    CompletableFuture<Void> updateAcl(Acl acl);

    /** 删除主体的全部 ACL。 */
    CompletableFuture<Void> deleteAcl(Subject subject);

    /** 删除指定策略类型下某资源的策略条目；无剩余条目时删除整个 ACL。 */
    CompletableFuture<Void> deleteAcl(Subject subject, PolicyType policyType, Resource resource);

    /** 按主体查询 ACL。 */
    CompletableFuture<Acl> getAcl(Subject subject);

    /** 列出 ACL；支持按主体键与资源键子串过滤。 */
    CompletableFuture<List<Acl>> listAcl(String subjectFilter, String resourceFilter);
}
