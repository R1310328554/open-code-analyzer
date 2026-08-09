/**
 * Copyright (c) 2013-2026 Nikita Koksharov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.redisson.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;

/**
 * Netty 初始化钩子接口。
 * <p>
 * 允许在 Bootstrap 与 Channel 创建完成后注入自定义配置或处理器。
 *
 * @author Nikita Koksharov
 *
 */
public interface NettyHook {

    /**
     * Redis 客户端创建并初始化 Netty Bootstrap 后调用。
     *
     * @param bootstrap Netty Bootstrap 对象
     */
    void afterBoostrapInitialization(Bootstrap bootstrap);

    /**
     * Netty Channel 创建并初始化后调用。
     *
     * @param channel Netty Channel 对象
     */
    void afterChannelInitialization(Channel channel);

}
