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

import io.netty.util.collection.IntObjectHashMap;

import java.util.HashMap;
import java.util.Map;

/**
 * DNS 资源记录类型（TYPE），对应 RFC 中各 RR 类型编号。
 * <p>
 * 提供常用类型的命名常量，并支持按数值或名称解析。
 */
public class DnsRecordType implements Comparable<DnsRecordType> {

    /** IPv4 地址记录（A，RFC 1035），将主机名映射为 32 位 IPv4 地址。 */
    public static final DnsRecordType A = new DnsRecordType(0x0001, "A");

    /** 域名服务器记录（NS，RFC 1035），委派区域至权威名称服务器。 */
    public static final DnsRecordType NS = new DnsRecordType(0x0002, "NS");

    /** 规范名称别名记录（CNAME，RFC 1035），解析时继续查询目标名称。 */
    public static final DnsRecordType CNAME = new DnsRecordType(0x0005, "CNAME");

    /** 起始授权记录（SOA，RFC 1035/2308），描述区域权威信息与刷新参数。 */
    public static final DnsRecordType SOA = new DnsRecordType(0x0006, "SOA");

    /** 指针记录（PTR，RFC 1035），常用于反向 DNS 解析，不继续递归查询。 */
    public static final DnsRecordType PTR = new DnsRecordType(0x000c, "PTR");

    /** 邮件交换记录（MX，RFC 1035），指定域的邮件服务器优先级列表。 */
    public static final DnsRecordType MX = new DnsRecordType(0x000f, "MX");

    /** 文本记录（TXT，RFC 1035），常用于 SPF、DKIM、DMARC 等机器可读数据。 */
    public static final DnsRecordType TXT = new DnsRecordType(0x0010, "TXT");

    /** 负责人记录（RP，RFC 1183），描述域管理员联系信息。 */
    public static final DnsRecordType RP = new DnsRecordType(0x0011, "RP");

    /** AFS 数据库记录（AFSDB，RFC 1183），定位 AFS 单元数据库服务器。 */
    public static final DnsRecordType AFSDB = new DnsRecordType(0x0012, "AFSDB");

    /** 签名记录（SIG，RFC 2535），DNSSEC 中已由 RRSIG 取代。 */
    public static final DnsRecordType SIG = new DnsRecordType(0x0018, "SIG");

    /** 密钥记录（KEY，RFC 2535/2930），DNSSEC 中由 DNSKEY 取代。 */
    public static final DnsRecordType KEY = new DnsRecordType(0x0019, "KEY");

    /** IPv6 地址记录（AAAA，RFC 3596），将主机名映射为 128 位 IPv6 地址。 */
    public static final DnsRecordType AAAA = new DnsRecordType(0x001c, "AAAA");

    /** 地理位置记录（LOC，RFC 1876），关联域名的地理坐标信息。 */
    public static final DnsRecordType LOC = new DnsRecordType(0x001d, "LOC");

    /** 服务定位记录（SRV，RFC 2782），通用服务主机与端口发现。 */
    public static final DnsRecordType SRV = new DnsRecordType(0x0021, "SRV");

    /** 命名权威指针记录（NAPTR，RFC 3403），支持正则重写与 URI 解析。 */
    public static final DnsRecordType NAPTR = new DnsRecordType(0x0023, "NAPTR");

    /** 密钥交换记录（KX，RFC 2230），标识域的密钥管理代理。 */
    public static final DnsRecordType KX = new DnsRecordType(0x0024, "KX");

    /** 证书记录（CERT，RFC 4398），存储 PKIX/SPKI/PGP 等证书。 */
    public static final DnsRecordType CERT = new DnsRecordType(0x0025, "CERT");

    /** 委派名称记录（DNAME，RFC 2672），为整棵子树创建别名（区别于 CNAME）。 */
    public static final DnsRecordType DNAME = new DnsRecordType(0x0027, "DNAME");

