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
package org.apache.rocketmq.proxy.auth;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import org.apache.rocketmq.auth.authentication.model.Subject;
import org.apache.rocketmq.auth.authorization.provider.AuthorizationMetadataProvider;
import org.apache.rocketmq.auth.authorization.model.Acl;
import org.apache.rocketmq.auth.config.AuthConfig;
import org.apache.rocketmq.proxy.service.metadata.MetadataService;

/**
 * Proxy 授权元数据提供者：委托 {@link MetadataService} 查询 ACL，写操作暂未实现。
 */
public class ProxyAuthorizationMetadataProvider implements AuthorizationMetadataProvider {

    /** 授权模块配置。 */
    protected AuthConfig authConfig;

    /** 元数据服务，用于读取 ACL 规则。 */
    protected MetadataService metadataService;

    /** 注入授权配置与元数据服务 Supplier。 */
    @Override
    public void initialize(AuthConfig authConfig, Supplier<?> metadataService) {
        this.authConfig = authConfig;
        if (metadataService != null) {
            this.metadataService = (MetadataService) metadataService.get();
        }
    }

    @Override
    public void shutdown() {

    }

    /** 创建 ACL（Proxy 侧未实现）。 */
    @Override
    public CompletableFuture<Void> createAcl(Acl acl) {
        return null;
    }

    @Override
    public CompletableFuture<Void> deleteAcl(Subject subject) {
        return null;
    }

    @Override
    public CompletableFuture<Void> updateAcl(Acl acl) {
        return null;
    }

    /** 异步按主体查询 {@link Acl}。 */
    @Override
    public CompletableFuture<Acl> getAcl(Subject subject) {
        return this.metadataService.getAcl(null, subject);
    }

    /** 列出 ACL（Proxy 侧未实现）。 */
    @Override
    public CompletableFuture<List<Acl>> listAcl(String subjectFilter, String resourceFilter) {
        return null;
    }
}
