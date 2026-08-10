/*
 * Copyright 2022 The Netty Project
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
package io.netty.handler.ssl.ocsp;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandler;
import io.netty.channel.ChannelPromise;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.ssl.SslHandshakeCompletionEvent;
import io.netty.resolver.dns.DnsNameResolver;
import io.netty.resolver.dns.DnsNameResolverBuilder;
import io.netty.util.AttributeKey;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import io.netty.util.concurrent.Promise;
import org.bouncycastle.cert.ocsp.BasicOCSPResp;
import org.bouncycastle.cert.ocsp.OCSPException;
import org.bouncycastle.cert.ocsp.RevokedStatus;
import org.bouncycastle.cert.ocsp.SingleResp;

import java.net.SocketAddress;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.List;

import static io.netty.util.internal.ObjectUtil.checkNotNull;

/**
 * {@link OcspServerCertificateValidator} 在 TLS 握手完成后通过 OCSP over HTTP/1.1
 * 校验服务端证书吊销状态。
 * <p>握手成功并触发 {@link SslHandshakeCompletionEvent#SUCCESS} 后，向证书 AIA 中的
 * OCSP 响应器发起查询并处理结果。</p>
 */
public class OcspServerCertificateValidator extends ByteToMessageDecoder implements ChannelOutboundHandler {
    /**
     * 标记由 {@link OcspServerCertificateValidator} 创建的全部 channel 的 pipeline 属性。
     */
    public static final AttributeKey<Boolean> OCSP_PIPELINE_ATTRIBUTE =
            AttributeKey.newInstance("io.netty.handler.ssl.ocsp.pipeline");

    /** 证书非 VALID 时是否关闭连接并抛异常 */
    private final boolean closeAndThrowIfNotValid;
    /** 是否强制校验 OCSP 响应 nonce */
    private final boolean validateNonce;
    /** I/O 传输配置 */
    private final IoTransport ioTransport;
    /** DNS 解析器 */
    private final DnsNameResolver dnsNameResolver;
    /** OCSP 查询是否进行中 */
    private boolean ocspQueryInProgress;
    /** OCSP 处理完成前是否有待处理的 read 请求 */
    private boolean readPending;

    /**
     * 创建新实例：不校验 nonce，使用默认 {@link IoTransport#DEFAULT} 与默认
     * {@link DnsNameResolver}，{@link #closeAndThrowIfNotValid} 为 {@code true}。
     */
    public OcspServerCertificateValidator() {
        this(false);
    }

    /**
     * 创建新实例：使用默认 {@link IoTransport#DEFAULT} 与默认 {@link DnsNameResolver}，
     * {@link #closeAndThrowIfNotValid} 为 {@code true}。
     *
     * @param validateNonce 为 {@code true} 时强制校验 OCSP 响应 nonce
     */
    public OcspServerCertificateValidator(boolean validateNonce) {
        this(validateNonce, IoTransport.DEFAULT);
    }

    /**
     * 创建新实例。
     *
     * @param validateNonce 为 {@code true} 时强制校验 OCSP 响应 nonce
     * @param ioTransport   使用的 {@link IoTransport}
     */
    public OcspServerCertificateValidator(boolean validateNonce, IoTransport ioTransport) {
        this(validateNonce, ioTransport, createDefaultResolver(ioTransport));
    }

    /**
     * 创建新实例，{@link #closeAndThrowIfNotValid} 为 {@code true}。
     *
     * @param validateNonce   为 {@code true} 时强制校验 OCSP 响应 nonce
     * @param ioTransport     使用的 {@link IoTransport}
     * @param dnsNameResolver 使用的 {@link DnsNameResolver}
     */
    public OcspServerCertificateValidator(boolean validateNonce, IoTransport ioTransport,
                                          DnsNameResolver dnsNameResolver) {
        this(true, validateNonce, ioTransport, dnsNameResolver);
    }

    /**
     * 创建新实例。
     *
     * @param closeAndThrowIfNotValid 为 {@code true} 时，证书非 {@link OcspResponse.Status#VALID}
     *                                则关闭 channel 并抛异常；为 {@code false} 时仅传播
     *                                {@link OcspValidationEvent} 由下游决定
     * @param validateNonce           为 {@code true} 时强制校验 OCSP 响应 nonce
     * @param ioTransport             使用的 {@link IoTransport}
     * @param dnsNameResolver         使用的 {@link DnsNameResolver}
     */
    public OcspServerCertificateValidator(boolean closeAndThrowIfNotValid, boolean validateNonce,
                                          IoTransport ioTransport, DnsNameResolver dnsNameResolver) {
        this.closeAndThrowIfNotValid = closeAndThrowIfNotValid;
        this.validateNonce = validateNonce;
        this.ioTransport = checkNotNull(ioTransport, "IoTransport");
        this.dnsNameResolver = checkNotNull(dnsNameResolver, "DnsNameResolver");
    }

