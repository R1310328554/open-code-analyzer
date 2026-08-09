/*
 * Copyright 2014 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package io.netty.handler.codec.http;

import io.netty.util.internal.StringUtil;

import java.util.Map;

/**
 * HTTP 消息 {@code toString()} 格式化工具（包内使用）。
 * <p>
 * 将请求/响应/Full 消息格式化为可读的多行文本，含解码结果、版本、头与正文摘要。
 */
final class HttpMessageUtil {

    /** 将 {@link HttpRequest} 追加到 StringBuilder。 */
    static StringBuilder appendRequest(StringBuilder buf, HttpRequest req) {
        appendCommon(buf, req);
        appendInitialLine(buf, req);
        appendHeaders(buf, req.headers());
        removeLastNewLine(buf);
        return buf;
    }

    /** 将 {@link HttpResponse} 追加到 StringBuilder。 */
    static StringBuilder appendResponse(StringBuilder buf, HttpResponse res) {
        appendCommon(buf, res);
        appendInitialLine(buf, res);
        appendHeaders(buf, res.headers());
        removeLastNewLine(buf);
        return buf;
    }

    private static void appendCommon(StringBuilder buf, HttpMessage msg) {
        buf.append(StringUtil.simpleClassName(msg));
        buf.append("(decodeResult: ");
        buf.append(msg.decoderResult());
        buf.append(", version: ");
        buf.append(msg.protocolVersion());
        buf.append(')');
        buf.append(StringUtil.NEWLINE);
    }

    /** 将 {@link FullHttpRequest} 追加到 StringBuilder。 */
    static StringBuilder appendFullRequest(StringBuilder buf, FullHttpRequest req) {
        appendFullCommon(buf, req);
        appendInitialLine(buf, req);
        appendHeaders(buf, req.headers());
        appendHeaders(buf, req.trailingHeaders());
        removeLastNewLine(buf);
        return buf;
    }

    /** 将 {@link FullHttpResponse} 追加到 StringBuilder。 */
    static StringBuilder appendFullResponse(StringBuilder buf, FullHttpResponse res) {
        appendFullCommon(buf, res);
        appendInitialLine(buf, res);
        appendHeaders(buf, res.headers());
        appendHeaders(buf, res.trailingHeaders());
        removeLastNewLine(buf);
        return buf;
    }

    private static void appendFullCommon(StringBuilder buf, FullHttpMessage msg) {
        buf.append(StringUtil.simpleClassName(msg));
        buf.append("(decodeResult: ");
        buf.append(msg.decoderResult());
        buf.append(", version: ");
        buf.append(msg.protocolVersion());
        buf.append(", content: ");
        buf.append(msg.content());
        buf.append(')');
        buf.append(StringUtil.NEWLINE);
    }

    /** 追加请求起始行（METHOD URI VERSION）。 */
    private static void appendInitialLine(StringBuilder buf, HttpRequest req) {
        buf.append(req.method());
        buf.append(' ');
        buf.append(req.uri());
        buf.append(' ');
        buf.append(req.protocolVersion());
        buf.append(StringUtil.NEWLINE);
    }

    /** 追加响应起始行（VERSION STATUS）。 */
    private static void appendInitialLine(StringBuilder buf, HttpResponse res) {
        buf.append(res.protocolVersion());
        buf.append(' ');
        buf.append(res.status());
        buf.append(StringUtil.NEWLINE);
    }

    private static void appendHeaders(StringBuilder buf, HttpHeaders headers) {
        for (Map.Entry<String, String> e: headers) {
            buf.append(e.getKey());
            buf.append(": ");
            buf.append(e.getValue());
            buf.append(StringUtil.NEWLINE);
        }
    }

    /** 移除末尾多余换行。 */
    private static void removeLastNewLine(StringBuilder buf) {
        buf.setLength(buf.length() - StringUtil.NEWLINE.length());
    }

    private HttpMessageUtil() { }
}
