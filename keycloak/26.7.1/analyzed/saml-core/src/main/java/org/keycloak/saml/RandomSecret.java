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

package org.keycloak.saml;

import java.security.SecureRandom;

/**
 * 生成密码学安全随机字节序列的工具类。
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class RandomSecret {
    /**
     * <p>
     * 创建指定长度的随机 {@code byte[]} 密钥材料。
     * </p>
     *
     * @param size 密钥字节长度
     *
     * @return 包含生成密钥字节的 {@code byte[]}
     */
    public static byte[] createRandomSecret(final int size) {
        SecureRandom random = new SecureRandom();
        byte[] secret = new byte[size];
        random.nextBytes(secret);
        return secret;
    }
}
