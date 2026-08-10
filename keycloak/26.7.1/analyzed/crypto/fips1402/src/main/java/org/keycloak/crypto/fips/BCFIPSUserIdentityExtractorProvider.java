/*
 * Copyright 2016 Analytical Graphics, Inc. and/or its affiliates
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

package org.keycloak.crypto.fips;

import java.io.ByteArrayInputStream;
import java.security.Principal;
import java.security.cert.CertificateParsingException;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import org.keycloak.common.crypto.UserIdentityExtractor;
import org.keycloak.common.crypto.UserIdentityExtractorProvider;

import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.ASN1TaggedObject;
import org.bouncycastle.asn1.DERUTF8String;
import org.bouncycastle.asn1.x500.AttributeTypeAndValue;
import org.bouncycastle.asn1.x500.RDN;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.asn1.x500.style.IETFUtils;
import org.jboss.logging.Logger;

/**
 * BCFIPS 环境下的 X.509 用户身份提取器提供器，从 DN 与 subjectAltName 解析登录标识。
 *
 * @author <a href="mailto:pnalyvayko@agi.com">Peter Nalyvayko</a>
 * @version $Revision: 1 $
 * @date 7/30/2016
 */
public class BCFIPSUserIdentityExtractorProvider  extends UserIdentityExtractorProvider {

    private static final Logger logger = Logger.getLogger(BCFIPSUserIdentityExtractorProvider.class.getName());

    /** 从 X500Name 指定 RDN 属性提取用户标识。 */
    class X500NameRDNExtractorBCProvider extends X500NameRDNExtractor {

        private ASN1ObjectIdentifier x500NameStyle;
        Function<X509Certificate[],Principal> x500Name;

        public X500NameRDNExtractorBCProvider(String attrName, Function<X509Certificate[], Principal> x500Name) {
            this.x500NameStyle = BCStyle.INSTANCE.attrNameToOID(attrName);
            this.x500Name = x500Name;
        }

        /** {@inheritDoc} 解析证书链中指定 DN 属性的值。 */
        @Override
        public Object extractUserIdentity(X509Certificate[] certs) {

            if (certs == null || certs.length == 0)
                throw new IllegalArgumentException();

            X500Name name = new X500Name(x500Name.apply(certs).getName());
            RDN[] rnds = name.getRDNs(x500NameStyle);
            if (rnds != null && rnds.length > 0) {
                RDN cn = rnds[0];
                if(cn.isMultiValued()){
                    AttributeTypeAndValue[] attributeTypeAndValues = cn.getTypesAndValues();
                    Optional<AttributeTypeAndValue> optionalFirst = Arrays.stream(attributeTypeAndValues).filter(attributeTypeAndValue -> attributeTypeAndValue.getType().getId().equals(x500NameStyle.getId())).findFirst();
                    if(optionalFirst.isPresent()) {
                        return IETFUtils.valueToString(optionalFirst.get().getValue());
                    }
                    else {
                        return null;
                    }
                }
                else {
                    return IETFUtils.valueToString(cn.getFirst().getValue());
                }
            }
            return null;
        }
    }

    /**
     * 从 subjectAltName 扩展提取用户标识（含 Microsoft UPN otherName）。
     */
    class SubjectAltNameExtractorBCProvider extends SubjectAltNameExtractor {

        // 用户主体名（UPN），常见于 Microsoft 智能卡登录证书
        private static final String UPN_OID = "1.3.6.1.4.1.311.20.2.3";

        private final int generalName;

        /**
         * 创建 subjectAltName 提取器。
         *
         * @param generalName an integer representing the general name. See {@link X509Certificate#getSubjectAlternativeNames()}
         */
        SubjectAltNameExtractorBCProvider(int generalName) {
            this.generalName = generalName;
        }

        @Override
        public Object extractUserIdentity(X509Certificate[] certs) {
            if (certs == null || certs.length == 0) {
                throw new IllegalArgumentException();
            }

            try {
                Collection<List<?>> subjectAlternativeNames = certs[0].getSubjectAlternativeNames();

                if (subjectAlternativeNames == null) {
                    return null;
                }

                Iterator<List<?>> iterator = subjectAlternativeNames.iterator();

                boolean foundUpn = false;
                String tempOtherName = null;
                String tempOid = null;

                while (iterator.hasNext() && !foundUpn) {
                    List<?> next = iterator.next();

                    if (Integer.class.cast(next.get(0)) == generalName) {

                        // 优先在 otherName 中查找 UPN OID，未找到时再回退其他类型
                        for (int i = 1 ; i<next.size() ; i++) {
                            Object obj = next.get(i);

                            // 非 otherName 类型直接返回对应值
                            if (generalName != 0) {
                                logger.tracef("Extracted identity '%s' from Subject Alternative Name of type '%d'", obj, generalName);
                                return obj;
                            }

                            // 自 Java 21 起条目可能含 String 类型标识；为兼容 Java 17 仍从 byte[] 解析
                            if (obj instanceof byte[]) {
                                byte[] otherNameBytes = (byte[]) obj;

                                try {
                                    ASN1InputStream asn1Stream = new ASN1InputStream(new ByteArrayInputStream(otherNameBytes));
                                    ASN1Encodable asn1otherName = asn1Stream.readObject();
                                    asn1otherName = unwrap(asn1otherName);

                                    ASN1Sequence asn1Sequence = ASN1Sequence.getInstance(asn1otherName);

                                    if (asn1Sequence != null) {
                                        ASN1Encodable encodedOid = asn1Sequence.getObjectAt(0);
                                        ASN1ObjectIdentifier oid = ASN1ObjectIdentifier.getInstance(unwrap(encodedOid));
                                        tempOid = oid.getId();

                                        ASN1Encodable principalNameEncoded = asn1Sequence.getObjectAt(1);
                                        DERUTF8String principalName = DERUTF8String.getInstance(unwrap(principalNameEncoded));

                                        tempOtherName = principalName.getString();

                                        // 已找到 UPN，无需继续扫描
                                        if (UPN_OID.equals(tempOid)) {
                                            foundUpn = true;
                                            break;
                                        }
                                    }

                                } catch (Exception e) {
                                    logger.error("Failed to parse subjectAltName", e);
                                }
                            } else {
                                logger.tracef("Ignoring the Subject alternative name entry. Entry number: %d, value: %s", i + 1, obj);
                            }
                        }

                    }
                }

                logger.tracef("Parsed otherName from subjectAltName. OID: '%s', Principal: '%s'", tempOid, tempOtherName);

                return tempOtherName;

            } catch (CertificateParsingException cause) {
                logger.errorf(cause, "Failed to obtain identity from subjectAltName extension");
            }

            return null;
        }


        /** 递归展开 ASN.1 标签包装。 */
        private ASN1Encodable unwrap(ASN1Encodable encodable) {
            while (encodable instanceof ASN1TaggedObject) {
                ASN1TaggedObject taggedObj = (ASN1TaggedObject) encodable;
                encodable = taggedObj.getObject();
            }

            return encodable;
        }
    }

    /** {@inheritDoc} 返回基于 BCFIPS X500Name 的 RDN 提取器。 */
    @Override
    public UserIdentityExtractor getX500NameExtractor(String identifier, Function<X509Certificate[], Principal> x500Name) {
        return new X500NameRDNExtractorBCProvider(identifier, x500Name);
    }

    /** {@inheritDoc} 返回 BCFIPS subjectAltName 提取器。 */
    @Override
    public SubjectAltNameExtractor getSubjectAltNameExtractor(int generalName) {
        return new SubjectAltNameExtractorBCProvider(generalName);
    }

}
