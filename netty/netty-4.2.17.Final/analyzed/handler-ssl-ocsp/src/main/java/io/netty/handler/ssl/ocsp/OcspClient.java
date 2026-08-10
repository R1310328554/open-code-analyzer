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

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoop;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.resolver.dns.DnsNameResolver;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.FutureListener;
import io.netty.util.concurrent.GenericFutureListener;
import io.netty.util.concurrent.Promise;
import io.netty.util.internal.ObjectUtil;
import io.netty.util.internal.SystemPropertyUtil;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.x509.AccessDescription;
import org.bouncycastle.asn1.x509.AuthorityInformationAccess;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.Extensions;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509CertificateHolder;
import org.bouncycastle.cert.ocsp.BasicOCSPResp;
import org.bouncycastle.cert.ocsp.CertificateID;
import org.bouncycastle.cert.ocsp.OCSPException;
import org.bouncycastle.cert.ocsp.OCSPReqBuilder;
import org.bouncycastle.cert.ocsp.OCSPResp;
import org.bouncycastle.operator.ContentVerifierProvider;
import org.bouncycastle.operator.DigestCalculatorProvider;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentVerifierProviderBuilder;
import org.bouncycastle.operator.jcajce.JcaDigestCalculatorProviderBuilder;

import java.net.InetAddress;
import java.net.URL;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertPathBuilder;
import java.security.cert.CertPathBuilderException;
import java.security.cert.CertStore;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CollectionCertStoreParameters;
import java.security.cert.PKIXBuilderParameters;
import java.security.cert.TrustAnchor;
import java.security.cert.X509CertSelector;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static io.netty.handler.codec.http.HttpMethod.POST;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;
import static io.netty.handler.ssl.ocsp.OcspHttpHandler.OCSP_REQUEST_TYPE;
import static io.netty.handler.ssl.ocsp.OcspHttpHandler.OCSP_RESPONSE_TYPE;
import static io.netty.util.internal.ObjectUtil.checkNotNull;
import static org.bouncycastle.asn1.ocsp.OCSPObjectIdentifiers.id_pkix_ocsp_nonce;
import static org.bouncycastle.asn1.x509.X509ObjectIdentifiers.id_ad_ocsp;
import static org.bouncycastle.cert.ocsp.CertificateID.HASH_SHA1;

/**
 * OCSP 客户端工具类：构建 OCSP 请求、经 HTTP/1.1 查询响应器并校验签名与 nonce。
 */
final class OcspClient {

    private static final InternalLogger logger = InternalLoggerFactory.getInstance(OcspClient.class);

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    /** OCSP HTTP 响应体最大聚合大小（可通过系统属性配置） */
    private static final int OCSP_RESPONSE_MAX_SIZE = SystemPropertyUtil.getInt(
            "io.netty.ocsp.responseSize", 1024 * 10);

    static {
        logger.debug("-Dio.netty.ocsp.responseSize: {} bytes", OCSP_RESPONSE_MAX_SIZE);
    }

