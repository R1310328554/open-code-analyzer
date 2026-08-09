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
package org.redisson.quarkus.client.it;

import org.redisson.config.Credentials;
import org.redisson.config.CredentialsResolver;

import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

/**
 * 集成测试用 {@link CredentialsResolver}：始终返回空 {@link Credentials}。
 * <p>用于验证 Quarkus Redisson 扩展在无认证 Redis 下的连接流程。
 *
 * @author Nikita Koksharov
 */
public final class MyCredentialsResolver implements CredentialsResolver {

    private final CompletionStage<Credentials> future = CompletableFuture.completedFuture(new Credentials());

    /** 忽略目标地址，返回预构造的空凭证 Future。 */
    @Override
    public CompletionStage<Credentials> resolve(InetSocketAddress address) {
        return future;
    }

}
