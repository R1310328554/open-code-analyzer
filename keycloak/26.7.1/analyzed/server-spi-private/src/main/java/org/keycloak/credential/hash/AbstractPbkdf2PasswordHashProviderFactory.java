/*
 * Copyright 2022 Red Hat, Inc. and/or its affiliates
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

package org.keycloak.credential.hash;

import org.keycloak.Config;
import org.keycloak.models.KeycloakSessionFactory;

/**
 * PBKDF2 密码哈希工厂的抽象基类，统一管理密码填充长度配置。
 * <p>在 FIPS 等模式下，PBKDF2 可能要求最短密码长度；填充不影响已有密码的校验兼容性。</p>
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public abstract class AbstractPbkdf2PasswordHashProviderFactory implements PasswordHashProviderFactory {

    public static final String MAX_PADDING_LENGTH_PROPERTY = "max-padding-length";

    // 编码前密码的最小长度；不足时用 '\0' 填充。默认 0 表示不填充。
    // 例如 FIPS（BCFIPS）模式下 PBKDF2 要求至少 14 字符（112 位）。
    // 向后兼容：已有未填充密码仍可与填充后编码结果正确校验。
    private int maxPaddingLength = 0;

    /** 从配置读取 {@link #MAX_PADDING_LENGTH_PROPERTY}，默认 0。 */
    @Override
    public void init(Config.Scope config) {
        this.maxPaddingLength = config.getInt(MAX_PADDING_LENGTH_PROPERTY, 0);
    }

    @Override
    public void postInit(KeycloakSessionFactory factory) {
    }

    @Override
    public void close() {
    }

    /** @return 密码填充目标长度 */
    public int getMaxPaddingLength() {
        return maxPaddingLength;
    }

    /** 设置密码填充目标长度（主要用于测试）。 */
    public void setMaxPaddingLength(int maxPaddingLength) {
        this.maxPaddingLength = maxPaddingLength;
    }
}
