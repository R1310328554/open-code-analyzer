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
 * {@link NettyHook} 的空实现，不对 Bootstrap 或 Channel 做任何额外配置。
 *
 * @author Nikita Koksharov
 *
 */
public class DefaultNettyHook implements NettyHook {

    /** Bootstrap 初始化后的钩子，默认空实现。 */
    @Override
    public void afterBoostrapInitialization(Bootstrap bootstrap) {
    }

    /** Channel 创建后的钩子，默认空实现。 */
    @Override
    public void afterChannelInitialization(Channel channel) {
    }

}
