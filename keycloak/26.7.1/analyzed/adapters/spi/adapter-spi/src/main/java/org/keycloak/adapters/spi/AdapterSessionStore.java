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
 * 适配器在认证重定向前保存/恢复原始 HTTP 请求的会话存储 SPI。
 *
 * <p>浏览器 SSO 流程会将用户重定向至 IdP，认证成功后需恢复被中断的请求；
 * 各容器（Servlet、Undertow 等）通过实现本接口提供具体存储机制。</p>
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public interface AdapterSessionStore {
    /** 将当前请求保存到会话，以便认证完成后恢复。 */
    void saveRequest();
    /** 从会话恢复先前保存的请求；若存在则返回 {@code true}。 */
    boolean restoreRequest();
}
