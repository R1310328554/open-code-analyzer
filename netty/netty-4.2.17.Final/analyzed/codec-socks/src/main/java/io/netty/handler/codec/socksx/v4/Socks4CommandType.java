/*
 * Copyright 2012 The Netty Project
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
package io.netty.handler.codec.socksx.v4;

import io.netty.util.internal.ObjectUtil;

/**
 * The type of {@link Socks4CommandRequest}.
 *
 * <p>SOCKS4 请求 CD 字段：0x01 为 CONNECT（客户端经代理连远端），
 * 0x02 为 BIND（代理在本地监听并反向连客户端，用于 FTP 等场景）。</p>
 */
public class Socks4CommandType implements Comparable<Socks4CommandType> {

    /** 建立到目标主机/端口的出站连接。 */
    public static final Socks4CommandType CONNECT = new Socks4CommandType(0x01, "CONNECT");
    /** 在代理侧绑定端口，等待目标回连。 */
    public static final Socks4CommandType BIND = new Socks4CommandType(0x02, "BIND");

    /** 解析 CD 字节；未知命令码包装为 {@code UNKNOWN(n)}。 */
    public static Socks4CommandType valueOf(byte b) {
        switch (b) {
        case 0x01:
            return CONNECT;
        case 0x02:
            return BIND;
        }

        return new Socks4CommandType(b);
    }

    private final byte byteValue;
    private final String name;
    private String text;

    public Socks4CommandType(int byteValue) {
        this(byteValue, "UNKNOWN");
    }

    public Socks4CommandType(int byteValue, String name) {
        this.name = ObjectUtil.checkNotNull(name, "name");
        this.byteValue = (byte) byteValue;
    }

    /** 返回线格式 CD 字节。 */
    public byte byteValue() {
        return byteValue;
    }

    @Override
    public int hashCode() {
        return byteValue;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Socks4CommandType)) {
            return false;
        }

        return byteValue == ((Socks4CommandType) obj).byteValue;
    }

    @Override
    public int compareTo(Socks4CommandType o) {
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
