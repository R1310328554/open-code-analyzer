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
package io.netty.handler.codec.http.websocketx.extensions.compression;

import io.netty.handler.codec.compression.ZlibCodecFactory;
import io.netty.handler.codec.http.websocketx.extensions.WebSocketClientExtension;
import io.netty.handler.codec.http.websocketx.extensions.WebSocketClientExtensionHandshaker;
import io.netty.handler.codec.http.websocketx.extensions.WebSocketExtensionData;
import io.netty.handler.codec.http.websocketx.extensions.WebSocketExtensionDecoder;
import io.netty.handler.codec.http.websocketx.extensions.WebSocketExtensionEncoder;
import io.netty.handler.codec.http.websocketx.extensions.WebSocketExtensionFilterProvider;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import static io.netty.handler.codec.http.websocketx.extensions.compression.PerMessageDeflateServerExtensionHandshaker.*;
import static io.netty.util.internal.ObjectUtil.*;

/**
 * 客户端 RFC 7692 permessage-deflate 扩展握手实现。
 * <p>协商窗口大小、上下文接管等参数；成功则使用 {@link PerMessageDeflateEncoder}/
 * {@link PerMessageDeflateDecoder} 对<strong>整条消息</strong>（含分片）压缩。
 */
public final class PerMessageDeflateClientExtensionHandshaker implements WebSocketClientExtensionHandshaker {

    /** 出站压缩级别 0–9 */
    private final int compressionLevel;
    /** 是否允许服务端指定客户端解压窗口（client_max_window_bits） */
    private final boolean allowClientWindowSize;
    /** 请求的服务端解压窗口位数（server_max_window_bits） */
    private final int requestedServerWindowSize;
    /** 是否允许服务端激活 client_no_context_takeover */
    private final boolean allowClientNoContext;
    /** 是否请求服务端启用 server_no_context_takeover */
    private final boolean requestedServerNoContext;
    private final WebSocketExtensionFilterProvider extensionFilterProvider;
    private final int maxAllocation;

    /**
     * Constructor with default configuration.
     * @deprecated
     *            Use {@link PerMessageDeflateClientExtensionHandshaker#
     *            PerMessageDeflateClientExtensionHandshaker(int)}.
     */
    @Deprecated
    public PerMessageDeflateClientExtensionHandshaker() {
        this(0);
    }

    /**
     * Constructor with default configuration.
     * @param maxAllocation
     *            Maximum size of the decompression buffer. Must be &gt;= 0. If zero, maximum size is not limited.
     */
    public PerMessageDeflateClientExtensionHandshaker(int maxAllocation) {
        this(6, ZlibCodecFactory.isSupportingWindowSizeAndMemLevel(), MAX_WINDOW_SIZE, false, false, maxAllocation);
    }

    /**
     * Constructor with custom configuration.
     *
     * @param compressionLevel
     *            Compression level between 0 and 9 (default is 6).
     * @param allowClientWindowSize
     *            allows WebSocket server to customize the client inflater window size
     *            (default is false).
     * @param requestedServerWindowSize
     *            indicates the requested sever window size to use if server inflater is customizable.
     * @param allowClientNoContext
     *            allows WebSocket server to activate client_no_context_takeover
     *            (default is false).
     * @param requestedServerNoContext
     *            indicates if client needs to activate server_no_context_takeover
     *            if server is compatible with (default is false).
     * @deprecated
     *            Use {@link PerMessageDeflateClientExtensionHandshaker#PerMessageDeflateClientExtensionHandshaker(
     *            int, boolean, int, boolean, boolean, int)}.
     */
    @Deprecated
    public PerMessageDeflateClientExtensionHandshaker(int compressionLevel,
                                                      boolean allowClientWindowSize, int requestedServerWindowSize,
                                                      boolean allowClientNoContext, boolean requestedServerNoContext) {
        this(compressionLevel, allowClientWindowSize, requestedServerWindowSize, allowClientNoContext,
             requestedServerNoContext, 0);
    }

    /**
     * Constructor with custom configuration.
     *
     * @param compressionLevel
     *            Compression level between 0 and 9 (default is 6).
     * @param allowClientWindowSize
     *            allows WebSocket server to customize the client inflater window size
     *            (default is false).
     * @param requestedServerWindowSize
     *            indicates the requested sever window size to use if server inflater is customizable.
     * @param allowClientNoContext
     *            allows WebSocket server to activate client_no_context_takeover
     *            (default is false).
     * @param requestedServerNoContext
     *            indicates if client needs to activate server_no_context_takeover
     *            if server is compatible with (default is false).
     * @param maxAllocation
     *            Maximum size of the decompression buffer. Must be &gt;= 0. If zero, maximum size is not limited.
     */
    public PerMessageDeflateClientExtensionHandshaker(int compressionLevel,
            boolean allowClientWindowSize, int requestedServerWindowSize,
            boolean allowClientNoContext, boolean requestedServerNoContext,
            int maxAllocation) {
        this(compressionLevel, allowClientWindowSize, requestedServerWindowSize,
             allowClientNoContext, requestedServerNoContext, WebSocketExtensionFilterProvider.DEFAULT, maxAllocation);
    }

