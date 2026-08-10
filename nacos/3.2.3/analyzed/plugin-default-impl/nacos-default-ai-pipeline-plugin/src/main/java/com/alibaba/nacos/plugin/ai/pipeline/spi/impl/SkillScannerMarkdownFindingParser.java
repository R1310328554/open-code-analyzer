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

import com.alibaba.nacos.plugin.ai.pipeline.model.Checkpoint;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 从 skill-scanner {@code --format markdown} 输出中提取发现项标题，映射为 {@link Checkpoint} 行。
 *
 * <p>定位 {@code ## Findings} 小节，收集其下每条 {@code ### ...} 标题作为失败检查点（例如 {@code ### HIGH — Prompt injection}）。</p>
 *
 * @author qiacheng.cxy
 * @since 3.2.0
 */
final class SkillScannerMarkdownFindingParser {
    
    private static final Pattern NEXT_H2_NOT_H3 = Pattern.compile("(?m)^## [^#]");
    
    private static final String CHECK_PROMPT_INJECTION = "Prompt injection 检查";
    
    private static final String CHECK_DATA_EXFILTRATION = "Data exfiltration 检查";
    
    private static final String CHECK_MALICIOUS_PATTERN = "Malicious code patterns 检查";
    
    private static final String CHECK_PIPELINE_TAINT = "Pipeline taint analysis 检查";
    
    private static final String CHECK_BYTECODE_INTEGRITY = "Bytecode integrity 检查";
    
    private static final String CHECK_LLM_SEMANTIC = "LLM semantic analysis 检查";
    
    private static final String CHECK_META_FILTERING = "Meta-analyzer filtering 检查";
    
    private SkillScannerMarkdownFindingParser() {
    }
    
    /**
     * 构建拒绝（失败）检查点：{@code ## Findings} 下每个三级标题对应一条失败记录。
     * 若未解析到标题，返回一条 HIGH/CRITICAL 兜底检查点以保证结构化输出。
     */
    static List<Checkpoint> buildRejectCheckpoints(String markdown) {
        List<String> titles = extractFindingTitles(markdown);
        if (titles.isEmpty()) {
            return Collections.singletonList(new Checkpoint("HIGH/CRITICAL 风险检测", false));
        }
        List<Checkpoint> list = new ArrayList<>(titles.size());
        for (String title : titles) {
            list.add(new Checkpoint(title, false));
        }
        return list;
    }
    
    /**
     * 扫描成功时构建通过检查点列表，按扫描选项包含 Prompt 注入、数据外泄等项；
     * 启用 LLM/Meta 分析时追加对应检查行。
     */
    static List<Checkpoint> buildPassCheckpoints(SkillScannerScanOptions scanOptions) {
        List<Checkpoint> list = new ArrayList<>();
        list.add(new Checkpoint(CHECK_PROMPT_INJECTION, true));
        list.add(new Checkpoint(CHECK_DATA_EXFILTRATION, true));
        list.add(new Checkpoint(CHECK_MALICIOUS_PATTERN, true));
        list.add(new Checkpoint(CHECK_PIPELINE_TAINT, true));
        list.add(new Checkpoint(CHECK_BYTECODE_INTEGRITY, true));
        if (scanOptions != null && scanOptions.isUseLlm()) {
            list.add(new Checkpoint(CHECK_LLM_SEMANTIC, true));
            if (scanOptions.isEnableMeta()) {
                list.add(new Checkpoint(CHECK_META_FILTERING, true));
            }
        }
        return list;
    }
    
    /** 从 {@code ## Findings} 小节内提取所有 {@code ### } 标题文本。 */

    static List<String> extractFindingTitles(String markdown) {
        if (markdown == null || markdown.isEmpty()) {
            return Collections.emptyList();
        }
        int findingsStart = indexOfIgnoreCase(markdown, "## Findings");
        if (findingsStart < 0) {
            return Collections.emptyList();
        }
        int lineAfterHeading = markdown.indexOf('\n', findingsStart);
        if (lineAfterHeading < 0) {
            return Collections.emptyList();
        }
        int bodyStart = lineAfterHeading + 1;
        int bodyEnd = findNextH2SectionStart(markdown, bodyStart);
        String section =
            bodyEnd < 0 ? markdown.substring(bodyStart) : markdown.substring(bodyStart, bodyEnd);
        return extractH3Titles(section);
    }
    
    private static int findNextH2SectionStart(String markdown, int from) {
        Matcher m = NEXT_H2_NOT_H3.matcher(markdown);
        if (m.find(from)) {
            return m.start();
        }
        return -1;
    }
    
    private static List<String> extractH3Titles(String section) {
        Pattern h3 = Pattern.compile("(?m)^###\\s+(.+)$");
        Matcher m = h3.matcher(section);
        List<String> titles = new ArrayList<>();
        while (m.find()) {
            String t = m.group(1).trim();
            if (!t.isEmpty()) {
                titles.add(t);
            }
        }
        return titles;
    }
    
    private static int indexOfIgnoreCase(String haystack, String needle) {
        return haystack.toLowerCase().indexOf(needle.toLowerCase());
    }
}
