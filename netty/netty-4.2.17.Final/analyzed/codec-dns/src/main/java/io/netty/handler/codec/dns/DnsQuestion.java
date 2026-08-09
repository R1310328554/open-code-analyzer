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
 * DNS 问题段接口，描述客户端希望解析的域名、类型与类。
 * <p>
 * 问题段无 TTL，{@link #timeToLive()} 恒返回 0。
 */
public interface DnsQuestion extends DnsRecord {
    /** 问题段不使用 TTL，此方法恒返回 {@code 0}。 */
    @Override
    long timeToLive();
}
