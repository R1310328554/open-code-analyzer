/*
 * Copyright 2019 The Netty Project
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
import io.netty.util.internal.StringUtil;

import java.security.InvalidAlgorithmParameterException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.Provider;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.KeyManagerFactorySpi;
import javax.net.ssl.ManagerFactoryParameters;
import javax.net.ssl.X509ExtendedKeyManager;
import javax.net.ssl.X509KeyManager;

/**
 * 简化自定义 {@link KeyManagerFactory} 实现的抽象基类。
 * <p>子类只需实现 {@code engineInit} 与 {@code engineGetKeyManagers}，无需直接处理 SPI。</p>
 */
public abstract class SimpleKeyManagerFactory extends KeyManagerFactory {

    /** 占位 Provider，满足 KeyManagerFactory 构造要求。 */
    private static final Provider PROVIDER = new Provider("", 0.0, "") {
        private static final long serialVersionUID = -2680540247105807895L;
    };

    /**
     * {@link SimpleKeyManagerFactorySpi} 需持有 {@link SimpleKeyManagerFactory} 以回调其方法，
     * 但 {@link KeyManagerFactory} 构造时即固定 SPI 且之后无法访问。
     * 因此通过 {@link FastThreadLocal} 在构造期间传递 SPI 与父实例的关联。
     */
    private static final FastThreadLocal<SimpleKeyManagerFactorySpi> CURRENT_SPI =
            new FastThreadLocal<SimpleKeyManagerFactorySpi>() {
                @Override
                protected SimpleKeyManagerFactorySpi initialValue() {
                    return new SimpleKeyManagerFactorySpi();
                }
            };

    /**
     * 创建新实例（默认空名称）。
     */
    protected SimpleKeyManagerFactory() {
        this(StringUtil.EMPTY_STRING);
    }

    /**
     * 创建新实例。
     *
     * @param name the name of this {@link KeyManagerFactory}
     */
    protected SimpleKeyManagerFactory(String name) {
        super(CURRENT_SPI.get(), PROVIDER, ObjectUtil.checkNotNull(name, "name"));
        CURRENT_SPI.get().init(this);
        CURRENT_SPI.remove();
    }

    /**
     * 使用 KeyStore 与密码初始化工厂。
     *
     * @see KeyManagerFactorySpi#engineInit(KeyStore, char[])
     */
    protected abstract void engineInit(KeyStore keyStore, char[] var2) throws Exception;

    /**
     * 使用 Provider 特定参数初始化工厂。
     *
     * @see KeyManagerFactorySpi#engineInit(ManagerFactoryParameters)
     */
    protected abstract void engineInit(ManagerFactoryParameters managerFactoryParameters) throws Exception;

    /**
     * 返回各密钥类型对应的 KeyManager 数组。
     *
     * @see KeyManagerFactorySpi#engineGetKeyManagers()
     */
    protected abstract KeyManager[] engineGetKeyManagers();

    /** 内部 SPI，将 JDK 回调转发至 SimpleKeyManagerFactory 子类。 */
    private static final class SimpleKeyManagerFactorySpi extends KeyManagerFactorySpi {

        private SimpleKeyManagerFactory parent;
        private volatile KeyManager[] keyManagers;

        void init(SimpleKeyManagerFactory parent) {
            this.parent = parent;
        }

        @Override
        protected void engineInit(KeyStore keyStore, char[] pwd) throws KeyStoreException {
            try {
                parent.engineInit(keyStore, pwd);
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
        protected KeyManager[] engineGetKeyManagers() {
            KeyManager[] keyManagers = this.keyManagers;
            if (keyManagers == null) {
                keyManagers = parent.engineGetKeyManagers();
                wrapIfNeeded(keyManagers);
                this.keyManagers = keyManagers;
            }
            return keyManagers.clone();
        }

        /** 将非 Extended 的 X509KeyManager 包装为 X509ExtendedKeyManager。 */
        private static void wrapIfNeeded(KeyManager[] keyManagers) {
            for (int i = 0; i < keyManagers.length; i++) {
                final KeyManager tm = keyManagers[i];
                if (tm instanceof X509KeyManager && !(tm instanceof X509ExtendedKeyManager)) {
                    keyManagers[i] = new X509KeyManagerWrapper((X509KeyManager) tm);
                }
            }
        }
    }
}
