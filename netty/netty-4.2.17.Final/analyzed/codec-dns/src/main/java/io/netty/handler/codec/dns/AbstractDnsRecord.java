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

import io.netty.util.internal.PlatformDependent;
import io.netty.util.internal.StringUtil;

import java.net.IDN;

import static io.netty.util.internal.ObjectUtil.checkNotNull;
import static io.netty.util.internal.ObjectUtil.checkPositiveOrZero;

/**
 * {@link DnsRecord} 的骨架实现，封装名称、类型、类与 TTL。
 */
public abstract class AbstractDnsRecord implements DnsRecord {

    private final String name;
    private final DnsRecordType type;
    private final short dnsClass;
    private final long timeToLive;
    private int hashCode;

    /**
     * 创建 {@link #CLASS_IN IN 类} 记录。
     *
     * @param name 域名
     * @param type 记录类型
     * @param timeToLive 记录 TTL
     */
    protected AbstractDnsRecord(String name, DnsRecordType type, long timeToLive) {
        this(name, type, CLASS_IN, timeToLive);
    }

    /**
     * 创建指定 DNS 类的记录。
     *
     * @param name 域名
     * @param type 记录类型
     * @param dnsClass 记录类，常见取值：
     *                 <ul>
     *                     <li>{@link #CLASS_IN}</li>
     *                     <li>{@link #CLASS_CSNET}</li>
     *                     <li>{@link #CLASS_CHAOS}</li>
     *                     <li>{@link #CLASS_HESIOD}</li>
     *                     <li>{@link #CLASS_NONE}</li>
     *                     <li>{@link #CLASS_ANY}</li>
     *                 </ul>
     * @param timeToLive 记录 TTL
     */
    protected AbstractDnsRecord(String name, DnsRecordType type, int dnsClass, long timeToLive) {
        checkPositiveOrZero(timeToLive, "timeToLive");
        // 转为 ASCII 并校验长度上限
        // 参见：
        //   - https://github.com/netty/netty/issues/4937
        //   - https://github.com/netty/netty/issues/4935
        this.name = appendTrailingDot(IDNtoASCII(name));
        this.type = checkNotNull(type, "type");
        this.dnsClass = (short) dnsClass;
        this.timeToLive = timeToLive;
    }

    private static String IDNtoASCII(String name) {
        checkNotNull(name, "name");
        if (PlatformDependent.isAndroid() && DefaultDnsRecordDecoder.ROOT.equals(name)) {
            // Android 10 之前 IDN 无法正确解析 "."
            // 参见 https://github.com/netty/netty/issues/10034
            return name;
        }
        return IDN.toASCII(name);
    }

    private static String appendTrailingDot(String name) {
        if (name.length() > 0 && name.charAt(name.length() - 1) != '.') {
            return name + '.';
        }
        return name;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public DnsRecordType type() {
        return type;
    }

    @Override
    public int dnsClass() {
        return dnsClass & 0xFFFF;
    }

    @Override
    public long timeToLive() {
        return timeToLive;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof DnsRecord)) {
            return false;
        }

        final DnsRecord that = (DnsRecord) obj;
        final int hashCode = this.hashCode;
        if (hashCode != 0 && hashCode != that.hashCode()) {
            return false;
        }

        return type().intValue() == that.type().intValue() &&
               dnsClass() == that.dnsClass() &&
               name().equalsIgnoreCase(that.name());
    }

    @Override
    public int hashCode() {
        final int hashCode = this.hashCode;
        if (hashCode != 0) {
            return hashCode;
        }

        return this.hashCode = name.toLowerCase().hashCode() * 31 + type().intValue() * 31 + dnsClass();
    }

    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder(64);

        buf.append(StringUtil.simpleClassName(this))
           .append('(')
           .append(name())
           .append(' ')
           .append(timeToLive())
           .append(' ');

        DnsMessageUtil.appendRecordClass(buf, dnsClass())
                      .append(' ')
                      .append(type().name())
                      .append(')');

        return buf.toString();
    }
}
