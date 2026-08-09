/**
 * Copyright (c) 2013-2026 Nikita Koksharov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.redisson.client;

import org.redisson.config.Credentials;
import org.redisson.config.CredentialsResolver;

import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

/**
 * 默认凭据解析器，始终返回空用户名与密码的 {@link Credentials}。
 * <p>
 * 适用于未启用 ACL 或凭据由配置静态注入的场景。
 *
 * @author Nikita Koksharov
 *
 */
public final class DefaultCredentialsResolver implements CredentialsResolver {

    /** 预完成的空凭据 Future，避免每次解析重复分配。 */
    private final CompletionStage<Credentials> future = CompletableFuture.completedFuture(new Credentials());

    /** 忽略目标地址，直接返回空凭据。 */
    @Override
    public CompletionStage<Credentials> resolve(InetSocketAddress address) {
        return future;
    }

}
