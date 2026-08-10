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
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alibaba.nacos.copilot.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.alibaba.nacos.api.ai.model.skills.Skill;
import com.alibaba.nacos.api.ai.model.skills.SkillResource;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.common.utils.StringUtils;
import com.alibaba.nacos.copilot.adapter.StreamResponseCallback;
import com.alibaba.nacos.copilot.capability.prompt.SkillOptimizationPrompt;
import com.alibaba.nacos.copilot.config.CopilotAgentManager;
import com.alibaba.nacos.copilot.model.ConversationHistory;
import com.alibaba.nacos.copilot.model.ConversationMessage;
import com.alibaba.nacos.copilot.model.SkillOptimizationRequest;
import com.alibaba.nacos.copilot.model.SkillOptimizationResponse;
import io.agentscope.core.ReActAgent;
import io.agentscope.core.agent.EventType;
import io.agentscope.core.agent.StreamOptions;
import io.agentscope.core.message.Msg;
import io.agentscope.core.message.MsgRole;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Skill 优化服务实现：构造多轮对话消息、调用 Agent 流式推理并推送优化分片。
 * Skill optimization service implementation.
 *
 * @author nacos
 */
@Service
public class SkillOptimizationServiceImpl implements SkillOptimizationService {
    
    /** SKILL.md 文件名常量。 */
    private static final String SKILL_MD_FILE_NAME = "SKILL.md";
    
    /** SKILL.md 在资源 Map 中的键名。 */
    private static final String SKILL_MD_KEY = "skill-md";
    
    /** 英文资源关键词，用于检测优化目标。 */
    private static final String RESOURCE_KEYWORD_EN = "resource";
    
    /** 中文资源关键词，用于检测优化目标。 */
    private static final String RESOURCE_KEYWORD_ZH = "资源";
    
    /** Copilot Agent 管理器。 */
    private final CopilotAgentManager agentManager;
    
    /** 注入 Agent 管理器。 */
    @Autowired
    public SkillOptimizationServiceImpl(CopilotAgentManager agentManager) {
        this.agentManager = agentManager;
    }
    
    @Override
    public void optimizeSkillStream(SkillOptimizationRequest request,
        StreamResponseCallback<SkillOptimizationResponse> callback) {
        // 1. 校验 Skill 对象
        Skill skill = request.getSkill();
        if (skill == null) {
            callback.onError(new NacosException(NacosException.INVALID_PARAM,
                "Skill object is required in request"));
            return;
        }
        
        // 2. 校验目标文件名（必填）
        if (StringUtils.isBlank(request.getTargetFileName())) {
            callback.onError(new NacosException(NacosException.INVALID_PARAM,
                "Target file name is required. Please select a file to optimize."));
            return;
        }
        
        // 3. 检查 Copilot 是否启用
        if (!agentManager.isEnabled()) {
            callback.onError(new NacosException(NacosException.INVALID_PARAM,
                "AI 功能未启用：请配置 Copilot API Key。请设置 nacos.copilot.llm.apiKey 或环境变量 COPILOT_API_KEY"));
            return;
        }
        
        // 4. 加载 Skill 优化系统提示词
        String systemPrompt = SkillOptimizationPrompt.SYSTEM_PROMPT;
        
        // 5. 构造多轮对话消息
        List<Msg> messages = buildConversationMessages(skill, request);
        
        // 6. 创建 Agent
        ReActAgent agent = agentManager.createAgent(systemPrompt);
        if (agent == null) {
            callback.onError(new NacosException(NacosException.INVALID_PARAM,
                "Failed to create Copilot agent. Please check configuration."));
            return;
        }
        
        // 7. 配置流式选项
        StreamOptions streamOptions = StreamOptions.builder()
            .eventTypes(EventType.REASONING, EventType.TOOL_RESULT)
            .incremental(true)
            .build();
        
        // 8. 以消息列表流式调用 Agent；前端自行累积解析
        Flux<io.agentscope.core.agent.Event> eventFlux = agent.stream(messages, streamOptions)
            .subscribeOn(Schedulers.boundedElastic());
        
        eventFlux.subscribe(StreamEventProcessor.createSubscriber(
            (type, content, done) -> {
                SkillOptimizationResponse response = new SkillOptimizationResponse();
                response.setType(type);
                response.setChunk(content);
                response.setDone(done);
                return response;
            },
            callback));
    }
    
