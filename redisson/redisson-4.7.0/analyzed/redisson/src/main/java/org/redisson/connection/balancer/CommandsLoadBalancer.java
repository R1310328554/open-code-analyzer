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
package org.redisson.connection.balancer;

import org.redisson.client.protocol.RedisCommand;
import org.redisson.connection.ClientConnectionsEntry;
import org.redisson.misc.RedisURI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 按命令名将特定 Redis 命令路由到指定节点。
 * <p>
 * 支持 {@code commandsMap}：主机名正则 → 命令名集合；
 * 已弃用的 {@link #setAddress}/{@link #setCommands} 仍可用但建议迁移。
 *
 * @author Nikita Koksharov
 *
 */
public class CommandsLoadBalancer extends RoundRobinLoadBalancer implements LoadBalancer {

    private static final Logger log = LoggerFactory.getLogger(CommandsLoadBalancer.class);

    /** 主机名正则 → 应路由到匹配节点的命令名集合。 */
    private final Map<Pattern, Set<String>> commandsMap = new HashMap<>();

    /** 已弃用：固定命令名集合。 */
    private Set<String> commands;
    /** 已弃用：固定目标节点地址。 */
    private RedisURI address;

    @Override
    /** 按命令名选择目标节点，未命中则回退轮询策略。 */
    public ClientConnectionsEntry getEntry(List<ClientConnectionsEntry> clientsCopy, RedisCommand<?> redisCommand) {
        String name = redisCommand.getName().toLowerCase(Locale.ENGLISH);

        if (commands != null
                && commands.contains(name)) {
            return clientsCopy.stream()
                                .filter(c -> address.equals(c.getClient().getAddr()))
                                .findAny()
                                .orElseGet(() -> {
                return super.getEntry(clientsCopy, null);
            });
        }

        for (Map.Entry<Pattern, Set<String>> e : commandsMap.entrySet()) {
            if (e.getValue().contains(name)) {
                List<ClientConnectionsEntry> s = filter(clientsCopy, e.getKey());
                if (!s.isEmpty()) {
                    return super.getEntry(s, null);
                }
            }
        }

        return super.getEntry(clientsCopy, null);
    }

    /**
     * 已弃用：设置命令重定向目标节点地址。
     *
     * @param address Redis 节点地址
     */
    @Deprecated
    public void setAddress(String address) {
        log.warn("address setting is deprecated. Use commandsMap setting instead.");
        this.address = new RedisURI(address);
    }

    /**
     * 已弃用：设置需重定向的命令名列表。
     *
     * @param commands 命令名列表
     */
    @Deprecated
    public void setCommands(List<String> commands) {
        log.warn("commands setting is deprecated. Use commandsMap setting instead.");
        this.commands = commands.stream()
                                    .map(c -> c.toLowerCase(Locale.ENGLISH))
                                    .collect(Collectors.toSet());
    }

    /**
     * 设置命令名到主机名正则的映射表。
     * <p>
     * YAML 示例：
     * <pre>
     *      loadBalancer: !&lt;org.redisson.connection.balancer.CommandsLoadBalancer&gt;
     *       commandsMap:
     *           "slavehost1.*" : ["get", "hget"]
     *           "slavehost2.*" : ["mget", "publish"]
     * </pre>
     *
     * @param value 键为主机名正则，值为应在此节点执行的命令名集合
     */
    public void setCommandsMap(Map<String, Set<String>> value) {
        for (Map.Entry<String, Set<String>> e : value.entrySet()) {
            Set<String> cc = e.getValue().stream()
                                            .map(c -> c.toLowerCase(Locale.ENGLISH))
                                            .collect(Collectors.toSet());
            this.commandsMap.put(Pattern.compile(e.getKey()), cc);
        }
    }

}
