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

import java.util.Map;

/**
 * 基于 {@code hostsMap} 映射 {@link org.redisson.misc.RedisURI} 的主机名。
 * <p>适用于 Docker/K8s 等场景：配置中的内网主机名需映射为客户端可访问的外网地址，
 * 端口保持不变。未命中映射时原样返回 URI。
 *
 * @author Nikita Koksharov
 *
 */
public class HostNatMapper implements NatMapper {

    /** 主机映射表：原始 host → 映射后 host。 */
    private Map<String, String> hostsMap;

    /** 查表替换 host，scheme 与 port 不变。 */
    @Override
    public RedisURI map(RedisURI uri) {
        String host = hostsMap.get(uri.getHost());
        if (host == null) {
            return uri;
        }
        return new RedisURI(uri.getScheme(), host, uri.getPort());
    }

    /**
     * 设置主机映射表，键与值均为 host 字符串。
     *
     * @param hostsMap 主机映射表
     */
    public void setHostsMap(Map<String, String> hostsMap) {
        this.hostsMap = hostsMap;
    }

}
