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
package org.keycloak.dom.saml.v1.assertion;

/**
 * <complexType name="SubjectLocalityType">
 * SAML 1.1 主体 locality DOM 类型：记录认证时主体的 IP 与 DNS 地址（可选）。
 <attribute name="IPAddress" type="string" use="optional"/> <attribute
 * name="DNSAddress" type="string" use="optional"/> </complexType>
 *
 * @author Anil.Saldhana@redhat.com
 * @since Jun 22, 2011
 */
public class SAML11SubjectLocalityType {

    /** 主体 IP 地址。 */
    protected String ipAddress;

    /** 主体 DNS 主机名。 */
    protected String dnsAddress;

    /** 返回 IP 地址。 */
    public String getIpAddress() {
        return ipAddress;
    }

    /** 设置 IP 地址。 */
    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    /** 返回 DNS 地址。 */
    public String getDnsAddress() {
        return dnsAddress;
    }

    /** 设置 DNS 地址。 */
    public void setDnsAddress(String dnsAddress) {
        this.dnsAddress = dnsAddress;
    }
}