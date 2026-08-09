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
 * DNS 响应码 {@code RCODE}，定义见 <a href="https://tools.ietf.org/html/rfc2929">RFC2929</a>。
 * <p>
 * 占响应报文标志字段低 4 位，表示查询处理结果。
 */
public class DnsResponseCode implements Comparable<DnsResponseCode> {

    /** 无错误（NoError，0），见 <a href="https://tools.ietf.org/html/rfc1035">RFC1035</a>。 */
    public static final DnsResponseCode NOERROR = new DnsResponseCode(0, "NoError");

    /** 格式错误（FormErr，1），见 <a href="https://tools.ietf.org/html/rfc1035">RFC1035</a>。 */
    public static final DnsResponseCode FORMERR = new DnsResponseCode(1, "FormErr");

    /** 服务器失败（ServFail，2），见 <a href="https://tools.ietf.org/html/rfc1035">RFC1035</a>。 */
    public static final DnsResponseCode SERVFAIL = new DnsResponseCode(2, "ServFail");

    /** 域名不存在（NXDomain，3），见 <a href="https://tools.ietf.org/html/rfc1035">RFC1035</a>。 */
    public static final DnsResponseCode NXDOMAIN = new DnsResponseCode(3, "NXDomain");

    /** 操作未实现（NotImp，4），见 <a href="https://tools.ietf.org/html/rfc1035">RFC1035</a>。 */
    public static final DnsResponseCode NOTIMP = new DnsResponseCode(4, "NotImp");

    /** 拒绝（Refused，5），见 <a href="https://tools.ietf.org/html/rfc1035">RFC1035</a>。 */
    public static final DnsResponseCode REFUSED = new DnsResponseCode(5, "Refused");

    /** 域名应不存在（YXDomain，6），见 <a href="https://tools.ietf.org/html/rfc2136">RFC2136</a>。 */
    public static final DnsResponseCode YXDOMAIN = new DnsResponseCode(6, "YXDomain");

    /** RR 集应不存在（YXRRSet，7），见 <a href="https://tools.ietf.org/html/rfc2136">RFC2136</a>。 */
    public static final DnsResponseCode YXRRSET = new DnsResponseCode(7, "YXRRSet");

    /** RR 集不存在（NXRRSet，8），见 <a href="https://tools.ietf.org/html/rfc2136">RFC2136</a>。 */
    public static final DnsResponseCode NXRRSET = new DnsResponseCode(8, "NXRRSet");

    /** 服务器非权威（NotAuth，9），见 <a href="https://tools.ietf.org/html/rfc2136">RFC2136</a>。 */
    public static final DnsResponseCode NOTAUTH = new DnsResponseCode(9, "NotAuth");

    /** 名称不在区域内（NotZone，10），见 <a href="https://tools.ietf.org/html/rfc2136">RFC2136</a>。 */
    public static final DnsResponseCode NOTZONE = new DnsResponseCode(10, "NotZone");

    /**
     * 版本/签名错误（BADVERS 或 BADSIG，16），见
     * <a href="https://tools.ietf.org/html/rfc2671">RFC2671</a> 与
     * <a href="https://tools.ietf.org/html/rfc2845">RFC2845</a>。
     */
    public static final DnsResponseCode BADVERS_OR_BADSIG = new DnsResponseCode(16, "BADVERS_OR_BADSIG");

    /** 密钥错误（BADKEY，17），见 <a href="https://tools.ietf.org/html/rfc2845">RFC2845</a>。 */
    public static final DnsResponseCode BADKEY = new DnsResponseCode(17, "BADKEY");

    /** 时间戳错误（BADTIME，18），见 <a href="https://tools.ietf.org/html/rfc2845">RFC2845</a>。 */
    public static final DnsResponseCode BADTIME = new DnsResponseCode(18, "BADTIME");

    /** 模式错误（BADMODE，19），见 <a href="https://tools.ietf.org/html/rfc2930">RFC2930</a>。 */
    public static final DnsResponseCode BADMODE = new DnsResponseCode(19, "BADMODE");

    /** 名称错误（BADNAME，20），见 <a href="https://tools.ietf.org/html/rfc2930">RFC2930</a>。 */
    public static final DnsResponseCode BADNAME = new DnsResponseCode(20, "BADNAME");

    /** 算法错误（BADALG，21），见 <a href="https://tools.ietf.org/html/rfc2930">RFC2930</a>。 */
    public static final DnsResponseCode BADALG = new DnsResponseCode(21, "BADALG");

    /**
     * 根据 {@code responseCode} 返回对应 {@link DnsResponseCode}。
     *
     * @param responseCode DNS RCODE 数值
     *
     * @return 对应的 {@link DnsResponseCode}；未知值创建新实例
     */
    public static DnsResponseCode valueOf(int responseCode) {
        switch (responseCode) {
        case 0:
            return NOERROR;
        case 1:
            return FORMERR;
        case 2:
            return SERVFAIL;
        case 3:
            return NXDOMAIN;
        case 4:
            return NOTIMP;
        case 5:
            return REFUSED;
        case 6:
            return YXDOMAIN;
        case 7:
            return YXRRSET;
        case 8:
            return NXRRSET;
        case 9:
            return NOTAUTH;
        case 10:
            return NOTZONE;
        case 16:
            return BADVERS_OR_BADSIG;
        case 17:
            return BADKEY;
        case 18:
            return BADTIME;
        case 19:
            return BADMODE;
        case 20:
            return BADNAME;
        case 21:
            return BADALG;
        default:
            return new DnsResponseCode(responseCode);
        }
    }

    private final int code;
    private final String name;
    private String text;

    private DnsResponseCode(int code) {
        this(code, "UNKNOWN");
    }

    public DnsResponseCode(int code, String name) {
        if (code < 0 || code > 65535) {
            throw new IllegalArgumentException("code: " + code + " (expected: 0 ~ 65535)");
        }

        this.code = code;
        this.name = checkNotNull(name, "name");
    }

    /** 返回该 {@link DnsResponseCode} 的数值。 */
    public int intValue() {
        return code;
    }

    @Override
    public int compareTo(DnsResponseCode o) {
        return intValue() - o.intValue();
    }

    @Override
    public int hashCode() {
        return intValue();
    }

    /** {@link DnsResponseCode} 相等性仅取决于 {@link #intValue()}。 */
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof DnsResponseCode)) {
            return false;
        }

        return intValue() == ((DnsResponseCode) o).intValue();
    }

    /** 返回格式化的响应码字符串。 */
    @Override
    public String toString() {
        String text = this.text;
        if (text == null) {
            this.text = text = name + '(' + intValue() + ')';
        }
        return text;
    }
}
