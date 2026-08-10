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

import io.netty.util.concurrent.FastThreadLocal;
import io.netty.util.internal.ObjectUtil;

import java.security.InvalidAlgorithmParameterException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.Provider;
import javax.net.ssl.ManagerFactoryParameters;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.TrustManagerFactorySpi;

/**
 * 简化自定义 {@link TrustManagerFactory} 实现的抽象基类。
 * <p>子类只需实现 {@code engineInit} 与 {@code engineGetTrustManagers}。</p>
 */
public abstract class SimpleTrustManagerFactory extends TrustManagerFactory {

    /** 占位 Provider。 */
    private static final Provider PROVIDER = new Provider("", 0.0, "") {
        private static final long serialVersionUID = -2680540247105807895L;
    };

    /**
     * {@link SimpleTrustManagerFactorySpi} 需持有父工厂引用以回调，
     * 但 {@link TrustManagerFactory} 构造时即绑定 SPI 且之后无法访问。
     * 通过 {@link FastThreadLocal} 在构造期间建立关联。
     */
    private static final FastThreadLocal<SimpleTrustManagerFactorySpi> CURRENT_SPI =
            new FastThreadLocal<SimpleTrustManagerFactorySpi>() {
                @Override
                protected SimpleTrustManagerFactorySpi initialValue() {
                    return new SimpleTrustManagerFactorySpi();
                }
            };

    /**
     * 创建新实例（默认空名称）。
     */
    protected SimpleTrustManagerFactory() {
        this("");
    }

    /**
     * 创建新实例。
     *
     * @param name the name of this {@link TrustManagerFactory}
     */
    protected SimpleTrustManagerFactory(String name) {
        super(CURRENT_SPI.get(), PROVIDER, name);
        CURRENT_SPI.get().init(this);
        CURRENT_SPI.remove();

        ObjectUtil.checkNotNull(name, "name");
    }

    /**
     * 使用 KeyStore 初始化信任材料。
     *
     * @see TrustManagerFactorySpi#engineInit(KeyStore)
     */
    protected abstract void engineInit(KeyStore keyStore) throws Exception;

    /**
     * 使用 Provider 特定参数初始化。
     *
     * @see TrustManagerFactorySpi#engineInit(ManagerFactoryParameters)
     */
    protected abstract void engineInit(ManagerFactoryParameters managerFactoryParameters) throws Exception;

    /**
     * 返回各信任类型对应的 TrustManager 数组。
     *
     * @see TrustManagerFactorySpi#engineGetTrustManagers()
     */
    protected abstract TrustManager[] engineGetTrustManagers();

    /** 内部 SPI，将 JDK 回调转发至 SimpleTrustManagerFactory 子类。 */
    static final class SimpleTrustManagerFactorySpi extends TrustManagerFactorySpi {

        private SimpleTrustManagerFactory parent;
        private volatile TrustManager[] trustManagers;

        void init(SimpleTrustManagerFactory parent) {
            this.parent = parent;
        }

        @Override
        protected void engineInit(KeyStore keyStore) throws KeyStoreException {
            try {
                parent.engineInit(keyStore);
            } catch (KeyStoreException e) {
                throw e;
            } catch (Exception e) {
                throw new KeyStoreException(e);
            }
        }

        @Override
        protected void engineInit(
                ManagerFactoryParameters managerFactoryParameters) throws InvalidAlgorithmParameterException {
            try {
                parent.engineInit(managerFactoryParameters);
            } catch (InvalidAlgorithmParameterException e) {
                throw e;
            } catch (Exception e) {
                throw new InvalidAlgorithmParameterException(e);
            }
        }

        @Override
        protected TrustManager[] engineGetTrustManagers() {
            TrustManager[] trustManagers = this.trustManagers;
            if (trustManagers == null) {
                trustManagers = parent.engineGetTrustManagers();
                this.trustManagers = trustManagers;
            }
            return trustManagers.clone();
        }
    }
}
