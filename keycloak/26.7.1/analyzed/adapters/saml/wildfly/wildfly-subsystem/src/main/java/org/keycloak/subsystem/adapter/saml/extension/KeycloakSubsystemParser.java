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

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;

import org.jboss.as.controller.PathAddress;
import org.jboss.as.controller.PathElement;
import org.jboss.as.controller.SimpleAttributeDefinition;
import org.jboss.as.controller.descriptions.ModelDescriptionConstants;
import org.jboss.as.controller.operations.common.Util;
import org.jboss.as.controller.parsing.ParseUtils;
import org.jboss.as.controller.persistence.SubsystemMarshallingContext;
import org.jboss.dmr.ModelNode;
import org.jboss.dmr.Property;
import org.jboss.staxmapper.XMLElementReader;
import org.jboss.staxmapper.XMLElementWriter;
import org.jboss.staxmapper.XMLExtendedStreamReader;
import org.jboss.staxmapper.XMLExtendedStreamWriter;

/**
 * {@code keycloak-saml} 子系统的 StAX XML 解析与序列化器。
 *
 * <p>将 standalone.xml 中的 secure-deployment、service-provider、identity-provider、
 * key/keystore 等配置读入 {@link ModelNode} 操作列表，并支持将运行时模型写回 XML。</p>
 */
class KeycloakSubsystemParser implements XMLStreamConstants, XMLElementReader<List<ModelNode>>, XMLElementWriter<SubsystemMarshallingContext> {

    /**
     * 解析子系统根元素，生成 add 操作及嵌套 secure-deployment 操作。
     * {@inheritDoc}
     */
    @Override
    public void readElement(final XMLExtendedStreamReader reader, final List<ModelNode> list) throws XMLStreamException {
        // 根元素不允许属性
        ParseUtils.requireNoAttributes(reader);
        ModelNode addKeycloakSub = Util.createAddOperation(PathAddress.pathAddress(KeycloakSamlExtension.PATH_SUBSYSTEM));
        list.add(addKeycloakSub);

        while (reader.hasNext() && nextTag(reader) != END_ELEMENT) {
            if (reader.getLocalName().equals(Constants.XML.SECURE_DEPLOYMENT)) {
                readSecureDeployment(reader, list);
            } else {
                throw ParseUtils.unexpectedElement(reader);
            }
        }
    }

    // 调试辅助：前进到下一个 START/END 标签
    private int nextTag(XMLExtendedStreamReader reader) throws XMLStreamException {
        return reader.nextTag();
    }

    /** 解析 {@code secure-deployment} 元素及其 service-provider 子树。 */
    void readSecureDeployment(XMLExtendedStreamReader reader, List<ModelNode> list) throws XMLStreamException {
        String name = readRequiredAttribute(reader, Constants.XML.NAME);

        PathAddress addr = PathAddress.pathAddress(
                PathElement.pathElement(ModelDescriptionConstants.SUBSYSTEM, KeycloakSamlExtension.SUBSYSTEM_NAME),
                PathElement.pathElement(Constants.Model.SECURE_DEPLOYMENT, name));
        ModelNode addSecureDeployment = Util.createAddOperation(addr);
        list.add(addSecureDeployment);

        Set<String> parsedElements = new HashSet<>();
        while (reader.hasNext() && nextTag(reader) != END_ELEMENT) {
            String tagName = reader.getLocalName();
            if (parsedElements.contains(tagName)) {
                // secure-deployment 下各子元素仅允许出现一次
                throw ParseUtils.unexpectedElement(reader);
            }
            if (tagName.equals(Constants.XML.SERVICE_PROVIDER)) {
                readServiceProvider(reader, list, addr);
            } else {
                throw ParseUtils.unexpectedElement(reader);
            }
            parsedElements.add(tagName);
        }
    }

