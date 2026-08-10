/*
 * Copyright 2014 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License, version 2.0 (the
 * "License"); you may not use this file except in compliance with the License. You may obtain a
 * copy of the License at:
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package io.netty.handler.codec.http2;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.handler.codec.UnsupportedValueConverter;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.DefaultHttpRequest;
import io.netty.handler.codec.http.DefaultHttpResponse;
import io.netty.handler.codec.http.FullHttpMessage;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMessage;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpUtil;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.util.AsciiString;
import io.netty.util.internal.InternalThreadLocalMap;
import io.netty.util.internal.StringUtil;

import java.net.URI;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import static io.netty.handler.codec.http.HttpHeaderNames.CONNECTION;
import static io.netty.handler.codec.http.HttpHeaderNames.COOKIE;
import static io.netty.handler.codec.http.HttpHeaderNames.TE;
import static io.netty.handler.codec.http.HttpHeaderValues.TRAILERS;
import static io.netty.handler.codec.http.HttpResponseStatus.parseLine;
import static io.netty.handler.codec.http.HttpScheme.HTTP;
import static io.netty.handler.codec.http.HttpScheme.HTTPS;
import static io.netty.handler.codec.http.HttpUtil.isAsteriskForm;
import static io.netty.handler.codec.http.HttpUtil.isOriginForm;
import static io.netty.handler.codec.http2.Http2Error.PROTOCOL_ERROR;
import static io.netty.handler.codec.http2.Http2Exception.connectionError;
import static io.netty.handler.codec.http2.Http2Exception.streamError;
import static io.netty.util.AsciiString.EMPTY_STRING;
import static io.netty.util.AsciiString.contentEqualsIgnoreCase;
import static io.netty.util.AsciiString.indexOf;
import static io.netty.util.AsciiString.trim;
import static io.netty.util.ByteProcessor.FIND_COMMA;
import static io.netty.util.ByteProcessor.FIND_SEMI_COLON;
import static io.netty.util.internal.ObjectUtil.checkNotNull;
import static io.netty.util.internal.StringUtil.isNullOrEmpty;
import static io.netty.util.internal.StringUtil.unescapeCsvFields;

/**
 * HTTP/1.x 与 HTTP/2 双向转换的工具类与常量。
 * <p>负责伪头部映射、Connection/TE/Cookie 等特殊规则，以及在 {@link HttpObject} 上携带流 ID 等扩展头。
 */
public final class HttpConversionUtil {
    // 路径/查询解析逻辑改编自 Vert.x HttpUtils（见下方链接）
    // Parsing logic adapted from Vert.x HttpUtils.parsePath/parseQuery:
    // https://github.com/eclipse-vertx/vert.x/blob/98a8ef6c8b408009ff86eb8277fd0bbb2b866857/
    // vertx-core/src/main/java/io/vertx/core/http/impl/HttpUtils.java#L279-L319
    /**
     * HTTP → HTTP/2 时不应直接复制的头部黑名单（由伪头部或连接语义替代）。
     */
    private static final CharSequenceMap<AsciiString> HTTP_TO_HTTP2_HEADER_BLACKLIST =
            new CharSequenceMap<AsciiString>();
    static {
        HTTP_TO_HTTP2_HEADER_BLACKLIST.add(CONNECTION, EMPTY_STRING);
        @SuppressWarnings("deprecation")
        AsciiString keepAlive = HttpHeaderNames.KEEP_ALIVE;
        HTTP_TO_HTTP2_HEADER_BLACKLIST.add(keepAlive, EMPTY_STRING);
        @SuppressWarnings("deprecation")
        AsciiString proxyConnection = HttpHeaderNames.PROXY_CONNECTION;
        HTTP_TO_HTTP2_HEADER_BLACKLIST.add(proxyConnection, EMPTY_STRING);
        HTTP_TO_HTTP2_HEADER_BLACKLIST.add(HttpHeaderNames.TRANSFER_ENCODING, EMPTY_STRING);
        HTTP_TO_HTTP2_HEADER_BLACKLIST.add(HttpHeaderNames.HOST, EMPTY_STRING);
        HTTP_TO_HTTP2_HEADER_BLACKLIST.add(HttpHeaderNames.UPGRADE, EMPTY_STRING);
        HTTP_TO_HTTP2_HEADER_BLACKLIST.add(ExtensionHeaderNames.STREAM_ID.text(), EMPTY_STRING);
        HTTP_TO_HTTP2_HEADER_BLACKLIST.add(ExtensionHeaderNames.SCHEME.text(), EMPTY_STRING);
        HTTP_TO_HTTP2_HEADER_BLACKLIST.add(ExtensionHeaderNames.PATH.text(), EMPTY_STRING);
    }

