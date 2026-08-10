/*
 * Copyright 2017 The Netty Project
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
package io.netty.handler.ssl;

import io.netty.util.internal.PlatformDependent;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import javax.net.ssl.SSLEngine;

/**
 * Contains methods that can be used to detect if conscrypt is usable.
 *
 * <p>通过反射检测 Google Conscrypt 是否可用，避免在 JDK6+ 运行时加载 JDK8+ 专用类。</p>
 */
final class Conscrypt {
    // 独立工具类：延迟加载 Conscrypt，保持 JDK6+ 字节码兼容。
    // This class exists to avoid loading other conscrypt related classes using features only available in JDK8+,
    // because we need to maintain JDK6+ runtime compatibility.
    /** 反射得到的 {@code Conscrypt.isConscrypt(SSLEngine)} 方法；不可用时为 null。 */
    private static final Method IS_CONSCRYPT_SSLENGINE;

    static {
        Method isConscryptSSLEngine = null;

        // Java 15+ 上 Conscrypt 集成方式不同，Android 仍尝试加载
        // Only works on Java14 and earlier for now
        // See https://github.com/google/conscrypt/issues/838
        if (PlatformDependent.javaVersion() < 15 || PlatformDependent.isAndroid()) {
            try {
                Class<?> providerClass = Class.forName("org.conscrypt.OpenSSLProvider", true,
                        PlatformDependent.getClassLoader(ConscryptAlpnSslEngine.class));
                providerClass.newInstance();

                Class<?> conscryptClass = Class.forName("org.conscrypt.Conscrypt", true,
                        PlatformDependent.getClassLoader(ConscryptAlpnSslEngine.class));
                isConscryptSSLEngine = conscryptClass.getMethod("isConscrypt", SSLEngine.class);
            } catch (Throwable ignore) {
                // Conscrypt 不在 classpath 或初始化失败时静默忽略
            }
        }
        IS_CONSCRYPT_SSLENGINE = isConscryptSSLEngine;
    }

    /**
     * Indicates whether or not conscrypt is available on the current system.
     *
     * <p>classpath 中 Conscrypt 已成功加载且反射方法可用时返回 true。</p>
     */
    static boolean isAvailable() {
        return IS_CONSCRYPT_SSLENGINE != null;
    }

    /**
     * Returns {@code true} if the passed in {@link SSLEngine} is handled by Conscrypt, {@code false} otherwise.
     *
     * <p>判断给定引擎是否由 Conscrypt 提供实现（用于 ALPN 等扩展路径选择）。</p>
     */
    static boolean isEngineSupported(SSLEngine engine) {
        try {
            return IS_CONSCRYPT_SSLENGINE != null && (Boolean) IS_CONSCRYPT_SSLENGINE.invoke(null, engine);
        } catch (IllegalAccessException ignore) {
            return false;
        } catch (InvocationTargetException ex) {
            throw new RuntimeException(ex);
        }
    }

    private Conscrypt() { }
}
