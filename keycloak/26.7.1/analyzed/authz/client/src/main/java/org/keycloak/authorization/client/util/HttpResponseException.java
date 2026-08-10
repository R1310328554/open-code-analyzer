/*
 *  Copyright 2016 Red Hat, Inc. and/or its affiliates
 *  and other contributors as indicated by the @author tags.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
package org.keycloak.authorization.client.util;

/**
 * <p>HTTP 响应状态码不在 2xx 范围时抛出的运行时异常。
 *
 * <p>携带状态码、原因短语及原始响应体字节，便于调用方诊断授权服务器错误。
 *
 * @author <a href="mailto:psilva@redhat.com">Pedro Igor</a>
 */
public class HttpResponseException extends RuntimeException {

    private final int statusCode;
    private final String reasonPhrase;
    private final byte[] bytes;

    /** 构造包含完整 HTTP 错误上下文的异常。 */
    public HttpResponseException(String message, int statusCode, String reasonPhrase, byte[] bytes) {
        super(message);
        this.statusCode = statusCode;
        this.reasonPhrase = reasonPhrase;
        this.bytes = bytes;
    }

    /** 返回 HTTP 状态码。 */
    public int getStatusCode() {
        return statusCode;
    }

    /** 返回 HTTP 原因短语。 */
    public String getReasonPhrase() {
        return reasonPhrase;
    }

    /** 返回响应体原始字节。 */
    public byte[] getBytes() {
        return bytes;
    }

    /** 若存在响应体，则在异常信息中附加服务器返回内容。 */
    @Override
    public String toString() {
        if (bytes != null) {
            return new StringBuilder(super.toString()).append(" / Response from server: ").append(new String(bytes)).toString();
        }
        return super.toString();
    }
}
