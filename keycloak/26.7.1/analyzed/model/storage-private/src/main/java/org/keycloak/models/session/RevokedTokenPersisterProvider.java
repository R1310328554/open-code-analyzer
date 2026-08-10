/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.keycloak.models.session;

import java.util.stream.Stream;

import org.keycloak.provider.Provider;

/**
 * 已吊销令牌持久化 Provider：将令牌吊销记录写入持久存储，Keycloak 重启后仍可查询。
 * <p>
 * 存储层可优化为仅追加写入、仅通过过期机制删除条目。首个启动的 Keycloak 实例会从存储中重新加载所有已过期的令牌。
 *
 * @author Alexander Schwartz
 */
public interface RevokedTokenPersisterProvider extends Provider {

    /**
     * 吊销指定 ID 的令牌。
     *
     * @param tokenId 令牌标识
     * @param lifetime 吊销记录的有效期（毫秒）
     */
    void revokeToken(String tokenId, long lifetime);

    /** 返回所有已吊销令牌的流。 */
    Stream<RevokedToken> getAllRevokedTokens();

    /** 清理所有已过期的吊销令牌记录。 */
    void expireTokens();
}
