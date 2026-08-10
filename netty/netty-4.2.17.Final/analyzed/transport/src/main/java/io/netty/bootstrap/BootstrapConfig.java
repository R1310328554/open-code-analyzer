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
package io.netty.bootstrap;

import io.netty.channel.Channel;
import io.netty.resolver.AddressResolverGroup;

import java.net.SocketAddress;

/**
 * 暴露 {@link Bootstrap} 客户端引导配置的只读视图。
 */
public final class BootstrapConfig extends AbstractBootstrapConfig<Bootstrap, Channel> {

    BootstrapConfig(Bootstrap bootstrap) {
        super(bootstrap);
    }

    /**
     * @return 已配置的远程地址，尚未配置时返回 {@code null}
     */
    public SocketAddress remoteAddress() {
        return bootstrap.remoteAddress();
    }

    /**
     * @return 已配置的 {@link AddressResolverGroup}；
     *         若已通过 {@link Bootstrap#disableResolver()} 禁用则为 {@code null}；
     *         尚未显式配置时返回默认解析器组
     */
    public AddressResolverGroup<?> resolver() {
        return bootstrap.resolver();
    }

    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder(super.toString());
        buf.setLength(buf.length() - 1);
        AddressResolverGroup<?> resolver = resolver();
        if (resolver != null) {
            buf.append(", resolver: ")
                    .append(resolver);
        }
        SocketAddress remoteAddress = remoteAddress();
        if (remoteAddress != null) {
            buf.append(", remoteAddress: ")
                    .append(remoteAddress);
        }
        return buf.append(')').toString();
    }
}
