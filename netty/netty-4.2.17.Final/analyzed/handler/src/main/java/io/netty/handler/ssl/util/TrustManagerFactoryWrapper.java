/*
 * Copyright 2014 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package io.netty.handler.ssl.util;

import io.netty.util.internal.ObjectUtil;

import java.security.KeyStore;
import javax.net.ssl.ManagerFactoryParameters;
import javax.net.ssl.TrustManager;

/**
 * 将已有 {@link TrustManager} 包装为 {@link javax.net.ssl.TrustManagerFactory}。
 * <p>{@code engineInit} 为空操作；用于 {@link SslContext} 构建时直接注入自定义 TrustManager。</p>
 */
public final class TrustManagerFactoryWrapper extends SimpleTrustManagerFactory {
    /** 被包装的信任管理器。 */
    private final TrustManager tm;

    public TrustManagerFactoryWrapper(TrustManager tm) {
        this.tm = ObjectUtil.checkNotNull(tm, "tm");
    }

    /** 忽略 KeyStore 初始化。 */
    @Override
    protected void engineInit(KeyStore keyStore) throws Exception { }

    /** 忽略 ManagerFactoryParameters 初始化。 */
    @Override
    protected void engineInit(ManagerFactoryParameters managerFactoryParameters)
            throws Exception { }

    @Override
    protected TrustManager[] engineGetTrustManagers() {
        return new TrustManager[] {tm};
    }
}
