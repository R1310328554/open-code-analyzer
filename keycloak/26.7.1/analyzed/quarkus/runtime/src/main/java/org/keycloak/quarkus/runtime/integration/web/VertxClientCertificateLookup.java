/*
 * Copyright 2021 Red Hat, Inc. and/or its affiliates
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

package org.keycloak.quarkus.runtime.integration.web;

import java.security.cert.X509Certificate;

import org.keycloak.http.HttpRequest;
import org.keycloak.services.x509.X509ClientCertificateLookup;

import org.jboss.logging.Logger;

/**
 * 基于 Vert.x/Quarkus HTTP 栈的 X.509 客户端证书查找实现。
 *
 * @author <a href="mailto:psilva@redhat.com">Pedro Igor</a>
 */
public class VertxClientCertificateLookup implements X509ClientCertificateLookup {

    private static final Logger logger = Logger.getLogger(VertxClientCertificateLookup.class);

    public VertxClientCertificateLookup() {
    }

    /** {@inheritDoc} 无额外资源需释放。 */
    @Override
    public void close() {

    }

    /** {@inheritDoc} 从 {@link HttpRequest} 读取 TLS 客户端证书链并在 trace 级别记录 SubjectDN。 */
    @Override
    public X509Certificate[] getCertificateChain(HttpRequest httpRequest) {
        X509Certificate[] certificates = httpRequest.getClientCertificateChain();

        if (logger.isTraceEnabled() && certificates != null) {
            for (X509Certificate cert : certificates) {
                logger.tracef("Certificate's SubjectDN => \"%s\"", cert.getSubjectX500Principal().getName());
            }
        }

        return certificates;
    }
}