    /** 解析 {@code service-provider} 元素、属性及 keys/IdP 等子配置。 */
    void readServiceProvider(XMLExtendedStreamReader reader, List<ModelNode> list, PathAddress parentAddr) throws XMLStreamException {
        String entityId = readRequiredAttribute(reader, Constants.XML.ENTITY_ID);

        PathAddress addr = PathAddress.pathAddress(parentAddr,
                PathElement.pathElement(Constants.Model.SERVICE_PROVIDER, entityId));
        ModelNode addServiceProvider = Util.createAddOperation(addr);
        list.add(addServiceProvider);

        for (int i = 0; i < reader.getAttributeCount(); i++) {
            String name = reader.getAttributeLocalName(i);
            if (Constants.XML.ENTITY_ID.equals(name)) {
                continue;
            }

            String value = reader.getAttributeValue(i);

            SimpleAttributeDefinition attr = ServiceProviderDefinition.lookup(name);
            if (attr == null) {
                throw ParseUtils.unexpectedAttribute(reader, i);
            }
            attr.parseAndSetParameter(value, addServiceProvider, reader);
        }

        Set<String> parsedElements = new HashSet<>();
        while (reader.hasNext() && nextTag(reader) != END_ELEMENT) {
            String tagName = reader.getLocalName();
            if (parsedElements.contains(tagName)) {
                // service-provider 下各子元素仅允许出现一次
                throw ParseUtils.unexpectedElement(reader);
            }
            if (Constants.XML.KEYS.equals(tagName)) {
                readKeys(list, reader, addr);
            } else if (Constants.XML.PRINCIPAL_NAME_MAPPING.equals(tagName)) {
                readPrincipalNameMapping(addServiceProvider, reader);
            } else if (Constants.XML.ROLE_IDENTIFIERS.equals(tagName)) {
                readRoleIdentifiers(addServiceProvider, reader);
            } else if (Constants.XML.ROLE_MAPPINGS_PROVIDER.equals(tagName)) {
                readRoleMappingsProvider(addServiceProvider, reader);
            } else if (Constants.XML.IDENTITY_PROVIDER.equals(tagName)) {
                readIdentityProvider(list, reader, addr);
            } else {
                throw ParseUtils.unexpectedElement(reader);
            }
            parsedElements.add(tagName);
        }
    }

    /** 解析 {@code identity-provider} 元素及 SSO/SLO/keys 等子树。 */
    void readIdentityProvider(List<ModelNode> list, XMLExtendedStreamReader reader, PathAddress parentAddr) throws XMLStreamException {
        String entityId = readRequiredAttribute(reader, Constants.XML.ENTITY_ID);

        PathAddress addr = PathAddress.pathAddress(parentAddr,
                PathElement.pathElement(Constants.Model.IDENTITY_PROVIDER, entityId));
        ModelNode addIdentityProvider = Util.createAddOperation(addr);
        list.add(addIdentityProvider);

        for (int i = 0; i < reader.getAttributeCount(); i++) {
            String name = reader.getAttributeLocalName(i);
            String value = reader.getAttributeValue(i);

            if (Constants.XML.ENTITY_ID.equals(name)
                    // 兼容 client-adapter/core keycloak_saml_adapter_1_6.xsd 中的 noop encryption 属性
                    || "encryption".equals(name)) {
                continue;
            }
            SimpleAttributeDefinition attr = IdentityProviderDefinition.lookup(name);
            if (attr == null) {
                throw ParseUtils.unexpectedAttribute(reader, i);
            }
            attr.parseAndSetParameter(value, addIdentityProvider, reader);
        }

        Set<String> parsedElements = new HashSet<>();
        while (reader.hasNext() && nextTag(reader) != END_ELEMENT) {
            String tagName = reader.getLocalName();
            if (parsedElements.contains(tagName)) {
                // identity-provider 下各子元素仅允许出现一次
                throw ParseUtils.unexpectedElement(reader);
            }

            if (Constants.XML.SINGLE_SIGN_ON.equals(tagName)) {
                readSingleSignOn(addIdentityProvider, reader);
            } else if (Constants.XML.SINGLE_LOGOUT.equals(tagName)) {
                readSingleLogout(addIdentityProvider, reader);
            } else if (Constants.XML.KEYS.equals(tagName)) {
                readKeys(list, reader, addr);
            } else if (Constants.XML.HTTP_CLIENT.equals(tagName)) {
                readHttpClient(addIdentityProvider, reader);
            } else if (Constants.XML.ALLOWED_CLOCK_SKEW.equals(tagName)) {
                readAllowedClockSkew(addIdentityProvider, reader);
            } else {
                throw ParseUtils.unexpectedElement(reader);
            }
            parsedElements.add(tagName);
        }
    }

