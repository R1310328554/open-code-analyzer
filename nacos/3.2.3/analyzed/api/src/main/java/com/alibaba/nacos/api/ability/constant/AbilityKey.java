/*
 * Copyright 1999-2022 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alibaba.nacos.api.ability.constant;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 能力键枚举，约束各端点（服务端/SDK/集群客户端）支持的功能标识。
 *
 * <p>同一 {@link AbilityMode} 下 {@link #getName()} 返回值必须唯一；
 * 静态块启动时会校验重复键名。</p>
 *
 * @author Daydreamer
 * @date 2022/8/31 12:27
 **/
public enum AbilityKey {
    
    /** 服务端是否支持通过 gRPC 注册/注销持久化实例。 */
    SERVER_PERSISTENT_INSTANCE_BY_GRPC("supportPersistentInstanceByGrpc",
        "support persistent instance by grpc",
        AbilityMode.SERVER),
    
    /** 服务端是否支持配置/命名的模糊 Watch。 */
    SERVER_FUZZY_WATCH("fuzzyWatch", "Server whether support fuzzy watch service or config",
        AbilityMode.SERVER),
    
    /** 服务端是否支持分布式锁。 */
    SERVER_DISTRIBUTED_LOCK("lock", "Server whether support distributed lock", AbilityMode.SERVER),
    
    /** 服务端是否支持 MCP 服务器发布与端点注册。 */
    SERVER_MCP_REGISTRY("mcp",
        "Server whether support release mcp server and register endpoint for mcp server",
        AbilityMode.SERVER),
    
    /** 服务端是否支持 Agent 与 Agent Card 发布及端点注册。 */
    SERVER_AGENT_REGISTRY("agent",
        "Server whether support release agent server and register endpoint for agent server",
        AbilityMode.SERVER),
    
    /** 服务端是否支持 A2A AgentCard 1.0 协议。 */
    SERVER_AGENT_CARD_V1("agentCardV1", "Server whether support A2A AgentCard 1.0 protocol",
        AbilityMode.SERVER),
    
    /** SDK 客户端是否支持配置/命名的模糊 Watch。 */
    SDK_CLIENT_FUZZY_WATCH("fuzzyWatch", "Client whether support fuzzy watch service or config",
        AbilityMode.SDK_CLIENT),
    
    /** SDK 客户端是否支持分布式锁。 */
    SDK_CLIENT_DISTRIBUTED_LOCK("lock", "Client whether support distributed lock",
        AbilityMode.SDK_CLIENT),
    
    /** SDK 客户端是否支持 MCP 服务器发布与端点注册。 */
    SDK_MCP_REGISTRY("mcp",
        "Client whether support release mcp server and register endpoint for mcp server",
        AbilityMode.SDK_CLIENT),
    
    /** SDK 客户端是否支持 Agent 与 Agent Card 发布及端点注册。 */
    SDK_AGENT_REGISTRY("agent",
        "Client whether support release agent server and register endpoint for agent server",
        AbilityMode.SDK_CLIENT),
    
    /** 集群客户端能力测试项（仅单元测试使用）。 */
    CLUSTER_CLIENT_TEST_1("test_1", "just for junit test", AbilityMode.CLUSTER_CLIENT);
    
    /** 能力键名字符串，用于序列化与能力表索引。 */
    private final String keyName;
    
    /** 能力的人类可读描述。 */
    private final String description;
    
    /** 能力所属端点类型（服务端/SDK/集群客户端）。 */
    private final AbilityMode mode;
    
    AbilityKey(String keyName, String description, AbilityMode mode) {
        this.keyName = keyName;
        this.description = description;
        this.mode = mode;
    }
    
    public String getName() {
        return keyName;
    }
    
    public String getDescription() {
        return description;
    }
    
    public AbilityMode getMode() {
        return mode;
    }
    
    /** 按 {@link AbilityMode} 分组的全部能力键索引。 */
    private static final Map<AbilityMode, Map<String, AbilityKey>> ALL_ABILITIES = new HashMap<>();
    
    /**
     * 获取指定模式下全部能力键枚举值。
     *
     * @return all keys
     */
    public static Collection<AbilityKey> getAllValues(AbilityMode mode) {
        return Collections.unmodifiableCollection(ALL_ABILITIES.get(mode).values());
    }
    
    /**
     * 获取指定模式下全部能力键名字符串。
     *
     * @return all names
     */
    public static Collection<String> getAllNames(AbilityMode mode) {
        return Collections.unmodifiableCollection(ALL_ABILITIES.get(mode).keySet());
    }
    
    /**
     * 判断指定模式下是否存在给定键名。
     *
     * @param name key name
     * @return whether contains
     */
    public static boolean isLegalKey(AbilityMode mode, String name) {
        return ALL_ABILITIES.get(mode).containsKey(name);
    }
    
    /**
     * 将字符串键→布尔值映射转换为 {@link AbilityKey}→布尔值映射（过滤非法键）。
     *
     * @param abilities map
     * @return enum map
     */
    public static Map<AbilityKey, Boolean> mapEnum(AbilityMode mode,
        Map<String, Boolean> abilities) {
        if (abilities == null || abilities.isEmpty()) {
            return Collections.emptyMap();
        }
        return abilities.entrySet().stream().filter(entry -> isLegalKey(mode, entry.getKey()))
            .collect(
                Collectors.toMap((entry) -> getEnum(mode, entry.getKey()), Map.Entry::getValue));
    }
    
    /**.
     * 将 {@link AbilityKey}→布尔值映射转换为字符串键→布尔值映射。
     *
     * @param abilities map
     * @return enum map
     */
    public static Map<String, Boolean> mapStr(Map<AbilityKey, Boolean> abilities) {
        if (abilities == null || abilities.isEmpty()) {
            return Collections.emptyMap();
        }
        return abilities.entrySet().stream()
            .collect(Collectors.toMap((entry) -> entry.getKey().getName(), Map.Entry::getValue));
    }
    
    /**.
     * 按模式与键名获取对应枚举常量。
     *
     * @param key string key
     * @return enum
     */
    public static AbilityKey getEnum(AbilityMode mode, String key) {
        return ALL_ABILITIES.get(mode).get(key);
    }
    
    static {
        // 开发者自检：确保同一 AbilityMode 下键名唯一
        try {
            for (AbilityKey value : AbilityKey.values()) {
                AbilityMode mode = value.getMode();
                Map<String, AbilityKey> map = ALL_ABILITIES.getOrDefault(mode, new HashMap<>());
                AbilityKey previous = map.putIfAbsent(value.getName(), value);
                if (previous != null) {
                    throw new IllegalStateException(
                        "Duplicate key name field " + value + " and " + previous + " under mode: "
                            + mode);
                }
                ALL_ABILITIES.put(mode, map);
            }
        } catch (Throwable t) {
            // 启动时打印重复键异常，便于开发者排查
            t.printStackTrace();
        }
    }
}
