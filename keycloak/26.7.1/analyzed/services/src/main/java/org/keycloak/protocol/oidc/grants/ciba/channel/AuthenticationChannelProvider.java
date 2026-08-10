/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates
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
package org.keycloak.protocol.oidc.grants.ciba.channel;

import org.keycloak.provider.Provider;

/**
 * CIBA 认证通道提供者接口。
 * <p>通过认证通道向外部认证设备（AD）发起身份认证与授权请求。</p>
 *
 * @author <a href="mailto:takashi.norimatsu.ws@hitachi.com">Takashi Norimatsu</a>
 */
public interface AuthenticationChannelProvider extends Provider {

    /**
     * 经认证通道向外部实体请求 AD 完成认证与授权。
     * @param request 后台认证端点收到的 {@link CIBAAuthenticationRequest}
     * @param infoUsedByAuthenticator 供 AD 识别用户的辅助信息（如 login_hint）
     * @return 请求是否已成功发出
     */
    boolean requestAuthentication(CIBAAuthenticationRequest request, String infoUsedByAuthenticator);
}
