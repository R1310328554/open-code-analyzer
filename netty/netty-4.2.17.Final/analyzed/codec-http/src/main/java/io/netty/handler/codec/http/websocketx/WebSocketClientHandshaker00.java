/*
 * Copyright 2012 The Netty Project
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
package io.netty.handler.codec.http.websocketx;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;

import java.net.URI;
import java.nio.ByteBuffer;
import java.util.concurrent.ThreadLocalRandom;

/**
 * HyBi-00 草案 WebSocket 客户端握手实现（Opening / Closing Handshake）。
 * <p>大量逻辑来自 Netty 3.2 HTTP 示例。
 */
public class WebSocketClientHandshaker00 extends WebSocketClientHandshaker {

    /** 期望的服务端 challenge 响应（MD5）。 */
    private ByteBuf expectedChallengeResponseBytes;

    /**
     * 创建握手器实例。
     *
     * @param webSocketURL
     *            URL for web socket communications. e.g "ws://myhost.com/mypath". Subsequent web socket frames will be
     *            sent to this URL.
     * @param version
     *            Version of web socket specification to use to connect to the server
     * @param subprotocol
     *            Sub protocol request sent to the server.
     * @param customHeaders
     *            Map of custom headers to add to the client request
     * @param maxFramePayloadLength
     *            Maximum length of a frame's payload
     */
    public WebSocketClientHandshaker00(URI webSocketURL, WebSocketVersion version, String subprotocol,
            HttpHeaders customHeaders, int maxFramePayloadLength) {
        this(webSocketURL, version, subprotocol, customHeaders, maxFramePayloadLength,
                DEFAULT_FORCE_CLOSE_TIMEOUT_MILLIS);
    }

    /**
     * 创建握手器实例（含强制关闭超时）。
     *
     * @param webSocketURL
     *            URL for web socket communications. e.g "ws://myhost.com/mypath". Subsequent web socket frames will be
     *            sent to this URL.
     * @param version
     *            Version of web socket specification to use to connect to the server
     * @param subprotocol
     *            Sub protocol request sent to the server.
     * @param customHeaders
     *            Map of custom headers to add to the client request
     * @param maxFramePayloadLength
     *            Maximum length of a frame's payload
     * @param forceCloseTimeoutMillis
     *            Close the connection if it was not closed by the server after timeout specified
     */
    public WebSocketClientHandshaker00(URI webSocketURL, WebSocketVersion version, String subprotocol,
                                       HttpHeaders customHeaders, int maxFramePayloadLength,
                                       long forceCloseTimeoutMillis) {
        this(webSocketURL, version, subprotocol, customHeaders, maxFramePayloadLength, forceCloseTimeoutMillis, false);
    }

    /**
     * 创建握手器实例（含强制关闭超时）。
     *
     * @param webSocketURL
     *            URL for web socket communications. e.g "ws://myhost.com/mypath". Subsequent web socket frames will be
     *            sent to this URL.
     * @param version
     *            Version of web socket specification to use to connect to the server
     * @param subprotocol
     *            Sub protocol request sent to the server.
     * @param customHeaders
     *            Map of custom headers to add to the client request
     * @param maxFramePayloadLength
     *            Maximum length of a frame's payload
     * @param forceCloseTimeoutMillis
     *            Close the connection if it was not closed by the server after timeout specified
     * @param  absoluteUpgradeUrl
     *            Use an absolute url for the Upgrade request, typically when connecting through an HTTP proxy over
     *            clear HTTP
     */
    WebSocketClientHandshaker00(URI webSocketURL, WebSocketVersion version, String subprotocol,
            HttpHeaders customHeaders, int maxFramePayloadLength,
            long forceCloseTimeoutMillis, boolean absoluteUpgradeUrl) {
        this(webSocketURL, version, subprotocol, customHeaders, maxFramePayloadLength, forceCloseTimeoutMillis,
                absoluteUpgradeUrl, true);
    }