    /** 解析 IdP 单点登录（SSO）子元素属性。 */
    void readSingleSignOn(ModelNode addIdentityProvider, XMLExtendedStreamReader reader) throws XMLStreamException {
        ModelNode sso = addIdentityProvider.get(Constants.Model.SINGLE_SIGN_ON);
        for (int i = 0; i < reader.getAttributeCount(); i++) {
            String name = reader.getAttributeLocalName(i);
            String value = reader.getAttributeValue(i);

            SimpleAttributeDefinition attr = SingleSignOnDefinition.lookup(name);
            if (attr == null) {
                throw ParseUtils.unexpectedAttribute(reader, i);
            }
            attr.parseAndSetParameter(value, sso, reader);
        }
        ParseUtils.requireNoContent(reader);
    }

    /** 解析 IdP 单点登出（SLO）子元素属性。 */
    void readSingleLogout(ModelNode addIdentityProvider, XMLExtendedStreamReader reader) throws XMLStreamException {
        ModelNode slo = addIdentityProvider.get(Constants.Model.SINGLE_LOGOUT);
        for (int i = 0; i < reader.getAttributeCount(); i++) {
            String name = reader.getAttributeLocalName(i);
            String value = reader.getAttributeValue(i);

            SimpleAttributeDefinition attr = SingleLogoutDefinition.lookup(name);
            if (attr == null) {
                throw ParseUtils.unexpectedAttribute(reader, i);
            }
            attr.parseAndSetParameter(value, slo, reader);
        }
        ParseUtils.requireNoContent(reader);
    }

    /** 解析 {@code keys} 容器及其多个 {@code key} 子元素。 */
    void readKeys(List<ModelNode> list, XMLExtendedStreamReader reader, PathAddress parentAddr) throws XMLStreamException {
        ParseUtils.requireNoAttributes(reader);
        List<ModelNode> keyList = new LinkedList<>();
        while (reader.hasNext() && nextTag(reader) != END_ELEMENT) {
            String tagName = reader.getLocalName();
            if (!Constants.XML.KEY.equals(tagName)) {
                throw ParseUtils.unexpectedElement(reader);
            }
            readKey(keyList, reader, parentAddr);
        }
        list.addAll(keyList);
    }

    /** 解析 IdP HTTP 客户端超时/连接池等属性。 */
    void readHttpClient(final ModelNode addIdentityProvider, final XMLExtendedStreamReader reader) throws XMLStreamException {
        ModelNode httpClientNode = addIdentityProvider.get(Constants.Model.HTTP_CLIENT);
        for (int i = 0; i < reader.getAttributeCount(); i++) {
            String name = reader.getAttributeLocalName(i);
            String value = reader.getAttributeValue(i);

            SimpleAttributeDefinition attr = HttpClientDefinition.lookup(name);
            if (attr == null) {
                throw ParseUtils.unexpectedAttribute(reader, i);
            }
            attr.parseAndSetParameter(value, httpClientNode, reader);
        }
        ParseUtils.requireNoContent(reader);
    }

    /** 解析允许的时钟偏差（元素文本为数值，unit 为属性）。 */
    void readAllowedClockSkew(ModelNode addIdentityProvider, XMLExtendedStreamReader reader) throws XMLStreamException {
        ModelNode allowedClockSkew = addIdentityProvider.get(Constants.Model.ALLOWED_CLOCK_SKEW);
        for (int i = 0; i < reader.getAttributeCount(); i++) {
            String name = reader.getAttributeLocalName(i);
            String value = reader.getAttributeValue(i);

            if (Constants.XML.ALLOWED_CLOCK_SKEW_UNIT.equals(name)) {
                SimpleAttributeDefinition attr = AllowedClockSkew.ALLOWED_CLOCK_SKEW_UNIT;
                attr.parseAndSetParameter(value, allowedClockSkew, reader);
            } else {
                throw ParseUtils.unexpectedAttribute(reader, i);
            }
        }
        // 实际偏差数值来自元素文本内容
        String value = reader.getElementText();
        SimpleAttributeDefinition attr = AllowedClockSkew.ALLOWED_CLOCK_SKEW_VALUE;
        attr.parseAndSetParameter(value, allowedClockSkew, reader);
    }