    /**
     * Constructor with custom configuration.
     *
     * @param compressionLevel
     *            Compression level between 0 and 9 (default is 6).
     * @param allowClientWindowSize
     *            allows WebSocket server to customize the client inflater window size
     *            (default is false).
     * @param requestedServerWindowSize
     *            indicates the requested sever window size to use if server inflater is customizable.
     * @param allowClientNoContext
     *            allows WebSocket server to activate client_no_context_takeover
     *            (default is false).
     * @param requestedServerNoContext
     *            indicates if client needs to activate server_no_context_takeover
     *            if server is compatible with (default is false).
     * @param extensionFilterProvider
     *            provides client extension filters for per message deflate encoder and decoder.
     * @deprecated
     *            Use {@link PerMessageDeflateClientExtensionHandshaker#PerMessageDeflateClientExtensionHandshaker(
     *            int, boolean, int, boolean, boolean, WebSocketExtensionFilterProvider, int)}.
     */
    @Deprecated
    public PerMessageDeflateClientExtensionHandshaker(int compressionLevel,
                                                      boolean allowClientWindowSize, int requestedServerWindowSize,
                                                      boolean allowClientNoContext, boolean requestedServerNoContext,
                                                      WebSocketExtensionFilterProvider extensionFilterProvider) {
        this(compressionLevel, allowClientWindowSize, requestedServerWindowSize,
                allowClientNoContext, requestedServerNoContext, extensionFilterProvider, 0);
    }

    /**
     * Constructor with custom configuration.
     *
     * @param compressionLevel
     *            Compression level between 0 and 9 (default is 6).
     * @param allowClientWindowSize
     *            allows WebSocket server to customize the client inflater window size
     *            (default is false).
     * @param requestedServerWindowSize
     *            indicates the requested sever window size to use if server inflater is customizable.
     * @param allowClientNoContext
     *            allows WebSocket server to activate client_no_context_takeover
     *            (default is false).
     * @param requestedServerNoContext
     *            indicates if client needs to activate server_no_context_takeover
     *            if server is compatible with (default is false).
     * @param extensionFilterProvider
     *            provides client extension filters for per message deflate encoder and decoder.
     * @param maxAllocation
     *            Maximum size of the decompression buffer. Must be &gt;= 0. If zero, maximum size is not limited.
     */
    public PerMessageDeflateClientExtensionHandshaker(int compressionLevel,
            boolean allowClientWindowSize, int requestedServerWindowSize,
            boolean allowClientNoContext, boolean requestedServerNoContext,
            WebSocketExtensionFilterProvider extensionFilterProvider,
            int maxAllocation) {

        if (requestedServerWindowSize > MAX_WINDOW_SIZE || requestedServerWindowSize < MIN_WINDOW_SIZE) {
            throw new IllegalArgumentException(
                    "requestedServerWindowSize: " + requestedServerWindowSize + " (expected: 8-15)");
        }
        if (compressionLevel < 0 || compressionLevel > 9) {
            throw new IllegalArgumentException(
                    "compressionLevel: " + compressionLevel + " (expected: 0-9)");
        }
        this.compressionLevel = compressionLevel;
        this.allowClientWindowSize = allowClientWindowSize;
        this.requestedServerWindowSize = requestedServerWindowSize;
        this.allowClientNoContext = allowClientNoContext;
        this.requestedServerNoContext = requestedServerNoContext;
        this.extensionFilterProvider = checkNotNull(extensionFilterProvider, "extensionFilterProvider");
        this.maxAllocation = checkPositiveOrZero(maxAllocation, "maxAllocation");
    }

    @Override
    /** 按配置构造 permessage-deflate 扩展请求参数 */
    public WebSocketExtensionData newRequestData() {
        HashMap<String, String> parameters = new HashMap<String, String>(4);
        if (requestedServerNoContext) {
            parameters.put(SERVER_NO_CONTEXT, null);
        }
        if (allowClientNoContext) {
            parameters.put(CLIENT_NO_CONTEXT, null);
        }
        if (requestedServerWindowSize != MAX_WINDOW_SIZE) {
            parameters.put(SERVER_MAX_WINDOW, Integer.toString(requestedServerWindowSize));
        }
        if (allowClientWindowSize) {
            parameters.put(CLIENT_MAX_WINDOW, null);
        }
        return new WebSocketExtensionData(PERMESSAGE_DEFLATE_EXTENSION, parameters);
    }

