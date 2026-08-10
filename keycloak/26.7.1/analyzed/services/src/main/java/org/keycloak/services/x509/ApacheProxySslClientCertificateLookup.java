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

import java.security.cert.X509Certificate;

import org.keycloak.common.util.PemException;
import org.keycloak.common.util.PemUtils;

/**
 * Apache 反向代理 SSL 客户端证书查找器。
 * <p>从 Apache 转发的 HTTP 头中提取 X.509 客户端证书，适用于 Keycloak 部署在 Apache 代理后的场景。</p>
 *
 * @author <a href="mailto:brat000012001@gmail.com">Peter Nalyvayko</a>
 * @version $Revision: 1 $
 * @since 3/29/2017
 */

public class ApacheProxySslClientCertificateLookup extends AbstractClientCertificateFromHttpHeadersLookup {

    public ApacheProxySslClientCertificateLookup(String sslCientCertHttpHeader,
                                                 String sslCertChainHttpHeaderPrefix,
                                                 int certificateChainLength) {
        super(sslCientCertHttpHeader, sslCertChainHttpHeaderPrefix, certificateChainLength);
    }

    /** 去除 PEM BEGIN/END 标记及换行符。 */
    private static String removeBeginEnd(String pem) {
        pem = pem.replace(PemUtils.BEGIN_CERT, "");
        pem = pem.replace(PemUtils.END_CERT, "");
        pem = pem.replace("\r\n", "");
        pem = pem.replace("\n", "");
        return pem.trim();
    }

    /** 解码 PEM 证书；若含 BEGIN/END 标记则先剥离再解码。 */
    @Override
    protected X509Certificate decodeCertificateFromPem(String pem) throws PemException {
        if (pem == null) {
            return null;
        }
        if (pem.startsWith(PemUtils.BEGIN_CERT)) {
            pem = removeBeginEnd(pem);
        }
        return PemUtils.decodeCertificate(pem);
    }
}
