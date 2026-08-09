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
package org.apache.rocketmq.auth.authentication.provider;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import org.apache.rocketmq.auth.authentication.model.User;
import org.apache.rocketmq.auth.config.AuthConfig;

/**
 * 认证元数据提供者：负责用户 CRUD 与查询，供管理接口与鉴权链使用。
 */
public interface AuthenticationMetadataProvider {

    /** 初始化存储后端与缓存，绑定 {@link AuthConfig} 与可选元数据服务。 */
    void initialize(AuthConfig authConfig, Supplier<?> metadataService);

    /** 关闭 RocksDB 与后台线程等资源。 */
    void shutdown();

    /** 创建用户并持久化。 */
    CompletableFuture<Void> createUser(User user);

    /** 按用户名删除用户。 */
    CompletableFuture<Void> deleteUser(String username);

    /** 更新已有用户信息。 */
    CompletableFuture<Void> updateUser(User user);

    /** 按用户名查询单个用户。 */
    CompletableFuture<User> getUser(String username);

    /** 列出用户；filter 非空时按用户名子串过滤。 */
    CompletableFuture<List<User>> listUser(String filter);
}
