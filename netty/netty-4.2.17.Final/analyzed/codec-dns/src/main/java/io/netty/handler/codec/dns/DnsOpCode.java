/*
 * Copyright 2015 The Netty Project
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
package io.netty.handler.codec.dns;

import static io.netty.util.internal.ObjectUtil.checkNotNull;

/**
 * DNS 操作码，定义见 <a href="https://tools.ietf.org/html/rfc2929">RFC2929</a>。
 * <p>
 * 标识报文类型：标准查询、反向查询、状态、Notify、动态更新等。
 */
public class DnsOpCode implements Comparable<DnsOpCode> {

    /** 标准查询（Query），见 <a href="https://tools.ietf.org/html/rfc1035">RFC1035</a>。 */
    public static final DnsOpCode QUERY = new DnsOpCode(0x00, "QUERY");

    /** 反向查询（IQuery），见 <a href="https://tools.ietf.org/html/rfc1035">RFC1035</a>。 */
    public static final DnsOpCode IQUERY = new DnsOpCode(0x01, "IQUERY");

    /** 状态查询（Status），见 <a href="https://tools.ietf.org/html/rfc1035">RFC1035</a>。 */
    public static final DnsOpCode STATUS = new DnsOpCode(0x02, "STATUS");

    /** 区域变更通知（Notify），见 <a href="https://tools.ietf.org/html/rfc1996">RFC1996</a>。 */
    public static final DnsOpCode NOTIFY = new DnsOpCode(0x04, "NOTIFY");

    /** 动态更新（Update），见 <a href="https://tools.ietf.org/html/rfc2136">RFC2136</a>。 */
    public static final DnsOpCode UPDATE = new DnsOpCode(0x05, "UPDATE");

    /** 根据字节值返回对应 {@link DnsOpCode}；未知值创建 UNKNOWN 实例。 */
    public static DnsOpCode valueOf(int b) {
        switch (b) {
        case 0x00:
            return QUERY;
        case 0x01:
            return IQUERY;
        case 0x02:
            return STATUS;
        case 0x04:
            return NOTIFY;
        case 0x05:
            return UPDATE;
        default:
            break;
        }

        return new DnsOpCode(b);
    }

    private final byte byteValue;
    private final String name;
    private String text;

    private DnsOpCode(int byteValue) {
        this(byteValue, "UNKNOWN");
    }

    public DnsOpCode(int byteValue, String name) {
        this.byteValue = (byte) byteValue;
        this.name = checkNotNull(name, "name");
    }

    /** 返回操作码的字节值。 */
    public byte byteValue() {
        return byteValue;
    }

    @Override
    public int hashCode() {
        return byteValue;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof DnsOpCode)) {
            return false;
        }

        return byteValue == ((DnsOpCode) obj).byteValue;
    }

    @Override
    public int compareTo(DnsOpCode o) {
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
