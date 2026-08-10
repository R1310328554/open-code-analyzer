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
package org.keycloak.saml.common.exceptions;

import java.security.GeneralSecurityException;
import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamException;

/**
 * SAML/XML 解析失败时抛出的通用异常，可携带 StAX 解析位置信息。
 * General Exception indicating parsing exception
 *
 * @author Anil.Saldhana@redhat.com
 * @since May 22, 2009
 */
public class ParsingException extends GeneralSecurityException {

    /** XML 流解析出错位置（若有）。 */
    private Location location;

    /** 构造无消息的 ParsingException。 */
    public ParsingException() {
        super();
    }

    /**
     * 构造带消息与原因的 ParsingException。
     *
     * @param message 异常描述
     * @param cause 原始异常
     */
    public ParsingException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * 构造带消息的 ParsingException。
     *
     * @param message 异常描述
     */
    public ParsingException(String message) {
        super(message);
    }

    /**
     * 构造仅包装原因的 ParsingException。
     *
     * @param cause 原始异常
     */
    public ParsingException(Throwable cause) {
        super(cause);
    }

    /**
     * 从 {@link XMLStreamException} 构造，并记录解析位置。
     *
     * @param xmle StAX 解析异常
     */
    public ParsingException(XMLStreamException xmle) {
        super(xmle);
        location = xmle.getLocation();
    }

    /** 返回 XML 解析出错位置。 */
    public Location getLocation() {
        return location;
    }

    @Override
    public String toString() {
        return "ParsingException [location=" + location + "]" + super.toString();
    }
}
