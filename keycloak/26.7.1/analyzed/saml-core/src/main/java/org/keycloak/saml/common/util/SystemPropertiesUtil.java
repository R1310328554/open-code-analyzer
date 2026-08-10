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
package org.keycloak.saml.common.util;

import javax.xml.XMLConstants;

/**
 * PicketLink/SAML 模块 JVM 级系统属性初始化与读取工具。
 *
 * @author Anil.Saldhana@redhat.com
 * @since Jul 1, 2011
 */
public class SystemPropertiesUtil {
    static {
        // XML 签名：忽略换行以保持 canonical 一致性
        String xmlSec = "org.apache.xml.security.ignoreLineBreaks";
        if (StringUtil.isNullOrEmpty(SecurityActions.getSystemProperty(xmlSec, ""))) {
            SecurityActions.setSystemProperty(xmlSec, "true");
        }

        String xmlSecOpenJdk = "com.sun.org.apache.xml.internal.security.ignoreLineBreaks";
        if (StringUtil.isNullOrEmpty(SecurityActions.getSystemProperty(xmlSecOpenJdk, ""))) {
            SecurityActions.setSystemProperty(xmlSecOpenJdk, "true");
        }

        // JAXP Schema 校验工厂实现
        String schemaFactoryProperty = "javax.xml.validation.SchemaFactory:" + XMLConstants.W3C_XML_SCHEMA_NS_URI;
        if (StringUtil.isNullOrEmpty(SecurityActions.getSystemProperty(schemaFactoryProperty, ""))) {
            SecurityActions.setSystemProperty(schemaFactoryProperty, "org.apache.xerces.jaxp.validation.XMLSchemaFactory");
        }

        // XACML 引擎：默认关闭 schema 校验
        String xacmlValidation = "org.jboss.security.xacml.schema.validation";
        if (StringUtil.isNullOrEmpty(SecurityActions.getSystemProperty(xacmlValidation, ""))) {
            SecurityActions.setSystemProperty(xacmlValidation, "false");
        }
    };

    /**
     * 触发静态初始化块，确保默认系统属性已设置（无其他副作用）。
     */
    public static void ensure() {
    }

    /**
     * 读取系统属性，不存在时返回默认值。
     * @param key 属性键
     * @param defaultValue 默认值
     * @return 属性值
     */
    public static String getSystemProperty(final String key, final String defaultValue){
        return SecurityActions.getSystemProperty(key,defaultValue);
    }
}