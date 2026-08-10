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
 * The type of {@link Socks5CommandRequest}.
 *
 * <p>SOCKS5 命令 CMD 字段（RFC 1928 §4）：CONNECT、BIND、UDP ASSOCIATE；
 * 未知 CMD 映射为 UNKNOWN 实例。</p>
 */
public class Socks5CommandType implements Comparable<Socks5CommandType> {

    /** 建立到目标主机的 TCP 流（0x01）。 */
    public static final Socks5CommandType CONNECT = new Socks5CommandType(0x01, "CONNECT");
    /** 绑定并等待入站连接，供 FTP 等场景（0x02）。 */
    public static final Socks5CommandType BIND = new Socks5CommandType(0x02, "BIND");
    /** 建立 UDP 关联（0x03）。 */
    public static final Socks5CommandType UDP_ASSOCIATE = new Socks5CommandType(0x03, "UDP_ASSOCIATE");

    /** 将 wire 字节解析为命令类型。 */
    public static Socks5CommandType valueOf(byte b) {
        switch (b) {
        case 0x01:
            return CONNECT;
        case 0x02:
            return BIND;
        case 0x03:
            return UDP_ASSOCIATE;
        }

        return new Socks5CommandType(b);
    }

    private final byte byteValue;
    private final String name;
    private String text;

    public Socks5CommandType(int byteValue) {
        this(byteValue, "UNKNOWN");
    }

    public Socks5CommandType(int byteValue, String name) {
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
        if (!(obj instanceof Socks5CommandType)) {
            return false;
        }

        return byteValue == ((Socks5CommandType) obj).byteValue;
    }

    @Override
    public int compareTo(Socks5CommandType o) {
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
