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
package org.redisson.config;

import org.redisson.misc.RedisURI;

/**
 * Redis 连接 URI 的 NAT/地址映射接口。
 * <p>在建立连接前将配置中的 {@link org.redisson.misc.RedisURI} 转换为客户
 * 端实际可达的 host/port，常用于容器、云 NAT 网关环境。
 *
 * @author Nikita Koksharov
 *
 * @see HostNatMapper
 * @see HostPortNatMapper
 */
@FunctionalInterface
public interface NatMapper {

    /**
     * 对输入 URI 应用映射规则。
     *
     * @param uri 原始 RedisURI
     * @return 映射后的 RedisURI
     */
    RedisURI map(RedisURI uri);

    /**
     * 返回恒等映射器，URI 原样返回（默认）。
     *
     * @return 不做转换的 {@link NatMapper} 实例
     */
    static NatMapper direct() {
        return new DefaultNatMapper();
    }

}