    @Override
    /** 逐项校验服务端响应参数，全部匹配则返回客户端扩展实例 */
    public WebSocketClientExtension handshakeExtension(WebSocketExtensionData extensionData) {
        if (!PERMESSAGE_DEFLATE_EXTENSION.equals(extensionData.name())) {
            return null;
        }

        boolean succeed = true;
        int clientWindowSize = MAX_WINDOW_SIZE;
        int serverWindowSize = MAX_WINDOW_SIZE;
        boolean serverNoContext = false;
        boolean clientNoContext = false;

        Iterator<Entry<String, String>> parametersIterator =
                extensionData.parameters().entrySet().iterator();
        while (succeed && parametersIterator.hasNext()) {
            Entry<String, String> parameter = parametersIterator.next();

            if (CLIENT_MAX_WINDOW.equalsIgnoreCase(parameter.getKey())) {
                // 服务端确认 client_max_window_bits
                if (allowClientWindowSize) {
                    // RFC 7692：client_max_window_bits 可有值或无值
                    String value = parameter.getValue();
                    if (value != null) {
                        // Let NumberFormatException bubble up if value is invalid
                        clientWindowSize = Integer.parseInt(value);
                        if (clientWindowSize > MAX_WINDOW_SIZE || clientWindowSize < MIN_WINDOW_SIZE) {
                            succeed = false;
                        }
                    }
                    // 无值时保持默认 MAX_WINDOW_SIZE
                } else {
                    succeed = false;
                }
            } else if (SERVER_MAX_WINDOW.equalsIgnoreCase(parameter.getKey())) {
                // 服务端确认的 server_max_window_bits
                serverWindowSize = Integer.parseInt(parameter.getValue());
                if (serverWindowSize > MAX_WINDOW_SIZE || serverWindowSize < MIN_WINDOW_SIZE) {
                    succeed = false;
                }
            } else if (CLIENT_NO_CONTEXT.equalsIgnoreCase(parameter.getKey())) {
                // 允许 client_no_context_takeover
                if (allowClientNoContext) {
                    clientNoContext = true;
                } else {
                    succeed = false;
                }
            } else if (SERVER_NO_CONTEXT.equalsIgnoreCase(parameter.getKey())) {
                // 服务端确认 server_no_context_takeover
                serverNoContext = true;
            } else {
                // 未知参数导致握手失败
                succeed = false;
            }
        }

        if ((requestedServerNoContext && !serverNoContext) ||
                requestedServerWindowSize < serverWindowSize) {
            succeed = false;
        }

        if (succeed) {
            return new PermessageDeflateExtension(serverNoContext, serverWindowSize,
                    clientNoContext, clientWindowSize, extensionFilterProvider, maxAllocation);
        } else {
            return null;
        }
    }

    /** 协商成功的 permessage-deflate 客户端扩展 */
    private final class PermessageDeflateExtension implements WebSocketClientExtension {

        private final boolean serverNoContext;
        private final int serverWindowSize;
        private final boolean clientNoContext;
        private final int clientWindowSize;
        private final WebSocketExtensionFilterProvider extensionFilterProvider;
        private final int maxAllocation;

        @Override
        public int rsv() {
            return RSV1;
        }

        PermessageDeflateExtension(boolean serverNoContext, int serverWindowSize,
                boolean clientNoContext, int clientWindowSize,
                WebSocketExtensionFilterProvider extensionFilterProvider, int maxAllocation) {
            this.serverNoContext = serverNoContext;
            this.serverWindowSize = serverWindowSize;
            this.clientNoContext = clientNoContext;
            this.clientWindowSize = clientWindowSize;
            this.extensionFilterProvider = extensionFilterProvider;
            this.maxAllocation = maxAllocation;
        }

        @Override
        public WebSocketExtensionEncoder newExtensionEncoder() {
            return new PerMessageDeflateEncoder(compressionLevel, clientWindowSize, clientNoContext,
                                                extensionFilterProvider.encoderFilter());
        }

        @Override
        public WebSocketExtensionDecoder newExtensionDecoder() {
            return new PerMessageDeflateDecoder(serverNoContext, extensionFilterProvider.decoderFilter(),
                                                maxAllocation);
        }
    }

}
