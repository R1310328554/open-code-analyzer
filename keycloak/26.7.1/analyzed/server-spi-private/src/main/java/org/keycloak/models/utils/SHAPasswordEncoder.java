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

package org.keycloak.models.utils;


import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;


/**
 * 基于 SHA 的密码编码器（强度可配置，输出 Base64）。
 * <p>创建实例时指定 SHA 位数（如 256、512）。</p>
 *
 * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
 */
public class SHAPasswordEncoder {

    private int strength;

    /** @param strength SHA 算法强度（如 256、512） */
    public SHAPasswordEncoder(int strength) {
        this.strength = strength;
    }

    /** 对明文密码做 SHA 摘要并 Base64 编码。
     * @param rawPassword 明文密码 */
    public String encode(String rawPassword) {
        MessageDigest messageDigest = getMessageDigest();

        byte[] digest = messageDigest.digest(rawPassword.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(digest);
    }

    /** 验证明文与已编码密码是否匹配。 */
    public boolean verify(String rawPassword, String encodedPassword) {
        return encode(rawPassword).equals(encodedPassword);
    }

    protected final MessageDigest getMessageDigest() throws IllegalArgumentException {
        String algorithm = "SHA-" + this.strength;

        try {
            return MessageDigest.getInstance(algorithm);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("invalid credential encoding algorithm");
        }
    }

    /** @return SHA 强度配置值 */
    public int getStrength() {
        return this.strength;
    }
}
