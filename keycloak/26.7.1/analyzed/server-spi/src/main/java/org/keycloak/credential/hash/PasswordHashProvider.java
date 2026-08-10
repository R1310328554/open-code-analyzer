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

import org.keycloak.credential.CredentialModel;
import org.keycloak.models.PasswordPolicy;
import org.keycloak.models.credential.PasswordCredentialModel;
import org.keycloak.provider.Provider;

/**
 * 密码哈希提供者 SPI：编码、校验密码并报告哈希强度。
 *
 * @author <a href="mailto:me@tsudot.com">Kunal Kerkar</a>
 */
public interface PasswordHashProvider extends Provider {
    /** 检查已存储凭据是否满足 realm 密码策略（如迭代次数）。 */
    boolean policyCheck(PasswordPolicy policy, PasswordCredentialModel credential);

    /** 将明文密码编码为 {@link PasswordCredentialModel}。 */
    PasswordCredentialModel encodedCredential(String rawPassword, int iterations);

    /**
     * 向后兼容的编码方法；推荐使用 {@link #encodedCredential(String, int)}。
     * Exists due the backwards compatibility. It is recommended to use {@link #encodedCredential(String, int)}
     */
    @Deprecated
    default
    String encode(String rawPassword, int iterations) {
        return rawPassword;
    }

    /** 校验明文密码是否与存储凭据匹配。 */
    boolean verify(String rawPassword, PasswordCredentialModel credential);

    /**
     * 返回哈希强度标识（非密码本身强度）；默认实现为迭代次数字符串。
     * Returns a string that denotes a hashing strength for a password (do not confuse with strength of the password itself!)
     * <p />
     * The default implementation is returning the number of iterations used for hashing password.
     * Another example could be memory and parallelism configuration for the Argon2 algorithm.
     * <p />
     * This can be used for example in a metric showing how many hashes were performed with what configuration
     *
     * @param credential The credential for which we want to obtain the string
     * @return string identifying hashing strength
     */
    default String credentialHashingStrength(PasswordCredentialModel credential) {
        return String.valueOf(credential.getPasswordCredentialData().getHashIterations());
    }

    /**
     * @deprecated 向后兼容；推荐使用 {@link #policyCheck(PasswordPolicy, PasswordCredentialModel)}
     * @deprecated Exists due the backwards compatibility. It is recommended to use {@link #policyCheck(PasswordPolicy, PasswordCredentialModel)}
     */
    @Deprecated
    default boolean policyCheck(PasswordPolicy policy, CredentialModel credential) {
        return policyCheck(policy, PasswordCredentialModel.createFromCredentialModel(credential));
    }

    /**
     * @deprecated 向后兼容；推荐使用 {@link #encodedCredential(String, int)}}
     * @deprecated Exists due the backwards compatibility. It is recommended to use {@link #encodedCredential(String, int)}}
     */
    @Deprecated
    default void encode(String rawPassword, int iterations, CredentialModel credential) {
        PasswordCredentialModel passwordCred = encodedCredential(rawPassword, iterations);

        credential.setCredentialData(passwordCred.getCredentialData());
        credential.setSecretData(passwordCred.getSecretData());
    }

    /**
     * @deprecated 向后兼容；推荐使用 {@link #verify(String, PasswordCredentialModel)}
     * @deprecated Exists due the backwards compatibility. It is recommended to use {@link #verify(String, PasswordCredentialModel)}
     */
    @Deprecated
    default boolean verify(String rawPassword, CredentialModel credential) {
        PasswordCredentialModel password = PasswordCredentialModel.createFromCredentialModel(credential);
        return verify(rawPassword, password);
    }
}
