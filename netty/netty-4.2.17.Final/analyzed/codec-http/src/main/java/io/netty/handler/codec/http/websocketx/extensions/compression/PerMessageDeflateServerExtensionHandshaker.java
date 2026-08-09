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
import io.netty.handler.codec.http.websocketx.extensions.WebSocketExtensionData;
import io.netty.handler.codec.http.websocketx.extensions.WebSocketExtensionDecoder;
import io.netty.handler.codec.http.websocketx.extensions.WebSocketExtensionEncoder;
import io.netty.handler.codec.http.websocketx.extensions.WebSocketExtensionFilterProvider;
import io.netty.handler.codec.http.websocketx.extensions.WebSocketServerExtension;
import io.netty.handler.codec.http.websocketx.extensions.WebSocketServerExtensionHandshaker;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import static io.netty.util.internal.ObjectUtil.*;

/**
 * 服务端 <a href="https://tools.ietf.org/html/draft-ietf-hybi-permessage-compression-18">permessage-deflate</a>
 * 扩展握手实现：解析客户端 Sec-WebSocket-Extensions 参数，协商窗口大小、
 * context takeover 与压缩级别，并生成对应的编解码器。
 */
public final class PerMessageDeflateServerExtensionHandshaker implements WebSocketServerExtensionHandshaker {

    /** Deflate 滑动窗口位数下限（2^8） */
    public static final int MIN_WINDOW_SIZE = 8;
    /** Deflate 滑动窗口位数上限（2^15，RFC 7692 默认值） */
    public static final int MAX_WINDOW_SIZE = 15;

    static final String PERMESSAGE_DEFLATE_EXTENSION = "permessage-deflate";
    static final String CLIENT_MAX_WINDOW = "client_max_window_bits";
    static final String SERVER_MAX_WINDOW = "server_max_window_bits";
    static final String CLIENT_NO_CONTEXT = "client_no_context_takeover";
    static final String SERVER_NO_CONTEXT = "server_no_context_takeover";

    /**
     * Deflate 压缩默认内存级别（zlib MAX_MEM_LEVEL）。
     */
    public static final int DEFAULT_MEM_LEVEL = 8;
    /** zlib 内存级别下限 */
    public static final int MIN_MEM_LEVEL = 1;
    /** zlib 内存级别上限 */
    public static final int MAX_MEM_LEVEL = 9;

    private final int compressionLevel;
    private final boolean allowServerWindowSize;
    private final int preferredClientWindowSize;
    private final boolean allowServerNoContext;
    private final boolean preferredClientNoContext;
    private final int serverWindowSize;
    private final int memLevel;
    private final WebSocketExtensionFilterProvider extensionFilterProvider;
    private final int maxAllocation;

    /**
     * Constructor with default configuration.
     *
     * @deprecated
     *            Use {@link PerMessageDeflateServerExtensionHandshaker#
     *            PerMessageDeflateServerExtensionHandshaker(int)}.
     */
    @Deprecated
    public PerMessageDeflateServerExtensionHandshaker() {
        this(0);
    }

    /**
     * Constructor with default configuration.
     *
     * @param maxAllocation
     *            Maximum size of the decompression buffer. Must be &gt;= 0. If zero, maximum size is not limited.
     */
    public PerMessageDeflateServerExtensionHandshaker(int maxAllocation) {
        this(6, ZlibCodecFactory.isSupportingWindowSizeAndMemLevel(), MAX_WINDOW_SIZE, false, false,
                MAX_WINDOW_SIZE, DEFAULT_MEM_LEVEL, maxAllocation);
    }

