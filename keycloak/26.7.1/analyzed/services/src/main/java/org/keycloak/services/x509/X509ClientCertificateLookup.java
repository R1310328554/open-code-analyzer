/*
 * Copyright 2017 Analytical Graphics, Inc. and/or its affiliates
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
 *
 */

package org.keycloak.services.x509;

import java.security.GeneralSecurityException;
import java.security.cert.X509Certificate;

import org.keycloak.http.HttpRequest;
import org.keycloak.provider.Provider;

/**
 * X.509 客户端证书查找提供者接口。
 * <p>定义从 HTTP 请求中提取客户端 TLS 客户端证书（及可选证书链）的 SPI 契约。</p>
 *
 * @author <a href="mailto:brat000012001@gmail.com">Peter Nalyvayko</a>
 * @version $Revision: 1 $
 * @since 3/26/2017
 */

public interface X509ClientCertificateLookup extends Provider {

    /**
     * 返回客户端证书，以及证书链中的其他证书（如有）。
     * <p>
     * 重要：实现必须确保证书来源可信。
     * 例如可借助 {@link HttpRequest#isProxyTrusted()} 方法验证反向代理是否受信。
     *
     * @param httpRequest 当前 HTTP 请求
     * @return 客户端证书链；若无证书则返回 {@code null}
     */
    X509Certificate[] getCertificateChain(HttpRequest httpRequest) throws GeneralSecurityException;
}
