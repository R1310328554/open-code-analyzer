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
package org.keycloak.saml.processing.core.saml.v1.writers;

import javax.xml.stream.XMLStreamWriter;

import org.keycloak.saml.common.PicketLinkLogger;
import org.keycloak.saml.common.PicketLinkLoggerFactory;

/**
 * SAML 1.1 XML 写入器抽象基类。
 * <p>持有 {@link XMLStreamWriter} 并定义协议、断言等常用命名空间前缀。</p>
 *
 * @author Anil.Saldhana@redhat.com
 * @since Jun 27, 2011
 */
public abstract class BaseSAML11Writer {

    /** 日志记录器。 */
    protected static final PicketLinkLogger logger = PicketLinkLoggerFactory.getLogger();

    /** 协议命名空间前缀（samlp）。 */
    protected static String PROTOCOL_PREFIX = "samlp";

    /** 断言命名空间前缀（saml）。 */
    protected static String ASSERTION_PREFIX = "saml";

    /** XACML-SAML 断言前缀。 */
    protected static String XACML_SAML_PREFIX = "xacml-saml";

    /** XACML-SAML 协议前缀。 */
    protected static String XACML_SAML_PROTO_PREFIX = "xacml-samlp";

    /** XML Schema Instance 前缀。 */
    protected static String XSI_PREFIX = "xsi";

    /** 底层 StAX 流写入器。 */
    protected XMLStreamWriter writer;

    /**
     * 使用给定 StAX 写入器构造基类。
     *
     * @param writer XML 流写入器
     */
    public BaseSAML11Writer(XMLStreamWriter writer) {
        this.writer = writer;
    }
}
