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
package org.keycloak.crypto;

/**
 * 密钥在领域中的运行状态：活跃签发、被动保留或已禁用。
 */
public enum KeyStatus {

    /** 当前用于签发的活跃密钥。 */
    ACTIVE, /** 已轮换但仍可用于验签的被动密钥。 */
    PASSIVE, /** 已禁用、不可使用的密钥。 */
    DISABLED;

    /**
     * 由布尔标志推导密钥状态。
     *
     * @param active 是否为活跃密钥
     * @param enabled 是否启用
     * @return 对应的 {@link KeyStatus}
     */
    public static KeyStatus from(boolean active, boolean enabled) {
        if (!enabled) {
            return KeyStatus.DISABLED;
        } else {
            return active ? KeyStatus.ACTIVE : KeyStatus.PASSIVE;
        }
    }

    /**
     * @return 是否为活跃状态
     */
    public boolean isActive() {
        return this.equals(ACTIVE);
    }

    /**
     * @return 是否处于可用状态（活跃或被动）
     */
    public boolean isEnabled() {
        return this.equals(ACTIVE) || this.equals(PASSIVE);
    }

}
