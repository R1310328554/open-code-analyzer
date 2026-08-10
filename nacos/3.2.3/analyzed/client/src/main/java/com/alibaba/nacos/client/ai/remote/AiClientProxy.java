/*
 * Copyright 1999-2025 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.client.ai.remote;

import com.alibaba.nacos.api.ai.model.prompt.Prompt;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.common.lifecycle.Closeable;

/**
 * AI 客户端传输层代理接口，抽象 gRPC 与 HTTP 两种实现。
 *
 * <p>定义 Prompt、Skill、AgentSpec 等 AI 操作的统一入口，使上层可在 gRPC（{@link AiGrpcClient}）与 HTTP（{@link AiHttpClientProxy}）之间切换。</p>
 *
 * @author nacos
 */
public interface AiClientProxy extends Closeable {
    
    /**
     * 按 latest/版本/标签查询 Prompt，支持 MD5 条件查询。
     *
     * @param promptKey Prompt 业务键
     * @param version   Prompt 版本（可选）
     * @param label     Prompt 标签（可选）
     * @param md5       客户端 MD5，用于条件查询（可选）
     * @return Prompt 详情
     * @throws NacosException 参数无效或请求失败时抛出
     */
    Prompt queryPrompt(String promptKey, String version, String label, String md5)
        throws NacosException;
    
    /**
     * 按 latest/版本/标签查询 Skill，支持 MD5 条件下载。
     *
     * <p>当 {@code md5} 与服务端发布内容指纹一致时，实现必须抛出 {@link NacosException#NOT_MODIFIED}，以便调用方保留本地缓存。</p>
     *
     * @param skillName Skill 名称
     * @param version   Skill 版本（可选）
     * @param label     Skill 标签（可选）
     * @param md5       客户端 MD5（可选）
     * @return Skill ZIP 字节及 MD5、解析版本响应头
     * @throws NacosException 参数无效或请求失败时抛出
     */
    SkillQueryResponse querySkill(String skillName, String version, String label, String md5)
        throws NacosException;
    
    /**
     * 按 latest/版本/标签查询 AgentSpec，支持 MD5 条件查询。
     *
     * <p>当 {@code md5} 与服务端发布内容指纹一致时，实现必须抛出 {@link NacosException#NOT_MODIFIED}，以便调用方保留本地缓存。</p>
     *
     * @param agentSpecName AgentSpec 名称
     * @param version       AgentSpec 版本（可选）
     * @param label         AgentSpec 标签（可选）
     * @param md5           客户端 MD5（可选）
     * @return AgentSpec 及 MD5、解析版本响应头
     * @throws NacosException 参数无效或请求失败时抛出
     */
    AgentSpecQueryResponse queryAgentSpec(String agentSpecName, String version, String label,
        String md5) throws NacosException;
}