    /**
     * 构造 user-assistant-user 三轮对话消息。
     * 流程：用户提交 Skill 内容 → 助手确认并询问优化方向 → 用户给出优化要求。
     *
     * @param skill 待优化的 Skill
     * @param request 优化请求
     * @return AgentScope 消息列表
     */
    @SuppressWarnings("PMD.MethodTooLongRule")
    private List<Msg> buildConversationMessages(Skill skill, SkillOptimizationRequest request) {
        List<Msg> messages = new ArrayList<>();
        
        // 判断请求中可用的上下文信息
        boolean hasOptimizationGoal = StringUtils.isNotBlank(request.getOptimizationGoal());
        boolean hasSelectedTools = false;
        List<Map<String, Object>> selectedMcpTools = null;
        boolean hasConversationHistory = request.getConversationHistory() != null
            && request.getConversationHistory().getMessages() != null
            && !request.getConversationHistory().getMessages().isEmpty();
        
        if (request.getParams() != null) {
            Object selectedMcpToolsObj = request.getParams().get("selectedMcpTools");
            if (selectedMcpToolsObj instanceof List) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> tools = (List<Map<String, Object>>) selectedMcpToolsObj;
                if (tools != null && !tools.isEmpty()) {
                    hasSelectedTools = true;
                    selectedMcpTools = tools;
                }
            }
        }
        
        // 目标文件名已在上方校验
        String targetFileName = request.getTargetFileName();
        
        // 消息 1：用户提供目标文件的 Skill 内容
        StringBuilder skillInfo = new StringBuilder();
        skillInfo.append("名称：").append(skill.getName()).append("\n");
        
        // 判断目标是 SKILL.md 还是资源文件
        boolean isSkillMd =
            SKILL_MD_FILE_NAME.equals(targetFileName) || SKILL_MD_KEY.equals(targetFileName);
        if (isSkillMd) {
            // 目标为 SKILL.md，包含描述与完整 Markdown
            skillInfo.append("描述：").append(skill.getDescription()).append("\n");
            skillInfo.append("SKILL.md：\n").append(skill.getSkillMd()).append("\n");
        } else if (skill.getResource() != null && !skill.getResource().isEmpty()) {
            // 目标为资源文件，仅包含匹配的资源内容
            boolean found = false;
            for (java.util.Map.Entry<String, SkillResource> entry : skill.getResource()
                .entrySet()) {
                String key = entry.getKey();
                SkillResource res = entry.getValue();
                
                // 按 key 或 name 匹配
                boolean matchByKey = key.equals(targetFileName);
                boolean matchByName = res.getName() != null && res.getName().equals(targetFileName);
                if (matchByKey || matchByName) {
                    found = true;
                    skillInfo.append("\n目标文件：").append(key).append("\n");
                    skillInfo.append("文件名：").append(res.getName()).append("\n");
                    if (StringUtils.isNotBlank(res.getType())) {
                        skillInfo.append("类型：").append(res.getType()).append("\n");
                    }
                    if (StringUtils.isNotBlank(res.getContent())) {
                        skillInfo.append("内容：\n").append(res.getContent()).append("\n");
                    }
                    break;
                }
            }
            if (!found) {
                // 未找到文件时列出全部资源供参考
                skillInfo.append("\n注意：未找到指定的文件 ").append(targetFileName)
                    .append("，以下是所有资源文件供参考：\n");
                skill.getResource().forEach((key, resource) -> {
                    skillInfo.append("- ").append(key).append(": ")
                        .append(resource.getName());
                    if (StringUtils.isNotBlank(resource.getType())) {
                        skillInfo.append(" (type: ").append(resource.getType()).append(")");
                    }
                    skillInfo.append("\n");
                });
            }
        }
        
        messages.add(Msg.builder()
            .textContent(skillInfo.toString())
            .role(MsgRole.USER)
            .build());
        