    /** 选项伪记录（OPT，RFC 2671），用于 EDNS(0) 扩展。 */
    public static final DnsRecordType OPT = new DnsRecordType(0x0029, "OPT");

    /** 地址前缀列表记录（APL，RFC 3123，实验性），以 CIDR 等形式描述地址范围。 */
    public static final DnsRecordType APL = new DnsRecordType(0x002a, "APL");

    /** 委派签名者记录（DS，RFC 4034），标识子区域的 DNSSEC 签名密钥。 */
    public static final DnsRecordType DS = new DnsRecordType(0x002b, "DS");

    /** SSH 公钥指纹记录（SSHFP，RFC 4255），在 DNS 中发布 SSH 主机密钥指纹。 */
    public static final DnsRecordType SSHFP = new DnsRecordType(0x002c, "SSHFP");

    /** IPsec 密钥记录（IPSECKEY，RFC 4025），供 IPsec 使用的密钥。 */
    public static final DnsRecordType IPSECKEY = new DnsRecordType(0x002d, "IPSECKEY");

    /** DNSSEC 签名记录（RRSIG，RFC 4034），对 RR 集进行密码学签名。 */
    public static final DnsRecordType RRSIG = new DnsRecordType(0x002e, "RRSIG");

    /** 下一安全记录（NSEC，RFC 4034），DNSSEC 中证明名称不存在。 */
    public static final DnsRecordType NSEC = new DnsRecordType(0x002f, "NSEC");

    /** DNS 密钥记录（DNSKEY，RFC 4034），DNSSEC 区域签名公钥。 */
    public static final DnsRecordType DNSKEY = new DnsRecordType(0x0030, "DNSKEY");

    /** DHCP 标识记录（DHCID，RFC 4701），与 DHCP FQDN 选项配合使用。 */
    public static final DnsRecordType DHCID = new DnsRecordType(0x0031, "DHCID");

    /** NSEC 第 3 版记录（NSEC3，RFC 5155），防区域遍历的存在性证明。 */
    public static final DnsRecordType NSEC3 = new DnsRecordType(0x0032, "NSEC3");

    /** NSEC3 参数记录（NSEC3PARAM，RFC 5155），配置 NSEC3 哈希参数。 */
    public static final DnsRecordType NSEC3PARAM = new DnsRecordType(0x0033, "NSEC3PARAM");

    /** TLSA 证书关联记录（TLSA，RFC 6698），DANE 中绑定 TLS 证书与域名。 */
    public static final DnsRecordType TLSA = new DnsRecordType(0x0034, "TLSA");

    /** 主机身份协议记录（HIP，RFC 5205），分离端点标识与定位角色。 */
    public static final DnsRecordType HIP = new DnsRecordType(0x0037, "HIP");

    /** SPF 记录（RFC 4408），发件人策略框架，格式同 TXT。 */
    public static final DnsRecordType SPF = new DnsRecordType(0x0063, "SPF");

    /** 事务密钥记录（TKEY，RFC 2930），为 TSIG 提供加密密钥材料。 */
    public static final DnsRecordType TKEY = new DnsRecordType(0x00f9, "TKEY");

    /** 事务签名记录（TSIG，RFC 2845），认证动态更新或递归响应来源。 */
    public static final DnsRecordType TSIG = new DnsRecordType(0x00fa, "TSIG");

    /** 增量区域传送记录（IXFR，RFC 1996），仅传输自指定序列号以来的变更。 */
    public static final DnsRecordType IXFR = new DnsRecordType(0x00fb, "IXFR");

    /** 权威区域传送记录（AXFR，RFC 1035），完整传输区域文件至辅服务器。 */
    public static final DnsRecordType AXFR = new DnsRecordType(0x00fc, "AXFR");

    /** 任意类型查询（ANY，RFC 1035），请求服务器返回该名称的所有已知记录。 */
    public static final DnsRecordType ANY = new DnsRecordType(0x00ff, "ANY");

