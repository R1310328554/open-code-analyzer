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

package io.netty.handler.proxy;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.socksx.v5.DefaultSocks5InitialRequest;
import io.netty.handler.codec.socksx.v5.DefaultSocks5CommandRequest;
import io.netty.handler.codec.socksx.v5.DefaultSocks5PasswordAuthRequest;
import io.netty.handler.codec.socksx.v5.DefaultSocks5PrivateAuthRequest;
import io.netty.handler.codec.socksx.v5.Socks5AddressType;
import io.netty.handler.codec.socksx.v5.Socks5AuthMethod;
import io.netty.handler.codec.socksx.v5.Socks5InitialRequest;
import io.netty.handler.codec.socksx.v5.Socks5InitialResponse;
import io.netty.handler.codec.socksx.v5.Socks5InitialResponseDecoder;
import io.netty.handler.codec.socksx.v5.Socks5ClientEncoder;
import io.netty.handler.codec.socksx.v5.Socks5CommandResponse;
import io.netty.handler.codec.socksx.v5.Socks5CommandResponseDecoder;
import io.netty.handler.codec.socksx.v5.Socks5CommandStatus;
import io.netty.handler.codec.socksx.v5.Socks5CommandType;
import io.netty.handler.codec.socksx.v5.Socks5PasswordAuthResponse;
import io.netty.handler.codec.socksx.v5.Socks5PasswordAuthResponseDecoder;
import io.netty.handler.codec.socksx.v5.Socks5PasswordAuthStatus;
import io.netty.handler.codec.socksx.v5.Socks5PrivateAuthResponse;
import io.netty.handler.codec.socksx.v5.Socks5PrivateAuthResponseDecoder;
import io.netty.handler.codec.socksx.v5.Socks5PrivateAuthStatus;
import io.netty.util.NetUtil;
import io.netty.util.internal.StringUtil;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Arrays;
import java.util.Collections;

/**
 * 使用
 * <a href="https://www.rfc-editor.org/rfc/rfc1928">SOCKS 协议第 5 版</a>
 * 建立盲转发代理隧道的 handler。
 */
public final class Socks5ProxyHandler extends ProxyHandler {

    private static final String PROTOCOL = "socks5";
    private static final String AUTH_PASSWORD = "password";
    private static final String AUTH_PRIVATE = "private";

    private static final byte NO_PRIVATE_AUTH_METHOD =
        Socks5AuthMethod.NO_AUTH.byteValue();

    /** 仅请求无认证的初始握手消息 */
    private static final Socks5InitialRequest INIT_REQUEST_NO_AUTH =
            new DefaultSocks5InitialRequest(Collections.singletonList(Socks5AuthMethod.NO_AUTH));

    /** 请求无认证或密码认证的初始握手消息 */
    private static final Socks5InitialRequest INIT_REQUEST_PASSWORD =
            new DefaultSocks5InitialRequest(Arrays.asList(Socks5AuthMethod.NO_AUTH, Socks5AuthMethod.PASSWORD));

    /** 用户名；无密码认证时为 null */
    private final String username;
    /** 密码 */
    private final String password;
    /** 私有认证方法字节码 */
    private final byte privateAuthMethod;
    /** 私有认证令牌 */
    private final byte[] privateToken;
    /** SOCKS5 客户端编码器 */
    private final Socks5ClientEncoder clientEncoder;

    private String decoderName;
    private String encoderName;

    public Socks5ProxyHandler(SocketAddress proxyAddress) {
        this(proxyAddress, null, null);
    }

    public Socks5ProxyHandler(SocketAddress proxyAddress, String username, String password) {
        super(proxyAddress);
        if (username != null && username.isEmpty()) {
            username = null;
        }
        if (password != null && password.isEmpty()) {
            password = null;
        }
        this.username = username;
        this.password = password;
        this.privateToken = null;
        this.privateAuthMethod = NO_PRIVATE_AUTH_METHOD; // 未指定私有认证方法
        this.clientEncoder = Socks5ClientEncoder.DEFAULT;
    }

