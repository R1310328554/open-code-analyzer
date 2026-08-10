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

package org.keycloak.services.clientregistration;

/**
 * 客户端注册过程中抛出的运行时异常。
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class ClientRegistrationException extends RuntimeException {

    /** 无参构造 */
    public ClientRegistrationException() {
        super();
    }

    /** @param message 异常消息 */
    public ClientRegistrationException(String message) {
        super(message);
    }

    /** @param throwable 原因异常 */
    public ClientRegistrationException(Throwable throwable) {
        super(throwable);
    }

    /** @param message 异常消息；@param throwable 原因异常 */
    public ClientRegistrationException(String message, Throwable throwable) {
        super(message, throwable);
    }
}
