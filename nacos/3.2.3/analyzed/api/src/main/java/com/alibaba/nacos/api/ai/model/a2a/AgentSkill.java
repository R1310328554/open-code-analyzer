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
 *
 */

package com.alibaba.nacos.api.ai.model.a2a;

import java.util.List;
import java.util.Objects;

/**
 * Agent 技能描述模型，声明 Agent 可执行的单一能力及其输入输出模式。
 *
 * <p>出现在 {@link AgentCardBasicInfo#getSkills()} 列表中，
 * 供客户端展示技能目录并选择合适的交互模式。</p>
 *
 * @author KiteSoar
 */
public class AgentSkill {
    
    /** 技能唯一标识。 */
    private String id;
    
    /** 技能显示名称。 */
    private String name;
    
    /** 技能功能描述。 */
    private String description;
    
    /** 技能分类标签列表。 */
    private List<String> tags;
    
    /** 技能使用示例短语列表。 */
    private List<String> examples;
    
    /** 支持的输入模式（如 text、file 等）。 */
    private List<String> inputModes;
    
    /** 支持的输出模式。 */
    private List<String> outputModes;
    
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public List<String> getTags() {
        return tags;
    }
    
    public void setTags(List<String> tags) {
        this.tags = tags;
    }
    
    public List<String> getExamples() {
        return examples;
    }
    
    public void setExamples(List<String> examples) {
        this.examples = examples;
    }
    
    public List<String> getInputModes() {
        return inputModes;
    }
    
    public void setInputModes(List<String> inputModes) {
        this.inputModes = inputModes;
    }
    
    public List<String> getOutputModes() {
        return outputModes;
    }
    
    public void setOutputModes(List<String> outputModes) {
        this.outputModes = outputModes;
    }
    
    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AgentSkill that = (AgentSkill) o;
        return Objects.equals(id, that.id) && Objects.equals(name, that.name)
            && Objects.equals(description,
                that.description)
            && Objects.equals(tags, that.tags) && Objects.equals(examples, that.examples)
            && Objects.equals(inputModes, that.inputModes)
            && Objects.equals(outputModes, that.outputModes);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id, name, description, tags, examples, inputModes, outputModes);
    }
}