        // 消息 2：助手确认收到并询问优化方式
        messages.add(Msg.builder()
            .textContent("我已经收到了这个 Skill 的信息。你希望我怎么优化这条 skill？")
            .role(MsgRole.ASSISTANT)
            .build());
        
        // 消息 3：用户给出优化要求；优先级：对话历史 > MCP 工具 > 优化目标（近因效应最高）
        if (!hasConversationHistory && !hasSelectedTools && !hasOptimizationGoal) {
            // 最简单场景：用户仅试探功能，检查明显可改进点
            String simpleRequest = "请帮我看看这个文件（" + targetFileName + "）有没有明显可以优化的地方。"
                + "请只优化这个文件的内容，其他文件保持不变。如果没有明显问题，保持原样即可。";
            messages.add(Msg.builder()
                .textContent(simpleRequest)
                .role(MsgRole.USER)
                .build());
        } else {
            // 复杂场景：组装结构化优化请求
            StringBuilder optimizationRequest = new StringBuilder();
            
            // 第一部分：对话历史（若有）
            if (hasConversationHistory) {
                optimizationRequest.append("以下是一段对话交互历史。请仔细分析这段对话，完成以下任务：\n");
                optimizationRequest.append("1. 分析对话中的交互场景：识别用户的需求、助手的处理逻辑、工具调用的模式和流程\n");
                optimizationRequest.append("2. 将对话场景沉淀为标准流程：提取出可复用的标准操作步骤和决策逻辑\n");
                optimizationRequest.append(
                    "3. 基于沉淀的标准流程优化 Skill：将分析出的标准流程融入到 Skill 的 instruction 中，确保 Skill 能够支持类似的对话场景\n\n");
                
                ConversationHistory history = request.getConversationHistory();
                if (StringUtils.isNotBlank(history.getTitle())) {
                    optimizationRequest.append("对话主题：").append(history.getTitle()).append("\n");
                }
                if (StringUtils.isNotBlank(history.getContext())) {
                    optimizationRequest.append("对话上下文：").append(history.getContext()).append("\n");
                }
                optimizationRequest.append("\n对话交互内容：\n");
                
                for (ConversationMessage message : history.getMessages()) {
                    if ("user".equalsIgnoreCase(message.getType())) {
                        optimizationRequest.append("用户：").append(message.getContent()).append("\n");
                    } else if ("tool_call".equalsIgnoreCase(message.getType())) {
                        optimizationRequest.append("工具调用：");
                        if (StringUtils.isNotBlank(message.getToolName())) {
                            optimizationRequest.append(message.getToolName());
                        }
                        if (message.getToolInput() != null && !message.getToolInput().isEmpty()) {
                            optimizationRequest.append("，参数：").append(message.getToolInput());
                        }
                        if (message.getToolOutput() != null) {
                            optimizationRequest.append("，结果：").append(message.getToolOutput());
                        }
                        optimizationRequest.append("\n");
                    } else if ("model".equalsIgnoreCase(message.getType())) {
                        optimizationRequest.append("助手：").append(message.getContent()).append("\n");
                    }
                }
            }
            
            // 第二部分：MCP 工具（若有）
            if (hasSelectedTools && selectedMcpTools != null) {
                if (hasConversationHistory) {
                    optimizationRequest.append("\n");
                }
                optimizationRequest.append("我希望将以下 MCP 工具整合到这个 Skill 中：\n\n");
                for (Map<String, Object> tool : selectedMcpTools) {
                    optimizationRequest.append("工具：").append(tool.get("name")).append("\n");
                    if (tool.get("description") != null) {
                        optimizationRequest.append("描述：").append(tool.get("description"))
                            .append("\n");
                    }
                    if (tool.get("inputSchema") != null) {
                        optimizationRequest.append("参数：").append(tool.get("inputSchema"))
                            .append("\n");
                    }
                    optimizationRequest.append("\n");
                }
            }
            
            // 第三部分：优化目标（近因效应优先级最高）
            if (hasOptimizationGoal) {
                if (hasConversationHistory || hasSelectedTools) {
                    optimizationRequest.append("\n");
                }
                optimizationRequest.append("【重要】我的优化目标是：").append(request.getOptimizationGoal())
                    .append("\n");
                optimizationRequest.append("请优先考虑并聚焦于这个优化目标，所有优化建议和改动都应该围绕这个目标展开。");
                
                // 优化目标涉及资源时，强调禁止将 SKILL.md 放入 resource
                String optimizationGoalLower = request.getOptimizationGoal().toLowerCase();
                boolean containsResourceKeyword =
                    optimizationGoalLower.contains(RESOURCE_KEYWORD_ZH)
                        || optimizationGoalLower.contains(RESOURCE_KEYWORD_EN)
                        || optimizationGoalLower.contains("增加")
                        || optimizationGoalLower.contains("添加")
                        || optimizationGoalLower.contains("add")
                        || optimizationGoalLower.contains("增加资源")
                        || optimizationGoalLower.contains("添加资源");
                if (containsResourceKeyword) {
                    optimizationRequest.append("\n【绝对禁止】如果优化目标涉及添加或增加资源，请注意：");
                    optimizationRequest.append("\n- 绝对不能将 SKILL.md 放在 resource 字段中");
                    optimizationRequest.append("\n- 绝对不能创建名为 SKILL.md 的资源文件");
                    optimizationRequest
                        .append("\n- 绝对不能将 SKILL.md 放在任何资源类型（template、data、script 等）下");
                    optimizationRequest
                        .append("\n- SKILL.md 是特殊的元数据文件，位于 skillMd 字段，不需要也不应该在 resource 中定义");
                    optimizationRequest
                        .append("\n- 只能添加真正的资源文件（如 .json、.yaml、.txt 等），绝对不能添加 SKILL.md");
                }
            }
            
            // 第四部分：结合上下文的最终优化指令
            optimizationRequest.append("\n\n");
            
            // 追加仅优化目标文件的约束
            String targetFileConstraint = "【重要】请只优化文件 " + targetFileName + " 的内容，其他文件保持不变。";
            
            // 始终追加 SKILL.md 禁止放入 resource 的约束
            optimizationRequest.append("\n【绝对禁止】无论优化目标是什么，都绝对不能：");
            optimizationRequest.append("\n- 将 SKILL.md 放在 resource 字段中");
            optimizationRequest.append("\n- 创建名为 SKILL.md 的资源文件");
            optimizationRequest.append("\n- 将 SKILL.md 放在任何资源类型下");
            
            if (hasOptimizationGoal) {
                optimizationRequest.append(targetFileConstraint).append(" ");
                optimizationRequest.append("请基于以上要求优化这个文件，务必优先满足我的优化目标");
                if (hasConversationHistory && hasSelectedTools) {
                    optimizationRequest.append("，同时将从对话历史中分析出的标准流程融入到优化方案中，并确保工具整合服务于优化目标");
                } else if (hasConversationHistory) {
                    optimizationRequest.append("，同时将从对话历史中分析出的标准流程融入到优化方案中");
                } else if (hasSelectedTools) {
                    optimizationRequest.append("，并确保工具整合服务于优化目标");
                }
                optimizationRequest.append("。");
            } else if (hasConversationHistory) {
                optimizationRequest.append(targetFileConstraint).append(" ");
                optimizationRequest.append("请基于以上要求，特别是从对话历史中分析出的标准流程，优化这个文件");
                if (hasSelectedTools) {
                    optimizationRequest.append("，并整合上述工具");
                }
                optimizationRequest.append("。");
            } else if (hasSelectedTools) {
                optimizationRequest.append(targetFileConstraint).append(" ");
                optimizationRequest.append("请基于以上要求，整合上述工具并优化这个文件。");
            } else {
                optimizationRequest.append(targetFileConstraint).append(" ");
                optimizationRequest.append("请基于以上要求优化这个文件。");
            }
            
            messages.add(Msg.builder()
                .textContent(optimizationRequest.toString())
                .role(MsgRole.USER)
                .build());
        }
        
        return messages;
    }
    
}
