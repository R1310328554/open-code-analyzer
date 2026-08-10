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
package org.keycloak.crypto.elytron;

import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.security.cert.CertificateParsingException;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;

import org.keycloak.common.crypto.UserIdentityExtractor;
import org.keycloak.common.crypto.UserIdentityExtractorProvider;

import org.jboss.logging.Logger;
import org.wildfly.security.asn1.ASN1;
import org.wildfly.security.asn1.DERDecoder;
import org.wildfly.security.asn1.OidsUtil;
import org.wildfly.security.x500.principal.X500AttributePrincipalDecoder;

/**
 * 基于 WildFly Elytron 的 X.509 用户身份提取器工厂，支持 X500 RDN 与 SubjectAltName 解析。
 *
 * @author <a href="mailto:david.anderson@redhat.com">David Anderson</a>
 */
public class ElytronUserIdentityExtractorProvider  extends UserIdentityExtractorProvider {

    private Logger log = Logger.getLogger(this.getClass());

    class X500NameRDNExtractorElytronProvider extends X500NameRDNExtractor {

        private String x500NameStyle;
        Function<X509Certificate[],Principal> x500Name;
        
        public X500NameRDNExtractorElytronProvider(String attrName, Function<X509Certificate[], Principal> x500Name) {
            // OidsUtil 无法映射 'EmailAddress'，需改用 'E' 对应的 OID
            // TODO: 向 wildfly-elytron 提交 issue，补充 'EmailAddress' 的 OID 映射
            if(attrName.equals("EmailAddress")) {
                attrName = "E";
            }
            this.x500NameStyle = OidsUtil.attributeNameToOid(OidsUtil.Category.RDN, attrName);
            log.debug("Attribute Name: " + attrName + " X500NameStyle OID: " + x500NameStyle);
            this.x500Name = x500Name;
        }

        @Override
        public Object extractUserIdentity(X509Certificate[] certs) {

            if (certs == null || certs.length == 0)
                throw new IllegalArgumentException();

                Principal name = x500Name.apply(certs);
                log.debug("Principal Name " + name.getName());
                X500AttributePrincipalDecoder xDecoder = new X500AttributePrincipalDecoder(x500NameStyle);
                String cn = xDecoder.apply(name);
            
                return cn;
            
        }
    }

    /**
     * 从 SubjectAltName 扩展中提取用户标识（含 Microsoft UPN 等 otherName）。
     */
    class SubjectAltNameExtractorEltronProvider extends SubjectAltNameExtractor {

        // 用户主体名（UPN），常见于 Microsoft 智能卡登录证书
        private static final String UPN_OID = "1.3.6.1.4.1.311.20.2.3";

        private final int generalName;

        /**
         * 创建 SubjectAltName 提取器实例。
         *
         * @param generalName an integer representing the general name. See
         *                    {@link X509Certificate#getSubjectAlternativeNames()}
         */
        SubjectAltNameExtractorEltronProvider(int generalName) {
            this.generalName = generalName;
        }

        @Override
        public Object extractUserIdentity(X509Certificate[] certs) {
            if (certs == null || certs.length == 0) {
                throw new IllegalArgumentException();
            }
            String subjectName = null;

            log.debug("SubjPrinc " + certs[0].getSubjectX500Principal());
            Collection<List<?>> subjectAlternativeNames;
            try {
                subjectAlternativeNames = certs[0].getSubjectAlternativeNames();
                if (subjectAlternativeNames == null) {
                    return null;
                }
                Iterator<List<?>> iterator = subjectAlternativeNames.iterator();
                boolean upnOidFound = false;
                log.debug(Arrays.toString(subjectAlternativeNames.toArray()));
                while (iterator.hasNext() && !upnOidFound) {
                    List<?> sbjAltName = iterator.next();

                    Integer nameType = (Integer) sbjAltName.get(0);
    
                    if (nameType == generalName) {

                        altName: for (int i = 1 ; i<sbjAltName.size() ; i++) {
                            Object obj = sbjAltName.get(i);

                            // 非 otherName 类型的 SAN 直接返回
                            if (generalName != 0) {
                                log.tracef("Extracted identity '%s' from Subject Alternative Name of type '%d'", obj, generalName);
                                return obj;
                            }

                            // Java 21 起 SAN 可能含额外 String 条目；为兼容 Java 17，仍从第 2 个 byte[] 条目解析
                            if (obj instanceof byte[]) {
                                byte[] otherNameBytes = (byte[]) obj;

                                DERDecoder derDecoder = new DERDecoder(otherNameBytes);
                                derDecoder.startSequence();
                                while (derDecoder.hasNextElement() && !upnOidFound) {
                                    int asn1Type = derDecoder.peekType();
                                    log.debug("ASN.1 Type: " + derDecoder.peekType());

                                    switch (asn1Type) {
                                        case ASN1.OBJECT_IDENTIFIER_TYPE:
                                            String oid = derDecoder.decodeObjectIdentifier();
                                            log.debug("OID: " + oid);
                                            if(UPN_OID.equals(oid)) {
                                                derDecoder.decodeImplicit(160);
                                                byte[] sb = derDecoder.drainElementValue();
                                                while(!Character.isLetterOrDigit(sb[0])) {
                                                    sb = Arrays.copyOfRange(sb, 1, sb.length);
                                                }
                                                subjectName = new String(sb, StandardCharsets.UTF_8);
                                                upnOidFound = true;
                                            }
                                            break;
                                        case ASN1.UTF8_STRING_TYPE:
                                            subjectName = derDecoder.decodeUtf8String();
                                            break;
                                        case ASN1.PRINTABLE_STRING_TYPE:
                                            subjectName = derDecoder.decodePrintableString();
                                            break;
                                        case ASN1.UNIVERSAL_STRING_TYPE:
                                            subjectName = derDecoder.decodeUniversalString();
                                            break;
                                        case ASN1.OCTET_STRING_TYPE:
                                            subjectName = derDecoder.decodeOctetStringAsString();
                                            break;
                                        case 0xa0:
                                            derDecoder.startExplicit(asn1Type);
                                            break;
                                        case ASN1.SEQUENCE_TYPE:
                                            continue altName; // sub-sequence is not expected
                                        default:
                                            derDecoder.skipElement();
                                            break;
                                    }
                                }
                            }
                        }
                    }
                }
            } catch (CertificateParsingException e) {
                log.error("Failed to parse Subject Name:",e);
            }
            
            log.debug("Subject Alt Name: " + subjectName);
            return subjectName;
        }
    }
                


    /** {@inheritDoc} 返回基于 X500 RDN 的身份提取器。 */
    @Override
    public UserIdentityExtractor getX500NameExtractor(String identifier, Function<X509Certificate[], Principal> x500Name) {
        return new X500NameRDNExtractorElytronProvider(identifier, x500Name);
    }

    /** {@inheritDoc} 返回基于 SubjectAltName 的身份提取器。 */
    @Override
    public SubjectAltNameExtractor getSubjectAltNameExtractor(int generalName) {
        return new SubjectAltNameExtractorEltronProvider(generalName);
    }

}
