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
package org.keycloak.saml.processing.core.saml.v2.common;

import org.keycloak.dom.saml.v2.SAML2Object;

import org.w3c.dom.Document;

/**
 * 同时持有 SAML 领域对象及其对应 DOM 文档的容器类。
 * <p>调用方需通过 {@link ThreadLocal} 等方式保证线程安全，每线程独立实例。</p>
 *
 * @author Anil.Saldhana@redhat.com
 * @since Aug 13, 2009
 */
public class SAMLDocumentHolder {

    /** SAML 2.0 领域对象。 */
    private SAML2Object samlObject;
    /** 对应的 W3C DOM 文档。 */
    private Document samlDocument;

    /** 仅持有 SAML 对象的构造器。 */
    public SAMLDocumentHolder(SAML2Object samlObject) {
        this.samlObject = samlObject;
    }

    /** 仅持有 DOM 文档的构造器。 */
    public SAMLDocumentHolder(Document samlDocument) {
        this.samlDocument = samlDocument;
    }

    /** 同时持有 SAML 对象与 DOM 文档的构造器。 */
    public SAMLDocumentHolder(SAML2Object samlObject, Document samlDocument) {
        this.samlObject = samlObject;
        this.samlDocument = samlDocument;
    }

    /** 返回 SAML 领域对象。 */
    public SAML2Object getSamlObject() {
        return samlObject;
    }

    /** 设置 SAML 领域对象。 */
    public void setSamlObject(SAML2Object samlObject) {
        this.samlObject = samlObject;
    }

    /** 返回 SAML DOM 文档。 */
    public Document getSamlDocument() {
        return samlDocument;
    }

    /** 设置 SAML DOM 文档。 */
    public void setSamlDocument(Document samlDocument) {
        this.samlDocument = samlDocument;
    }
}