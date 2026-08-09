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
package org.redisson.config;

import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

/**
 * 凭据解析器接口，在连接与重连过程中动态获取 Valkey/Redis 认证凭据。
 * <p>
 * 适用于令牌轮换、Entra ID 等外部认证场景；参见 {@link EntraIdCredentialsResolver}。
 *
 * @author Nikita Koksharov
 *
 */
public interface CredentialsResolver {

    /**
     * 异步解析指定 Valkey/Redis 节点 <code>address</code> 的认证凭据。
     *
     * @param address Valkey 或 Redis 节点地址
     * @return 包含 {@link Credentials} 的 CompletionStage
     */
    CompletionStage<Credentials> resolve(InetSocketAddress address);

    /**
     * 返回在下次需要刷新凭据时完成的 CompletionStage。
     * <p>
     * The returned CompletionStage should complete when an external authentication
     * system changed credentials and CompletionStage instance returned
     * by {@link #resolve(InetSocketAddress)} method has been updated.
     * <p>
     * For continuous monitoring, implementations should return a new CompletionStage
     * instance after each credentials update to support chaining multiple renewal events.
     *
     * @return 凭据续期信号；完成时表示应重新调用 {@link #resolve(InetSocketAddress)}
     *
     * @see EntraIdCredentialsResolver
     *
     */
    /** 默认实现返回永不完成的 Future（静态凭据场景无需续期）。 */
    default CompletionStage<Void> nextRenewal() {
        return new CompletableFuture<>();
    }

}