    /**
     * Constructor with custom configuration.
     *
     * @param compressionLevel
     *            Compression level between 0 and 9 (default is 6).
     * @param allowServerWindowSize
     *            allows WebSocket client to customize the server inflater window size
     *            (default is false).
     * @param preferredClientWindowSize
     *            indicates the preferred client window size to use if client inflater is customizable.
     * @param allowServerNoContext
     *            allows WebSocket client to activate server_no_context_takeover
     *            (default is false).
     * @param preferredClientNoContext
     *            indicates if server prefers to activate client_no_context_takeover
     *            if client is compatible with (default is false).
     * @deprecated
     *            Use {@link PerMessageDeflateServerExtensionHandshaker#PerMessageDeflateServerExtensionHandshaker(
     *            int, boolean, int, boolean, boolean, int)}.
     */
    @Deprecated
    public PerMessageDeflateServerExtensionHandshaker(int compressionLevel, boolean allowServerWindowSize,
                                                      int preferredClientWindowSize,
                                                      boolean allowServerNoContext, boolean preferredClientNoContext) {
        this(compressionLevel, allowServerWindowSize, preferredClientWindowSize, allowServerNoContext,
                preferredClientNoContext, MAX_WINDOW_SIZE, DEFAULT_MEM_LEVEL, 0);
    }

    /**
     * Constructor with custom configuration.
     *
     * @param compressionLevel
     *            Compression level between 0 and 9 (default is 6).
     * @param allowServerWindowSize
     *            allows WebSocket client to customize the server inflater window size
     *            (default is false).
     * @param preferredClientWindowSize
     *            indicates the preferred client window size to use if client inflater is customizable.
     * @param allowServerNoContext
     *            allows WebSocket client to activate server_no_context_takeover
     *            (default is false).
     * @param preferredClientNoContext
     *            indicates if server prefers to activate client_no_context_takeover
     *            if client is compatible with (default is false).
     * @param maxAllocation
     *            Maximum size of the decompression buffer. Must be &gt;= 0. If zero, maximum size is not limited.
     */
    public PerMessageDeflateServerExtensionHandshaker(int compressionLevel, boolean allowServerWindowSize,
            int preferredClientWindowSize,
            boolean allowServerNoContext, boolean preferredClientNoContext, int maxAllocation) {
        this(compressionLevel, allowServerWindowSize, preferredClientWindowSize, allowServerNoContext,
             preferredClientNoContext, MAX_WINDOW_SIZE, DEFAULT_MEM_LEVEL, maxAllocation);
    }

    /**
     * Constructor with custom configuration including server-side compressor memory limits.
     *
     * @param compressionLevel
     *            Compression level between 0 and 9 (default is 6).
     * @param allowServerWindowSize
     *            allows WebSocket client to customize the server inflater window size
     *            (default is false).
     * @param preferredClientWindowSize
     *            indicates the preferred client window size to use if client inflater is customizable.
     * @param allowServerNoContext
     *            allows WebSocket client to activate server_no_context_takeover
     *            (default is false).
     * @param preferredClientNoContext
     *            indicates if server prefers to activate client_no_context_takeover
     *            if client is compatible with (default is false).
     * @param serverWindowSize
     *            upper bound (in bits, 8-15) on the server-side deflate window. If the client offers
     *            {@code server_max_window_bits=N}, the negotiated value is {@code min(N, serverWindowSize)}.
     *            Set lower than {@link #MAX_WINDOW_SIZE} to reduce per-connection memory.
     * @param memLevel
     *            zlib memory level for the server-side deflater (1-9). Lower values reduce per-connection
     *            memory at the cost of compression ratio.
     * @param maxAllocation
     *            Maximum size of the decompression buffer. Must be &gt;= 0. If zero, maximum size is not limited.
     */
    public PerMessageDeflateServerExtensionHandshaker(int compressionLevel, boolean allowServerWindowSize,
            int preferredClientWindowSize,
            boolean allowServerNoContext, boolean preferredClientNoContext,
            int serverWindowSize, int memLevel, int maxAllocation) {
        this(compressionLevel, allowServerWindowSize, preferredClientWindowSize, allowServerNoContext,
             preferredClientNoContext, serverWindowSize, memLevel,
             WebSocketExtensionFilterProvider.DEFAULT, maxAllocation);
    }

