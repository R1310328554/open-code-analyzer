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
 * The status of {@link Socks4CommandResponse}.
 *
 * <p>SOCKS4 应答 CD 字段取值。标准值 0x5a~0x5d 对应成功、拒绝、ident 不可达、ident 认证失败；
 * 未知字节会包装为 {@code UNKNOWN(n)} 实例以便调试。</p>
 */
public class Socks4CommandStatus implements Comparable<Socks4CommandStatus> {

    /** 请求已授予，代理将建立或已建立连接（0x5a）。 */
    public static final Socks4CommandStatus SUCCESS = new Socks4CommandStatus(0x5a, "SUCCESS");
    /** 代理拒绝或本地规则导致失败（0x5b）。 */
    public static final Socks4CommandStatus REJECTED_OR_FAILED = new Socks4CommandStatus(0x5b, "REJECTED_OR_FAILED");
    /** 无法连接 identd 服务（0x5c）。 */
    public static final Socks4CommandStatus IDENTD_UNREACHABLE = new Socks4CommandStatus(0x5c, "IDENTD_UNREACHABLE");
    /** identd 报告的用户 ID 与请求 USERID 不一致（0x5d）。 */
    public static final Socks4CommandStatus IDENTD_AUTH_FAILURE = new Socks4CommandStatus(0x5d, "IDENTD_AUTH_FAILURE");

    /**
     * 将应答 CD 字节映射为已知常量；非标准值返回带 {@code UNKNOWN} 名称的新实例。
     */
    public static Socks4CommandStatus valueOf(byte b) {
        switch (b) {
        case 0x5a:
            return SUCCESS;
        case 0x5b:
            return REJECTED_OR_FAILED;
        case 0x5c:
            return IDENTD_UNREACHABLE;
        case 0x5d:
            return IDENTD_AUTH_FAILURE;
        }

        return new Socks4CommandStatus(b);
    }

    private final byte byteValue;
    private final String name;
    private String text;

    public Socks4CommandStatus(int byteValue) {
        this(byteValue, "UNKNOWN");
    }

    public Socks4CommandStatus(int byteValue, String name) {
        this.name = ObjectUtil.checkNotNull(name, "name");
        this.byteValue = (byte) byteValue;
    }

    /** 返回协议线格式中的 CD 字节。 */
    public byte byteValue() {
        return byteValue;
    }

    /** 是否表示代理已接受请求（仅 0x5a 为 true）。 */
    public boolean isSuccess() {
        return byteValue == 0x5a;
    }

    @Override
    public int hashCode() {
        return byteValue;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Socks4CommandStatus)) {
            return false;
        }

        return byteValue == ((Socks4CommandStatus) obj).byteValue;
    }

    @Override
    public int compareTo(Socks4CommandStatus o) {
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
