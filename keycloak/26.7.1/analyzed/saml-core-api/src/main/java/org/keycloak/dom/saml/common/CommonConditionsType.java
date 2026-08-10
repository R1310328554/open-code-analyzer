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
package org.keycloak.dom.saml.common;

import java.io.Serializable;
import javax.xml.datatype.XMLGregorianCalendar;

/**
 * SAML 条件基类，定义断言有效的时间窗口（NotBefore / NotOnOrAfter）。
 *
 * @author Anil.Saldhana@redhat.com
 * @since Jun 22, 2011
 */
public class CommonConditionsType implements Serializable {

    protected XMLGregorianCalendar notBefore;

    protected XMLGregorianCalendar notOnOrAfter;

    /**
     * 获取条件生效起始时间（NotBefore）。
     *
     * @return 可能的值为 {@link XMLGregorianCalendar }
     */
    public XMLGregorianCalendar getNotBefore() {
        return notBefore;
    }

    /**
     * 设置条件生效起始时间（NotBefore）。
     *
     * @param value 允许的值为 {@link XMLGregorianCalendar }
     */
    public void setNotBefore(XMLGregorianCalendar value) {
        this.notBefore = value;
    }

    /**
     * 获取条件失效时间（NotOnOrAfter，含该时刻即失效）。
     *
     * @return 可能的值为 {@link XMLGregorianCalendar }
     */
    public XMLGregorianCalendar getNotOnOrAfter() {
        return notOnOrAfter;
    }

    /**
     * 设置条件失效时间（NotOnOrAfter）。
     *
     * @param value 允许的值为 {@link XMLGregorianCalendar }
     */
    public void setNotOnOrAfter(XMLGregorianCalendar value) {
        this.notOnOrAfter = value;
    }
}