    /**
     * 通过 OCSP 查询证书状态。
     *
     * @param x509Certificate       待校验的客户端 {@link X509Certificate}
     * @param issuer                客户端证书的颁发者 {@link X509Certificate}
     * @param validateResponseNonce 为 {@code true} 时启用 OCSP 响应 nonce 校验
     * @param ioTransport           使用的 {@link IoTransport}
     * @param responsePromise      {@link BasicOCSPResp} 的 {@link Promise}
     */
    static void query(final X509Certificate x509Certificate,
                                        final X509Certificate issuer, final boolean validateResponseNonce,
                                        final IoTransport ioTransport, final DnsNameResolver dnsNameResolver,
                                        final Promise<BasicOCSPResp> responsePromise) {
        final EventLoop eventLoop = ioTransport.eventLoop();
        eventLoop.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    DigestCalculatorProvider digestCalculatorProvider = new JcaDigestCalculatorProviderBuilder()
                            .build();

                    CertificateID certificateID = new CertificateID(digestCalculatorProvider.get(HASH_SHA1),
                            new JcaX509CertificateHolder(issuer),
                            x509Certificate.getSerialNumber());

                    // 构建 OCSP 请求并加入 CertificateID
                    OCSPReqBuilder builder = new OCSPReqBuilder();
                    builder.addRequest(certificateID);

                    // 生成 16 字节 nonce 并加入请求（RFC 8954 §2.1 要求响应器至少接受 16 字节）
                    byte[] nonce = new byte[16];
                    SECURE_RANDOM.nextBytes(nonce);
                    final DEROctetString derNonce = new DEROctetString(nonce);
                    builder.setRequestExtensions(new Extensions(new Extension(id_pkix_ocsp_nonce, false, derNonce)));

                    // 从证书解析 OCSP URL 并发起查询
                    URL uri = new URL(parseOcspUrlFromCertificate(x509Certificate));

                    // 确定端口
                    int port = uri.getPort();
                    if (port == -1) {
                        port = uri.getDefaultPort();
                    }

                    // 构造 HTTP 路径
                    String path = uri.getPath();
                    if (path.isEmpty()) {
                        path = "/";
                    } else {
                        if (uri.getQuery() != null) {
                            path = path + '?' + uri.getQuery();
                        }
                    }

                    Promise<OCSPResp> ocspResponsePromise = query(eventLoop,
                            Unpooled.wrappedBuffer(builder.build().getEncoded()),
                            uri.getHost(), port, path, ioTransport, dnsNameResolver);

                    // 收到响应后校验
                    ocspResponsePromise.addListener((GenericFutureListener<Future<OCSPResp>>) future -> {
                        if (future.isSuccess()) {
                            final Object responseObject;
                            try {
                                responseObject = future.getNow().getResponseObject();
                            } catch (OCSPException e) {
                                responsePromise.setFailure(future.cause());
                                return;
                            }
                            if (responseObject instanceof BasicOCSPResp) {
                                validateResponse(x509Certificate, digestCalculatorProvider, responsePromise,
                                        (BasicOCSPResp) responseObject, derNonce, issuer, validateResponseNonce);
                            } else {
                                responsePromise.tryFailure(new OCSPException("Unsupported OCSP response type: "
                                        + (responseObject == null ? null : responseObject.getClass())));
                            }
                        } else {
                            responsePromise.tryFailure(future.cause());
                        }
                    });
                } catch (Exception ex) {
                    responsePromise.tryFailure(ex);
                }
            }
        });
    }

    /**
     * 经 HTTP/1.1 向 OCSP 响应器查询证书状态。
     *
     * @param eventLoop   执行 HTTP 请求的 {@link EventLoop}
     * @param ocspRequest 含 OCSP 请求数据的 {@link ByteBuf}
     * @param host        OCSP 响应器主机名
     * @param port        OCSP 响应器端口
     * @param path        OCSP 响应器路径
     * @param ioTransport 使用的 {@link IoTransport}
     * @return 包含 {@link OCSPResp} 的 {@link Promise}
     */
    private static Promise<OCSPResp> query(final EventLoop eventLoop, final ByteBuf ocspRequest,
                                           final String host, final int port, final String path,
                                           final IoTransport ioTransport, final DnsNameResolver dnsNameResolver) {
        final Promise<OCSPResp> responsePromise = eventLoop.newPromise();

        try {
            final Bootstrap bootstrap = new Bootstrap()
                    .group(ioTransport.eventLoop())
                    .option(ChannelOption.TCP_NODELAY, true)
                    .channelFactory(ioTransport.socketChannel())
                    .attr(OcspServerCertificateValidator.OCSP_PIPELINE_ATTRIBUTE, Boolean.TRUE)
                    .handler(new Initializer(responsePromise, 10 * 1000));
            dnsNameResolver.resolve(host).addListener((FutureListener<InetAddress>) future -> {

                // DNS 解析成功则连接 OCSP 服务器，否则标记失败
                if (future.isSuccess()) {
                    InetAddress hostAddress = future.getNow();
                    final ChannelFuture channelFuture = bootstrap.connect(hostAddress, port);
                    channelFuture.addListener(f -> {
                        if (f.isSuccess()) {
                            FullHttpRequest request = new DefaultFullHttpRequest(HTTP_1_1, POST, path,
                                    ocspRequest);
                            request.headers().add(HttpHeaderNames.HOST, host);
                            request.headers().add(HttpHeaderNames.USER_AGENT, "Netty OCSP Client");
                            request.headers().add(HttpHeaderNames.CONTENT_TYPE, OCSP_REQUEST_TYPE);
                            request.headers().add(HttpHeaderNames.ACCEPT_ENCODING, OCSP_RESPONSE_TYPE);
                            request.headers().add(HttpHeaderNames.CONTENT_LENGTH, ocspRequest.readableBytes());

                            // 发送 OCSP HTTP 请求
                            channelFuture.channel().writeAndFlush(request);
                        } else {
                            responsePromise.tryFailure(new IllegalStateException(
                                    "Connection to OCSP Responder Failed", f.cause()));
                        }
                    });
                } else {
                    responsePromise.tryFailure(future.cause());
                }
            });
        } catch (Exception ex) {
            responsePromise.tryFailure(ex);
        }

        return responsePromise;
    }

    /** 校验 OCSP 响应：条目数、CertID、可选 nonce 与签名 */
    private static void validateResponse(
            X509Certificate x509Certificate, DigestCalculatorProvider digestCalculatorProvider,
            Promise<BasicOCSPResp> responsePromise, BasicOCSPResp basicResponse,
            DEROctetString derNonce, X509Certificate issuer, boolean validateNonce) {
        try {
            // 仅请求一张证书，响应条目数须为 1
            int responses = basicResponse.getResponses().length;
            if (responses != 1) {
                responsePromise.tryFailure(
                        new IllegalArgumentException("Expected number of responses was 1 but got: " + responses));
                return;
            }

            CertificateID respCertId = basicResponse.getResponses()[0].getCertID();
            if (!respCertId.matchesIssuer(new JcaX509CertificateHolder(issuer), digestCalculatorProvider)
                    || !respCertId.getSerialNumber().equals(x509Certificate.getSerialNumber())) {
                responsePromise.tryFailure(
                        new CertificateException("OCSP response CertID does not match queried certificate"));
                return;
            }

            if (validateNonce) {
                validateNonce(basicResponse, derNonce);
            }
            validateSignature(basicResponse, issuer);
            responsePromise.trySuccess(basicResponse);
        } catch (Exception ex) {
            responsePromise.tryFailure(ex);
        }
    }

    /**
     * 校验 OCSP 响应中的 nonce 是否与请求一致。
     */
    private static void validateNonce(BasicOCSPResp basicResponse, DEROctetString encodedNonce) throws OCSPException {
        Extension nonceExt = basicResponse.getExtension(id_pkix_ocsp_nonce);
        if (nonceExt != null) {
            DEROctetString responseNonceString = (DEROctetString) nonceExt.getExtnValue();
            if (!responseNonceString.equals(encodedNonce)) {
                throw new OCSPException("Nonce does not match");
            }
        } else {
            throw new IllegalArgumentException("Nonce is not present");
        }
    }

    /**
     * 校验 OCSP 响应签名（使用响应器证书或颁发者证书）。
     */
    static void validateSignature(BasicOCSPResp resp, X509Certificate issuerCertificate) throws OCSPException {
        try {
            X509CertificateHolder[] certs = resp.getCerts();
            JcaContentVerifierProviderBuilder providerBuilder = new JcaContentVerifierProviderBuilder();

            // 响应中包含证书时，验证证书链
            if (certs != null && certs.length > 0) {

                // 使用响应中第一张证书验证 OCSP 签名
                X509CertificateHolder responderCert = certs[0];

                ContentVerifierProvider responderVerifier = providerBuilder.build(responderCert);

                if (!resp.isSignatureValid(responderVerifier)) {
                    throw new OCSPException("OCSP response signature is not valid");
                }

                // 用 CertPathBuilder 构建从响应器到颁发者的证书链
                validateCertificateChain(responderCert, certs, issuerCertificate);
            } else {
                // 无嵌入证书时使用颁发者证书验证签名
                ContentVerifierProvider issuerVerifier = providerBuilder.build(issuerCertificate);

                if (!resp.isSignatureValid(issuerVerifier)) {
                    throw new OCSPException("OCSP response signature is not valid");
                }
            }
        } catch (OperatorCreationException e) {
            throw new OCSPException("Error validating OCSP-Signature", e);
        } catch (CertificateException e) {
            throw new OCSPException("Error while processing certificates for OCSP signature validation", e);
        }
    }

    /**
     * 验证从 OCSP 响应器证书到颁发者的证书链是否可构建。
     * 使用 Java {@link CertPathBuilder} 构造并校验路径。
     */
    private static void validateCertificateChain(X509CertificateHolder responderCert,
                                                   X509CertificateHolder[] allCerts,
                                                   X509Certificate issuerCertificate) throws OCSPException {
        try {
            // BouncyCastle 证书持有者转为 Java X509Certificate
            List<X509Certificate> certList = new ArrayList<>(allCerts.length);
            for (X509CertificateHolder certHolder : allCerts) {
                certList.add(new JcaX509CertificateConverter().getCertificate(certHolder));
            }

            CertStore certStore = CertStore.getInstance("Collection",
                    new CollectionCertStoreParameters(certList));

            X509CertSelector targetConstraints = new X509CertSelector();
            targetConstraints.setCertificate(new JcaX509CertificateConverter().getCertificate(responderCert));

            TrustAnchor trustAnchor = new TrustAnchor(issuerCertificate, null);

            PKIXBuilderParameters pkixParams = new PKIXBuilderParameters(
                    Collections.singleton(trustAnchor), targetConstraints);
            pkixParams.addCertStore(certStore);
            pkixParams.setRevocationEnabled(false); // 校验 OCSP 响应本身时不查吊销

            CertPathBuilder builder = CertPathBuilder.getInstance("PKIX");
            builder.build(pkixParams);

            // 执行到此表示链有效
        } catch (CertPathBuilderException e) {
            throw new OCSPException("OCSP responder certificate is not trusted by issuer: " + e.getMessage(), e);
        } catch (InvalidAlgorithmParameterException | NoSuchAlgorithmException e) {
            throw new OCSPException("Error setting up certificate path validation", e);
        } catch (CertificateException e) {
            throw new OCSPException("Error converting certificates for path validation", e);
        }
    }

    /**
     * 从证书的 AIA 扩展解析 OCSP 端点 URL。
     *
     * @param cert 待解析证书
     * @return OCSP 端点 URL
     * @throws NullPointerException     无法定位 OCSP 响应器 URL 时
     * @throws IllegalArgumentException 无法将 X509Certificate 转为 JcaX509CertificateHolder 时
     */
    private static String parseOcspUrlFromCertificate(X509Certificate cert) {
        X509CertificateHolder holder;
        try {
            holder = new JcaX509CertificateHolder(cert);
        } catch (CertificateEncodingException e) {
            throw new IllegalArgumentException("Error while parsing X509Certificate into JcaX509CertificateHolder", e);
        }

        AuthorityInformationAccess aiaExtension = AuthorityInformationAccess.fromExtensions(holder.getExtensions());

        // 查找 OCSP 响应器 URL
        if (aiaExtension != null) {
            for (AccessDescription accessDescription : aiaExtension.getAccessDescriptions()) {
                if (accessDescription.getAccessMethod().equals(id_ad_ocsp)) {
                    return accessDescription.getAccessLocation().getName().toASN1Primitive().toString();
                }
            }
        }

        throw new NoOcspResponderException("Unable to find OCSP responder URL in Certificate");
    }

    /** 初始化 OCSP HTTP 客户端 pipeline */
    static final class Initializer extends ChannelInitializer<SocketChannel> {

        private final Promise<OCSPResp> responsePromise;
        private final long timeoutMillis;

        Initializer(Promise<OCSPResp> responsePromise, long timeoutMillis) {
            this.responsePromise = checkNotNull(responsePromise, "responsePromise");
            this.timeoutMillis = ObjectUtil.checkPositive(timeoutMillis, "timeoutMillis");
        }

        @Override
        protected void initChannel(SocketChannel socketChannel) {
            ChannelPipeline pipeline = socketChannel.pipeline();
            pipeline.addLast(new HttpClientCodec());
            pipeline.addLast(new HttpObjectAggregator(OCSP_RESPONSE_MAX_SIZE));
            pipeline.addLast(new OcspHttpHandler(responsePromise, timeoutMillis));
        }
    }

    private OcspClient() {
        // 禁止外部实例化
    }
}