    /**
     * 创建握手器实例（含强制关闭超时）。
     *
     * @param webSocketURL
     *            URL for web socket communications. e.g "ws://myhost.com/mypath". Subsequent web socket frames will be
     *            sent to this URL.
     * @param version
     *            Version of web socket specification to use to connect to the server
     * @param subprotocol
     *            Sub protocol request sent to the server.
     * @param customHeaders
     *            Map of custom headers to add to the client request
     * @param maxFramePayloadLength
     *            Maximum length of a frame's payload
     * @param forceCloseTimeoutMillis
     *            Close the connection if it was not closed by the server after timeout specified
     * @param  absoluteUpgradeUrl
     *            Use an absolute url for the Upgrade request, typically when connecting through an HTTP proxy over
     *            clear HTTP
     * @param generateOriginHeader
     *            Allows to generate the `Origin` header value for handshake request
     *            according to the given webSocketURL
     */
    WebSocketClientHandshaker00(URI webSocketURL, WebSocketVersion version, String subprotocol,
                                HttpHeaders customHeaders, int maxFramePayloadLength,
                                long forceCloseTimeoutMillis, boolean absoluteUpgradeUrl,
                                boolean generateOriginHeader) {
        super(webSocketURL, version, subprotocol, customHeaders, maxFramePayloadLength, forceCloseTimeoutMillis,
                absoluteUpgradeUrl, generateOriginHeader);
    }

    /**
     * 向服务端发送 Opening Handshake 请求（HyBi-00 格式）。
     * </p>
     *
     * <pre>
     * GET /demo HTTP/1.1
     * Upgrade: WebSocket
     * Connection: Upgrade
     * Host: example.com
     * Origin: http://example.com
     * Sec-WebSocket-Key1: 4 @1  46546xW%0l 1 5
     * Sec-WebSocket-Key2: 12998 5 Y3 1  .P00
     *
     * ^n:ds[4U
     * </pre>
     *
     */
    @Override
    protected FullHttpRequest newHandshakeRequest() {
        // 生成 Key1/Key2 与 8 字节随机 key3，并计算期望的 MD5 challenge
        int spaces1 = WebSocketUtil.randomNumber(1, 12);
        int spaces2 = WebSocketUtil.randomNumber(1, 12);

        int max1 = Integer.MAX_VALUE / spaces1;
        int max2 = Integer.MAX_VALUE / spaces2;

        int number1 = WebSocketUtil.randomNumber(0, max1);
        int number2 = WebSocketUtil.randomNumber(0, max2);

        int product1 = number1 * spaces1;
        int product2 = number2 * spaces2;

        String key1 = Integer.toString(product1);
        String key2 = Integer.toString(product2);

        key1 = insertRandomCharacters(key1);
        key2 = insertRandomCharacters(key2);

        key1 = insertSpaces(key1, spaces1);
        key2 = insertSpaces(key2, spaces2);

        byte[] key3 = WebSocketUtil.randomBytes(8);

        ByteBuffer buffer = ByteBuffer.allocate(4);
        buffer.putInt(number1);
        byte[] number1Array = buffer.array();
        buffer = ByteBuffer.allocate(4);
        buffer.putInt(number2);
        byte[] number2Array = buffer.array();

        byte[] challenge = new byte[16];
        System.arraycopy(number1Array, 0, challenge, 0, 4);
        System.arraycopy(number2Array, 0, challenge, 4, 4);
        System.arraycopy(key3, 0, challenge, 8, 8);
        expectedChallengeResponseBytes = Unpooled.wrappedBuffer(WebSocketUtil.md5(challenge));

        URI wsURL = uri();

        // 组装 HTTP Upgrade 请求
        FullHttpRequest request = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, upgradeUrl(wsURL),
                Unpooled.wrappedBuffer(key3));
        HttpHeaders headers = request.headers();

        if (customHeaders != null) {
            headers.add(customHeaders);
            if (!headers.contains(HttpHeaderNames.HOST)) {
                // Only add HOST header if customHeaders did not contain it.
                //
                // See https://github.com/netty/netty/issues/10101
                headers.set(HttpHeaderNames.HOST, websocketHostValue(wsURL));
            }
        } else {
            headers.set(HttpHeaderNames.HOST, websocketHostValue(wsURL));
        }

        headers.set(HttpHeaderNames.UPGRADE, HttpHeaderValues.WEBSOCKET)
               .set(HttpHeaderNames.CONNECTION, HttpHeaderValues.UPGRADE)
               .set(HttpHeaderNames.SEC_WEBSOCKET_KEY1, key1)
               .set(HttpHeaderNames.SEC_WEBSOCKET_KEY2, key2);

