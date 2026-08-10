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
package org.keycloak.subsystem.adapter.saml.extension;

import org.jboss.as.controller.SimpleAttributeDefinition;
import org.jboss.as.controller.SimpleAttributeDefinitionBuilder;
import org.jboss.dmr.ModelType;

/**
 * 密钥库内证书条目的属性定义。
 *
 * <p>描述从 Java KeyStore 中按别名加载证书时所需的 {@code certificate-alias} 配置。</p>
 *
 * @author <a href="mailto:mstrukel@redhat.com">Marko Strukelj</a>
 */
public class KeyStoreCertificateDefinition {

    /** 证书在密钥库中的别名。 */
    static final SimpleAttributeDefinition CERTIFICATE_ALIAS =
            new SimpleAttributeDefinitionBuilder(Constants.Model.CERTIFICATE_ALIAS, ModelType.STRING, true)
                    .setXmlName(Constants.XML.CERTIFICATE_ALIAS)
                    .build();

    /** 按 XML 元素/属性名解析证书相关字段。 */
    static SimpleAttributeDefinition lookup(String xmlName) {
        return Constants.XML.CERTIFICATE_ALIAS.equals(xmlName) ? CERTIFICATE_ALIAS : null;
    }
}
