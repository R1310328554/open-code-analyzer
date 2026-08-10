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
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.util.AttributeKey;
import io.netty.util.internal.ObjectUtil;
import io.netty.util.internal.StringUtil;

import java.net.SocketAddress;
import java.util.Map;

/**
 * 暴露 {@link AbstractBootstrap} 当前配置的快照视图（只读查询，不影响 Bootstrap 本身）。
 */
public abstract class AbstractBootstrapConfig<B extends AbstractBootstrap<B, C>, C extends Channel> {

    protected final B bootstrap;

    protected AbstractBootstrapConfig(B bootstrap) {
        this.bootstrap = ObjectUtil.checkNotNull(bootstrap, "bootstrap");
    }

    /**
     * @return 已配置的本地地址，尚未配置时返回 {@code null}
     */
    public final SocketAddress localAddress() {
        return bootstrap.localAddress();
    }

    /**
     * @return 已配置的 {@link ChannelFactory}，尚未配置时返回 {@code null}
     */
    @SuppressWarnings("deprecation")
    public final ChannelFactory<? extends C> channelFactory() {
        return bootstrap.channelFactory();
    }

    /**
     * @return 已配置的 {@link ChannelHandler}，尚未配置时返回 {@code null}
     */
    public final ChannelHandler handler() {
        return bootstrap.handler();
    }

    /**
     * @return Channel 选项映射的副本
     */
    public final Map<ChannelOption<?>, Object> options() {
        return bootstrap.options();
    }

    /**
     * @return Channel 属性映射的副本
     */
    public final Map<AttributeKey<?>, Object> attrs() {
        return bootstrap.attrs();
    }

    /**
     * @return 已配置的 {@link EventLoopGroup}，尚未配置时返回 {@code null}
     */
    @SuppressWarnings("deprecation")
    public final EventLoopGroup group() {
        return bootstrap.group();
    }

    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder()
                .append(StringUtil.simpleClassName(this))
                .append('(');
        EventLoopGroup group = group();
        if (group != null) {
            buf.append("group: ")
                    .append(StringUtil.simpleClassName(group))
                    .append(", ");
        }
        @SuppressWarnings("deprecation")
        ChannelFactory<? extends C> factory = channelFactory();
        if (factory != null) {
            buf.append("channelFactory: ")
                    .append(factory)
                    .append(", ");
        }
        SocketAddress localAddress = localAddress();
        if (localAddress != null) {
            buf.append("localAddress: ")
                    .append(localAddress)
                    .append(", ");
        }

        Map<ChannelOption<?>, Object> options = options();
        if (!options.isEmpty()) {
            buf.append("options: ")
                    .append(options)
                    .append(", ");
        }
        Map<AttributeKey<?>, Object> attrs = attrs();
        if (!attrs.isEmpty()) {
            buf.append("attrs: ")
                    .append(attrs)
                    .append(", ");
        }
        ChannelHandler handler = handler();
        if (handler != null) {
            buf.append("handler: ")
                    .append(handler)
                    .append(", ");
        }
        if (buf.charAt(buf.length() - 1) == '(') {
            buf.append(')');
        } else {
            buf.setCharAt(buf.length() - 2, ')');
            buf.setLength(buf.length() - 1);
        }
        return buf.toString();
    }
}
