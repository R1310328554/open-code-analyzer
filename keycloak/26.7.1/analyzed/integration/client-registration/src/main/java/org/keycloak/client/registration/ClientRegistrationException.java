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

package org.keycloak.client.registration;

/**
 * 客户端注册 API 调用失败时抛出的受检异常。
 * <p>
 * 封装 HTTP 层错误、JSON 序列化/反序列化失败及连接异常等场景，
 * 供 {@link ClientRegistration} 及其调用方统一处理。
 * </p>
 *
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
public class ClientRegistrationException extends Exception {

    /**
     * @param s 错误描述
     * @param throwable 根因
     */
    public ClientRegistrationException(String s, Throwable throwable) {
        super(s, throwable);
    }

    /**
     * @param s 错误描述
     */
    public ClientRegistrationException(String s) {
        super(s);
    }

}