    /**
     * RFC 7540 §8.1 消息流之外生成的 {@link HttpRequest} 使用的占位方法。
     */
    public static final HttpMethod OUT_OF_MESSAGE_SEQUENCE_METHOD = HttpMethod.OPTIONS;

    /**
     * 同上场景下占位请求的路径。
     */
    public static final String OUT_OF_MESSAGE_SEQUENCE_PATH = "";

    /**
     * 同上场景下占位响应的状态码。
     */
    public static final HttpResponseStatus OUT_OF_MESSAGE_SEQUENCE_RETURN_CODE = HttpResponseStatus.OK;

    /**
     * RFC 7540 §8.1.2.3：:path 不得为空，空路径应规范化为 {@code /}。
     */
    private static final AsciiString EMPTY_REQUEST_PATH = AsciiString.cached("/");

    private HttpConversionUtil() {
    }

    /**
     * 在 HTTP/1.x {@link HttpObject} 上携带 HTTP/2 语义信息的扩展头名称。
     */
    public enum ExtensionHeaderNames {
        /**
         * 标识生成该 {@code HttpObject} 的 HTTP/2 流 ID。
         * <p>
         * {@code "x-http2-stream-id"}
         */
        STREAM_ID("x-http2-stream-id"),
        /**
         * 对应 :scheme 伪头部的值。
         * <p>
         * {@code "x-http2-scheme"}
         */
        SCHEME("x-http2-scheme"),
        /**
         * 对应 :path 伪头部的值。
         * <p>
         * {@code "x-http2-path"}
         */
        PATH("x-http2-path"),
        /**
         * PUSH_PROMISE 关联的父流 ID。
         * <p>
         * {@code "x-http2-stream-promise-id"}
         */
        STREAM_PROMISE_ID("x-http2-stream-promise-id"),
        /**
         * 该流所依赖的父流 ID（优先级树）。
         * <p>
         * {@code "x-http2-stream-dependency-id"}
         */
        STREAM_DEPENDENCY_ID("x-http2-stream-dependency-id"),
        /**
         * 流优先级权重（非默认值时携带）。
         * <p>
         * {@code "x-http2-stream-weight"}
         */
        STREAM_WEIGHT("x-http2-stream-weight");

        private final AsciiString text;

        ExtensionHeaderNames(String text) {
            this.text = AsciiString.cached(text);
        }

        public AsciiString text() {
            return text;
        }
    }

    /**
     * 按 HTTP/2 规则将状态码文本解析为 {@link HttpResponseStatus}（禁止 101 Switching Protocols）。
     *
     * @param status The status from an HTTP/2 frame
     * @return The HTTP/1.x status
     * @throws Http2Exception If there is a problem translating from HTTP/2 to HTTP/1.x
     */
    public static HttpResponseStatus parseStatus(CharSequence status) throws Http2Exception {
        HttpResponseStatus result;
        try {
            result = parseLine(status);
            if (result == HttpResponseStatus.SWITCHING_PROTOCOLS) {
                throw connectionError(PROTOCOL_ERROR, "Invalid HTTP/2 status code '%d'", result.code());
            }
        } catch (Http2Exception e) {
            throw e;
        } catch (Throwable t) {
            throw connectionError(PROTOCOL_ERROR, t,
                            "Unrecognized HTTP status code '%s' encountered in translation to HTTP/1.x", status);
        }
        return result;
    }

    /**
     * Create a new object to contain the response data
     *
     * @param streamId The stream associated with the response
     * @param http2Headers The initial set of HTTP/2 headers to create the response with
     * @param alloc The {@link ByteBufAllocator} to use to generate the content of the message
     * @param validateHttpHeaders <ul>
     *        <li>{@code true} to validate HTTP headers in the http-codec</li>
     *        <li>{@code false} not to validate HTTP headers in the http-codec</li>
     *        </ul>
     * @return A new response object which represents headers/data
     * @throws Http2Exception see {@link #addHttp2ToHttpHeaders(int, Http2Headers, FullHttpMessage, boolean)}
     */
    public static FullHttpResponse toFullHttpResponse(int streamId, Http2Headers http2Headers, ByteBufAllocator alloc,
                                                      boolean validateHttpHeaders) throws Http2Exception {
        return toFullHttpResponse(streamId, http2Headers, alloc.buffer(), validateHttpHeaders);
    }

