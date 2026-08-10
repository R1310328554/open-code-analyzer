/*
 * Copyright 2025 Red Hat, Inc. and/or its affiliates
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
package org.keycloak.crl;

import java.security.GeneralSecurityException;
import java.security.cert.X509CRL;
import java.util.concurrent.Callable;

import org.keycloak.provider.Provider;

/**
 * CRL（证书吊销列表）缓存存储提供者 SPI。
 * <p>支持按 key 从缓存读取或加载 X509 CRL，以及主动刷新缓存条目。</p>
 *
 * @author rmartinc
 */
public interface CrlStorageProvider extends Provider {

    /**
     * 按 key 获取 CRL：命中缓存则直接返回，否则通过 {@code loader} 加载并写入缓存。
     * @param key CRL 缓存键
     * @param loader 缓存未命中时的加载回调
     * @return 已缓存的 {@link X509CRL}
     * @throws GeneralSecurityException 加载或解析失败时抛出
     */
    X509CRL get(String key, Callable<X509CRL> loader) throws GeneralSecurityException;

    /**
     * 强制刷新指定 key 的 CRL 缓存条目。
     * @param key CRL 缓存键
     * @param loader 用于获取最新 CRL 的加载回调
     * @return 刷新成功返回 {@code true}，否则 {@code false}
     * @throws GeneralSecurityException 加载或解析失败时抛出
     */
    boolean refreshCache(String key, Callable<X509CRL> loader) throws GeneralSecurityException;

}