    /**
     * Constructor with custom configuration.
     *
     * @param compressionLevel
     *            Compression level between 0 and 9 (default is 6).
     * @param allowServerWindowSize
     *            allows WebSocket client to customize the server inflater window size
     *            (default is false).
     * @param preferredClientWindowSize
     *            indicates the preferred client window size to use if client inflater is customizable.
     * @param allowServerNoContext
     *            allows WebSocket client to activate server_no_context_takeover
     *            (default is false).
     * @param preferredClientNoContext
     *            indicates if server prefers to activate client_no_context_takeover
     *            if client is compatible with (default is false).
     * @param extensionFilterProvider
     *            provides server extension filters for per message deflate encoder and decoder.
     * @deprecated
     *            Use {@link PerMessageDeflateServerExtensionHandshaker#PerMessageDeflateServerExtensionHandshaker(
     *            int, boolean, int, boolean, boolean, WebSocketExtensionFilterProvider, int)}.
     */
    @Deprecated
    public PerMessageDeflateServerExtensionHandshaker(int compressionLevel, boolean allowServerWindowSize,
                                                      int preferredClientWindowSize,
                                                      boolean allowServerNoContext, boolean preferredClientNoContext,
                                                      WebSocketExtensionFilterProvider extensionFilterProvider) {
        this(compressionLevel, allowServerWindowSize, preferredClientWindowSize, allowServerNoContext,
                preferredClientNoContext, MAX_WINDOW_SIZE, DEFAULT_MEM_LEVEL, extensionFilterProvider, 0);
    }

    /**
     * Constructor with custom configuration.
     *
     * @param compressionLevel
     *            Compression level between 0 and 9 (default is 6).
     * @param allowServerWindowSize
     *            allows WebSocket client to customize the server inflater window size
     *            (default is false).
     * @param preferredClientWindowSize
     *            indicates the preferred client window size to use if client inflater is customizable.
     * @param allowServerNoContext
     *            allows WebSocket client to activate server_no_context_takeover
     *            (default is false).
     * @param preferredClientNoContext
     *            indicates if server prefers to activate client_no_context_takeover
     *            if client is compatible with (default is false).
     * @param extensionFilterProvider
     *            provides server extension filters for per message deflate encoder and decoder.
     * @param maxAllocation
     *            Maximum size of the decompression buffer. Must be &gt;= 0. If zero, maximum size is not limited.
     * @deprecated
     *            Use {@link PerMessageDeflateServerExtensionHandshaker#PerMessageDeflateServerExtensionHandshaker(
     *            int, boolean, int, boolean, boolean, int, int, WebSocketExtensionFilterProvider, int)}.
     */
    @Deprecated
    public PerMessageDeflateServerExtensionHandshaker(int compressionLevel, boolean allowServerWindowSize,
            int preferredClientWindowSize,
            boolean allowServerNoContext, boolean preferredClientNoContext,
            WebSocketExtensionFilterProvider extensionFilterProvider,
            int maxAllocation) {
        this(compressionLevel, allowServerWindowSize, preferredClientWindowSize, allowServerNoContext,
                preferredClientNoContext, MAX_WINDOW_SIZE, DEFAULT_MEM_LEVEL, extensionFilterProvider, maxAllocation);
    }

