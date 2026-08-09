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
 * 基于 {@code hostsPortMap} 同时映射 {@link org.redisson.misc.RedisURI} 的主机与端口。
 * <p>键格式 {@code host:port}，值格式 {@code 127.0.0.1:6379}；
 * 适用于 NAT/端口转发场景。未命中映射时原样返回 URI。
 *
 * @author Nikita Koksharov
 *
 */
public class HostPortNatMapper implements NatMapper {

    /** host:port → 映射后 host:port 对照表。 */
    private Map<String, String> hostsPortMap;

    /** 以 host:port 为键查表，解析映射后的 host 与 port 构造新 URI。 */
    @Override
    public RedisURI map(RedisURI uri) {
        String hostPort = hostsPortMap.get(uri.getHost() + ":" + uri.getPort());
        if (hostPort == null) {
            return uri;
        }

        // 从最后一个冒号拆分 host 与 port（兼容 IPv6 需更复杂解析，此处按 host:port 约定）
        int lastColonIdx = hostPort.lastIndexOf(":");
        String host = hostPort.substring(0, lastColonIdx);
        String port = hostPort.substring(lastColonIdx + 1);
        return new RedisURI(uri.getScheme(), host, Integer.parseInt(port));
    }

    /**
     * 设置 host:port 映射表；键与值均为 {@code host:port} 格式，如 {@code 127.0.0.1:6379}。
     *
     * @param hostsPortMap 主机端口映射表
     */
    public void setHostsPortMap(Map<String, String> hostsPortMap) {
        this.hostsPortMap = hostsPortMap;
    }

}
