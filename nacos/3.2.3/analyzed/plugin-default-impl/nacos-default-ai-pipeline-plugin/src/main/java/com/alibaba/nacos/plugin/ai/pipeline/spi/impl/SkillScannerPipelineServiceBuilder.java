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

package com.alibaba.nacos.plugin.ai.pipeline.spi.impl;

import com.alibaba.nacos.common.utils.StringUtils;
import com.alibaba.nacos.plugin.ai.pipeline.spi.PublishPipelineService;
import com.alibaba.nacos.plugin.ai.pipeline.spi.PublishPipelineServiceBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

/**
 * {@link SkillScannerPipelineService} 的 SPI 构建器。
 *
 * <p>初始化时检测 skill-scanner 是否可用，未安装则记录安装指引； 支持通过 {@code nacos.plugin.ai-pipeline.skill-scanner.*} 配置扫描选项：</p>
 * <ul>
 *   <li>{@code useLlm} — 为 {@code true} 时传递 {@code --use-llm}（语义分析，需 API Key）</li>
 *   <li>{@code llmApiKey} — 写入子进程 {@code SKILL_SCANNER_LLM_API_KEY}</li>
 *   <li>{@code llmModel} — 写入子进程 {@code SKILL_SCANNER_LLM_MODEL}</li>
 *   <li>{@code llmProvider} — {@code anthropic} 或 {@code openai}，对应 {@code --llm-provider}</li>
 *   <li>{@code enableMeta} — 为 {@code true} 时传递 {@code --enable-meta}</li>
 * </ul>
 *
 * @author qiacheng.cxy
 */
public class SkillScannerPipelineServiceBuilder implements PublishPipelineServiceBuilder {
    
    private static final Logger LOGGER =
        LoggerFactory.getLogger(SkillScannerPipelineServiceBuilder.class);
    
    /** 覆盖 scanner 可执行路径或命令名的配置键。 */

    private static final String PROPERTY_COMMAND = "command";
    
    /** 可执行路径的兼容配置键（executable）。 */
    private static final String PROPERTY_EXECUTABLE = "executable";
    
    /** 可执行路径的兼容配置键（path）。 */
    private static final String PROPERTY_PATH = "path";
    
    /** 返回流水线标识 skill-scanner。 */
    @Override
    public String pipelineId() {
        return "skill-scanner";
    }
    
    /** 解析配置并构建 {@link SkillScannerPipelineService} 实例。 */
    @Override
    public PublishPipelineService build(Properties properties) {
        SkillScannerScanOptions scanOptions = SkillScannerScanOptions.fromProperties(properties);
        String resolvedCommand = resolveSkillScannerCommand(properties);
        if (StringUtils.isBlank(resolvedCommand)) {
            LOGGER.warn("[SkillScannerPipeline] skill-scanner 未安装，插件将拒绝发布。{}",
                SkillScannerPipelineService.INSTALLATION_HINT);
        } else {
            if (scanOptions.isUseLlm()) {
                LOGGER.info(
                    "[SkillScannerPipeline] skill-scanner 已就绪，已启用 LLM 语义分析（--use-llm），command={}",
                    resolvedCommand);
            } else {
                LOGGER.info("[SkillScannerPipeline] skill-scanner 已就绪，插件已加载（静态扫描），command={}",
                    resolvedCommand);
            }
        }
        return new SkillScannerPipelineService(resolvedCommand, scanOptions);
    }
    
    /**
     * 从节点属性或系统 PATH 解析 skill-scanner 可执行路径。
     *
     * @param properties 流水线节点属性
     * @return 可执行路径，未找到时返回 {@code null}
     */
    private String resolveSkillScannerCommand(Properties properties) {
        for (String configured : getConfiguredCandidates(properties)) {
            String resolved = resolveCandidate(configured);
            if (StringUtils.isNotBlank(resolved)) {
                return resolved;
            }
        }
        
        return resolveCandidate(SkillScannerPipelineService.DEFAULT_SKILL_SCANNER_CMD);
    }
    
    /** 收集 command/executable/path 等配置项作为候选命令。 */
    private List<String> getConfiguredCandidates(Properties properties) {
        Set<String> result = new LinkedHashSet<>();
        addConfiguredCandidate(result, properties.getProperty(PROPERTY_COMMAND));
        addConfiguredCandidate(result, properties.getProperty(PROPERTY_EXECUTABLE));
        addConfiguredCandidate(result, properties.getProperty(PROPERTY_PATH));
        return new ArrayList<>(result);
    }
    
    private void addConfiguredCandidate(Set<String> candidates, String value) {
        if (StringUtils.isNotBlank(value)) {
            candidates.add(value.trim());
        }
    }
    
    /** 解析单个候选：绝对路径校验或 PATH 查找。 */
    private String resolveCandidate(String candidate) {
        if (StringUtils.isBlank(candidate)) {
            return null;
        }
        
        String expanded = expandHome(candidate.trim());
        if (containsPathSeparator(expanded)) {
            Path path = Paths.get(expanded).toAbsolutePath().normalize();
            if (Files.isRegularFile(path) && Files.isExecutable(path)) {
                return path.toString();
            }
            LOGGER.debug("[SkillScannerPipeline] skill-scanner 路径不存在或不可执行: {}", path);
            return null;
        }
        
        String pathResolved = findExecutableInPath(expanded);
        if (StringUtils.isNotBlank(pathResolved)) {
            return pathResolved;
        }
        
        LOGGER.debug("[SkillScannerPipeline] 在 PATH 中未找到命令: {}", expanded);
        return null;
    }
    
    /** 在 PATH 及 ~/.local/bin 中查找可执行文件。 */
    private String findExecutableInPath(String command) {
        String pathEnv = getPathEnv();
        if (StringUtils.isBlank(pathEnv)) {
            return null;
        }
        
        String userHome = System.getProperty("user.home", "");
        Set<String> directories = new LinkedHashSet<>();
        for (String each : pathEnv.split(File.pathSeparator)) {
            if (StringUtils.isNotBlank(each)) {
                directories.add(each.trim());
            }
        }
        if (StringUtils.isNotBlank(userHome)) {
            directories.add(Paths.get(userHome, ".local", "bin").toString());
        }
        
        for (String each : directories) {
            Path candidate = Paths.get(expandHome(each), command).toAbsolutePath().normalize();
            if (Files.isRegularFile(candidate) && Files.isExecutable(candidate)) {
                return candidate.toString();
            }
        }
        return null;
    }
    
    private boolean containsPathSeparator(String candidate) {
        return candidate.contains(File.separator) || candidate.contains("/")
            || candidate.contains("\\");
    }
    
    private String expandHome(String candidate) {
        if (candidate.startsWith("~/")) {
            String userHome = System.getProperty("user.home", "");
            if (StringUtils.isNotBlank(userHome)) {
                return Paths.get(userHome, candidate.substring(2)).toString();
            }
        }
        return candidate;
    }
    
    /** 读取 PATH 环境变量（便于单测覆写）。 */
    String getPathEnv() {
        return System.getenv("PATH");
    }
}
