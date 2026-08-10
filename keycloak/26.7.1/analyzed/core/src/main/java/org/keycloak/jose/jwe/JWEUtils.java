/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates
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

package org.keycloak.jose.jwe;

import java.security.SecureRandom;

/**
 * JWE 辅助工具：生成密码学安全的随机字节（用于 CEK、IV 等）。
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class JWEUtils {

    private JWEUtils() {
    }

    /**
     * 生成指定长度的随机密钥材料。
     *
     * @param bytes 字节长度
     * @return 随机字节数组
     */
    public static byte[] generateSecret(int bytes) {
        byte[] buf = new byte[bytes];
        new SecureRandom().nextBytes(buf);
        return buf;
    }
}
