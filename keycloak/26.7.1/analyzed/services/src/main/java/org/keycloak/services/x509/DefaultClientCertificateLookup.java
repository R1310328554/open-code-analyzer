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

import org.keycloak.http.HttpRequest;

import org.jboss.logging.Logger;

/**
 * 默认 X.509 客户端证书查找器。
 * <p>直接从入站 TLS 连接读取客户端证书及证书链（不经 HTTP 头转发）。</p>
 *
 * @author <a href="mailto:brat000012001@gmail.com">Peter Nalyvayko</a>
 * @version $Revision: 1 $
 * @since 3/26/2017
 */

public class DefaultClientCertificateLookup implements X509ClientCertificateLookup {

    private static final Logger logger = Logger.getLogger(DefaultClientCertificateLookup.class);

    public DefaultClientCertificateLookup() {
    }

    @Override
    public void close() {

    }

    /**
     * 从 HTTP 请求直接获取 TLS 客户端证书链。
     *
     * @return 证书链，trace 级别下输出各证书 SubjectDN
     */
    @Override
    public X509Certificate[] getCertificateChain(HttpRequest httpRequest) {

        X509Certificate[] certs = httpRequest.getClientCertificateChain();
        if (logger.isTraceEnabled() && certs != null) {
            for (X509Certificate cert : certs) {
                logger.tracef("Certificate's SubjectDN => \"%s\"", cert.getSubjectDN().getName());
            }
        }
        return certs;
    }
}
