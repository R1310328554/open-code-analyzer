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
package io.netty.handler.codec.dns;

import io.netty.util.internal.StringUtil;

/**
 * <a href="https://tools.ietf.org/html/rfc6891#section-6.1">OPT RR</a> 伪记录。
 *
 * 用于 <a href="https://tools.ietf.org/html/rfc6891#section-6.1.3">
 *     DNS 扩展机制 EDNS(0)</a>。
 */
public abstract class AbstractDnsOptPseudoRrRecord extends AbstractDnsRecord implements DnsOptPseudoRecord {

    protected AbstractDnsOptPseudoRrRecord(int maxPayloadSize, int extendedRcode, int version) {
        super(StringUtil.EMPTY_STRING, DnsRecordType.OPT, maxPayloadSize, packIntoLong(extendedRcode, version));
    }

    protected AbstractDnsOptPseudoRrRecord(int maxPayloadSize) {
        super(StringUtil.EMPTY_STRING, DnsRecordType.OPT, maxPayloadSize, 0);
    }

    // 参见 RFC 6891 §6.1.3
    private static long packIntoLong(int val, int val2) {
        // 暂不支持 DO 与 Z 字段，填 0
        return ((val & 0xffL) << 24 | (val2 & 0xff) << 16) & 0xFFFFFFFFL;
    }

    @Override
    public int extendedRcode() {
        return (short) (((int) timeToLive() >> 24) & 0xff);
    }

    @Override
    public int version() {
        return (short) (((int) timeToLive() >> 16) & 0xff);
    }

    @Override
    public int flags() {
       return (short) ((short) timeToLive() & 0xff);
    }

    @Override
    public String toString() {
        return toStringBuilder().toString();
    }

    final StringBuilder toStringBuilder() {
        return new StringBuilder(64)
                .append(StringUtil.simpleClassName(this))
                .append('(')
                .append("OPT flags:")
                .append(flags())
                .append(" version:")
                .append(version())
                .append(" extendedRecode:")
                .append(extendedRcode())
                .append(" udp:")
                .append(dnsClass())
                .append(')');
    }
}
