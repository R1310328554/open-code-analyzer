/*
 * Copyright 1999-2023 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.config.server.model.gray;

import com.alibaba.nacos.api.exception.NacosException;

import java.util.Map;

/**
 * 灰度规则抽象基类：由 type 与 version 决定具体解析与匹配逻辑。
 * 构造时解析原始表达式，解析失败则标记为无效；子类实现 {@link #parse} 与 {@link #match}。
 * Gray rule. type with version determined parse logic.
 *
 * @author shiyiyue
 */
public abstract class AbstractGrayRule implements GrayRule {
    
    /** 原始灰度规则表达式字符串 */
    protected String rawGrayRuleExp;
    
    /** 规则优先级，数值越大越优先匹配 */
    protected int priority;
    
    /** 规则是否有效（解析成功且语义合法） */
    protected volatile boolean valid = true;
    
    /** 无参构造，供 SPI 与反射实例化使用 */
    public AbstractGrayRule() {
    }
    
    /**
     * 根据原始表达式与优先级构造灰度规则。
     *
     * @param rawGrayRuleExp 原始灰度表达式
     * @param priority       匹配优先级
     */
    public AbstractGrayRule(String rawGrayRuleExp, int priority) {
        try {
            parse(rawGrayRuleExp);
            this.priority = priority;
        } catch (NacosException e) {
            valid = false;
        }
        this.rawGrayRuleExp = rawGrayRuleExp;
    }
    
    /**
     * 解析原始灰度规则表达式为内部结构。
     *
     * @param rawGrayRule 原始灰度规则字符串
     * @throws NacosException 解析失败时抛出
     * @date 2024/3/14
     */
    protected abstract void parse(String rawGrayRule) throws NacosException;
    
    /**
     * 判断客户端连接标签是否命中本灰度规则。
     *
     * @param labels 连接侧标签 Map（如 ClientIp、VipserverTag 等）
     * @return 命中返回 true
     * @date 2024/3/14
     */
    public abstract boolean match(Map<String, String> labels);
    
    /** 规则是否在构造/解析后仍有效 */
    public boolean isValid() {
        return valid;
    }
    
    /**
     * 获取灰度规则类型标识（如 beta、tag、tagv2）。
     *
     * @return 规则 type
     * @date 2024/3/14
     */
    public abstract String getType();
    
    /**
     * 获取灰度规则版本号，与 type 共同唯一定位实现类。
     *
     * @return 规则 version
     * @date 2024/3/14
     */
    public abstract String getVersion();
    
    /** 获取持久化/展示用的原始表达式 */
    public String getRawGrayRuleExp() {
        return rawGrayRuleExp;
    }
    
    /** 获取规则优先级 */
    public int getPriority() {
        return priority;
    }
    
    /** 设置规则优先级 */
    public void setPriority(int priority) {
        this.priority = priority;
    }
}
