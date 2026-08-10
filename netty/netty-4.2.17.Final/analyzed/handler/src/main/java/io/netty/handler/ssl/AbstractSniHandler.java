/*
 * Copyright 2017 The Netty Project
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
package io.netty.handler.ssl;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.CharsetUtil;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.ScheduledFuture;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

import static io.netty.util.internal.ObjectUtil.checkPositiveOrZero;

/**
 * <p>Enables <a href="https://tools.ietf.org/html/rfc3546#section-3.1">SNI
 * (Server Name Indication)</a> extension for server side SSL. For clients
 * support SNI, the server could have multiple host name bound on a single IP.
 * The client will send host name in the handshake data so server could decide
 * which certificate to choose for the host name.</p>
 *
 * <p>服务端 SNI 抽象处理器：在 TLS ClientHello 到达后解析扩展中的主机名，异步 {@link #lookup} 选择
 * {@link SslContext}/{@link SslHandler}，再完成握手。流程：接收 ClientHello → 提取 hostname →
 * 查找配置 → {@link #onLookupComplete} 替换 pipeline 中的 SSL 组件。</p>
 */
public abstract class AbstractSniHandler<T> extends SslClientHelloHandler<T> {

    /**
     * 从 ClientHello 原始字节中解析 SNI 主机名（RFC 6066 Server Name 扩展，type=0 host_name）。
     *
     * <p>按 TLS 记录内 ClientHello 布局跳过固定字段与可变 session/cipher/compression，遍历 extensions；
     * 找到 extension_type=0 且 name_type=0 时返回 ASCII 主机名（小写）。解析失败返回 {@code null}。</p>
     */
    private static String extractSniHostname(ByteBuf in) {
        // See https://tools.ietf.org/html/rfc5246#section-7.4.1.2
        //
        // Decode the ssl client hello packet.
        //
        // struct {
        //    ProtocolVersion client_version;
        //    Random random;
        //    SessionID session_id;
        //    CipherSuite cipher_suites<2..2^16-2>;
        //    CompressionMethod compression_methods<1..2^8-1>;
        //    select (extensions_present) {
        //        case false:
        //            struct {};
        //        case true:
        //            Extension extensions<0..2^16-1>;
        //    };
        // } ClientHello;
        //

        // We have to skip bytes until SessionID (which sum to 34 bytes in this case).
        // 跳过 client_version(2) + random(32) 共 34 字节至 SessionID
        int offset = in.readerIndex();
        int endOffset = in.writerIndex();
        offset += 34;

        if (endOffset - offset >= 6) {
            final int sessionIdLength = in.getUnsignedByte(offset);
            offset += sessionIdLength + 1;

            final int cipherSuitesLength = in.getUnsignedShort(offset);
            offset += cipherSuitesLength + 2;

            final int compressionMethodLength = in.getUnsignedByte(offset);
            offset += compressionMethodLength + 1;

            final int extensionsLength = in.getUnsignedShort(offset);
            offset += 2;
            final int extensionsLimit = offset + extensionsLength;

            // Extensions should never exceed the record boundary.
            // 扩展区不得超出当前 TLS 记录边界
            if (extensionsLimit <= endOffset) {
                while (extensionsLimit - offset >= 4) {
                    final int extensionType = in.getUnsignedShort(offset);
                    offset += 2;

                    final int extensionLength = in.getUnsignedShort(offset);
                    offset += 2;

                    if (extensionsLimit - offset < extensionLength) {
                        break;
                    }

                    // SNI
                    // See https://tools.ietf.org/html/rfc6066#page-6
                    // extension_type == 0 表示 server_name 扩展
                    if (extensionType == 0) {
                        offset += 2; // ServerNameList 长度字段
                        if (extensionsLimit - offset < 3) {
                            break;
                        }

                        final int serverNameType = in.getUnsignedByte(offset);
                        offset++;

                        if (serverNameType == 0) {
                            // host_name 类型
                            final int serverNameLength = in.getUnsignedShort(offset);
                            offset += 2;

                            if (extensionsLimit - offset < serverNameLength) {
                                break;
                            }

                            final String hostname = in.toString(offset, serverNameLength, CharsetUtil.US_ASCII);
                            return hostname.toLowerCase(Locale.US);
                        } else {
                            // invalid enum value
                            // 非 host_name 类型，停止解析
                            break;
                        }
                    }

                    offset += extensionLength;
                }
            }
        }
        return null;
    }

    /** 默认握手超时：10 秒。 */
    static final long DEFAULT_HANDSHAKE_TIMEOUT_MILLIS = TimeUnit.SECONDS.toMillis(10);
    /** 握手超时毫秒数，{@code 0} 表示不启用超时。 */
    protected final long handshakeTimeoutMillis;
    /** 握手超时定时任务。 */
    private ScheduledFuture<?> timeoutFuture;
    /** 最近一次从 ClientHello 解析出的 SNI 主机名。 */
    private String hostname;

