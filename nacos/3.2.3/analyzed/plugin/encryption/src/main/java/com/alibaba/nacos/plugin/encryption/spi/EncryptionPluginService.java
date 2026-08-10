/*
 * Copyright 1999-2018 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alibaba.nacos.plugin.encryption.spi;

/**
 * 加解密 SPI 接口。
 *
 * <p>各加密算法插件需实现本接口，供 {@link com.alibaba.nacos.plugin.encryption.EncryptionPluginManager}
 * 按算法名加载并调用。</p>
 *
 * @author lixiaoshuang
 */
public interface EncryptionPluginService {
    
    /**
     * 使用指定密钥加密明文内容。
     *
     * @param secretKey secret key
     * @param content   content unencrypted
     * @return encrypt value
     */
    String encrypt(String secretKey, String content);
    
    /**
     * 使用指定密钥解密密文内容。
     *
     * @param secretKey secret key
     * @param content   encrypted
     * @return decrypt value
     */
    String decrypt(String secretKey, String content);
    
    /**
     * 生成新的密钥。
     *
     * @return Secret key
     */
    String generateSecretKey();
    
    /**
     * 返回本插件所实现的加密算法名称。
     *
     * @return name
     */
    String algorithmName();
    
    /**
     * 加密密钥本身（用于密钥存储保护）。
     *
     * @param secretKey secretKey
     * @return encrypted secretKey
     */
    String encryptSecretKey(String secretKey);
    
    /**
     * 解密密钥本身（用于密钥存储保护）。
     *
     * @param secretKey secretKey
     * @return decrypted secretKey
     */
    String decryptSecretKey(String secretKey);
}
