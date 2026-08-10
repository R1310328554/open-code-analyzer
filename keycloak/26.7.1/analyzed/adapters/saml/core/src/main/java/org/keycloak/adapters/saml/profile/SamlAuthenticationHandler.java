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

package org.keycloak.adapters.saml.profile;

import org.keycloak.adapters.saml.OnSessionCreated;
import org.keycloak.adapters.spi.AuthChallenge;
import org.keycloak.adapters.spi.AuthOutcome;

/**
 * SAML 认证处理器接口，封装单次 HTTP 交互中的 SAML 握手与认证结果。
 *
 * <p>实现类负责解析 {@code SAMLRequest}/{@code SAMLResponse}、校验签名与断言，
 * 并在成功时通过 {@link OnSessionCreated} 回调通知会话已建立。</p>
 *
 * @author <a href="mailto:psilva@redhat.com">Pedro Igor</a>
 */
public interface SamlAuthenticationHandler {

    /**
     * 处理当前请求的 SAML 认证流程。
     *
     * @param onCreateSession 会话创建成功后的回调
     * @return 认证结果枚举
     */
    AuthOutcome handle(OnSessionCreated onCreateSession);

    /**
     * 返回需要向客户端呈现的认证质询（如重定向至 IdP）。
     *
     * @return 认证质询对象；无质询时可为 {@code null}
     */
    AuthChallenge getChallenge();
}
