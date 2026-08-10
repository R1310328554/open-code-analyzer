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

package com.alibaba.nacos.plugin.encryption.handler;

import com.alibaba.nacos.common.utils.Pair;
import com.alibaba.nacos.plugin.encryption.EncryptionPluginManager;
import com.alibaba.nacos.plugin.encryption.spi.EncryptionPluginService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.stream.Stream;

/**
 * 配置加解密处理器。
 *
 * <p>根据 dataId 前缀 {@code cipher-} 解析算法名，
 * 委托 {@link EncryptionPluginManager} 查找对应 SPI 实现完成加解密。</p>
 *
 * @author lixiaoshuang
 */
public class EncryptionHandler {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(EncryptionHandler.class);
    
    /** dataId 加密前缀，格式示例：cipher-AES-dataId。 */
    private static final String PREFIX = "cipher-";
    
    /**
     * 执行加密：生成密钥、加密内容并封装密钥密文。
     *
     * @param dataId  配置 dataId，含算法标识
     * @param content 待加密的明文内容
     * @return 密钥密文与内容密文的 Pair
     */
    public static Pair<String, String> encryptHandler(String dataId, String content) {
        if (!checkCipher(dataId)) {
            return Pair.with("", content);
        }
        Optional<String> algorithmName = parseAlgorithmName(dataId);
        Optional<EncryptionPluginService> optional = algorithmName.flatMap(
            EncryptionPluginManager.instance()::findEncryptionService);
        if (!optional.isPresent()) {
            LOGGER.warn(
                "[EncryptionHandler] [encryptHandler] No encryption program with the corresponding name found");
            return Pair.with("", content);
        }
        EncryptionPluginService encryptionPluginService = optional.get();
        String secretKey = encryptionPluginService.generateSecretKey();
        String encryptContent = encryptionPluginService.encrypt(secretKey, content);
        return Pair.with(encryptionPluginService.encryptSecretKey(secretKey), encryptContent);
    }
    
    /**
     * 执行解密：解密密钥后解密配置内容。
     *
     * @param dataId    配置 dataId，含算法标识
     * @param secretKey 存储的密钥密文
     * @param content   待解密的密文内容
     * @return 明文密钥与明文内容的 Pair
     */
    public static Pair<String, String> decryptHandler(String dataId, String secretKey,
        String content) {
        if (!checkCipher(dataId)) {
            return Pair.with(secretKey, content);
        }
        Optional<String> algorithmName = parseAlgorithmName(dataId);
        Optional<EncryptionPluginService> optional = algorithmName.flatMap(
            EncryptionPluginManager.instance()::findEncryptionService);
        if (!optional.isPresent()) {
            LOGGER.warn(
                "[EncryptionHandler] [decryptHandler] No encryption program with the corresponding name found");
            return Pair.with(secretKey, content);
        }
        EncryptionPluginService encryptionPluginService = optional.get();
        String decryptSecretKey = encryptionPluginService.decryptSecretKey(secretKey);
        String decryptContent = encryptionPluginService.decrypt(decryptSecretKey, content);
        return Pair.with(decryptSecretKey, decryptContent);
    }
    
    /**
     * 从 dataId 解析加密算法名（{@code cipher-} 后第一段）。
     *
     * @param dataId 配置 dataId
     * @return 算法名 Optional
     */
    private static Optional<String> parseAlgorithmName(String dataId) {
        return Stream.of(dataId.split("-")).skip(1).findFirst();
    }
    
    /**
     * 判断 dataId 是否需要加解密（以 {@code cipher-} 开头且非前缀本身）。
     *
     * @param dataId 配置 dataId
     * @return 是否需要加解密
     */
    private static boolean checkCipher(String dataId) {
        return dataId.startsWith(PREFIX) && !PREFIX.equals(dataId);
    }
}
