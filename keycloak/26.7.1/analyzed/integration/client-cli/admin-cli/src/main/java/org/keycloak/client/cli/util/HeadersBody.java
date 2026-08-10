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
package org.keycloak.client.cli.util;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.Charset;

import org.apache.http.entity.ContentType;

import static org.keycloak.client.cli.util.IoUtil.copyStream;

/**
 * HTTP 请求或响应的「头 + 可选正文」封装。
 * <p>
 * 提供将正文读为字符串/字节数组及按 Content-Type 推断字符集的能力。
 *
 * @author <a href="mailto:mstrukel@redhat.com">Marko Strukelj</a>
 */
public class HeadersBody {

    /** 关联的 HTTP 头集合。 */
    private Headers headers;
    /** 正文输入流，可为 {@code null}。 */
    private InputStream body;


    /** 仅含头的构造器（无正文）。 */
    public HeadersBody(Headers headers) {
        this.headers = headers;
    }

    /**
     * 含头与正文的构造器。
     *
     * @param headers HTTP 头
     * @param body 正文流
     */
    public HeadersBody(Headers headers, InputStream body) {
        this.headers = headers;
        this.body = body;
    }

    /** 返回 HTTP 头集合。 */
    public Headers getHeaders() {
        return headers;
    }

    /** 返回正文输入流。 */
    public InputStream getBody() {
        return body;
    }

    /** 读取全部正文并按 Content-Type 字符集解码为字符串。 */
    public String readBodyString() {
        byte [] buffer = readBodyBytes();
        return new String(buffer, getContentCharset());
    }

    /** 读取全部正文为字节数组。 */
    public byte[] readBodyBytes() {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        copyStream(getBody(), os);
        return os.toByteArray();
    }

    /**
     * 从 {@code Content-Type} 解析字符集，缺省为 ISO-8859-1。
     *
     * @return 正文字符集
     */
    public Charset getContentCharset() {
        return headers.getContentType().map(ContentType::getCharset).orElseGet(() -> Charset.forName("iso-8859-1"));
    }

}