    /**
     * Constructor with full custom configuration.
     *
     * @param compressionLevel
     *            Compression level between 0 and 9 (default is 6).
     * @param allowServerWindowSize
     *            allows WebSocket client to customize the server inflater window size
     *            (default is false).
     * @param preferredClientWindowSize
     *            indicates the preferred client window size to use if client inflater is customizable.
     * @param allowServerNoContext
     *            allows WebSocket client to activate server_no_context_takeover
     *            (default is false).
     * @param preferredClientNoContext
     *            indicates if server prefers to activate client_no_context_takeover
     *            if client is compatible with (default is false).
     * @param serverWindowSize
     *            upper bound (in bits, 8-15) on the server-side deflate window. If the client offers
     *            {@code server_max_window_bits=N}, the negotiated value is {@code min(N, serverWindowSize)}.
     *            Per <a href="https://tools.ietf.org/html/rfc7692#section-7.1.2.1">RFC 7692 §7.1.2.1</a>
     *            the server may include {@code server_max_window_bits} with the same or smaller value than the
     *            offer; this handshaker also advertises it unilaterally when {@code serverWindowSize} is less
     *            than {@link #MAX_WINDOW_SIZE}.
     * @param memLevel
     *            zlib memory level for the server-side deflater (1-9). Lower values reduce per-connection
     *            memory at the cost of compression ratio.
     * @param extensionFilterProvider
     *            provides server extension filters for per message deflate encoder and decoder.
     * @param maxAllocation
     *            Maximum size of the decompression buffer. Must be &gt;= 0. If zero, maximum size is not limited.
     */
    public PerMessageDeflateServerExtensionHandshaker(int compressionLevel, boolean allowServerWindowSize,
            int preferredClientWindowSize,
            boolean allowServerNoContext, boolean preferredClientNoContext,
            int serverWindowSize, int memLevel,
            WebSocketExtensionFilterProvider extensionFilterProvider,
            int maxAllocation) {
        if (preferredClientWindowSize > MAX_WINDOW_SIZE || preferredClientWindowSize < MIN_WINDOW_SIZE) {
            throw new IllegalArgumentException(
                    "preferredServerWindowSize: " + preferredClientWindowSize + " (expected: 8-15)");
        }
        if (compressionLevel < 0 || compressionLevel > 9) {
            throw new IllegalArgumentException(
                    "compressionLevel: " + compressionLevel + " (expected: 0-9)");
        }
        this.compressionLevel = compressionLevel;
        this.allowServerWindowSize = allowServerWindowSize;
        this.preferredClientWindowSize = preferredClientWindowSize;
        this.allowServerNoContext = allowServerNoContext;
        this.preferredClientNoContext = preferredClientNoContext;
        this.serverWindowSize = checkInRange(serverWindowSize, MIN_WINDOW_SIZE, MAX_WINDOW_SIZE, "serverWindowSize");
        this.memLevel = checkInRange(memLevel, MIN_MEM_LEVEL, MAX_MEM_LEVEL, "memLevel");
        this.extensionFilterProvider = checkNotNull(extensionFilterProvider, "extensionFilterProvider");
        this.maxAllocation = checkPositiveOrZero(maxAllocation, "maxAllocation");
    }

    @Override
    /** 协商 permessage-deflate 参数；无法兼容时返回 null 拒绝该扩展 */
    public WebSocketServerExtension handshakeExtension(WebSocketExtensionData extensionData) {
        if (!PERMESSAGE_DEFLATE_EXTENSION.equals(extensionData.name())) {
            return null;
        }

        boolean deflateEnabled = true;
        int clientWindowSize = MAX_WINDOW_SIZE;
        int negotiatedServerWindowSize = this.serverWindowSize;
        boolean serverNoContext = false;
        boolean clientNoContext = false;

        Iterator<Entry<String, String>> parametersIterator =
                extensionData.parameters().entrySet().iterator();
        while (deflateEnabled && parametersIterator.hasNext()) {
            Entry<String, String> parameter = parametersIterator.next();

            if (CLIENT_MAX_WINDOW.equalsIgnoreCase(parameter.getKey())) {
                // RFC 7692：client_max_window_bits 可带数值或不带（由服务端指定偏好值）
                String value = parameter.getValue();
                if (value != null) {
                    // 非法数值让 NumberFormatException 向上抛出
                    clientWindowSize = Integer.parseInt(value);
                    if (clientWindowSize > MAX_WINDOW_SIZE || clientWindowSize < MIN_WINDOW_SIZE) {
                        deflateEnabled = false;
                    }
                } else {
                    // 未指定数值时使用服务端偏好的客户端窗口大小
                    clientWindowSize = preferredClientWindowSize;
                }
            } else if (SERVER_MAX_WINDOW.equalsIgnoreCase(parameter.getKey())) {
                // 仅在允许客户端定制服务端窗口时才接受该参数
                if (allowServerWindowSize) {
                    int clientOfferedServerWindowSize = Integer.parseInt(parameter.getValue());
                    if (clientOfferedServerWindowSize > MAX_WINDOW_SIZE
                            || clientOfferedServerWindowSize < MIN_WINDOW_SIZE) {
                        deflateEnabled = false;
                    } else {
                        // RFC 7692 §7.1.2.1：服务端以不大于客户端提议的值应答；
                        // 再受 serverWindowSize 上限约束以控制内存占用
                        negotiatedServerWindowSize = Math.min(clientOfferedServerWindowSize, this.serverWindowSize);
                    }
                } else {
                    deflateEnabled = false;
                }
            } else if (CLIENT_NO_CONTEXT.equalsIgnoreCase(parameter.getKey())) {
                // 客户端支持时按服务端偏好决定是否禁用客户端 context takeover
                clientNoContext = preferredClientNoContext;
            } else if (SERVER_NO_CONTEXT.equalsIgnoreCase(parameter.getKey())) {
                // 仅在允许时才激活 server_no_context_takeover
                if (allowServerNoContext) {
                    serverNoContext = true;
                } else {
                    deflateEnabled = false;
                }
            } else {
                // 未知参数导致拒绝扩展
                deflateEnabled = false;
            }
        }

        if (deflateEnabled) {
            return new PermessageDeflateExtension(compressionLevel, serverNoContext,
                    negotiatedServerWindowSize, memLevel, clientNoContext, clientWindowSize,
                    extensionFilterProvider, maxAllocation);
        } else {
            return null;
        }
    }

