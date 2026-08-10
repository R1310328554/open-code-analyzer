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

package org.keycloak.common.crypto;


import java.security.Principal;
import java.security.cert.X509Certificate;
import java.util.Optional;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.keycloak.common.util.PemUtils;

import org.jboss.logging.Logger;

/**
 * 用户身份提取器的工厂与组合工具 SPI。
 *
 * <p>提供基于 X500 名称、SubjectAltName、正则匹配及 PEM 证书等多种提取策略，
 * 并支持 {@link OrBuilder} 链式组合多个提取器。</p>
 *
 * @author <a href="mailto:pnalyvayko@agi.com">Peter Nalyvayko</a>
 * @version $Revision: 1 $
 * @date 7/30/2016
 */

public abstract class UserIdentityExtractorProvider {

    private static final Logger logger = Logger.getLogger(UserIdentityExtractorProvider.class);

    /** 从证书 SubjectAltName 扩展提取身份的抽象提取器。 */
    public abstract class  SubjectAltNameExtractor implements UserIdentityExtractor {

    }

    /** 从 X500 名称 RDN 提取身份的抽象提取器。 */
    public abstract class X500NameRDNExtractor implements UserIdentityExtractor {
    }

    /** 依次尝试两个提取器，前者返回 {@code null} 时使用后者。 */
    protected class OrExtractor implements UserIdentityExtractor {

        UserIdentityExtractor extractor;
        UserIdentityExtractor other;
        OrExtractor(UserIdentityExtractor extractor, UserIdentityExtractor other) {
            this.extractor = extractor;
            this.other = other;

            if (this.extractor == null)
                throw new IllegalArgumentException("extractor is null");
            if (this.other == null)
                throw new IllegalArgumentException("other is null");
        }

        @Override
        public Object extractUserIdentity(X509Certificate[] certs) {
            Object result = this.extractor.extractUserIdentity(certs);
            if (result == null)
                result = this.other.extractUserIdentity(certs);
            return result;
        }
    }

    /** 对提取值应用正则表达式并返回第一个捕获组作为身份。 */
    public class PatternMatcher implements UserIdentityExtractor {
        private final String _pattern;
        private final Function<X509Certificate[],String> _f;
        PatternMatcher(String pattern, Function<X509Certificate[],String> valueToMatch) {
            _pattern = pattern;
            _f = valueToMatch;
        }

        @Override
        public Object extractUserIdentity(X509Certificate[] certs) {
            String value = Optional.ofNullable(_f.apply(certs)).orElseThrow(IllegalArgumentException::new);

            Pattern r = Pattern.compile(_pattern, Pattern.CASE_INSENSITIVE);

            Matcher m = r.matcher(value);

            if (!m.find()) {
                logger.debugf("[PatternMatcher:extract] No matches were found for input \"%s\", pattern=\"%s\"", value, _pattern);
                return null;
            }

            if (m.groupCount() != 1) {
                logger.debugf("[PatternMatcher:extract] Match produced more than a single group for input \"%s\", pattern=\"%s\"", value, _pattern);
                return null;
            }

            return m.group(1);
        }
    }

    /** 构建“或”组合提取器的辅助类。 */
    public class OrBuilder {
        UserIdentityExtractor extractor;
        UserIdentityExtractor other;
        OrBuilder(UserIdentityExtractor extractor) {
            this.extractor = extractor;
        }

        /** 与另一提取器组成 {@link OrExtractor}。 */
        public UserIdentityExtractor or(UserIdentityExtractor other) {
            return new OrExtractor(extractor, other);
        }
    }

    /** 以给定提取器为起点创建“或”组合构建器。 */
    public OrBuilder either(UserIdentityExtractor extractor) {
        return new OrBuilder(extractor);
    }
    
    /** 返回将证书链首证书 PEM 编码作为用户身份的提取器。 */
    public UserIdentityExtractor getCertificatePemIdentityExtractor() {
        return new UserIdentityExtractor() {
            @Override
            public Object extractUserIdentity(X509Certificate[] certs) {
                if (certs == null || certs.length == 0) {
                    throw new IllegalArgumentException();
                }
                
                String pem = PemUtils.encodeCertificate(certs[0]);
                logger.debugf("Using PEM certificate \"%s\" as user identity.", pem);
                return pem;
            }
        };
    }

    /**
     * 返回对提取值应用正则并取第一个捕获组的提取器。
     *
     * @param pattern 正则表达式（须含恰好一个捕获组）
     * @param valueToMatch 从证书链提取待匹配字符串的函数
     */
    public UserIdentityExtractor getPatternIdentityExtractor(String pattern,
                                                                 Function<X509Certificate[],String> valueToMatch) {
                                                                     return new PatternMatcher(pattern, valueToMatch);
                                                                 }

    /**
     * 从 X500 名称指定 RDN 标识符提取用户身份。
     *
     * @param identifier RDN 属性名（如 CN、UID）
     * @param x500Name 从证书链获取 {@link Principal} 的函数
     */
    public abstract UserIdentityExtractor getX500NameExtractor(String identifier, Function<X509Certificate[],Principal> x500Name);

    /**
     * 从 SubjectAltName 扩展提取用户身份。
     *
     * @param generalName 通用名称类型整数，参见 {@link X509Certificate#getSubjectAlternativeNames()}
     * @return 对应 generalName 的提取器
     */
    public abstract SubjectAltNameExtractor getSubjectAltNameExtractor(int generalName);
}
