/*
 * Copyright 2025 Red Hat, Inc. and/or its affiliates
 *  and other contributors as indicated by the @author tags.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.keycloak.protocol.oidc.encode;

import org.keycloak.models.ClientSessionContext;
import org.keycloak.provider.Provider;

/**
 * 将令牌上下文编码进访问令牌 ID，后续可从 token id 解析而无需依赖非标准 claim。
 * <p>例如可区分轻量/常规访问令牌、在线/离线会话等。</p>
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public interface TokenContextEncoderProvider extends Provider {

    /**
     * 从客户端会话上下文构建令牌上下文。
     * @param clientSessionContext 客户端会话上下文
     * @param rawTokenId 原始 token id
     * @param isOffline 是否为离线令牌
     * @return 访问令牌上下文
     */
    AccessTokenContext getTokenContextFromClientSessionContext(ClientSessionContext clientSessionContext, String rawTokenId, boolean isOffline);

    /**
     * 从编码后的 token id 解析令牌上下文。
     * @param encodedTokenId 含上下文前缀的 token id
     * @return 访问令牌上下文
     */
    AccessTokenContext getTokenContextFromTokenId(String encodedTokenId);

    /**
     * 将令牌上下文编码为 token id 前缀。
     * @param tokenContext 访问令牌上下文
     * @return 编码后的 token id
     */
    String encodeTokenId(AccessTokenContext tokenContext);
}
