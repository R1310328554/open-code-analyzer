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

package org.keycloak.adapters.cloned;

/**
 * 适配器 HTTP 客户端操作异常。
 *
 * <p>在下载 SAML 描述符或执行 HTTP 请求失败时抛出。</p>
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class HttpClientAdapterException extends Exception {

    /**
     * 构造带消息的异常。
     *
     * @param message 错误描述
     */
    public HttpClientAdapterException(String message) {
        super(message);
    }

    /**
     * 构造带消息及根因的异常。
     *
     * @param message 错误描述
     * @param t 根因
     */
    public HttpClientAdapterException(String message, Throwable t) {
        super(message, t);
    }
}
