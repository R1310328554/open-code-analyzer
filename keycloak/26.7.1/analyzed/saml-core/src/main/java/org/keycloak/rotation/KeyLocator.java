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

package org.keycloak.rotation;

import java.security.Key;
import java.security.KeyException;
import java.security.KeyManagementException;
import java.security.MessageDigest;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.List;
import javax.xml.crypto.XMLStructure;
import javax.xml.crypto.dsig.keyinfo.KeyInfo;
import javax.xml.crypto.dsig.keyinfo.KeyName;
import javax.xml.crypto.dsig.keyinfo.KeyValue;
import javax.xml.crypto.dsig.keyinfo.X509Data;

/**
 * 定义按 ID 获取安全密钥的方法。
 * <p>
 * 若 {@code KeyLocator} 实现希望使其全部密钥可被迭代访问，应同时实现
 * {@link Iterable}&lt;{@code T extends }{@link Key}&gt; 接口。
 * 基类 {@code KeyLocator} 不直接扩展 {@link Iterable}，以支持无法枚举密钥的定位器实现。
 *
 * @author <a href="mailto:hmlnarik@redhat.com">Hynek Mlnařík</a>
 */
public interface KeyLocator extends Iterable<Key> {

    /**
     * 返回指定 ID 的密钥。
     *
     * @param kid 密钥 ID
     * @return 用于验证给定输入签名的密钥
     * @throws KeyManagementException 密钥管理失败时抛出
     */
    Key getKey(String kid) throws KeyManagementException;

    /**
     * 检查给定密钥是否存在于定位器中。
     *
     * @param key 待查找的密钥
     * @return 若存在则返回同一密钥，否则返回 null
     * @throws KeyManagementException 密钥管理失败时抛出
     */
    default Key getKey(Key key) throws KeyManagementException {
        if (key == null) {
            return null;
        }
        for (Key k : this) {
            if (k.getAlgorithm().equals(key.getAlgorithm()) && MessageDigest.isEqual(k.getEncoded(), key.getEncoded())) {
                return key;
            }
        }
        return null;
    }

    /**
     * 返回定位器中由 KeyInfo 数字签名结构所表示的密钥。
     * 默认实现遍历 KeyInfo 内容，返回首个匹配的 KeyName、X509Data 或 PublicKey。
     *
     * @param info 待解析的 KeyInfo
     * @return 找到的密钥，未找到则返回 null
     * @throws KeyManagementException 密钥管理失败时抛出
     */
    default Key getKey(KeyInfo info) throws KeyManagementException {
        if (info == null) {
            return null;
        }
        Key key = null;
        for (XMLStructure xs : (List<XMLStructure>) info.getContent()) {
            if (xs instanceof KeyName) {
                key = getKey(((KeyName) xs).getName());
            } else if (xs instanceof X509Data) {
                for (Object content : ((X509Data) xs).getContent()) {
                    if (content instanceof X509Certificate) {
                        key = getKey(((X509Certificate) content).getPublicKey());
                        if (key != null) {
                            return key;
                        }
                        // 仅第一张 X509 证书为签名者，其余为证书链组成部分
                        break;
                    }
                }
            } else if (xs instanceof KeyValue) {
                try {
                    key = getKey(((KeyValue) xs).getPublicKey());
                } catch (KeyException e) {
                    throw new KeyManagementException(e);
                }
            }
            if (key != null) {
                return key;
            }
        }
        return null;
    }

    /**
     * 若此定位器以任何方式缓存密钥，则强制清空缓存并重新加载密钥。
     */
    void refreshKeyCache();

    /**
     * 辅助类，便于按 {@link Key} 内容哈希进行定位与比较。
     */
    public static class KeyHash {
        private final Key key;
        private final int keyHash;

        /**
         * 为给定密钥创建哈希包装。
         *
         * @param key 待哈希的密钥
         */
        public KeyHash(Key key) {
            this.key = key;
            this.keyHash = Arrays.hashCode(key.getEncoded());
        }

        @Override
        public int hashCode() {
            return keyHash;
        }

        @Override
        public boolean equals(Object o) {
            if (o instanceof KeyHash) {
                KeyHash other = (KeyHash) o;
                return keyHash == other.keyHash &&
                        key.getAlgorithm().equals(other.key.getAlgorithm()) &&
                        MessageDigest.isEqual(key.getEncoded(), other.key.getEncoded());
            }
            return false;
        }
    }
}
