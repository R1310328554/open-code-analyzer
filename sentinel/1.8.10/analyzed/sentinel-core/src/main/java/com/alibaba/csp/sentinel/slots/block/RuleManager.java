/*
 * Copyright 1999-2018 Alibaba Group Holding Ltd.
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
package com.alibaba.csp.sentinel.slots.block;

import com.alibaba.csp.sentinel.util.function.Function;
import com.alibaba.csp.sentinel.util.function.Predicate;
import com.alibaba.csp.sentinel.config.SentinelConfig;

import java.util.*;
import java.util.regex.Pattern;

/**
 * 统一的规则管理工具，主要用于正则规则与简单规则的匹配和缓存。
 *
 * @author quguai
 * @date 2023/10/9 20:35
 */
public class RuleManager<R> {

    private Map<String, List<R>> originalRules = new HashMap<>();
    private Map<Pattern, List<R>> regexRules = new HashMap<>();
    private Map<String, List<R>> regexCacheRules = new HashMap<>();
    private Map<String, List<R>> simpleRules = new HashMap<>();
    private Function<List<R>, List<R>> generator = Function.identity();

    private final Predicate<R> predicate;

    public RuleManager() {
        predicate = r -> r instanceof AbstractRule && ((AbstractRule) r).isRegex();
    }

    public RuleManager(Function<List<R>, List<R>> generator, Predicate<R> predicate) {
        this.generator = generator;
        this.predicate = predicate;
    }

    /**
     * 从数据源更新规则，按正则表达式拆分规则映射，
     * 重建正则规则缓存以减少发布规则时的性能损耗。
     *
     * @param rulesMap 原始规则映射
     */
    public void updateRules(Map<String, List<R>> rulesMap) {
        originalRules = rulesMap;
        Map<Pattern, List<R>> regexRules = new HashMap<>();
        Map<String, List<R>> simpleRules = new HashMap<>();
        for (Map.Entry<String, List<R>> entry : rulesMap.entrySet()) {
            String resource = entry.getKey();
            List<R> rules = entry.getValue();

            List<R> rulesOfSimple = new ArrayList<>();
            List<R> rulesOfRegex = new ArrayList<>();
            for (R rule : rules) {
                if (predicate.test(rule)) {
                    rulesOfRegex.add(rule);
                } else {
                    rulesOfSimple.add(rule);
                }
            }
            if (!rulesOfRegex.isEmpty()) {
                regexRules.put(Pattern.compile(resource), rulesOfRegex);
            }
            if (!rulesOfSimple.isEmpty()) {
                simpleRules.put(resource, rulesOfSimple);
            }
        }
        // rebuild regex cache rules
        setRules(regexRules, simpleRules);
    }

    /**
     * 按资源名获取规则，将正则匹配后的规则列表缓存以提升性能。
     *
     * @param resource 资源名
     * @return 匹配到的规则列表
     */
    public List<R> getRules(String resource) {
        List<R> result = new ArrayList<>(simpleRules.getOrDefault(resource, Collections.emptyList()));
        if (regexRules.isEmpty() || (SentinelConfig.shouldSkipRegexIfSimpleRuleMatched() && !result.isEmpty())) {
            return result;
        }
        if (regexCacheRules.containsKey(resource)) {
            result.addAll(regexCacheRules.get(resource));
            return result;
        }
        synchronized (this) {
            if (regexCacheRules.containsKey(resource)) {
                result.addAll(regexCacheRules.get(resource));
                return result;
            }
            List<R> compilers = matcherFromRegexRules(resource);
            regexCacheRules.put(resource, compilers);
            result.addAll(compilers);
            return result;
        }
    }

    /**
     * 获取正则规则与简单规则中的全部规则。
     *
     * @return 规则列表
     */
    public List<R> getRules() {
        List<R> rules = new ArrayList<>();
        for (Map.Entry<Pattern, List<R>> entry : regexRules.entrySet()) {
            rules.addAll(entry.getValue());
        }
        for (Map.Entry<String, List<R>> entry : simpleRules.entrySet()) {
            rules.addAll(entry.getValue());
        }
        return rules;
    }

    /**
     * 获取原始规则，包含正则规则与简单规则。
     *
     * @return 原始规则映射
     */
    public Map<String, List<R>> getOriginalRules() {
        return originalRules;
    }

    /**
     * 根据资源名判断是否配置了规则。
     *
     * @param resource 资源名
     * @return 是否已配置规则
     */

    public boolean hasConfig(String resource) {
        if (resource == null) {
            return false;
        }
        return !getRules(resource).isEmpty();
    }

    /**
     * 校验规则的资源名字段是否为合法的正则表达式。
     *
     * @param rule 待校验规则
     * @return 是否为合法的正则规则
     */
    public static boolean checkRegexResourceField(AbstractRule rule) {
        if (!rule.isRegex()) {
            return true;
        }
        String resourceName = rule.getResource();
        try {
            Pattern.compile(resourceName);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private List<R> matcherFromRegexRules(String resource) {
        List<R> compilers = new ArrayList<>();
        for (Map.Entry<Pattern, List<R>> entry : regexRules.entrySet()) {
            if (entry.getKey().matcher(resource).matches()) {
                compilers.addAll(generator.apply(entry.getValue()));
            }
        }
        return compilers;
    }

    private synchronized void setRules(Map<Pattern, List<R>> regexRules, Map<String, List<R>> simpleRules) {
        this.regexRules = regexRules;
        this.simpleRules = simpleRules;
        if (regexRules.isEmpty()) {
            this.regexCacheRules = Collections.emptyMap();
            return;
        }
        // rebuild from regex cache rules
        Map<String, List<R>> rebuildCacheRule = new HashMap<>(regexCacheRules.size());
        for (String resource : regexCacheRules.keySet()) {
            rebuildCacheRule.put(resource, matcherFromRegexRules(resource));
        }
        this.regexCacheRules = rebuildCacheRule;
    }
}
