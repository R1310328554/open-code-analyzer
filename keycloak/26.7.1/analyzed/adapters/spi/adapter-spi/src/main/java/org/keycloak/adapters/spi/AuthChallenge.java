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

package org.keycloak.adapters.spi;

/**
 * 适配器向未认证客户端发送认证质询（Challenge）的 SPI。
 *
 * <p>实现类负责构造重定向、401 响应或其他协议相关的质询行为。</p>
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public interface AuthChallenge {
    /**
     * 向客户端发送认证质询。
     *
     * @param exchange HTTP 请求/响应门面
     * @return 若质询已成功发送则返回 {@code true}
     */
    boolean challenge(HttpFacade exchange);

    /**
     * 部分平台（如 Undertow）需要预先获知将返回的 HTTP 状态码。
     *
     * @return 质询响应的 HTTP 状态码
     */
    int getResponseCode();
}