    /**
     * 使用自定义私有认证方法创建 SOCKS5 代理 handler。
     *
     * @param proxyAddress     SOCKS5 代理服务器地址
     * @param privateAuthMethod 私有认证方法码（须在 0x80–0xFE 范围内）
     * @param privateToken     私有认证使用的令牌
     * @param customEncoder    自定义 SOCKS5 消息编码器；为 {@code null} 时使用
     *                         {@link Socks5ClientEncoder#DEFAULT}
     * @throws IllegalArgumentException 若 privateAuthMethod 不在有效范围内
     */
    public Socks5ProxyHandler(SocketAddress proxyAddress, byte privateAuthMethod, byte[] privateToken,
                              Socks5ClientEncoder customEncoder) {
        super(proxyAddress);
        if (!Socks5AuthMethod.isPrivateMethod(privateAuthMethod)) {
            throw new IllegalArgumentException(
                    "privateAuthMethod: " + (privateAuthMethod & 0xFF) + " (expected: 0x80-0xFE)");
        }
        this.username = this.password = null;
        this.privateToken = privateToken;
        this.privateAuthMethod = privateAuthMethod;
        this.clientEncoder = customEncoder != null ? customEncoder : Socks5ClientEncoder.DEFAULT;
    }

    @Override
    public String protocol() {
        return PROTOCOL;
    }

    @Override
    public String authScheme() {
        Socks5AuthMethod authMethod = socksAuthMethod();
        if (Socks5AuthMethod.isPrivateMethod(authMethod.byteValue())) {
            return AUTH_PRIVATE;
        }
        if (authMethod == Socks5AuthMethod.PASSWORD) {
            return AUTH_PASSWORD;
        }
        return AUTH_NONE;
    }

    public String username() {
        return username;
    }

    public String password() {
        return password;
    }

    @Override
    protected void addCodec(ChannelHandlerContext ctx) throws Exception {
        ChannelPipeline p = ctx.pipeline();
        String name = ctx.name();

        Socks5InitialResponseDecoder decoder = new Socks5InitialResponseDecoder();
        p.addBefore(name, null, decoder);

        decoderName = p.context(decoder).name();
        encoderName = decoderName + ".encoder";

        p.addBefore(name, encoderName, clientEncoder);
    }

    @Override
    protected void removeEncoder(ChannelHandlerContext ctx) throws Exception {
        ctx.pipeline().remove(encoderName);
    }

    @Override
    protected void removeDecoder(ChannelHandlerContext ctx) throws Exception {
        ChannelPipeline p = ctx.pipeline();
        if (p.context(decoderName) != null) {
            p.remove(decoderName);
        }
    }

    @Override
    protected Object newInitialMessage(ChannelHandlerContext ctx) throws Exception {
        Socks5AuthMethod authMethod = socksAuthMethod();
        if (authMethod == Socks5AuthMethod.PASSWORD) {
            return INIT_REQUEST_PASSWORD;
        }
        if (Socks5AuthMethod.isPrivateMethod(authMethod.byteValue())) {
            return new DefaultSocks5InitialRequest(Arrays.asList(Socks5AuthMethod.NO_AUTH,
                authMethod));
        }
        return INIT_REQUEST_NO_AUTH;
    }

