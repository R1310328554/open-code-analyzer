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

package org.keycloak.keys;

import org.keycloak.crypto.KeyStatus;

/**
 * 密钥元数据抽象基类：描述密钥提供者 ID、优先级、kid 及 {@link org.keycloak.crypto.KeyStatus}。
 *
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
public abstract class KeyMetadata {

    private String providerId;
    private long providerPriority;

    private String kid;

    private KeyStatus status;

    /** @return 密钥提供者 ID */
    public String getProviderId() {
        return providerId;
    }

    /** @param providerId 密钥提供者 ID */
    public void setProviderId(String providerId) {
        this.providerId = providerId;
    }

    /** @return 提供者优先级（数值越大优先级越高） */
    public long getProviderPriority() {
        return providerPriority;
    }

    /** @param providerPriority 提供者优先级 */
    public void setProviderPriority(long providerPriority) {
        this.providerPriority = providerPriority;
    }

    /** @return 密钥标识符（Key ID） */
    public String getKid() {
        return kid;
    }

    /** @param kid 密钥标识符 */
    public void setKid(String kid) {
        this.kid = kid;
    }

    /** @return 密钥状态（活动/被动/禁用等） */
    public KeyStatus getStatus() {
        return status;
    }

    /** @param status 密钥状态 */
    public void setStatus(KeyStatus status) {
        this.status = status;
    }

}
