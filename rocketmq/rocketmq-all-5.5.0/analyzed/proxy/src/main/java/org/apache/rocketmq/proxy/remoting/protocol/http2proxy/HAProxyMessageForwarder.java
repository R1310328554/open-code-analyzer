/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.apache.rocketmq.proxy.remoting.protocol.http2proxy;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.haproxy.HAProxyCommand;
import io.netty.handler.codec.haproxy.HAProxyMessage;
import io.netty.handler.codec.haproxy.HAProxyProtocolVersion;
import io.netty.handler.codec.haproxy.HAProxyProxiedProtocol;
import io.netty.handler.codec.haproxy.HAProxyTLV;
import io.netty.util.Attribute;
import io.netty.util.DefaultAttributeMap;
import java.lang.reflect.Field;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.rocketmq.acl.common.AclUtils;
import org.apache.rocketmq.common.constant.CommonConstants;
import org.apache.rocketmq.common.constant.HAProxyConstants;
import org.apache.rocketmq.common.constant.LoggerName;
import org.apache.rocketmq.logging.org.slf4j.Logger;
import org.apache.rocketmq.logging.org.slf4j.LoggerFactory;
import org.apache.rocketmq.remoting.common.RemotingHelper;
import org.apache.rocketmq.remoting.netty.AttributeKeys;

/**
 * HAProxy 消息转发器：将 Remoting 侧 PROXY 协议信息同步至 gRPC 出站通道。
 */
public class HAProxyMessageForwarder extends ChannelInboundHandlerAdapter {

    private static final Logger log = LoggerFactory.getLogger(LoggerName.ROCKETMQ_REMOTING_NAME);

    private static final Field FIELD_ATTRIBUTE =
        FieldUtils.getField(DefaultAttributeMap.class, "attributes", true);

    /** 目标 gRPC 出站 {@link Channel}。 */
    private final Channel outboundChannel;

    /** 绑定待写入 HAProxy 消息的出站通道。 */
    public HAProxyMessageForwarder(final Channel outboundChannel) {
        this.outboundChannel = outboundChannel;
    }

    @Override
    /** 首包到达时转发 PROXY 信息并继续传递原始消息。 */
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        try {
            forwardHAProxyMessage(ctx.channel(), outboundChannel);
            ctx.fireChannelRead(msg);
        } catch (Exception e) {
            log.error("Forward HAProxyMessage from Remoting to gRPC server error.", e);
            throw e;
        } finally {
            ctx.pipeline().remove(this);
        }
    }

    /** 构建 {@link HAProxyMessage} 并同步写入出站通道。 */
    private void forwardHAProxyMessage(Channel inboundChannel, Channel outboundChannel) throws Exception {
        if (!(inboundChannel instanceof DefaultAttributeMap)) {
            return;
        }

        HAProxyMessage message = buildHAProxyMessage(inboundChannel);
        if (message == null) {
            return;
        }

        outboundChannel.writeAndFlush(message).sync();
    }

    /** 从通道属性或本地/远程地址组装 HAProxy v2 PROXY 消息。 */
    protected HAProxyMessage buildHAProxyMessage(Channel inboundChannel) throws IllegalAccessException, DecoderException {
        String sourceAddress = null, destinationAddress = null;
        int sourcePort = 0, destinationPort = 0;
        if (inboundChannel.hasAttr(AttributeKeys.PROXY_PROTOCOL_ADDR)) {
            Attribute<?>[] attributes = (Attribute<?>[]) FieldUtils.readField(FIELD_ATTRIBUTE, inboundChannel);
            if (ArrayUtils.isEmpty(attributes)) {
                return null;
            }
            for (Attribute<?> attribute : attributes) {
                String attributeKey = attribute.key().name();
                if (!StringUtils.startsWith(attributeKey, HAProxyConstants.PROXY_PROTOCOL_PREFIX)) {
                    continue;
                }
                String attributeValue = (String) attribute.get();
                if (StringUtils.isEmpty(attributeValue)) {
                    continue;
                }
                if (attribute.key() == AttributeKeys.PROXY_PROTOCOL_ADDR) {
                    sourceAddress = attributeValue;
                }
                if (attribute.key() == AttributeKeys.PROXY_PROTOCOL_PORT) {
                    sourcePort = Integer.parseInt(attributeValue);
                }
                if (attribute.key() == AttributeKeys.PROXY_PROTOCOL_SERVER_ADDR) {
                    destinationAddress = attributeValue;
                }
                if (attribute.key() == AttributeKeys.PROXY_PROTOCOL_SERVER_PORT) {
                    destinationPort = Integer.parseInt(attributeValue);
                }
            }
        } else {
            String remoteAddr = RemotingHelper.parseChannelRemoteAddr(inboundChannel);
            sourceAddress = StringUtils.substringBeforeLast(remoteAddr, CommonConstants.COLON);
            sourcePort = Integer.parseInt(StringUtils.substringAfterLast(remoteAddr, CommonConstants.COLON));

            String localAddr = RemotingHelper.parseChannelLocalAddr(inboundChannel);
            destinationAddress = StringUtils.substringBeforeLast(localAddr, CommonConstants.COLON);
            destinationPort = Integer.parseInt(StringUtils.substringAfterLast(localAddr, CommonConstants.COLON));
        }

        HAProxyProxiedProtocol proxiedProtocol = AclUtils.isColon(sourceAddress) ? HAProxyProxiedProtocol.TCP6 :
            HAProxyProxiedProtocol.TCP4;

        List<HAProxyTLV> haProxyTLVs = buildHAProxyTLV(inboundChannel);

        return new HAProxyMessage(HAProxyProtocolVersion.V2, HAProxyCommand.PROXY,
            proxiedProtocol, sourceAddress, destinationAddress, sourcePort, destinationPort, haProxyTLVs);
    }

    /** 收集以 TLV 前缀标记的通道扩展属性。 */
    protected List<HAProxyTLV> buildHAProxyTLV(Channel inboundChannel) throws IllegalAccessException, DecoderException {
        List<HAProxyTLV> result = new ArrayList<>();
        if (!inboundChannel.hasAttr(AttributeKeys.PROXY_PROTOCOL_ADDR)) {
            return result;
        }
        Attribute<?>[] attributes = (Attribute<?>[]) FieldUtils.readField(FIELD_ATTRIBUTE, inboundChannel);
        if (ArrayUtils.isEmpty(attributes)) {
            return result;
        }
        for (Attribute<?> attribute : attributes) {
            String attributeKey = attribute.key().name();
            if (!StringUtils.startsWith(attributeKey, HAProxyConstants.PROXY_PROTOCOL_TLV_PREFIX)) {
                continue;
            }
            String attributeValue = (String) attribute.get();
            HAProxyTLV haProxyTLV = buildHAProxyTLV(attributeKey, attributeValue);
            if (haProxyTLV != null) {
                result.add(haProxyTLV);
            }
        }
        return result;
    }

    /** 将单个 TLV 属性键值对解码为 {@link HAProxyTLV}。 */
    protected HAProxyTLV buildHAProxyTLV(String attributeKey, String attributeValue) throws DecoderException {
        String typeString = StringUtils.substringAfter(attributeKey, HAProxyConstants.PROXY_PROTOCOL_TLV_PREFIX);
        ByteBuf byteBuf = Unpooled.buffer();
        byteBuf.writeBytes(attributeValue.getBytes(Charset.defaultCharset()));
        return new HAProxyTLV(Hex.decodeHex(typeString)[0], byteBuf);
    }
}
