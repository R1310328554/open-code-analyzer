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
package org.redisson.client.protocol.decoder;

import io.netty.buffer.ByteBuf;
import io.netty.util.CharsetUtil;
import org.redisson.client.handler.State;
import org.redisson.client.protocol.Decoder;
import org.redisson.cluster.ClusterNodeInfo;
import org.redisson.cluster.ClusterNodeInfo.Flag;
import org.redisson.cluster.ClusterSlotRange;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * {@code CLUSTER NODES} 文本回复解码器。
 * <p>
 * 按行解析节点 ID、地址、角色标志、主从关系及槽位区间，
 * 生成 {@link ClusterNodeInfo} 列表。
 *
 * @author Nikita Koksharov
 *
 */
public class ClusterNodesDecoder implements Decoder<List<ClusterNodeInfo>> {

    /** 连接 URI 使用的协议前缀（如 {@code redis}、{@code rediss}）。 */
    private final String scheme;

    /** @param scheme URI scheme，用于拼接节点地址 */
    public ClusterNodesDecoder(String scheme) {
        super();
        this.scheme = scheme;
    }

    /** 将整段 UTF-8 文本按行拆分为集群节点信息对象。 */
    @Override
    public List<ClusterNodeInfo> decode(ByteBuf buf, State state) throws IOException {
        String response = buf.toString(CharsetUtil.UTF_8);
        
        List<ClusterNodeInfo> nodes = new ArrayList<>();
        for (String nodeInfo : response.split("\n")) {
            ClusterNodeInfo node = new ClusterNodeInfo(nodeInfo);
            String[] params = nodeInfo.split(" ");

            String nodeId = params[0];
            node.setNodeId(nodeId);

            // 第三列为逗号分隔的角色/状态标志
            String flags = params[2];
            for (String flag : flags.split(",")) {
                for (Flag nodeInfoFlag : ClusterNodeInfo.Flag.values()) {
                    if (nodeInfoFlag.getValue().equalsIgnoreCase(flag)) {
                        node.addFlag(nodeInfoFlag);
                        break;
                    }
                }
            }
            
            if (!node.containsFlag(Flag.NOADDR)) {
                String[] parts = params[1].split(",");
                String uri = createUri(parts[0]);
                if (uri == null) {
                    continue;
                }
                node.setAddress(uri);
                if (parts.length == 2) {
                    node.setHostName(parts[1]);
                }
            }

            String slaveOf = params[3];
            if (!"-".equals(slaveOf)) {
                node.setSlaveOf(slaveOf);
            }

            // 第 9 列起为槽位或迁移标记
            if (params.length > 8) {
                for (int i = 0; i < params.length - 8; i++) {
                    String slots = params[i + 8];
                    // 跳过槽迁移中的临时标记
                    if (slots.contains("-<-") || slots.contains("->-")) {
                        continue;
                    }

                    String[] parts = slots.split("-");
                    if (parts.length == 1) {
                        node.addSlotRange(new ClusterSlotRange(Integer.parseInt(parts[0]), Integer.parseInt(parts[0])));
                    } else if (parts.length == 2) {
                        node.addSlotRange(new ClusterSlotRange(Integer.parseInt(parts[0]), Integer.parseInt(parts[1])));
                    }
                }
            }
            nodes.add(node);
        }
        return nodes;
    }

    /** 从 {@code host:port@bus-port} 片段提取客户端地址并加上 scheme。 */
    private String createUri(String part) {

        String addr = part.split("@")[0];
        String name = addr.substring(0, addr.lastIndexOf(":"));
        if (name.isEmpty()) {
            // 地址为空则跳过该节点（如无有效 host）
            return null;
        }
        return scheme + "://" + addr;
    }

}
