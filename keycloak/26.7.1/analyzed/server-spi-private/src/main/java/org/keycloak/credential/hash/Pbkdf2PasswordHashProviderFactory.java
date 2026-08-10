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

package org.keycloak.credential.hash;

import org.keycloak.models.KeycloakSession;

import org.jboss.logging.Logger;

/**
 * PBKDF2-HMAC-SHA1 密码哈希工厂（已弃用）。
 * <p>推荐迭代次数 130 万次，性能较差；请改用 {@code pbkdf2-sha256} 或 {@code pbkdf2-sha512}。</p>
 *
 * @author <a href="mailto:me@tsudot.com">Kunal Kerkar</a>
 * @deprecated PBKDF2-SHA1 在推荐迭代次数下速度过慢，请使用 SHA256/SHA512 变体。
 */
@Deprecated
public class Pbkdf2PasswordHashProviderFactory extends AbstractPbkdf2PasswordHashProviderFactory implements PasswordHashProviderFactory {

    private static final Logger LOG = Logger.getLogger(Pbkdf2PasswordHashProviderFactory.class);

    public static final String ID = "pbkdf2";

    public static final String PBKDF2_ALGORITHM = "PBKDF2WithHmacSHA1";

    /** OWASP 密码存储备忘单推荐的 PBKDF2-HMAC-SHA1 迭代次数。 */

    public static final int DEFAULT_ITERATIONS = 1_300_000;

    private static boolean usageWarningPrinted;

    /** 创建 SHA1 变体提供者；首次调用时打印弃用警告。 */
    @Override
    public PasswordHashProvider create(KeycloakSession session) {
        if (!usageWarningPrinted) {
            LOG.warnf("Detected usage of password hashing provider '%s'. The provider is no longer recommended, use 'pbkdf2-sha256' or 'pbkdf2-sha512' instead.", ID);
            usageWarningPrinted = true;
        }
        return new Pbkdf2PasswordHashProvider(ID, PBKDF2_ALGORITHM, DEFAULT_ITERATIONS, getMaxPaddingLength());
    }

    /** @return 提供者 ID：{@code pbkdf2} */
    @Override
    public String getId() {
        return ID;
    }

    /** 较低优先级（-100），让 SHA256/SHA512 变体优先。 */
    @Override
    public int order() {
        return -100;
    }
}