    /** 证书颁发机构授权记录（CAA，RFC 6844），限制可为域签发证书的 CA。 */
    public static final DnsRecordType CAA = new DnsRecordType(0x0101, "CAA");

    /** DNSSEC 信任锚记录（TA），无签名根场景下的信任锚，格式同 DS。 */
    public static final DnsRecordType TA = new DnsRecordType(0x8000, "TA");

    /** DNSSEC 旁路验证记录（DLV，RFC 4431），在委派链外发布信任锚。 */
    public static final DnsRecordType DLV = new DnsRecordType(0x8001, "DLV");

    /** 服务绑定记录（SVCB，RFC 9460），通过 DNS 描述服务参数与替代端点。 */
    public static final DnsRecordType SVCB = new DnsRecordType(0x0040, "SVCB");

    /** HTTPS 服务绑定记录（HTTPS，RFC 9460），SVCB 的 HTTPS 专用类型。 */
    public static final DnsRecordType HTTPS = new DnsRecordType(0x0041, "HTTPS");

    private static final Map<String, DnsRecordType> BY_NAME = new HashMap<String, DnsRecordType>();
    private static final IntObjectHashMap<DnsRecordType> BY_TYPE = new IntObjectHashMap<DnsRecordType>();
    private static final String EXPECTED;

    static {
        DnsRecordType[] all = {
                A, NS, CNAME, SOA, PTR, MX, TXT, RP, AFSDB, SIG, KEY, AAAA, LOC, SRV, NAPTR, KX, CERT, DNAME, OPT, APL,
                DS, SSHFP, IPSECKEY, RRSIG, NSEC, DNSKEY, DHCID, NSEC3, NSEC3PARAM, TLSA, HIP, SPF, TKEY, TSIG, IXFR,
                AXFR, ANY, CAA, TA, DLV, SVCB, HTTPS
        };

        final StringBuilder expected = new StringBuilder(512);

        expected.append(" (expected: ");
        for (DnsRecordType type: all) {
            BY_NAME.put(type.name(), type);
            BY_TYPE.put(type.intValue(), type);

            expected.append(type.name())
                    .append('(')
                    .append(type.intValue())
                    .append("), ");
        }

        expected.setLength(expected.length() - 2);
        expected.append(')');
        EXPECTED = expected.toString();
    }

    public static DnsRecordType valueOf(int intValue) {
        DnsRecordType result = BY_TYPE.get(intValue);
        if (result == null) {
            return new DnsRecordType(intValue);
        }
        return result;
    }

    public static DnsRecordType valueOf(String name) {
        DnsRecordType result = BY_NAME.get(name);
        if (result == null) {
            throw new IllegalArgumentException("name: " + name + EXPECTED);
        }
        return result;
    }

    private final int intValue;
    private final String name;
    private String text;

    private DnsRecordType(int intValue) {
        this(intValue, "UNKNOWN");
    }

    public DnsRecordType(int intValue, String name) {
        if ((intValue & 0xffff) != intValue) {
            throw new IllegalArgumentException("intValue: " + intValue + " (expected: 0 ~ 65535)");
        }
        this.intValue = intValue;
        this.name = name;
    }

    /** 返回类型名称（与 BIND 配置文件中的写法一致）。 */
    public String name() {
        return name;
    }

    /** 返回该类型在 DNS 协议中的 16 位数值。 */
    public int intValue() {
        return intValue;
    }

    @Override
    public int hashCode() {
        return intValue;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof DnsRecordType && ((DnsRecordType) o).intValue == intValue;
    }

    @Override
    public int compareTo(DnsRecordType o) {
        return intValue() - o.intValue();
    }

    @Override
    public String toString() {
        String text = this.text;
        if (text == null) {
            this.text = text = name + '(' + intValue() + ')';
        }
        return text;
    }
}