    @Override
    protected boolean handleResponse(ChannelHandlerContext ctx, Object response) throws Exception {
        if (response instanceof Socks5InitialResponse) {
            Socks5InitialResponse res = (Socks5InitialResponse) response;
            Socks5AuthMethod authMethod = socksAuthMethod();
            Socks5AuthMethod resAuthMethod = res.authMethod();
            if (resAuthMethod != Socks5AuthMethod.NO_AUTH && resAuthMethod != authMethod
                && !Socks5AuthMethod.isPrivateMethod(resAuthMethod.byteValue())) {
                // 服务器既不允许匿名，也未接受请求的认证方式
                throw new ProxyConnectException(exceptionMessage("unexpected authMethod: " + res.authMethod()));
            }

            if (resAuthMethod == Socks5AuthMethod.NO_AUTH) {
                sendConnectCommand(ctx);
            } else if (resAuthMethod == Socks5AuthMethod.PASSWORD) {
                // 密码认证：发送认证请求
                ctx.pipeline().replace(decoderName, decoderName, new Socks5PasswordAuthResponseDecoder());
                sendToProxyServer(new DefaultSocks5PasswordAuthRequest(
                        username != null? username : "", password != null? password : ""));
            } else if (Socks5AuthMethod.isPrivateMethod(resAuthMethod.byteValue())) {
                ctx.pipeline().replace(decoderName, decoderName, new Socks5PrivateAuthResponseDecoder());
                sendToProxyServer(new DefaultSocks5PrivateAuthRequest(privateToken));
            } else {
                // 不应到达此处
                throw new Error("Unexpected authMethod: " + resAuthMethod);
            }

            return false;
        }

        if (response instanceof Socks5PasswordAuthResponse) {
            // 收到服务器认证响应
            Socks5PasswordAuthResponse res = (Socks5PasswordAuthResponse) response;
            if (res.status() != Socks5PasswordAuthStatus.SUCCESS) {
                throw new ProxyConnectException(exceptionMessage("authStatus: " + res.status()));
            }

            sendConnectCommand(ctx);
            return false;
        }

        if (response instanceof Socks5PrivateAuthResponse) {
            Socks5PrivateAuthResponse res = (Socks5PrivateAuthResponse) response;
            if (res.status() != Socks5PrivateAuthStatus.SUCCESS) {
                throw new ProxyConnectException(exceptionMessage("privateAuthStatus: " + res.status()));
            }

            sendConnectCommand(ctx);
            return false;
        }

        // 应为来自服务器的最后一条消息
        Socks5CommandResponse res = (Socks5CommandResponse) response;
        if (res.status() != Socks5CommandStatus.SUCCESS) {
            throw new ProxyConnectException(exceptionMessage("status: " + res.status()));
        }

        return true;
    }

    /** 根据配置确定 SOCKS 认证方式 */
    private Socks5AuthMethod socksAuthMethod() {
        Socks5AuthMethod authMethod;
        if (privateToken != null && privateToken.length > 0) {
            authMethod = new Socks5AuthMethod(privateAuthMethod & 0xFF, "PRIVATE_" + (privateAuthMethod & 0xFF));
        } else if (username == null && password == null) {
            authMethod = Socks5AuthMethod.NO_AUTH;
        } else {
            authMethod = Socks5AuthMethod.PASSWORD;
        }
        return authMethod;
    }

    /** 发送 CONNECT 命令并切换为命令响应解码器 */
    private void sendConnectCommand(ChannelHandlerContext ctx) throws Exception {
        InetSocketAddress raddr = destinationAddress();
        Socks5AddressType addrType;
        String rhost;
        if (raddr.isUnresolved()) {
            addrType = Socks5AddressType.DOMAIN;
            rhost = raddr.getHostString();
        } else {
            rhost = raddr.getAddress().getHostAddress();
            if (NetUtil.isValidIpV4Address(rhost)) {
                addrType = Socks5AddressType.IPv4;
            } else if (NetUtil.isValidIpV6Address(rhost)) {
                addrType = Socks5AddressType.IPv6;
            } else {
                throw new ProxyConnectException(
                        exceptionMessage("unknown address type: " + StringUtil.simpleClassName(rhost)));
            }
        }

        ctx.pipeline().replace(decoderName, decoderName, new Socks5CommandResponseDecoder());
        sendToProxyServer(new DefaultSocks5CommandRequest(Socks5CommandType.CONNECT, addrType, rhost, raddr.getPort()));
    }
}
