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

package org.keycloak.services.clientregistration;

import org.keycloak.events.EventBuilder;
import org.keycloak.provider.Provider;

/**
 * 动态客户端注册 Provider SPI 接口。
 * <p>各协议/格式（OIDC、SAML、安装配置等）实现此接口以提供注册端点。</p>
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
public interface ClientRegistrationProvider extends Provider {

    /** 注入注册端点认证上下文 */
    void setAuth(ClientRegistrationAuth auth);

    /** @return 当前认证上下文 */
    ClientRegistrationAuth getAuth();

    /** 注入事件构建器 */
    void setEvent(EventBuilder event);

    /** @return 事件构建器 */
    EventBuilder getEvent();

}
