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
package org.keycloak.dom.saml.v2.assertion;

import java.io.Serializable;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * <p>
 * Java class for AudienceRestrictionType complex type.
 * SAML 2.0 受众限制条件：限定断言可被哪些受众 URI 接受。

 *
 * <p>
 * The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="AudienceRestrictionType">
 *   &lt;complexContent>
 *     &lt;extension base="{urn:oasis:names:tc:SAML:2.0:assertion}ConditionAbstractType">
 *       &lt;sequence>
 *         &lt;element ref="{urn:oasis:names:tc:SAML:2.0:assertion}Audience" maxOccurs="unbounded"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
public class AudienceRestrictionType extends ConditionAbstractType implements Serializable {

    protected List<URI> audience = new ArrayList<>();

    /**
     * 添加一个受众 URI。
     *
     * Add an audience
     *
     * @param audienceval
     */
    public void addAudience(URI audienceval) {
        audience.add(audienceval);
    }

    /**
     * 移除一个受众 URI。
     *
     * Remove an audience
     *
     * @param audienceval
     */
    public void removeAudience(URI audienceval) {
        audience.remove(audienceval);
    }

    /**
     * 获取受众 URI 列表（只读）。
     *
     * Gets the value of the audience property.
     */
    public List<URI> getAudience() {
        return Collections.unmodifiableList(this.audience);
    }
}