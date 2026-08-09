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

/**
 * DNS 资源记录（Resource Record）公共接口。
 * <p>
 * 每条记录包含域名、类型、类、TTL 等头部字段；RDATA 由具体子类型承载。
 */
public interface DnsRecord {

    /** DNS 资源记录类：{@code IN}（Internet）。 */
    int CLASS_IN = 0x0001;

    /** DNS 资源记录类：{@code CSNET}。 */
    int CLASS_CSNET = 0x0002;

    /** DNS 资源记录类：{@code CHAOS}。 */
    int CLASS_CHAOS = 0x0003;

    /** DNS 资源记录类：{@code HESIOD}。 */
    int CLASS_HESIOD = 0x0004;

    /** DNS 资源记录类：{@code NONE}。 */
    int CLASS_NONE = 0x00fe;

    /** DNS 资源记录类：{@code ANY}（任意类）。 */
    int CLASS_ANY = 0x00ff;

    /** 返回该资源记录的域名。 */
    String name();

    /** 返回该资源记录的类型。 */
    DnsRecordType type();

    /**
     * 返回该资源记录的类（Class）。
     *
     * @return 类值，通常为以下常量之一：
     *         <ul>
     *             <li>{@link #CLASS_IN}</li>
     *             <li>{@link #CLASS_CSNET}</li>
     *             <li>{@link #CLASS_CHAOS}</li>
     *             <li>{@link #CLASS_HESIOD}</li>
     *             <li>{@link #CLASS_NONE}</li>
     *             <li>{@link #CLASS_ANY}</li>
     *         </ul>
     */
    int dnsClass();

    /** 返回该资源记录的 TTL（生存时间，秒）。 */
    long timeToLive();
}
