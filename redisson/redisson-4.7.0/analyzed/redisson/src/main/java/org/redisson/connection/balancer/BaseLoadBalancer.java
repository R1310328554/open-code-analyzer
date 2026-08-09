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

import org.redisson.connection.ClientConnectionsEntry;

import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 负载均衡器基类，提供按主机名正则过滤从节点列表的能力。
 *
 * @author Nikita Koksharov
 *
 */
public abstract class BaseLoadBalancer implements LoadBalancer {

    /** 主机名:端口 过滤正则（可选）。 */
    private Pattern pattern;

    /**
     * 设置主机名过滤正则，匹配 {@code host:port} 格式。
     *
     * @param value 正则表达式
     */
    public void setRegex(String value) {
        this.pattern = Pattern.compile(value);
    }

    /** 使用实例级 pattern 过滤从节点列表。 */
    protected List<ClientConnectionsEntry> filter(List<ClientConnectionsEntry> entries) {
        return filter(entries, pattern);
    }

    /** 按指定 pattern 过滤从节点，pattern 为 null 时原样返回。 */
    protected final List<ClientConnectionsEntry> filter(List<ClientConnectionsEntry> entries, Pattern pattern) {
        if (pattern == null) {
            return entries;
        }
        return entries.stream().filter(e ->
                        pattern.matcher(e.getClient().getAddr().getHostName() + ":" + e.getClient().getAddr().getPort()).matches())
                .collect(Collectors.toList());
    }

}