    /** 解析单个 {@code key} 资源及其 PEM 或 key-store 子配置。 */
    void readKey(List<ModelNode> list, XMLExtendedStreamReader reader, PathAddress parentAddr) throws XMLStreamException {
        PathAddress addr = PathAddress.pathAddress(parentAddr,
                PathElement.pathElement(Constants.Model.KEY, "key-" + list.size()));
        ModelNode addKey = Util.createAddOperation(addr);
        list.add(addKey);

        for (int i = 0; i < reader.getAttributeCount(); i++) {
            String name = reader.getAttributeLocalName(i);
            String value = reader.getAttributeValue(i);

            SimpleAttributeDefinition attr = KeyDefinition.lookup(name);
            if (attr == null) {
                throw ParseUtils.unexpectedAttribute(reader, i);
            }
            attr.parseAndSetParameter(value, addKey, reader);
        }

        Set<String> parsedElements = new HashSet<>();
        while (reader.hasNext() && nextTag(reader) != END_ELEMENT) {
            String tagName = reader.getLocalName();
            if (parsedElements.contains(tagName)) {
                // key 下各子元素仅允许出现一次
                throw ParseUtils.unexpectedElement(reader);
            }

            if (Constants.XML.KEY_STORE.equals(tagName)) {
                readKeyStore(addKey, reader);
            } else if (Constants.XML.PRIVATE_KEY_PEM.equals(tagName)
                    || Constants.XML.PUBLIC_KEY_PEM.equals(tagName)
                    || Constants.XML.CERTIFICATE_PEM.equals(tagName)) {

                readNoAttrElementContent(KeyDefinition.lookupElement(tagName), addKey, reader);
            } else {
                throw ParseUtils.unexpectedElement(reader);
            }
            parsedElements.add(tagName);
        }
    }

    /** 读取无属性的纯文本子元素并写入模型。 */
    void readNoAttrElementContent(SimpleAttributeDefinition attr, ModelNode model, XMLExtendedStreamReader reader) throws XMLStreamException {
        ParseUtils.requireNoAttributes(reader);
        String value = reader.getElementText();
        attr.parseAndSetParameter(value, model, reader);
    }

    /** 解析 {@code key-store} 元素，校验 file/resource 与 password 必填。 */
    void readKeyStore(ModelNode addKey, XMLExtendedStreamReader reader) throws XMLStreamException {
        ModelNode addKeyStore = addKey.get(Constants.Model.KEY_STORE);

        for (int i = 0; i < reader.getAttributeCount(); i++) {
            String name = reader.getAttributeLocalName(i);
            String value = reader.getAttributeValue(i);

            SimpleAttributeDefinition attr = KeyStoreDefinition.lookup(name);
            if (attr == null) {
                throw ParseUtils.unexpectedAttribute(reader, i);
            }
            attr.parseAndSetParameter(value, addKeyStore, reader);
        }

        if (!addKeyStore.hasDefined(Constants.Model.FILE) && !addKeyStore.hasDefined(Constants.Model.RESOURCE)) {
            throw new XMLStreamException("KeyStore element must have 'file' or 'resource' attribute set", reader.getLocation());
        }
        if (!addKeyStore.hasDefined(Constants.Model.PASSWORD)) {
            throw ParseUtils.missingRequired(reader, Constants.XML.PASSWORD);
        }

        Set<String> parsedElements = new HashSet<>();
        while (reader.hasNext() && nextTag(reader) != END_ELEMENT) {
            String tagName = reader.getLocalName();
            if (parsedElements.contains(tagName)) {
                // key-store 下各子元素仅允许出现一次
                throw ParseUtils.unexpectedElement(reader);
            }
            if (Constants.XML.PRIVATE_KEY.equals(tagName)) {
                readPrivateKey(reader, addKeyStore);
            } else if (Constants.XML.CERTIFICATE.equals(tagName)) {
                readCertificate(reader, addKeyStore);
            } else {
                throw ParseUtils.unexpectedElement(reader);
            }
            parsedElements.add(tagName);
        }
    }


