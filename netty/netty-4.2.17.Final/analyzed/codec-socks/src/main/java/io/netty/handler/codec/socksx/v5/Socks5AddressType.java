/*
 * Copyright 2013 The Netty Project
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

package io.netty.handler.codec.socksx.v5;

import io.netty.util.internal.ObjectUtil;

/**
 * The type of address in {@link Socks5CommandRequest} and {@link Socks5CommandResponse}.
 *
 * <p>SOCKS5 地址类型 ATYP（RFC 1928）：IPv4(0x01)、域名(0x03)、IPv6(0x04)。
 * 未知字节可通过 {@link #valueOf(byte)} 构造带 UNKNOWN 名称的实例。</p>
 */
public class Socks5AddressType implements Comparable<Socks5AddressType> {

    /** IPv4 地址，4 字节。 */
    public static final Socks5AddressType IPv4 = new Socks5AddressType(0x01, "IPv4");
    /** 域名，1 字节长度前缀 + ASCII。 */
    public static final Socks5AddressType DOMAIN = new Socks5AddressType(0x03, "DOMAIN");
    /** IPv6 地址，16 字节。 */
    public static final Socks5AddressType IPv6 = new Socks5AddressType(0x04, "IPv6");

    /** 将 wire 字节解析为地址类型；非标准值返回 UNKNOWN 实例。 */
    public static Socks5AddressType valueOf(byte b) {
        switch (b) {
        case 0x01:
            return IPv4;
        case 0x03:
            return DOMAIN;
        case 0x04:
            return IPv6;
        }

        return new Socks5AddressType(b);
    }

    private final byte byteValue;
    private final String name;
    private String text;

    public Socks5AddressType(int byteValue) {
        this(byteValue, "UNKNOWN");
    }

    public Socks5AddressType(int byteValue, String name) {
        this.name = ObjectUtil.checkNotNull(name, "name");
        this.byteValue = (byte) byteValue;
    }

    public byte byteValue() {
        return byteValue;
    }

    @Override
    public int hashCode() {
        return byteValue;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Socks5AddressType)) {
            return false;
        }

        return byteValue == ((Socks5AddressType) obj).byteValue;
    }

    @Override
    public int compareTo(Socks5AddressType o) {
        return byteValue - o.byteValue;
    }

    @Override
    public String toString() {
        String text = this.text;
        if (text == null) {
            this.text = text = name + '(' + (byteValue & 0xFF) + ')';
        }
        return text;
    }
}
