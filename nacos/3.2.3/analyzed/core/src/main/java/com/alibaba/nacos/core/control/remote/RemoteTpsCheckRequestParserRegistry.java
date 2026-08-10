/*
 *
 * Copyright 1999-2021 Alibaba Group Holding Ltd.
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
 *
 */

package com.alibaba.nacos.core.control.remote;

import com.alibaba.nacos.plugin.control.Loggers;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 远程 TPS 请求解析器注册表：按解析器名称索引 {@link RemoteTpsCheckRequestParser}，供 {@link TpsControlRequestFilter} 在 RPC 链路查找解析实现。
 * remote tps check request parser registry.
 *
 * @author shiyiyue
 */
public class RemoteTpsCheckRequestParserRegistry {
    
    /** 解析器名称到实例的并发映射。 */
    static final Map<String, RemoteTpsCheckRequestParser> PARSER_MAP = new ConcurrentHashMap<>();
    
    /**
     * 注册远程 TPS 解析器；同名解析器会被覆盖并记录日志。
     *
     * @param remoteTpsCheckParser 待注册的解析器实例
     */
    public static void register(RemoteTpsCheckRequestParser remoteTpsCheckParser) {
        RemoteTpsCheckRequestParser prevRemoteTpsCheckParser = PARSER_MAP
            .put(remoteTpsCheckParser.getName(), remoteTpsCheckParser);
        if (prevRemoteTpsCheckParser != null) {
            Loggers.CONTROL.info(
                "RemoteTpsCheckRequestParser  name  {},point name {} will be replaced with {}",
                remoteTpsCheckParser.getName(), remoteTpsCheckParser.getPointName(),
                remoteTpsCheckParser.getClass().getSimpleName());
        } else {
            Loggers.CONTROL.info(
                "RemoteTpsCheckRequestParser register parser {} of name {},point name {}",
                remoteTpsCheckParser.getClass().getSimpleName(), remoteTpsCheckParser.getName(),
                remoteTpsCheckParser.getPointName());
        }
    }
    
    /**
     * 按名称获取已注册的解析器。
     *
     * @param name 解析器注册名
     * @return 解析器实例，未注册时返回 null
     */
    public static RemoteTpsCheckRequestParser getParser(String name) {
        return PARSER_MAP.get(name);
    }
}
