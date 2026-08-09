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
package org.apache.rocketmq.auth.authentication.manager;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.apache.rocketmq.auth.authentication.model.User;
import org.apache.rocketmq.auth.config.AuthConfig;

/**
 * 认证元数据管理接口：用户 CRUD 及超级用户判定。
 */
public interface AuthenticationMetadataManager {

    /** 关闭底层元数据提供者。 */
    void shutdown();

    /** 根据配置初始化默认用户与内部客户端凭证。 */
    void initUser(AuthConfig authConfig);

    /** 创建新用户。 */
    CompletableFuture<Void> createUser(User user);

    /** 更新已有用户的密码、类型或状态。 */
    CompletableFuture<Void> updateUser(User user);

    /** 删除用户并同步清理 ACL。 */
    CompletableFuture<Void> deleteUser(String username);

    /** 按用户名查询用户。 */
    CompletableFuture<User> getUser(String username);

    /** 按过滤条件列出用户。 */
    CompletableFuture<List<User>> listUser(String filter);

    /** 判断用户是否为 {@link UserType#SUPER}。 */
    CompletableFuture<Boolean> isSuperUser(String username);
}