    /**
     * @param handshakeTimeoutMillis    the handshake timeout in milliseconds
     *
     * <p>使用默认 ClientHello 最大长度与指定握手超时。</p>
     */
    protected AbstractSniHandler(long handshakeTimeoutMillis) {
        this(DEFAULT_MAX_CLIENT_HELLO_LENGTH, handshakeTimeoutMillis);
    }

    /**
     * @param maxClientHelloLength     the maximum length of the client hello message.
     * @param handshakeTimeoutMillis    the handshake timeout in milliseconds
     *
     * <p>限制 ClientHello 缓冲大小并设置握手超时。</p>
     */
    protected AbstractSniHandler(int maxClientHelloLength, long handshakeTimeoutMillis) {
        super(maxClientHelloLength);
        this.handshakeTimeoutMillis = checkPositiveOrZero(handshakeTimeoutMillis, "handshakeTimeoutMillis");
    }

    /** 默认 10 秒握手超时。 */
    public AbstractSniHandler() {
        this(DEFAULT_MAX_CLIENT_HELLO_LENGTH, DEFAULT_HANDSHAKE_TIMEOUT_MILLIS);
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        if (ctx.channel().isActive()) {
            checkStartTimeout(ctx);
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        ctx.fireChannelActive();
        checkStartTimeout(ctx);
    }

    /**
     * 若配置了超时且尚未调度，则在 EventLoop 上安排握手超时关闭与 {@link SniCompletionEvent}。
     */
    private void checkStartTimeout(final ChannelHandlerContext ctx) {
        if (handshakeTimeoutMillis <= 0 || timeoutFuture != null) {
            return;
        }
        timeoutFuture = ctx.executor().schedule(new Runnable() {
            @Override
            public void run() {
                if (ctx.channel().isActive()) {
                    SslHandshakeTimeoutException exception = new SslHandshakeTimeoutException(
                        "handshake timed out after " + handshakeTimeoutMillis + "ms");
                    ctx.fireUserEventTriggered(new SniCompletionEvent(exception));
                    ctx.close();
                }
            }
        }, handshakeTimeoutMillis, TimeUnit.MILLISECONDS);
    }

    /**
     * 父类回调：解析 ClientHello 中的 SNI，再委托 {@link #lookup(ChannelHandlerContext, String)}。
     */
    @Override
    protected Future<T> lookup(ChannelHandlerContext ctx, ByteBuf clientHello) throws Exception {
        hostname = clientHello == null ? null : extractSniHostname(clientHello);

        return lookup(ctx, hostname);
    }

    /**
     * 查找完成后取消超时，调用子类 {@link #onLookupComplete}，并触发 SNI 完成事件。
     */
    @Override
    protected void onLookupComplete(ChannelHandlerContext ctx, Future<T> future) throws Exception {
        if (timeoutFuture != null) {
            timeoutFuture.cancel(false);
        }
        try {
            onLookupComplete(ctx, hostname, future);
        } finally {
            fireSniCompletionEvent(ctx, hostname, future);
        }
    }

    /**
     * Kicks off a lookup for the given SNI value and returns a {@link Future} which in turn will
     * notify the {@link #onLookupComplete(ChannelHandlerContext, String, Future)} on completion.
     *
     * @see #onLookupComplete(ChannelHandlerContext, String, Future)
     *
     * <p>子类实现：根据 hostname 异步查找 SSL 上下文或处理器配置（如虚拟主机映射）。</p>
     */
    protected abstract Future<T> lookup(ChannelHandlerContext ctx, String hostname) throws Exception;

    /**
     * Called upon completion of the {@link #lookup(ChannelHandlerContext, String)} {@link Future}.
     *
     * @see #lookup(ChannelHandlerContext, String)
     *
     * <p>子类实现：lookup 成功后替换 pipeline、安装 {@link SslHandler} 等。</p>
     */
    protected abstract void onLookupComplete(ChannelHandlerContext ctx,
                                             String hostname, Future<T> future) throws Exception;

    /** 向 pipeline 发送 {@link SniCompletionEvent}，携带 hostname 与 lookup 成败。 */
    private static void fireSniCompletionEvent(ChannelHandlerContext ctx, String hostname, Future<?> future) {
        Throwable cause = future.cause();
        if (cause == null) {
            ctx.fireUserEventTriggered(new SniCompletionEvent(hostname));
        } else {
            ctx.fireUserEventTriggered(new SniCompletionEvent(hostname, cause));
        }
    }
}
