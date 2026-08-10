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

package org.keycloak.models;

/**
 * 客户端配置解析器：对 {@link ClientModel} 常用布尔/属性访问的薄封装。
 * <p>遗留类，后续可能移除；新代码应直接使用 {@link ClientModel}。</p>
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class ClientConfigResolver {
    /** 被解析的客户端模型。 */
    protected ClientModel client;

    /** @param client 目标客户端 */
    public ClientConfigResolver(ClientModel client) {
        this.client = client;
    }

    /** @param name 客户端属性名
     * @return 属性值 */
    public String resolveAttribute(String name) {
        return client.getAttribute(name);
    }

    /** @return 是否启用前端通道登出 */
    public boolean isFrontchannelLogout() {
        return client.isFrontchannelLogout();
    }

    /** @return 是否需要用户同意 */
    boolean isConsentRequired() {
        return client.isConsentRequired();
    }

    /** @return 是否启用标准授权码流程 */
    boolean isStandardFlowEnabled() {
        return client.isStandardFlowEnabled();
    }

    /** @return 是否启用服务账户 */
    boolean isServiceAccountsEnabled() {
        return client.isServiceAccountsEnabled();
    }
}