        if (generateOriginHeader && !headers.contains(HttpHeaderNames.ORIGIN)) {
            headers.set(HttpHeaderNames.ORIGIN, websocketOriginValue(wsURL));
        }

        String expectedSubprotocol = expectedSubprotocol();
        if (expectedSubprotocol != null && !expectedSubprotocol.isEmpty()) {
            headers.set(HttpHeaderNames.SEC_WEBSOCKET_PROTOCOL, expectedSubprotocol);
        }

        // 设置 Content-Length 以规避部分已知服务端缺陷
        headers.set(HttpHeaderNames.CONTENT_LENGTH, key3.length);
        return request;
    }

    /**
     * 校验服务端 Opening Handshake 响应。
     * </p>
     *
     * <pre>
     * HTTP/1.1 101 WebSocket Protocol Handshake
     * Upgrade: WebSocket
     * Connection: Upgrade
     * Sec-WebSocket-Origin: http://example.com
     * Sec-WebSocket-Location: ws://example.com/demo
     * Sec-WebSocket-Protocol: sample
     *
     * 8jKS'y:G*Co,Wxa-
     * </pre>
     *
     * @param response
     *            HTTP response returned from the server for the request sent by beginOpeningHandshake00().
     * @throws WebSocketHandshakeException
     */
    @Override
    protected void verify(FullHttpResponse response) {
        HttpResponseStatus status = response.status();
        if (!HttpResponseStatus.SWITCHING_PROTOCOLS.equals(status)) {
            throw new WebSocketClientHandshakeException("Invalid handshake response getStatus: " + status, response);
        }

        HttpHeaders headers = response.headers();
        CharSequence upgrade = headers.get(HttpHeaderNames.UPGRADE);
        if (!HttpHeaderValues.WEBSOCKET.contentEqualsIgnoreCase(upgrade)) {
            throw new WebSocketClientHandshakeException("Invalid handshake response upgrade: " + upgrade, response);
        }

        if (!headers.containsValue(HttpHeaderNames.CONNECTION, HttpHeaderValues.UPGRADE, true)) {
            throw new WebSocketClientHandshakeException("Invalid handshake response connection: "
                    + headers.get(HttpHeaderNames.CONNECTION), response);
        }

        ByteBuf challenge = response.content();
        if (!challenge.equals(expectedChallengeResponseBytes)) {
            throw new WebSocketClientHandshakeException("Invalid challenge", response);
        }
    }

    /** 在 key 字符串中随机插入可打印字符。 */
    private static String insertRandomCharacters(String key) {
        int count = WebSocketUtil.randomNumber(1, 12);

        char[] randomChars = new char[count];
        int randCount = 0;
        while (randCount < count) {
            int rand = ThreadLocalRandom.current().nextInt(0x7e) + 0x21;
            if (0x21 < rand && rand < 0x2f || 0x3a < rand && rand < 0x7e) {
                randomChars[randCount] = (char) rand;
                randCount += 1;
            }
        }

        for (int i = 0; i < count; i++) {
            int split = WebSocketUtil.randomNumber(0, key.length());
            String part1 = key.substring(0, split);
            String part2 = key.substring(split);
            key = part1 + randomChars[i] + part2;
        }

        return key;
    }

    /** 在 key 中插入指定数量的空格以满足 HyBi-00 格式。 */
    private static String insertSpaces(String key, int spaces) {
        for (int i = 0; i < spaces; i++) {
            int split = WebSocketUtil.randomNumber(1, key.length() - 1);
            String part1 = key.substring(0, split);
            String part2 = key.substring(split);
            key = part1 + ' ' + part2;
        }

        return key;
    }

    @Override
    protected WebSocketFrameDecoder newWebsocketDecoder() {
        return new WebSocket00FrameDecoder(maxFramePayloadLength());
    }

    @Override
    protected WebSocketFrameEncoder newWebSocketEncoder() {
        return new WebSocket00FrameEncoder();
    }

    @Override
    public WebSocketClientHandshaker00 setForceCloseTimeoutMillis(long forceCloseTimeoutMillis) {
        super.setForceCloseTimeoutMillis(forceCloseTimeoutMillis);
        return this;
    }

}
