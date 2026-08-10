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

/**
 * 灰度规则持久化 DTO：以 JSON 形式存入数据库，含 type、version、表达式与优先级。
 * 与 {@link GrayRuleManager} 配合完成序列化/反序列化及实例构造。
 * description.
 *
 * @author rong
 * @date 2024-03-14 10:57
 */
public class ConfigGrayPersistInfo {
    
    /** 灰度规则类型（如 beta、tag、tagv2） */
    private String type;
    
    /** 规则版本号，与 type 联合定位实现类 */
    private String version;
    
    /** 原始灰度表达式字符串 */
    private String expr;
    
    /** 规则匹配优先级 */
    private int priority;
    
    /**
     * 构造持久化信息。
     *
     * @param type     规则类型
     * @param version  规则版本
     * @param expr     原始表达式
     * @param priority 优先级
     */
    public ConfigGrayPersistInfo(String type, String version, String expr, int priority) {
        this.type = type;
        this.version = version;
        this.expr = expr;
        this.priority = priority;
    }
    
    /** 获取规则类型 */
    public String getType() {
        return type;
    }
    
    /** 设置规则类型 */
    public void setType(String type) {
        this.type = type;
    }
    
    /** 获取规则版本 */
    public String getVersion() {
        return version;
    }
    
    /** 设置规则版本 */
    public void setVersion(String version) {
        this.version = version;
    }
    
    /** 获取原始表达式 */
    public String getExpr() {
        return expr;
    }
    
    /** 设置原始表达式 */
    public void setExpr(String expr) {
        this.expr = expr;
    }
    
    /** 获取优先级 */
    public int getPriority() {
        return priority;
    }
    
    /** 设置优先级 */
    public void setPriority(int priority) {
        this.priority = priority;
    }
}
