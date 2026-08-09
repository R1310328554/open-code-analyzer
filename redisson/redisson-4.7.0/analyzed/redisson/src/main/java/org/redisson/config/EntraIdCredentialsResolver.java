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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.authentication.core.Token;
import redis.clients.authentication.core.TokenListener;
import redis.clients.authentication.core.TokenManager;

import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

/**
 * 基于 Microsoft Entra ID（原 Azure AD）的 Redis 凭据解析器。
 * <p>
 * 实现 {@link CredentialsResolver}，为 Azure Cache for Redis 或 Azure Managed Redis
 * 提供无密码的令牌认证；自动处理令牌获取、刷新与续期通知。
 *
 * @author Nikita Koksharov
 *
 */
public class EntraIdCredentialsResolver implements CredentialsResolver {


    private static final Logger log = LoggerFactory.getLogger(EntraIdCredentialsResolver.class);

    /** 底层令牌管理器，负责 Entra ID 令牌生命周期。 */
    final TokenManager tokenManager;

    /** 当前可用的凭据 Future；令牌更新时替换。 */
    volatile CompletableFuture<Credentials> future = new CompletableFuture<>();
    /** 凭据续期信号 Future；新令牌就绪时完成旧实例。 */
    volatile CompletableFuture<Void> renewalFuture = new CompletableFuture<>();

    /** 启动 TokenManager 并注册令牌变更监听器。 */
    public EntraIdCredentialsResolver(TokenManager tokenManager) {
        this.tokenManager = tokenManager;

        TokenListener listener = new TokenListener() {

            @Override
            public void onTokenRenewed(Token token) {
                if (!future.isDone()) {
                    // 首次令牌就绪，完成初始 future
                    future.complete(new Credentials(token.getUser(), token.getValue()));
                } else {
                    future = CompletableFuture.completedFuture(new Credentials(token.getUser(), token.getValue()));
                    CompletableFuture<Void> oldFuture = renewalFuture;
                    renewalFuture = new CompletableFuture<>();
                    oldFuture.complete(null);
                }
            }

            @Override
            public void onError(Exception e) {
                log.error("Unable to renew token", e);
            }
        };

        try {
            tokenManager.start(listener, false);
        } catch (Exception e) {
            CompletableFuture<Credentials> cf = new CompletableFuture<>();
            cf.completeExceptionally(e);
            future = cf;
            tokenManager.stop();
            throw new IllegalStateException("Unable to start", e);
        }
    }

    /** 返回当前凭据 Future（与节点地址无关，令牌全局有效）。 */
    @Override
    public CompletionStage<Credentials> resolve(InetSocketAddress address) {
        return future;
    }

    /** 返回下次令牌续期时应关注的 Future。 */
    @Override
    public CompletionStage<Void> nextRenewal() {
        return renewalFuture;
    }

}