    /** 解析 key-store 内 {@code private-key} 子元素的别名与密码。 */
    void readPrivateKey(XMLExtendedStreamReader reader, ModelNode addKeyStore) throws XMLStreamException {
        for (int i = 0; i < reader.getAttributeCount(); i++) {
            String name = reader.getAttributeLocalName(i);
            String value = reader.getAttributeValue(i);

            SimpleAttributeDefinition attr = KeyStorePrivateKeyDefinition.lookup(name);
            if (attr == null) {
                throw ParseUtils.unexpectedAttribute(reader, i);
            }
            attr.parseAndSetParameter(value, addKeyStore, reader);
        }

        if (!addKeyStore.hasDefined(Constants.Model.PRIVATE_KEY_ALIAS)) {
            throw ParseUtils.missingRequired(reader, Constants.XML.PRIVATE_KEY_ALIAS);
        }
        if (!addKeyStore.hasDefined(Constants.Model.PRIVATE_KEY_PASSWORD)) {
            throw ParseUtils.missingRequired(reader, Constants.XML.PRIVATE_KEY_PASSWORD);
        }

        ParseUtils.requireNoContent(reader);
    }

    /** 解析 key-store 内 {@code certificate} 子元素的证书别名。 */
    void readCertificate(XMLExtendedStreamReader reader, ModelNode addKeyStore) throws XMLStreamException {
        for (int i = 0; i < reader.getAttributeCount(); i++) {
            String name = reader.getAttributeLocalName(i);
            String value = reader.getAttributeValue(i);

            SimpleAttributeDefinition attr = KeyStoreCertificateDefinition.lookup(name);
            if (attr == null) {
                throw ParseUtils.unexpectedAttribute(reader, i);
            }
            attr.parseAndSetParameter(value, addKeyStore, reader);
        }

        if (!addKeyStore.hasDefined(Constants.Model.CERTIFICATE_ALIAS)) {
            throw ParseUtils.missingRequired(reader, Constants.XML.CERTIFICATE_ALIAS);
        }

        ParseUtils.requireNoContent(reader);
    }

    /** 解析 SP 角色标识符列表（{@code role-identifiers/attribute}）。 */
    void readRoleIdentifiers(ModelNode addServiceProvider, XMLExtendedStreamReader reader) throws XMLStreamException {
        ParseUtils.requireNoAttributes(reader);

        while (reader.hasNext() && nextTag(reader) != END_ELEMENT) {
            String tagName = reader.getLocalName();

            if (!Constants.XML.ATTRIBUTE.equals(tagName)) {
                throw ParseUtils.unexpectedElement(reader);
            }

            ParseUtils.requireSingleAttribute(reader, Constants.XML.NAME);
            String name = ParseUtils.readStringAttributeElement(reader, Constants.XML.NAME);

            ServiceProviderDefinition.ROLE_ATTRIBUTES.parseAndAddParameterElement(name, addServiceProvider, reader);
        }
    }

    /** 解析角色映射 SPI 提供者 ID 及其 property 配置项。 */
    void readRoleMappingsProvider(final ModelNode addServiceProvider, final XMLExtendedStreamReader reader) throws XMLStreamException {
        String providerId = readRequiredAttribute(reader, Constants.XML.ID);
        ServiceProviderDefinition.ROLE_MAPPINGS_PROVIDER_ID.parseAndSetParameter(providerId, addServiceProvider, reader);

        while (reader.hasNext() && nextTag(reader) != END_ELEMENT) {
            String tagName = reader.getLocalName();
            if (!Constants.XML.PROPERTY.equals(tagName)) {
                throw ParseUtils.unexpectedElement(reader);
            }
            final String[] array = ParseUtils.requireAttributes(reader, Constants.XML.NAME, Constants.XML.VALUE);
            ServiceProviderDefinition.ROLE_MAPPINGS_PROVIDER_CONFIG.parseAndAddParameterElement(array[0], array[1], addServiceProvider, reader);
            ParseUtils.requireNoContent(reader);
        }
    }

