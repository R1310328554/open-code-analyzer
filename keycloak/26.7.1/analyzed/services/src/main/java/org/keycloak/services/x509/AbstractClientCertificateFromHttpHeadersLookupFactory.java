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

import org.keycloak.Config;
import org.keycloak.models.KeycloakSessionFactory;

import org.jboss.logging.Logger;

/**
 * 基于 HTTP 头读取客户端证书的查找器工厂抽象基类。
 * <p>从 SPI 配置读取证书头名称、链前缀及链长度等参数。</p>
 *
 * @author <a href="mailto:brat000012001@gmail.com">Peter Nalyvayko</a>
 * @version $Revision: 1 $
 * @since 4/4/2017
 */

public abstract class AbstractClientCertificateFromHttpHeadersLookupFactory implements X509ClientCertificateLookupFactory {

    private final static Logger logger = Logger.getLogger(AbstractClientCertificateFromHttpHeadersLookupFactory.class);

    /** 配置项：除叶子证书外的链节数量 */
    protected final static String CERTIFICATE_CHAIN_LENGTH = "certificateChainLength";
    /** 配置项：客户端证书 HTTP 头名称 */
    protected final static String HTTP_HEADER_CLIENT_CERT = "sslClientCert";
    /** 配置项：证书链 HTTP 头前缀 */
    protected final static String HTTP_HEADER_CERT_CHAIN_PREFIX = "sslCertChainPrefix";

    /** 客户端证书 HTTP 头名称 */
    protected String sslClientCertHttpHeader;
    /** 证书链 HTTP 头前缀 */
    protected String sslChainHttpHeaderPrefix;
    /** 额外链节数量，默认 1 */
    protected int certificateChainLength = 1;

    /** 从 SPI 配置初始化 HTTP 头名称与链长度。 */
    @Override
    public void init(Config.Scope config) {
        certificateChainLength = config.getInt(CERTIFICATE_CHAIN_LENGTH, 1);
        logger.tracev("{0}: ''{1}''", CERTIFICATE_CHAIN_LENGTH, certificateChainLength);

        sslClientCertHttpHeader = config.get(HTTP_HEADER_CLIENT_CERT, "");
        logger.tracev("{0}:   ''{1}''", HTTP_HEADER_CLIENT_CERT, sslClientCertHttpHeader);

        sslChainHttpHeaderPrefix = config.get(HTTP_HEADER_CERT_CHAIN_PREFIX, null);
        if (sslChainHttpHeaderPrefix != null) {
            logger.tracev("{0}:  ''{1}''", HTTP_HEADER_CERT_CHAIN_PREFIX, sslChainHttpHeaderPrefix);
        } else {
            logger.tracev("{0} was not configured", HTTP_HEADER_CERT_CHAIN_PREFIX);
        }
    }

    @Override
    public void postInit(KeycloakSessionFactory factory) {
    }

    @Override
    public void close() {

    }

}
