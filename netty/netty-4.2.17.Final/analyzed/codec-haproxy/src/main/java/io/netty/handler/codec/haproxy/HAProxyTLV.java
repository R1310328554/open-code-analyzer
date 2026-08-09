/*
 * Copyright 2016 The Netty Project
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

package io.netty.handler.codec.haproxy;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.DefaultByteBufHolder;
import io.netty.util.internal.StringUtil;

import static io.netty.util.internal.ObjectUtil.*;

/**
 * PROXY 协议 v2 的 Type-Length-Value 扩展向量。
 * <p>
 * 可携带 ALPN、Authority、SSL 等附加信息；内容以 {@link ByteBuf} 持有并参与引用计数。
 *
 * @see HAProxySSLTLV
 */
public class HAProxyTLV extends DefaultByteBufHolder {

    private final Type type;
    private final byte typeByteValue;

    /** 返回本 TLV 在报文中的总字节数（type 1 + length 2 + content）。 */
    int totalNumBytes() {
        return 3 + contentNumBytes(); // type(1) + length(2) + content
    }

    int contentNumBytes() {
        return content().readableBytes();
    }

    /** PROXY 协议 1.5 规范注册的 TLV 类型。 */
    public enum Type {
        PP2_TYPE_ALPN,
        PP2_TYPE_AUTHORITY,
        PP2_TYPE_SSL,
        PP2_TYPE_SSL_VERSION,
        PP2_TYPE_SSL_CN,
        PP2_TYPE_NETNS,
        /** 规范未定义的自定义 TLV 类型。 */
        OTHER;

        /**
         * 按规范字节值解析 {@link Type}；非官方值返回 {@link Type#OTHER}。
         *
         * @param byteValue the byte for a type
         *
         * @return the {@link Type} of a TLV
         */
        public static Type typeForByteValue(byte byteValue) {
            switch (byteValue) {
            case 0x01:
                return PP2_TYPE_ALPN;
            case 0x02:
                return PP2_TYPE_AUTHORITY;
            case 0x20:
                return PP2_TYPE_SSL;
            case 0x21:
                return PP2_TYPE_SSL_VERSION;
            case 0x22:
                return PP2_TYPE_SSL_CN;
            case 0x30:
                return PP2_TYPE_NETNS;
            default:
                return OTHER;
            }
        }

        /**
         * 返回规范定义的 {@link Type} 字节值。
         *
         * @param type the {@link Type}
         *
         * @return the byte value of the {@link Type}.
         */
        public static byte byteValueForType(Type type) {
            switch (type) {
            case PP2_TYPE_ALPN:
                return 0x01;
            case PP2_TYPE_AUTHORITY:
                return 0x02;
            case PP2_TYPE_SSL:
                return 0x20;
            case PP2_TYPE_SSL_VERSION:
                return 0x21;
            case PP2_TYPE_SSL_CN:
                return 0x22;
            case PP2_TYPE_NETNS:
                return 0x30;
            default:
                throw new IllegalArgumentException("unknown type: " + type);
            }
        }
    }

    /**
     * 按原始 type 字节创建 TLV（适用于非标准类型）。
     *
     * @param typeByteValue the byteValue of the TLV. This is especially important if non-standard TLVs are used
     * @param content the raw content of the TLV
     */
    public HAProxyTLV(byte typeByteValue, ByteBuf content) {
        this(Type.typeForByteValue(typeByteValue), typeByteValue, content);
    }

    /**
     * 按 {@link Type} 创建 TLV。
     *
     * @param type the {@link Type} of the TLV
     * @param content the raw content of the TLV
     */
    public HAProxyTLV(Type type, ByteBuf content) {
        this(type, Type.byteValueForType(type), content);
    }

    /** 包内构造，同时指定 {@link Type} 与原始 type 字节。 */
    HAProxyTLV(final Type type, final byte typeByteValue, final ByteBuf content) {
        super(content);
        this.type = checkNotNull(type, "type");
        this.typeByteValue = typeByteValue;
    }

    /** 返回本 TLV 的 {@link Type}。 */
    public Type type() {
        return type;
    }

    /** 返回 TLV 类型的原始字节值。 */
    public byte typeByteValue() {
        return typeByteValue;
    }

    @Override
    public HAProxyTLV copy() {
        return replace(content().copy());
    }

    @Override
    public HAProxyTLV duplicate() {
        return replace(content().duplicate());
    }

    @Override
    public HAProxyTLV retainedDuplicate() {
        return replace(content().retainedDuplicate());
    }

    @Override
    public HAProxyTLV replace(ByteBuf content) {
        return new HAProxyTLV(type, typeByteValue, content);
    }

    @Override
    public HAProxyTLV retain() {
        super.retain();
        return this;
    }

    @Override
    public HAProxyTLV retain(int increment) {
        super.retain(increment);
        return this;
    }

    @Override
    public HAProxyTLV touch() {
        super.touch();
        return this;
    }

    @Override
    public HAProxyTLV touch(Object hint) {
        super.touch(hint);
        return this;
    }

    @Override
    public String toString() {
        return StringUtil.simpleClassName(this) +
               "(type: " + type() +
               ", typeByteValue: " + typeByteValue() +
               ", content: " + contentToString() + ')';
    }
}