    /** 基于 {@link IoTransport} 创建默认 DNS 解析器 */
    protected static DnsNameResolver createDefaultResolver(final IoTransport ioTransport) {
        return new DnsNameResolverBuilder()
                .eventLoop(ioTransport.eventLoop())
                .datagramChannelFactory(ioTransport.datagramChannel())
                .socketChannelFactory(ioTransport.socketChannel())
                .build();
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
        // OCSP 处理完成前仅缓冲入站数据，handler 移除后再正常解码
    }

    @Override
    public void userEventTriggered(final ChannelHandlerContext ctx, final Object evt) throws Exception {
        if (evt instanceof SslHandshakeCompletionEvent) {
            SslHandshakeCompletionEvent sslHandshakeCompletionEvent = (SslHandshakeCompletionEvent) evt;

            // TLS 握手成功后才执行 OCSP 校验；失败则转发事件并移除自身
            if (sslHandshakeCompletionEvent.isSuccess()) {
                Certificate[] certificates = ctx.pipeline().get(SslHandler.class)
                        .engine()
                        .getSession()
                        .getPeerCertificates();

                assert certificates.length >= 2 : "There must an end-entity certificate and issuer certificate";

                Promise<BasicOCSPResp> ocspRespPromise = ctx.executor().newPromise();
                OcspClient.query((X509Certificate) certificates[0], (X509Certificate) certificates[1],
                        validateNonce, ioTransport, dnsNameResolver, ocspRespPromise);
                ocspQueryInProgress = true;
                ocspRespPromise.addListener((GenericFutureListener<Future<BasicOCSPResp>>) future -> {
                    ocspQueryInProgress = false;
                    try {
                        if (future.isSuccess()) {
                            SingleResp response = future.getNow().getResponses()[0];

                            Date current = new Date();
                            Date thisUpdate = response.getThisUpdate();
                            Date nextUpdate = response.getNextUpdate();
                            if (thisUpdate == null || !current.after(thisUpdate) ||
                                    (nextUpdate != null && !current.before(nextUpdate))) {
                                ctx.fireExceptionCaught(new IllegalStateException("OCSP Response is out-of-date"));
                                return;
                            }

                            OcspResponse.Status status;
                            if (response.getCertStatus() == null) {
                                // null 表示证书有效
                                status = OcspResponse.Status.VALID;
                            } else if (response.getCertStatus() instanceof RevokedStatus) {
                                status = OcspResponse.Status.REVOKED;
                            } else {
                                status = OcspResponse.Status.UNKNOWN;
                            }

                            ctx.fireUserEventTriggered(new OcspValidationEvent(
                                    new OcspResponse(status, response.getThisUpdate(), response.getNextUpdate())));

                            // 证书非 VALID 且配置了 closeAndThrowIfNotValid 时关闭并抛错
                            if (status != OcspResponse.Status.VALID && closeAndThrowIfNotValid) {
                                ctx.fireExceptionCaught(new OCSPException(
                                        "Certificate not valid. Status: " + status));
                                ctx.close();
                            }
                        } else {
                            ctx.fireExceptionCaught(future.cause());
                            if (closeAndThrowIfNotValid) {
                                ctx.close();
                            }
                        }
                    } finally {
                        ctx.fireUserEventTriggered(evt);
                        ctx.pipeline().remove(this);
                        if (readPending) {
                            readPending = false;
                            ctx.read();
                        }
                    }
                });
            } else {
                ctx.fireUserEventTriggered(evt);
            }
        } else {
            ctx.fireUserEventTriggered(evt);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        ctx.close();
    }

    @Override
    public void bind(ChannelHandlerContext ctx, SocketAddress localAddress, ChannelPromise promise) throws Exception {
        ctx.bind(localAddress, promise);
    }

    @Override
    public void connect(ChannelHandlerContext ctx, SocketAddress remoteAddress,
                        SocketAddress localAddress, ChannelPromise promise) throws Exception {
        ctx.connect(remoteAddress, localAddress, promise);
    }

    @Override
    public void disconnect(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
        ctx.disconnect(promise);
    }

    @Override
    public void close(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
        ctx.close(promise);
    }

    @Override
    public void deregister(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
        ctx.deregister(promise);
    }

    @Override
    public void read(ChannelHandlerContext ctx) throws Exception {
        // OCSP 查询完成前暂停 read
        if (ocspQueryInProgress) {
            readPending = true;
        } else {
            readPending = false;
            ctx.read();
        }
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        ctx.write(msg, promise);
    }

    @Override
    public void flush(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }
}
