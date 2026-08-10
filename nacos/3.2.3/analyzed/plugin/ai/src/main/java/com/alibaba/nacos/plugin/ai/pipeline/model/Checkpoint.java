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

package com.alibaba.nacos.plugin.ai.pipeline.model;

import java.util.Objects;

/**
 * 发布流水线安全插件上报的单个审计检查项。
 *
 * <p>每个检查项对应一条可读的安全或合规维度，记录该项是否通过，
 * 供发布结果汇总与控制台展示。</p>
 *
 * @author qiacheng.cxy
 */
public class Checkpoint {
    
    /** 审计维度的可读名称，例如「敏感信息扫描」。 */
    private String title;
    
    /** 该审计维度是否通过。 */
    private boolean passed;
    
    public Checkpoint() {
    }
    
    /**
     * 构造指定标题与通过状态的检查项。
     *
     * @param title  审计维度名称
     * @param passed 是否通过
     */
    public Checkpoint(String title, boolean passed) {
        this.title = title;
        this.passed = passed;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public boolean getPassed() {
        return passed;
    }
    
    public void setPassed(boolean passed) {
        this.passed = passed;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Checkpoint that = (Checkpoint) o;
        return Objects.equals(title, that.title) && Objects.equals(passed, that.passed);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(title, passed);
    }
}
