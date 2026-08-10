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

package org.keycloak.protocol;

/**
 * 协议映射器配置无效时抛出的受检异常，可携带 i18n 消息键与参数。
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class ProtocolMapperConfigException extends Exception {

    /** 国际化消息键。 */
    private String messageKey;
    /** 消息模板占位参数。 */
    private Object[] parameters;

    /** @param message 错误描述 */
    public ProtocolMapperConfigException(String message) {
        super(message);
    }

    /** @param message 错误描述
     * @param messageKey 国际化消息键 */
    public ProtocolMapperConfigException(String message, String messageKey) {
        super(message);
        this.messageKey = messageKey;
    }

    public ProtocolMapperConfigException(String message, Throwable cause) {
        super(message, cause);
    }

    public ProtocolMapperConfigException(String message, String messageKey, Throwable cause) {
        super(message, cause);
        this.messageKey = messageKey;
    }

    public ProtocolMapperConfigException(String message, Object ... parameters) {
        super(message);
        this.parameters = parameters;
    }

    public ProtocolMapperConfigException(String messageKey, String message, Object ... parameters) {
        super(message);
        this.messageKey = messageKey;
        this.parameters = parameters;
    }

    /** @return 国际化消息键 */
    public String getMessageKey() {
        return messageKey;
    }

    /** @return 消息模板参数数组 */
    public Object[] getParameters() {
        return parameters;
    }

    /** @param parameters 消息模板参数 */
    public void setParameters(Object[] parameters) {
        this.parameters = parameters;
    }

}