    /** 解析主体名映射策略及可选的 SAML 属性名。 */
    void readPrincipalNameMapping(ModelNode addServiceProvider, XMLExtendedStreamReader reader) throws XMLStreamException {

        boolean policySet = false;

        for (int i = 0; i < reader.getAttributeCount(); i++) {
            String name = reader.getAttributeLocalName(i);
            String value = reader.getAttributeValue(i);

            if (Constants.XML.PRINCIPAL_NAME_MAPPING_POLICY.equals(name)) {
                policySet = true;
                ServiceProviderDefinition.PRINCIPAL_NAME_MAPPING_POLICY.parseAndSetParameter(value, addServiceProvider, reader);
            } else if (Constants.XML.PRINCIPAL_NAME_MAPPING_ATTRIBUTE_NAME.equals(name)) {
                ServiceProviderDefinition.PRINCIPAL_NAME_MAPPING_ATTRIBUTE_NAME.parseAndSetParameter(value, addServiceProvider, reader);
            } else {
                throw ParseUtils.unexpectedAttribute(reader, i);
            }
        }

        if (!policySet) {
            throw ParseUtils.missingRequired(reader, Constants.XML.PRINCIPAL_NAME_MAPPING_POLICY);
        }
        ParseUtils.requireNoContent(reader);
    }

    /**
     * 读取必填 XML 属性；缺失时抛出解析异常。
     *
     * @param reader StAX 读取器
     * @param attrName 属性本地名
     * @return 属性值
     */
    String readRequiredAttribute(XMLExtendedStreamReader reader, String attrName) throws XMLStreamException {
        String value = null;
        for (int i = 0; i < reader.getAttributeCount(); i++) {
            String attr = reader.getAttributeLocalName(i);
            if (attr.equals(attrName)) {
                value = reader.getAttributeValue(i);
                break;
            }
        }
        if (value == null) {
            throw ParseUtils.missingRequired(reader, Collections.singleton(attrName));
        }
        return value;
    }

    /**
     * 将子系统模型序列化为 XML 根元素。
     * {@inheritDoc}
     */
    @Override
    public void writeContent(final XMLExtendedStreamWriter writer, final SubsystemMarshallingContext context) throws XMLStreamException {
        context.startSubsystemElement(KeycloakSamlExtension.CURRENT_NAMESPACE, false);
        writeSecureDeployment(writer, context.getModelNode());
        writer.writeEndElement();
    }

    /** 序列化所有 secure-deployment 条目。 */
    public void writeSecureDeployment(XMLExtendedStreamWriter writer, ModelNode model) throws XMLStreamException {
        if (!model.get(Constants.Model.SECURE_DEPLOYMENT).isDefined()) {
            return;
        }

        for (Property sp : model.get(Constants.Model.SECURE_DEPLOYMENT).asPropertyList()) {
            writer.writeStartElement(Constants.XML.SECURE_DEPLOYMENT);
            writer.writeAttribute(Constants.XML.NAME, sp.getName());

            writeSps(writer, sp.getValue());
            writer.writeEndElement();
        }
    }

    /** 序列化 secure-deployment 下的全部 service-provider 配置。 */
    void writeSps(final XMLExtendedStreamWriter writer, final ModelNode model) throws XMLStreamException {
        if (!model.isDefined()) {
            return;
        }
        for (Property sp : model.get(Constants.Model.SERVICE_PROVIDER).asPropertyList()) {
            writer.writeStartElement(Constants.XML.SERVICE_PROVIDER);
            writer.writeAttribute(Constants.XML.ENTITY_ID, sp.getName());
            ModelNode spAttributes = sp.getValue();
            for (SimpleAttributeDefinition attr : ServiceProviderDefinition.ATTRIBUTES) {
                attr.getMarshaller().marshallAsAttribute(attr, spAttributes, false, writer);
            }
            writeKeys(writer, spAttributes.get(Constants.Model.KEY));
            writePrincipalNameMapping(writer, spAttributes);
            writeRoleIdentifiers(writer, spAttributes);
            writeRoleMappingsProvider(writer, spAttributes);
            writeIdentityProvider(writer, spAttributes.get(Constants.Model.IDENTITY_PROVIDER));

            writer.writeEndElement();
        }
    }

