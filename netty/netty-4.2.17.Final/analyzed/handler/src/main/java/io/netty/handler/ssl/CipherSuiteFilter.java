/*
 * Copyright 2014 The Netty Project
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
package io.netty.handler.ssl;

import javax.net.ssl.SSLEngine;
import java.util.List;
import java.util.Set;

/**
 * Provides a means to filter the supplied cipher suite based upon the supported and default cipher suites.
 *
 * <p>根据当前 {@link SSLEngine} 支持的套件与 Netty 推荐默认套件，过滤用户配置的密码套件列表。</p>
 */
public interface CipherSuiteFilter {
    /**
     * Filter the requested {@code ciphers} based upon other cipher characteristics.
     * @param ciphers The requested ciphers
     * @param defaultCiphers The default recommended ciphers for the current {@link SSLEngine} as determined by Netty
     * @param supportedCiphers The supported ciphers for the current {@link SSLEngine}
     * @return The filter list of ciphers. Must not return {@code null}.
     *
     * <p>按引擎能力与 Netty 策略筛选密码套件；{@code ciphers} 为 null 时实现类决定回退到默认或支持列表。</p>
     */
    String[] filterCipherSuites(Iterable<String> ciphers, List<String> defaultCiphers, Set<String> supportedCiphers);
}