    /** 协商成功后封装扩展实例，负责创建编解码器与应答 Sec-WebSocket-Extensions 头 */
    private static class PermessageDeflateExtension implements WebSocketServerExtension {

        private final int compressionLevel;
        private final boolean serverNoContext;
        private final int serverWindowSize;
        private final int memLevel;
        private final boolean clientNoContext;
        private final int clientWindowSize;
        private final WebSocketExtensionFilterProvider extensionFilterProvider;
        private final int maxAllocation;

        PermessageDeflateExtension(int compressionLevel, boolean serverNoContext,
                int serverWindowSize, int memLevel, boolean clientNoContext, int clientWindowSize,
                WebSocketExtensionFilterProvider extensionFilterProvider, int maxAllocation) {
            this.compressionLevel = compressionLevel;
            this.serverNoContext = serverNoContext;
            this.serverWindowSize = serverWindowSize;
            this.memLevel = memLevel;
            this.clientNoContext = clientNoContext;
            this.clientWindowSize = clientWindowSize;
            this.extensionFilterProvider = extensionFilterProvider;
            this.maxAllocation = maxAllocation;
        }

        @Override
        public int rsv() {
            return RSV1;
        }

        @Override
        public WebSocketExtensionEncoder newExtensionEncoder() {
            return new PerMessageDeflateEncoder(compressionLevel, serverWindowSize, memLevel, serverNoContext,
                                                extensionFilterProvider.encoderFilter());
        }

        @Override
        public WebSocketExtensionDecoder newExtensionDecoder() {
            return new PerMessageDeflateDecoder(clientNoContext, extensionFilterProvider.decoderFilter(),
                                                maxAllocation);
        }

        @Override
        /** 构建握手应答中的 Sec-WebSocket-Extensions 参数 */
        public WebSocketExtensionData newReponseData() {
            HashMap<String, String> parameters = new HashMap<String, String>(4);
            if (serverNoContext) {
                parameters.put(SERVER_NO_CONTEXT, null);
            }
            if (clientNoContext) {
                parameters.put(CLIENT_NO_CONTEXT, null);
            }
            if (serverWindowSize != MAX_WINDOW_SIZE) {
                parameters.put(SERVER_MAX_WINDOW, Integer.toString(serverWindowSize));
            }
            if (clientWindowSize != MAX_WINDOW_SIZE) {
                parameters.put(CLIENT_MAX_WINDOW, Integer.toString(clientWindowSize));
            }
            return new WebSocketExtensionData(PERMESSAGE_DEFLATE_EXTENSION, parameters);
        }
    }

}
