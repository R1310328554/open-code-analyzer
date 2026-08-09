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

import io.netty.util.internal.StringUtil;

/**
 * {@link DnsQuestion} 的默认实现，表示 DNS 查询报文中的问题段。
 */
public class DefaultDnsQuestion extends AbstractDnsRecord implements DnsQuestion {

    /**
     * 创建 {@link #CLASS_IN IN 类} 问题记录。
     *
     * @param name the domain name of the DNS question
     * @param type the type of the DNS question
     */
    public DefaultDnsQuestion(String name, DnsRecordType type) {
        super(name, type, 0);
    }

    /**
     * 创建指定 DNS 类的问题记录。
     *
     * @param name the domain name of the DNS question
     * @param type the type of the DNS question
     * @param dnsClass the class of the record, usually one of the following:
     *                 <ul>
     *                     <li>{@link #CLASS_IN}</li>
     *                     <li>{@link #CLASS_CSNET}</li>
     *                     <li>{@link #CLASS_CHAOS}</li>
     *                     <li>{@link #CLASS_HESIOD}</li>
     *                     <li>{@link #CLASS_NONE}</li>
     *                     <li>{@link #CLASS_ANY}</li>
     *                 </ul>
     */
    public DefaultDnsQuestion(String name, DnsRecordType type, int dnsClass) {
        super(name, type, dnsClass, 0);
    }

    /** 返回 {@code 类名(域名 类 类型)} 格式的可读字符串。 */
    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder(64);

        buf.append(StringUtil.simpleClassName(this))
           .append('(')
           .append(name())
           .append(' ');

        DnsMessageUtil.appendRecordClass(buf, dnsClass())
                      .append(' ')
                      .append(type().name())
                      .append(')');

        return buf.toString();
    }
}