    void writeIdentityProvider(XMLExtendedStreamWriter writer, ModelNode model) throws XMLStreamException {
        if (!model.isDefined()) {
            return;
        }

        for (Property idp : model.asPropertyList()) {
            writer.writeStartElement(Constants.XML.IDENTITY_PROVIDER);
            writer.writeAttribute(Constants.XML.ENTITY_ID, idp.getName());

            ModelNode idpAttributes = idp.getValue();
            for (SimpleAttributeDefinition attr : IdentityProviderDefinition.ATTRIBUTES) {
                attr.getMarshaller().marshallAsAttribute(attr, idpAttributes, false, writer);
            }

            writeSingleSignOn(writer, idpAttributes.get(Constants.Model.SINGLE_SIGN_ON));
            writeSingleLogout(writer, idpAttributes.get(Constants.Model.SINGLE_LOGOUT));
            writeKeys(writer, idpAttributes.get(Constants.Model.KEY));
            writeHttpClient(writer, idpAttributes.get(Constants.Model.HTTP_CLIENT));
            writeAllowedClockSkew(writer, idpAttributes.get(Constants.Model.ALLOWED_CLOCK_SKEW));
            writer.writeEndElement();
        }
    }

    void writeSingleSignOn(XMLExtendedStreamWriter writer, ModelNode model) throws XMLStreamException {
        if (!model.isDefined()) {
            return;
        }
        writer.writeStartElement(Constants.XML.SINGLE_SIGN_ON);
        for (SimpleAttributeDefinition attr : SingleSignOnDefinition.ATTRIBUTES) {
            attr.getMarshaller().marshallAsAttribute(attr, model, false, writer);
        }
        writer.writeEndElement();
    }

    void writeSingleLogout(XMLExtendedStreamWriter writer, ModelNode model) throws XMLStreamException {
        if (!model.isDefined()) {
            return;
        }
        writer.writeStartElement(Constants.XML.SINGLE_LOGOUT);
        for (SimpleAttributeDefinition attr : SingleLogoutDefinition.ATTRIBUTES) {
            attr.getMarshaller().marshallAsAttribute(attr, model, false, writer);
        }
        writer.writeEndElement();
    }

    void writeKeys(XMLExtendedStreamWriter writer, ModelNode model) throws XMLStreamException {
        if (!model.isDefined()) {
            return;
        }
        boolean contains = false;
        for (Property key : model.asPropertyList()) {
            if (!contains) {
                writer.writeStartElement(Constants.XML.KEYS);
                contains = true;
            }
            writer.writeStartElement(Constants.XML.KEY);

            ModelNode keyAttributes = key.getValue();
            for (SimpleAttributeDefinition attr : KeyDefinition.ATTRIBUTES) {
                attr.getMarshaller().marshallAsAttribute(attr, keyAttributes, false, writer);
            }
            for (SimpleAttributeDefinition attr : KeyDefinition.ELEMENTS) {
                attr.getMarshaller().marshallAsElement(attr, keyAttributes, false, writer);
            }
            writeKeyStore(writer, keyAttributes.get(Constants.Model.KEY_STORE));

            writer.writeEndElement();
        }
        if (contains) {
            writer.writeEndElement();
        }
    }

    void writeHttpClient(XMLExtendedStreamWriter writer, ModelNode httpClientModel) throws XMLStreamException {
        if (!httpClientModel.isDefined()) {
            return;
        }
        writer.writeStartElement(Constants.XML.HTTP_CLIENT);
        for (SimpleAttributeDefinition attr : HttpClientDefinition.ATTRIBUTES) {
            attr.marshallAsAttribute(httpClientModel, false, writer);
        }
        writer.writeEndElement();
    }

    void writeAllowedClockSkew(XMLExtendedStreamWriter writer, ModelNode allowedClockSkew) throws XMLStreamException {
        if (!allowedClockSkew.isDefined()) {
            return;
        }
        writer.writeStartElement(Constants.XML.ALLOWED_CLOCK_SKEW);
        AllowedClockSkew.ALLOWED_CLOCK_SKEW_UNIT.getMarshaller().marshallAsAttribute(AllowedClockSkew.ALLOWED_CLOCK_SKEW_UNIT, allowedClockSkew, false, writer);
        ModelNode allowedClockSkewValue = allowedClockSkew.get(Constants.Model.ALLOWED_CLOCK_SKEW_VALUE);
        char[] chars = allowedClockSkewValue.asString().toCharArray();
        writer.writeCharacters(chars, 0, chars.length);
        writer.writeEndElement();
    }

