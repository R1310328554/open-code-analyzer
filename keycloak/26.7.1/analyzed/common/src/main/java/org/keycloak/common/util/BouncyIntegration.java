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

package org.keycloak.common.util;

import java.security.Provider;
import java.security.Security;

import org.keycloak.common.crypto.CryptoIntegration;

/**
 * BouncyCastle 安全提供方集成辅助类。
 *
 * <p>启动时通过 {@link org.keycloak.common.crypto.CryptoIntegration} 加载
 * BouncyCastle 或 BouncyCastle FIPS 提供方，并暴露其 JCA 注册名称。</p>
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class BouncyIntegration {

    /** 当前 classpath 上生效的 BouncyCastle 提供方名称。 */
    public static final String PROVIDER = loadProvider();

    /** 从 {@link CryptoIntegration} 解析 BouncyCastle 提供方；不可用时回退到首个 JCA 提供方。 */
    private static String loadProvider() {
        Provider provider = CryptoIntegration.getProvider().getBouncyCastleProvider();
        if (provider == null) {
            return Security.getProviders()[0].getName();
            // throw new RuntimeException("Failed to load required security provider: BouncyCastleProvider or BouncyCastleFipsProvider");
        }
        return provider.getName();
    }

}
