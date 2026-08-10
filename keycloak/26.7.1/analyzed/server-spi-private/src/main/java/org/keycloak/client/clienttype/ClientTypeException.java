/*
 * Copyright 2021 Red Hat, Inc. and/or its affiliates
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
 *
 */

package org.keycloak.client.clienttype;

import org.keycloak.models.ModelException;

/**
 * 客户端类型配置与校验失败时抛出的 {@link ModelException} 子类。
 * <p>通过 {@link Message} 枚举统一错误消息，便于服务层一致处理。</p>
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class ClientTypeException extends ModelException {

    private ClientTypeException(String message, Object... parameters) {
        super(message, parameters);
    }

    /** 客户端类型相关错误消息枚举。 */
    public enum Message {
        /** 集中注册客户端类型异常消息，保证各服务文案一致。 */
        INVALID_CLIENT_TYPE("Invalid client type"),
        CANNOT_CHANGE_CLIENT_TYPE("Not supported to change client type"),
        INVALID_CLIENT_TYPE_PROVIDER("Did not find client type provider"),
        CLIENT_TYPE_FIELD_NOT_APPLICABLE("Invalid configuration of 'applicable' property on client type"),
        INVALID_CLIENT_TYPE_CONFIGURATION("Invalid configuration of property on client type"),
        DUPLICATE_CLIENT_TYPE("Duplicated client type name"),
        CLIENT_UPDATE_FAILED_CLIENT_TYPE_VALIDATION("Cannot change property of client as it is not allowed by the specified client type."),
        CLIENT_TYPE_NOT_FOUND("Client type not found."),
        CLIENT_TYPE_FAILED_TO_LOAD("Failed to load client type.");

        private final String message;

        Message(String message) {
            this.message = message;
        }

        /** 以当前消息模板构造 {@link ClientTypeException}。 */
        public ClientTypeException exception(Object... parameters) {
            return new ClientTypeException(message, parameters);
        }

        public String getMessage() {
            return message;
        }
    }
}