    /**
     * Create a new object to contain the response data
     *
     * @param streamId The stream associated with the response
     * @param http2Headers The initial set of HTTP/2 headers to create the response with
     * @param content {@link ByteBuf} content to put in {@link FullHttpResponse}
     * @param validateHttpHeaders <ul>
     *        <li>{@code true} to validate HTTP headers in the http-codec</li>
     *        <li>{@code false} not to validate HTTP headers in the http-codec</li>
     *        </ul>
     * @return A new response object which represents headers/data
     * @throws Http2Exception see {@link #addHttp2ToHttpHeaders(int, Http2Headers, FullHttpMessage, boolean)}
     */
    public static FullHttpResponse toFullHttpResponse(int streamId, Http2Headers http2Headers, ByteBuf content,
                                                      boolean validateHttpHeaders)
                    throws Http2Exception {
        HttpResponseStatus status = parseStatus(http2Headers.status());
        // HTTP/2 无 status-line，版本与 reason phrase 统一设为 HTTP/1.1 + 状态码
        FullHttpResponse msg = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, status, content,
                                                           validateHttpHeaders);
        try {
            addHttp2ToHttpHeaders(streamId, http2Headers, msg, false);
        } catch (Http2Exception e) {
            msg.release();
            throw e;
        } catch (Throwable t) {
            msg.release();
            throw streamError(streamId, PROTOCOL_ERROR, t, "HTTP/2 to HTTP/1.x headers conversion error");
        }
        return msg;
    }

    /**
     * Create a new object to contain the request data
     *
     * @param streamId The stream associated with the request
     * @param http2Headers The initial set of HTTP/2 headers to create the request with
     * @param alloc The {@link ByteBufAllocator} to use to generate the content of the message
     * @param validateHttpHeaders <ul>
     *        <li>{@code true} to validate HTTP headers in the http-codec</li>
     *        <li>{@code false} not to validate HTTP headers in the http-codec</li>
     *        </ul>
     * @return A new request object which represents headers/data
     * @throws Http2Exception see {@link #addHttp2ToHttpHeaders(int, Http2Headers, FullHttpMessage, boolean)}
     */
    public static FullHttpRequest toFullHttpRequest(int streamId, Http2Headers http2Headers, ByteBufAllocator alloc,
                                                    boolean validateHttpHeaders) throws Http2Exception {
        return toFullHttpRequest(streamId, http2Headers, alloc.buffer(), validateHttpHeaders);
    }

    private static String extractPath(CharSequence method, Http2Headers headers) {
        if (HttpMethod.CONNECT.asciiName().contentEqualsIgnoreCase(method)) {
            // CONNECT 的请求目标即 :authority，见 RFC 7231 §4.3.6
            return checkNotNull(headers.authority(),
                    "authority header cannot be null in the conversion to HTTP/1.x").toString();
        } else {
            return checkNotNull(headers.path(),
                    "path header cannot be null in conversion to HTTP/1.x").toString();
        }
    }

    /**
     * Create a new object to contain the request data
     *
     * @param streamId The stream associated with the request
     * @param http2Headers The initial set of HTTP/2 headers to create the request with
     * @param content {@link ByteBuf} content to put in {@link FullHttpRequest}
     * @param validateHttpHeaders <ul>
     *        <li>{@code true} to validate HTTP headers in the http-codec</li>
     *        <li>{@code false} not to validate HTTP headers in the http-codec</li>
     *        </ul>
     * @return A new request object which represents headers/data
     * @throws Http2Exception see {@link #addHttp2ToHttpHeaders(int, Http2Headers, FullHttpMessage, boolean)}
     */
    public static FullHttpRequest toFullHttpRequest(int streamId, Http2Headers http2Headers, ByteBuf content,
                                                boolean validateHttpHeaders) throws Http2Exception {
        // HTTP/2 不携带 HTTP/1.1 请求行中的版本字段
        final CharSequence method = checkNotNull(http2Headers.method(),
                "method header cannot be null in conversion to HTTP/1.x");
        final CharSequence path = extractPath(method, http2Headers);
        FullHttpRequest msg = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.valueOf(method
                        .toString()), path.toString(), content, validateHttpHeaders);
        try {
            addHttp2ToHttpHeaders(streamId, http2Headers, msg, false);
        } catch (Http2Exception e) {
            msg.release();
            throw e;
        } catch (Throwable t) {
            msg.release();
            throw streamError(streamId, PROTOCOL_ERROR, t, "HTTP/2 to HTTP/1.x headers conversion error");
        }
        return msg;
    }

    /**
     * Create a new object to contain the request data.
     *
     * @param streamId The stream associated with the request
     * @param http2Headers The initial set of HTTP/2 headers to create the request with
     * @param validateHttpHeaders <ul>
     *        <li>{@code true} to validate HTTP headers in the http-codec</li>
     *        <li>{@code false} not to validate HTTP headers in the http-codec</li>
     *        </ul>
     * @return A new request object which represents headers for a chunked request
     * @throws Http2Exception see {@link #addHttp2ToHttpHeaders(int, Http2Headers, FullHttpMessage, boolean)}
     */
    public static HttpRequest toHttpRequest(int streamId, Http2Headers http2Headers, boolean validateHttpHeaders)
                    throws Http2Exception {
        // HTTP/2 不携带 HTTP/1.1 请求行中的版本字段
        final CharSequence method = checkNotNull(http2Headers.method(),
                "method header cannot be null in conversion to HTTP/1.x");
        final CharSequence path = extractPath(method, http2Headers);
        HttpRequest msg = new DefaultHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.valueOf(method.toString()),
                path.toString(), validateHttpHeaders);
        try {
            addHttp2ToHttpHeaders(streamId, http2Headers, msg.headers(), msg.protocolVersion(), false, true);
        } catch (Http2Exception e) {
            throw e;
        } catch (Throwable t) {
            throw streamError(streamId, PROTOCOL_ERROR, t, "HTTP/2 to HTTP/1.x headers conversion error");
        }
        return msg;
    }

    /**
     * Create a new object to contain the response data.
     *
     * @param streamId The stream associated with the response
     * @param http2Headers The initial set of HTTP/2 headers to create the response with
     * @param validateHttpHeaders <ul>
     *        <li>{@code true} to validate HTTP headers in the http-codec</li>
     *        <li>{@code false} not to validate HTTP headers in the http-codec</li>
     *        </ul>
     * @return A new response object which represents headers for a chunked response
     * @throws Http2Exception see {@link #addHttp2ToHttpHeaders(int, Http2Headers,
     *         HttpHeaders, HttpVersion, boolean, boolean)}
     */
    public static HttpResponse toHttpResponse(final int streamId,
                                              final Http2Headers http2Headers,
                                              final boolean validateHttpHeaders) throws Http2Exception {
        final HttpResponseStatus status = parseStatus(http2Headers.status());
        // HTTP/2 无 status-line 的版本与 reason phrase
        final HttpResponse msg = new DefaultHttpResponse(HttpVersion.HTTP_1_1, status, validateHttpHeaders);
        try {
            addHttp2ToHttpHeaders(streamId, http2Headers, msg.headers(), msg.protocolVersion(), false, false);
        } catch (final Http2Exception e) {
            throw e;
        } catch (final Throwable t) {
            throw streamError(streamId, PROTOCOL_ERROR, t, "HTTP/2 to HTTP/1.x headers conversion error");
        }
        return msg;
    }

    /**
     * 将 HTTP/2 头部翻译并写入 HTTP/1.x {@link FullHttpMessage}（初始头或 trailer）。
     *
     * @param streamId The stream associated with {@code sourceHeaders}.
     * @param inputHeaders The HTTP/2 headers to convert.
     * @param destinationMessage The object which will contain the resulting HTTP/1.x headers.
     * @param addToTrailer {@code true} to add to trailing headers. {@code false} to add to initial headers.
     * @throws Http2Exception If not all HTTP/2 headers can be translated to HTTP/1.x.
     * @see #addHttp2ToHttpHeaders(int, Http2Headers, HttpHeaders, HttpVersion, boolean, boolean)
     */
    public static void addHttp2ToHttpHeaders(int streamId, Http2Headers inputHeaders,
                    FullHttpMessage destinationMessage, boolean addToTrailer) throws Http2Exception {
        addHttp2ToHttpHeaders(streamId, inputHeaders,
                addToTrailer ? destinationMessage.trailingHeaders() : destinationMessage.headers(),
                destinationMessage.protocolVersion(), addToTrailer, destinationMessage instanceof HttpRequest);
    }

    /**
     * Translate and add HTTP/2 headers to HTTP/1.x headers.
     *
     * @param streamId The stream associated with {@code sourceHeaders}.
     * @param inputHeaders The HTTP/2 headers to convert.
     * @param outputHeaders The object which will contain the resulting HTTP/1.x headers..
     * @param httpVersion What HTTP/1.x version {@code outputHeaders} should be treated as when doing the conversion.
     * @param isTrailer {@code true} if {@code outputHeaders} should be treated as trailing headers.
     * {@code false} otherwise.
     * @param isRequest {@code true} if the {@code outputHeaders} will be used in a request message.
     * {@code false} for response message.
     * @throws Http2Exception If not all HTTP/2 headers can be translated to HTTP/1.x.
     */
    public static void addHttp2ToHttpHeaders(int streamId, Http2Headers inputHeaders, HttpHeaders outputHeaders,
            HttpVersion httpVersion, boolean isTrailer, boolean isRequest) throws Http2Exception {
        Http2ToHttpHeaderTranslator translator = new Http2ToHttpHeaderTranslator(streamId, outputHeaders, isRequest);
        try {
            translator.translateHeaders(inputHeaders);
        } catch (Http2Exception ex) {
            throw ex;
        } catch (Throwable t) {
            throw streamError(streamId, PROTOCOL_ERROR, t, "HTTP/2 to HTTP/1.x headers conversion error");
        }

        outputHeaders.remove(HttpHeaderNames.TRANSFER_ENCODING);
        outputHeaders.remove(HttpHeaderNames.TRAILER);
        if (!isTrailer) {
            outputHeaders.setInt(ExtensionHeaderNames.STREAM_ID.text(), streamId);
            HttpUtil.setKeepAlive(outputHeaders, httpVersion, true);
        }
    }

    /**
     * 将 HTTP/1.x 消息转换为 HTTP/2 头部（含伪头部推导）。
     * <p>扩展头 {@link ExtensionHeaderNames#SCHEME} 仅在 Host/Request-Line 无法推断时使用；
     * {@link ExtensionHeaderNames#PATH} 被忽略，路径始终从 Request-Line 提取。
     */
    public static Http2Headers toHttp2Headers(HttpMessage in, boolean validateHeaders) {
        HttpHeaders inHeaders = in.headers();
        final Http2Headers out = new DefaultHttp2Headers(validateHeaders, inHeaders.size());
        if (in instanceof HttpRequest) {
            HttpRequest request = (HttpRequest) in;
            String host = inHeaders.getAsString(HttpHeaderNames.HOST);
            if (isOriginForm(request.uri()) || isAsteriskForm(request.uri())) {
                out.path(new AsciiString(request.uri()));
                setHttp2Scheme(inHeaders, out);
            } else {
                String requestTarget = request.uri();
                out.path(toHttp2Path(requestTarget));
                if (hasSchemeAndAuthority(requestTarget)) {
                    URI requestTargetUri = URI.create(http2PathlessRequestTarget(requestTarget));
                    // Host 为空时从绝对 URI 的 authority 补全
                    host = isNullOrEmpty(host) ? requestTargetUri.getAuthority() : host;
                    setHttp2Scheme(inHeaders, requestTargetUri, out);
                } else {
                    int schemeEnd = schemeEnd(requestTarget);
                    if (schemeEnd != -1) {
                        setHttp2Scheme(inHeaders, requestTarget.substring(0, schemeEnd), -1, out);
                    } else {
                        setHttp2Scheme(inHeaders, out);
                    }
                }
            }
            setHttp2Authority(host, out);
            out.method(request.method().asciiName());
        } else if (in instanceof HttpResponse) {
            HttpResponse response = (HttpResponse) in;
            out.status(response.status().codeAsText());
        }

        // 复制尚未被伪头部逻辑消费的普通头部
        toHttp2Headers(inHeaders, out);
        return out;
    }

    public static Http2Headers toHttp2Headers(HttpHeaders inHeaders, boolean validateHeaders) {
        if (inHeaders.isEmpty()) {
            return EmptyHttp2Headers.INSTANCE;
        }

        final Http2Headers out = new DefaultHttp2Headers(validateHeaders, inHeaders.size());
        toHttp2Headers(inHeaders, out);
        return out;
    }

    private static CharSequenceMap<AsciiString> toLowercaseMap(Iterator<? extends CharSequence> valuesIter,
                                                               int arraySizeHint) {
        UnsupportedValueConverter<AsciiString> valueConverter = UnsupportedValueConverter.<AsciiString>instance();
        CharSequenceMap<AsciiString> result = new CharSequenceMap<AsciiString>(true, valueConverter, arraySizeHint);

        while (valuesIter.hasNext()) {
            AsciiString lowerCased = AsciiString.of(valuesIter.next()).toLowerCase();
            try {
                int index = lowerCased.forEachByte(FIND_COMMA);
                if (index != -1) {
                    int start = 0;
                    do {
                        result.add(lowerCased.subSequence(start, index, false).trim(), EMPTY_STRING);
                        start = index + 1;
                    } while (start < lowerCased.length() &&
                             (index = lowerCased.forEachByte(start, lowerCased.length() - start, FIND_COMMA)) != -1);
                    result.add(lowerCased.subSequence(start, lowerCased.length(), false).trim(), EMPTY_STRING);
                } else {
                    result.add(lowerCased.trim(), EMPTY_STRING);
                }
            } catch (Exception e) {
                // This is not expect to happen because FIND_COMMA never throws but must be caught
                // because of the ByteProcessor interface.
                throw new IllegalStateException(e);
            }
        }
        return result;
    }

    /**
     * 按 RFC 7540 §8.1.2.2 过滤 TE 头：仅保留值为 {@code trailers} 的条目。
     * @param entry An entry whose name is {@link HttpHeaderNames#TE}.
     * @param out the resulting HTTP/2 headers.
     */
    private static void toHttp2HeadersFilterTE(Entry<CharSequence, CharSequence> entry,
                                               Http2Headers out) {
        if (indexOf(entry.getValue(), ',', 0) == -1) {
            if (contentEqualsIgnoreCase(trim(entry.getValue()), TRAILERS)) {
                out.add(TE, TRAILERS);
            }
        } else {
            List<CharSequence> teValues = unescapeCsvFields(entry.getValue());
            for (CharSequence teValue : teValues) {
                if (contentEqualsIgnoreCase(trim(teValue), TRAILERS)) {
                    out.add(TE, TRAILERS);
                    break;
                }
            }
        }
    }

    public static void toHttp2Headers(HttpHeaders inHeaders, Http2Headers out) {
        Iterator<Entry<CharSequence, CharSequence>> iter = inHeaders.iteratorCharSequence();
        // Connection 头列出的子项也需从复制列表中排除
        CharSequenceMap<AsciiString> connectionBlacklist =
            toLowercaseMap(inHeaders.valueCharSequenceIterator(CONNECTION), 8);
        while (iter.hasNext()) {
            Entry<CharSequence, CharSequence> entry = iter.next();
            final AsciiString aName = AsciiString.of(entry.getKey()).toLowerCase();
            if (!HTTP_TO_HTTP2_HEADER_BLACKLIST.contains(aName) && !connectionBlacklist.contains(aName)) {
                // RFC 7540 §8.1.2.2：TE 仅允许 trailers
                if (aName.contentEqualsIgnoreCase(TE)) {
                    toHttp2HeadersFilterTE(entry, out);
                } else if (aName.contentEqualsIgnoreCase(COOKIE)) {
                    CharSequence valueCs = entry.getValue();
                    // 校验 Cookie 格式；不符合 RFC 6265 分隔规则则整段保留
                    boolean invalid = false;
                    for (int i = 0; i < valueCs.length(); i++) {
                        char c = valueCs.charAt(i);
                        if (c == ';') {
                            if (i + 1 >= valueCs.length() || valueCs.charAt(i + 1) != ' ') {
                                // 分号后必须跟空格，否则视为非法 Cookie 行
                                invalid = true;
                                break;
                            }
                            i++; // skip space
                        } else if (c > 255) {
                            // 非 ASCII 字符时不拆分
                            invalid = true;
                            break;
                        }
                    }

                    if (invalid) {
                        out.add(COOKIE, valueCs);
                    } else {
                        splitValidCookieHeader(out, valueCs);
                    }
                } else {
                    out.add(aName, entry.getValue());
                }
            }
        }
    }

    private static void splitValidCookieHeader(Http2Headers out, CharSequence valueCs) {
        try {
            AsciiString value = AsciiString.of(valueCs);
            // 拆成多条 cookie 头以利于 HPACK 压缩（RFC 7540 §8.1.2.5）
            int index = value.forEachByte(FIND_SEMI_COLON);
            if (index != -1) {
                int start = 0;
                do {
                    out.add(COOKIE, value.subSequence(start, index, false));
                    assert index + 1 < value.length();
                    assert value.charAt(index + 1) == ' ';
                    // skip 2 characters "; " (see https://tools.ietf.org/html/rfc6265#section-4.2.1)
                    start = index + 2;
                } while (start < value.length() &&
                        (index = value.forEachByte(start, value.length() - start, FIND_SEMI_COLON)) != -1);
                assert start < value.length();
                out.add(COOKIE, value.subSequence(start, value.length(), false));
            } else {
                out.add(COOKIE, value);
            }
        } catch (Exception e) {
            // This is not expect to happen because FIND_SEMI_COLON never throws but must be caught
            // because of the ByteProcessor interface.
            throw new IllegalStateException(e);
        }
    }

    /**
     * 从 request-target 生成 HTTP/2 {@code :path}（含 query），遵循 RFC 7230 §5.3。
     */
    private static AsciiString toHttp2Path(String uri) {
        String path = dropEmptyFragment(parsePath(uri));
        String query = parseQuery(uri);
        if (isNullOrEmpty(query)) {
            return path.isEmpty() ? EMPTY_REQUEST_PATH : new AsciiString(path);
        }
        StringBuilder pathBuilder = new StringBuilder(path.length() + query.length() + 1);
        pathBuilder.append(path);
        appendQuery(pathBuilder, query);
        return new AsciiString(pathBuilder.toString());
    }

    /**
     * 从 request-target 提取 path 部分（基于 Vert.x HttpUtils.parsePath）。
     */
    private static String parsePath(String uri) {
        if (uri.isEmpty()) {
            return StringUtil.EMPTY_STRING;
        }
        int i;
        if (uri.charAt(0) == '/') {
            i = 0;
        } else {
            i = uri.indexOf("://");
            // Netty 增强：先校验 scheme 再按 :// 解析 authority
            if (!isValidScheme(uri, i)) {
                i = 0;
            } else {
                int authorityStart = i + 3;
                // Netty change: only accept '/' before query/fragment as path start.
                int queryOrFragmentStart = queryOrFragmentStart(uri, authorityStart);
                i = uri.indexOf('/', authorityStart);
                if (i == -1 || (queryOrFragmentStart != -1 && queryOrFragmentStart < i)) {
                    // contains no /
                    return "/";
                }
            }
        }

        int queryStart = uri.indexOf('?', i);
        if (queryStart == -1) {
            queryStart = uri.length();
            if (i == 0) {
                return uri;
            }
        }
        return uri.substring(i, queryStart);
    }

    /**
     * Extract the query out of a request-target or returns {@code null} if no query was found.
     */
    private static String parseQuery(String uri) {
        int i = uri.indexOf('?');
        if (i == -1) {
            return null;
        } else {
            return uri.substring(i + 1);
        }
    }

    private static String dropEmptyFragment(String path) {
        // Netty change: old URI-based conversion dropped an empty fragment delimiter.
        return path.endsWith("#") ? path.substring(0, path.length() - 1) : path;
    }

    private static void appendQuery(StringBuilder pathBuilder, String query) {
        int fragmentStart = query.indexOf('#');
        if (fragmentStart == 0) {
            // Netty change: old URI-based conversion skipped an empty query before a fragment.
            pathBuilder.append(query);
        } else if (fragmentStart == query.length() - 1) {
            // Netty change: old URI-based conversion dropped an empty fragment delimiter after a query.
            pathBuilder.append('?').append(query, 0, fragmentStart);
        } else {
            pathBuilder.append('?').append(query);
        }
    }

    static int queryOrFragmentStart(String uri, int searchStart) {
        int queryStart = uri.indexOf('?', searchStart);
        int fragmentStart = uri.indexOf('#', searchStart);
        return queryStart == -1 ? fragmentStart :
                fragmentStart == -1 ? queryStart : Math.min(queryStart, fragmentStart);
    }

    // Netty 增强：判断 request-target 是否含 scheme://authority，用于伪头部提取
    static boolean hasSchemeAndAuthority(String requestTarget) {
        int schemeEnd = requestTarget.indexOf("://");
        return isValidScheme(requestTarget, schemeEnd);
    }

    private static int schemeEnd(String requestTarget) {
        int schemeEnd = requestTarget.indexOf(':');
        return isValidScheme(requestTarget, schemeEnd) ? schemeEnd : -1;
    }

    // Netty addition: prepare only scheme://authority for URI validation.
    private static String http2PathlessRequestTarget(String requestTarget) {
        int schemeEnd = requestTarget.indexOf("://");
        int authorityStart = schemeEnd + 3;
        // Netty addition: strip before path/query/fragment; Vert.x parsePath does not validate authority.
        int pathStart = requestTarget.indexOf('/', authorityStart);
        int delimiter = queryOrFragmentStart(requestTarget, authorityStart);
        if (pathStart != -1 && (delimiter == -1 || pathStart < delimiter)) {
            delimiter = pathStart;
        }
        if (delimiter == -1) {
            return requestTarget;
        }
        return delimiter == authorityStart ? requestTarget.substring(0, delimiter + 1) :
                requestTarget.substring(0, delimiter);
    }

    // Netty addition: validate the text before :// as a scheme.
    static boolean isValidScheme(String uri, int schemeEnd) {
        if (schemeEnd <= 0) {
            return false;
        }
        char first = uri.charAt(0);
        if (!isAlpha(first)) {
            return false;
        }
        for (int i = 1; i < schemeEnd; ++i) {
            char c = uri.charAt(i);
            if (!isAlpha(c) && (c < '0' || c > '9') && c != '+' && c != '-' && c != '.') {
                return false;
            }
        }
        return true;
    }

    private static boolean isAlpha(char c) {
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z');
    }

    // package-private for testing only
    static void setHttp2Authority(String authority, Http2Headers out) {
        // RFC 7540：:authority 不得包含已废弃的 userinfo 子组件
        if (authority != null) {
            if (authority.isEmpty()) {
                out.authority(EMPTY_STRING);
            } else {
                int start = authority.indexOf('@') + 1;
                int length = authority.length() - start;
                if (length == 0) {
                    throw new IllegalArgumentException("authority: " + authority);
                }
                out.authority(new AsciiString(authority, start, length));
            }
        }
    }

    private static void setHttp2Scheme(HttpHeaders in, Http2Headers out) {
        setHttp2Scheme(in, URI.create(""), out);
    }

    private static void setHttp2Scheme(HttpHeaders in, URI uri, Http2Headers out) {
        setHttp2Scheme(in, uri.getScheme(), uri.getPort(), out);
    }

    private static void setHttp2Scheme(HttpHeaders in, String scheme, int port, Http2Headers out) {
        if (!isNullOrEmpty(scheme)) {
            out.scheme(new AsciiString(scheme));
            return;
        }

        // 优先消费 x-http2-scheme 扩展头
        CharSequence cValue = in.get(ExtensionHeaderNames.SCHEME.text());
        if (cValue != null) {
            out.scheme(AsciiString.of(cValue));
            return;
        }

        if (port == HTTPS.port()) {
            out.scheme(HTTPS.name());
        } else if (port == HTTP.port()) {
            out.scheme(HTTP.name());
        } else {
            throw new IllegalArgumentException(":scheme must be specified. " +
                    "see https://tools.ietf.org/html/rfc7540#section-8.1.2.3");
        }
    }

    /**
     * HTTP/2 → HTTP/1.x 头部名翻译器（伪头部映射为 Host 或扩展头）。
     */
    private static final class Http2ToHttpHeaderTranslator {
        /**
         * HTTP/2 伪头部到 HTTP/1.x 头部名的映射表。
         */
        private static final CharSequenceMap<AsciiString>
            REQUEST_HEADER_TRANSLATIONS = new CharSequenceMap<AsciiString>();
        private static final CharSequenceMap<AsciiString>
            RESPONSE_HEADER_TRANSLATIONS = new CharSequenceMap<AsciiString>();
        static {
            RESPONSE_HEADER_TRANSLATIONS.add(Http2Headers.PseudoHeaderName.AUTHORITY.value(),
                            HttpHeaderNames.HOST);
            RESPONSE_HEADER_TRANSLATIONS.add(Http2Headers.PseudoHeaderName.SCHEME.value(),
                            ExtensionHeaderNames.SCHEME.text());
            REQUEST_HEADER_TRANSLATIONS.add(RESPONSE_HEADER_TRANSLATIONS);
            RESPONSE_HEADER_TRANSLATIONS.add(Http2Headers.PseudoHeaderName.PATH.value(),
                            ExtensionHeaderNames.PATH.text());
        }

        private final int streamId;
        private final HttpHeaders output;
        private final CharSequenceMap<AsciiString> translations;

        /**
         * Create a new instance
         *
         * @param output The HTTP/1.x headers object to store the results of the translation
         * @param request if {@code true}, translates headers using the request translation map. Otherwise uses the
         *        response translation map.
         */
        Http2ToHttpHeaderTranslator(int streamId, HttpHeaders output, boolean request) {
            this.streamId = streamId;
            this.output = output;
            translations = request ? REQUEST_HEADER_TRANSLATIONS : RESPONSE_HEADER_TRANSLATIONS;
        }

        void translateHeaders(Iterable<Entry<CharSequence, CharSequence>> inputHeaders) throws Http2Exception {
            // lazily created as needed
            StringBuilder cookies = null;
            boolean hostHeaderFound = false;

            for (Entry<CharSequence, CharSequence> entry : inputHeaders) {
                final CharSequence name = entry.getKey();
                final CharSequence value = entry.getValue();
                AsciiString translatedName = translations.get(name);
                if (translatedName != null) {
                    if (translatedName.contentEqualsIgnoreCase(HttpHeaderNames.HOST)) {
                        hostHeaderFound = true;
                    }
                    output.add(translatedName, AsciiString.of(value));
                } else if (!Http2Headers.PseudoHeaderName.isPseudoHeader(name)) {
                    // https://tools.ietf.org/html/rfc7540#section-8.1.2.3
                    // All headers that start with ':' are only valid in HTTP/2 context
                    if (name.length() == 0 || name.charAt(0) == ':') {
                        throw streamError(streamId, PROTOCOL_ERROR,
                                "Invalid HTTP/2 header '%s' encountered in translation to HTTP/1.x", name);
                    }
                    if (COOKIE.equals(name)) {
                        // RFC 7540 §8.1.2.5：多条 cookie 头合并为一条 HTTP/1 Cookie
                        if (cookies == null) {
                            cookies = InternalThreadLocalMap.get().stringBuilder();
                        } else if (cookies.length() > 0) {
                            cookies.append("; ");
                        }
                        cookies.append(value);
                    } else if (contentEqualsIgnoreCase(HttpHeaderNames.HOST, name)) {
                        // :authority 与 host 冲突时视为协议错误，避免 HTTP/1 出现重复 Host
                        if (hostHeaderFound) {
                            if (!contentEqualsIgnoreCase(output.get(HttpHeaderNames.HOST), value)) {
                                throw streamError(streamId, PROTOCOL_ERROR,
                                        "Conflicting ':authority' and 'host' headers found");
                            }
                        } else {
                            hostHeaderFound = true;
                            output.add(name, value);
                        }
                    } else {
                        output.add(name, value);
                    }
                }
            }
            if (cookies != null) {
                output.add(COOKIE, cookies.toString());
            }
        }
    }
}
