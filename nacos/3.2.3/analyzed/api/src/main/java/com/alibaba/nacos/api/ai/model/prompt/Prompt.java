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

package com.alibaba.nacos.api.ai.model.prompt;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * AI Prompt 实体，用于 Nacos Prompt 管理。
 *
 * <p>Prompt 以 Nacos 配置形式存储：固定 group 为 {@code nacos-ai-prompt}，
 * dataId 为 {@code {promptKey}.json}，内容为 JSON 格式。</p>
 *
 * @author nacos
 */
public class Prompt implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /** Prompt 唯一键（命名空间内唯一标识）。 */
    private String promptKey;
    
    /** Prompt 版本号，格式为 {@code major.minor.patch}（如 {@code 1.0.0}）。 */
    private String version;
    
    /** Prompt 模板正文，可含 {@code {{variableName}}} 占位符。 */
    private String template;
    
    /** Prompt 内容 MD5 摘要，用于 CAS 乐观锁更新。 */
    private String md5;
    
    /** 变量定义列表（含可选默认值）；旧版无变量元数据时为 {@code null}。 */
    private List<PromptVariable> variables;
    
    public Prompt() {
    }
    
    public Prompt(String promptKey, String version, String template) {
        this.promptKey = promptKey;
        this.version = version;
        this.template = template;
    }
    
    public String getPromptKey() {
        return promptKey;
    }
    
    public void setPromptKey(String promptKey) {
        this.promptKey = promptKey;
    }
    
    public String getVersion() {
        return version;
    }
    
    public void setVersion(String version) {
        this.version = version;
    }
    
    public String getTemplate() {
        return template;
    }
    
    public void setTemplate(String template) {
        this.template = template;
    }
    
    public String getMd5() {
        return md5;
    }
    
    public void setMd5(String md5) {
        this.md5 = md5;
    }
    
    public List<PromptVariable> getVariables() {
        return variables;
    }
    
    public void setVariables(List<PromptVariable> variables) {
        this.variables = variables;
    }
    
    /**
     * 渲染 Prompt 模板，将占位变量替换为实际值。
     *
     * <p>模板中使用 {@code {{variableName}}} 语法声明变量。
     * 先合并变量定义中的默认值，再以 {@code userVariables} 覆盖。</p>
     *
     * <p>示例：
     * <pre>
     * Prompt prompt = new Prompt("greeting", "1.0.0", "Hello {{name}}, welcome to {{place}}!");
     * Map&lt;String, String&gt; userVars = new HashMap&lt;&gt;();
     * userVars.put("name", "Alice");
     * userVars.put("place", "Nacos");
     * String result = prompt.render(userVars);
     * // 结果: "Hello Alice, welcome to Nacos!"
     * </pre>
     * </p>
     *
     * @param userVariables 变量名到取值的映射（key 为变量名，value 为替换文本）
     * @return 替换后的 Prompt 内容；无可用取值时返回原始模板
     */
    public String render(Map<String, String> userVariables) {
        if (template == null) {
            return null;
        }
        
        Map<String, String> merged = new HashMap<>();
        if (variables != null) {
            for (PromptVariable v : variables) {
                if (v.getDefaultValue() != null) {
                    merged.put(v.getName(), v.getDefaultValue());
                }
            }
        }
        if (userVariables != null) {
            merged.putAll(userVariables);
        }
        
        if (merged.isEmpty()) {
            return template;
        }
        
        String result = template;
        for (Map.Entry<String, String> entry : merged.entrySet()) {
            String placeholder = "{{" + entry.getKey() + "}}";
            String value = entry.getValue() != null ? entry.getValue() : "";
            result = result.replace(placeholder, value);
        }
        return result;
    }
    
    @Override
    public String toString() {
        return "Prompt{" + "promptKey='" + promptKey + '\'' + ", version='" + version + '\'' + '}';
    }
}