    void writeKeyStore(XMLExtendedStreamWriter writer, ModelNode model) throws XMLStreamException {
        if (!model.isDefined()) {
            return;
        }
        writer.writeStartElement(Constants.XML.KEY_STORE);
        for (SimpleAttributeDefinition attr : KeyStoreDefinition.ATTRIBUTES) {
            attr.getMarshaller().marshallAsAttribute(attr, model, false, writer);
        }
        writePrivateKey(writer, model);
        writeCertificate(writer, model);
        writer.writeEndElement();
    }

    void writeCertificate(XMLExtendedStreamWriter writer, ModelNode model) throws XMLStreamException {
        ModelNode value = model.get(Constants.Model.CERTIFICATE_ALIAS);
        if (!value.isDefined()) {
            return;
        }
        writer.writeStartElement(Constants.XML.CERTIFICATE);
        SimpleAttributeDefinition attr = KeyStoreCertificateDefinition.CERTIFICATE_ALIAS;
        attr.getMarshaller().marshallAsAttribute(attr, model, false, writer);
        writer.writeEndElement();
    }

    void writePrivateKey(XMLExtendedStreamWriter writer, ModelNode model) throws XMLStreamException {
        ModelNode pk_alias = model.get(Constants.Model.PRIVATE_KEY_ALIAS);
        ModelNode pk_password = model.get(Constants.Model.PRIVATE_KEY_PASSWORD);

        if (!pk_alias.isDefined() && !pk_password.isDefined()) {
            return;
        }
        writer.writeStartElement(Constants.XML.PRIVATE_KEY);
        for (SimpleAttributeDefinition attr : KeyStorePrivateKeyDefinition.ATTRIBUTES) {
            attr.getMarshaller().marshallAsAttribute(attr, model, false, writer);
        }
        writer.writeEndElement();
    }

    void writeRoleIdentifiers(XMLExtendedStreamWriter writer, ModelNode model) throws XMLStreamException {
        ModelNode value = model.get(Constants.Model.ROLE_ATTRIBUTES);
        if (!value.isDefined()) {
            return;
        }

        List<ModelNode> items = value.asList();
        if (items.size() == 0) {
            return;
        }

        writer.writeStartElement(Constants.XML.ROLE_IDENTIFIERS);
        for (ModelNode item : items) {
            writer.writeStartElement(Constants.XML.ATTRIBUTE);
            writer.writeAttribute("name", item.asString());
            writer.writeEndElement();
        }
        writer.writeEndElement();
    }

    void writeRoleMappingsProvider(final XMLExtendedStreamWriter writer, final ModelNode model) throws XMLStreamException {
        ModelNode providerId = model.get(Constants.Model.ROLE_MAPPINGS_PROVIDER_ID);
        if (!providerId.isDefined()) {
            return;
        }
        writer.writeStartElement(Constants.XML.ROLE_MAPPINGS_PROVIDER);
        writer.writeAttribute(Constants.XML.ID, providerId.asString());
        ServiceProviderDefinition.ROLE_MAPPINGS_PROVIDER_CONFIG.marshallAsElement(model, false, writer);
        writer.writeEndElement();
    }

    void writePrincipalNameMapping(XMLExtendedStreamWriter writer, ModelNode model) throws XMLStreamException {

        ModelNode policy = model.get(Constants.Model.PRINCIPAL_NAME_MAPPING_POLICY);
        ModelNode mappingAttribute = model.get(Constants.Model.PRINCIPAL_NAME_MAPPING_ATTRIBUTE_NAME);
        if (!policy.isDefined() && !mappingAttribute.isDefined()) {
            return;
        }
        writer.writeStartElement(Constants.XML.PRINCIPAL_NAME_MAPPING);
        if (policy.isDefined()) {
            writer.writeAttribute(Constants.XML.PRINCIPAL_NAME_MAPPING_POLICY, policy.asString());
        }
        if (mappingAttribute.isDefined()) {
            writer.writeAttribute(Constants.XML.PRINCIPAL_NAME_MAPPING_ATTRIBUTE_NAME, mappingAttribute.asString());
        }
        writer.writeEndElement();
    }
}
