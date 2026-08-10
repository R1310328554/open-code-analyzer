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

package org.keycloak.keys;

import java.util.List;
import java.util.function.Predicate;

import org.keycloak.crypto.KeyWrapper;
import org.keycloak.provider.Provider;

/**
 * 公钥存储 SPI：缓存客户端/IdP 等外部公钥，供 JWT 客户端认证、JWE 与 SAML 验签使用。
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public interface PublicKeyStorageProvider extends Provider {


    /**
     * 按 {@code kid} 与算法获取用于验签的公钥（如 JWT 客户端认证）。
     *
     * @param modelKey 缓存模型键
     * @param kid JWK 密钥 ID
     * @param algorithm 期望算法（JWK 未指定算法时可为空）
     * @param loader 缓存未命中时的加载器
     * @return 匹配的 {@link org.keycloak.crypto.KeyWrapper}
     */
	KeyWrapper getPublicKey(String modelKey, String kid, String algorithm, PublicKeyLoader loader);

    /**
     * 获取首个匹配算法的公钥（多密钥客户端的 JWT 验签或 JWE 加密 CEK 等场景）。
     *
     * @param modelKey 缓存模型键
     * @param algorithm 期望算法
     * @param loader 缓存未命中时的加载器
     * @return 首个匹配的密钥，无则 {@code null}
     */
    KeyWrapper getFirstPublicKey(String modelKey, String algorithm, PublicKeyLoader loader);

    /**
     * 返回首个满足谓词的公钥（SAML 通过元数据 URL 拉取密钥等）。
     *
     * @param modelKey 缓存模型键
     * @param predicate 密钥过滤条件
     * @param loader 缓存未命中时的加载器
     * @return 匹配的密钥或 {@code null}
     */
    KeyWrapper getFirstPublicKey(String modelKey, Predicate<KeyWrapper> predicate, PublicKeyLoader loader);

    /**
     * 返回模型键下的全部公钥。
     *
     * @param modelKey 缓存模型键
     * @param loader 缓存未命中时的加载器
     * @return 公钥列表
     */
    List<KeyWrapper> getKeys(String modelKey, PublicKeyLoader loader);

    /**
     * 强制重新加载指定模型键的公钥。
     *
     * @param modelKey 缓存模型键
     * @param loader 加载器
     * @return 是否成功重新加载
     */
    boolean reloadKeys(String modelKey, PublicKeyLoader loader);
}
