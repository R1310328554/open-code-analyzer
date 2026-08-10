/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates
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

package org.keycloak.utils;

import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.SignatureException;
import java.security.cert.X509CRL;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.security.auth.x500.X500Principal;

import org.keycloak.models.KeycloakSession;
import org.keycloak.truststore.TruststoreProvider;

import org.jboss.logging.Logger;

/**
 * X509 证书吊销列表（CRL）校验工具。
 * <p>验证 CRL 签名并检查客户端证书是否已被吊销。</p>
 *
 * @author <a href="mailto:brat000012001@gmail.com">Peter Nalyvayko</a>
 * @version $Revision: 1 $
 * @since 10/31/2016
 */

public final class CRLUtils {

    private static final Logger log = Logger.getLogger(CRLUtils.class);


    /**
     * 校验 CRL 签名，并检查证书链首项（客户端证书）是否未被吊销。
     *
     * @param certs 首项为客户端证书，其余为证书链
     * @param crl 待校验的 CRL
     * @param session Keycloak 会话（用于访问信任库）
     * @throws GeneralSecurityException 签名无效、证书已吊销或信任链不完整时抛出
     */
    public static void check(X509Certificate[] certs, X509CRL crl, KeycloakSession session) throws GeneralSecurityException {
        if (certs == null || certs.length < 1) {
            throw new GeneralSecurityException("Not possible to verify signature on CRL because no certificate chain was passed.");
        }

        X500Principal crlIssuerPrincipal = crl.getIssuerX500Principal();
        X509Certificate crlSignatureCertificate = null;

        // 在 CA 证书链中查找签发 CRL 的证书
        for (X509Certificate currentCACert: certs) {
            if (crlIssuerPrincipal.equals(currentCACert.getSubjectX500Principal())) {
                crlSignatureCertificate = currentCACert;

                log.tracef("Found certificate used to sign CRL in the CA chain of the certificate. CRL issuer: %s", crlIssuerPrincipal);
                break;
            }
        }

        // 证书链中未找到时，回退到信任库查找 CRL 签发者
        if (crlSignatureCertificate == null) {
            log.tracef("Not found CRL issuer '%s' in the CA chain of the certificate. Fallback to lookup CRL issuer in the truststore", crlIssuerPrincipal);
            findCRLSignatureCertificateInTruststore(session, certs, crl);
        } else {
            // 用找到的证书验证 CRL 签名
            crl.verify(crlSignatureCertificate.getPublicKey());
        }

        // 最后检查客户端证书是否在 CRL 中
        if (crl.isRevoked(certs[0])) {
            String message = String.format("Certificate has been revoked, certificate's subject: %s", certs[0].getSubjectDN().getName());
            log.debug(message);
            throw new GeneralSecurityException(message);
        }
    }


    /** 在信任库中定位 CRL 签发者证书并验证与客户端证书链的信任锚关系。 */
    private static X509Certificate findCRLSignatureCertificateInTruststore(KeycloakSession session, X509Certificate[] certs, X509CRL crl) throws GeneralSecurityException {
        TruststoreProvider truststoreProvider = session.getProvider(TruststoreProvider.class);
        if (truststoreProvider == null || truststoreProvider.getTruststore() == null) {
            throw new GeneralSecurityException("Truststore not available");
        }

        X500Principal crlIssuerPrincipal = crl.getIssuerX500Principal();
        Map<X500Principal, List<X509Certificate>> rootCerts = truststoreProvider.getRootCertificates();
        Map<X500Principal, List<X509Certificate>> intermediateCerts = truststoreProvider.getIntermediateCertificates();

        List<X509Certificate> crlSignatureCertificates = intermediateCerts.get(crlIssuerPrincipal);
        X509Certificate crlSignatureCertificate = null;

        if (crlSignatureCertificates == null) {
            crlSignatureCertificates = rootCerts.get(crlIssuerPrincipal);
        }

        for (X509Certificate cacert : crlSignatureCertificates) {
            try {
                    crl.verify(cacert.getPublicKey());
            } catch (InvalidKeyException | SignatureException e) {
                    continue;
            }
            crlSignatureCertificate = cacert;
            break;
        }

        if (crlSignatureCertificate == null) {
            throw new GeneralSecurityException("Not available certificate for CRL issuer '" + crlIssuerPrincipal + "' in the truststore, nor in the CA chain");
        } else {
            log.tracef("Found CRL issuer certificate with subject '%s' in the truststore. Verifying trust anchor", crlIssuerPrincipal);
        }

        // 检查 CRL 签发者与待验证书链是否存在信任锚（RFC 5280 §6.3.3(f)）
        Set<X500Principal> certificateCAPrincipals = Arrays.asList(certs).stream()
                .map(X509Certificate::getIssuerX500Principal)
                .collect(Collectors.toSet());

        X500Principal currentCRLAnchorPrincipal = crlIssuerPrincipal;

        for (X500Principal certificateCAPrincipal : certificateCAPrincipals) {
            if (certificateCAPrincipal.equals(currentCRLAnchorPrincipal)) {
                log.tracef("Found trust anchor of the CRL issuer '%s' in the CA chain. Anchor is '%s'", crlIssuerPrincipal, currentCRLAnchorPrincipal);
                return crlSignatureCertificate;
            }
        }

        // 信任锚不在提供的证书链中，继续在信任库中查找
        List<X509Certificate> currentCRLAnchorCertificates = intermediateCerts.get(currentCRLAnchorPrincipal);
        if (currentCRLAnchorCertificates == null) {
            currentCRLAnchorCertificates = rootCerts.get(currentCRLAnchorPrincipal);
        }
        if (currentCRLAnchorCertificates == null) {
            throw new GeneralSecurityException("Certificate for CRL issuer '" + crlIssuerPrincipal + "' available in the truststore, but doesn't have trust anchors with the CA chain.");
        }

        return crlSignatureCertificate;
    }

}
