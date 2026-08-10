/*
 * Copyright 1999-2026 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.plugin.visibility.spi;

/**
 * 单资源可见性校验结果。
 *
 * <p>封装校验是否通过及拒绝原因，通过工厂方法 {@link #allow()} 与 {@link #deny(String)} 创建。</p>
 *
 * @author xiweng.yy
 */
public class ValidationResult {
    
    /** 是否允许访问。 */
    private final boolean allowed;
    
    /** 拒绝原因（允许时为 {@code null}）。 */
    private final String reason;
    
    private ValidationResult(boolean allowed, String reason) {
        this.allowed = allowed;
        this.reason = reason;
    }
    
    /**
     * 创建允许访问的结果。
     *
     * @return 允许结果
     */
    public static ValidationResult allow() {
        return new ValidationResult(true, null);
    }
    
    /**
     * 创建拒绝访问的结果。
     *
     * @param reason 拒绝原因
     * @return 拒绝结果
     */
    public static ValidationResult deny(String reason) {
        return new ValidationResult(false, reason);
    }
    
    /**
     * 判断是否允许访问。
     *
     * @return 允许返回 {@code true}
     */
    public boolean isAllowed() {
        return allowed;
    }
    
    /**
     * 获取拒绝原因。
     *
     * @return 拒绝原因，允许时为 {@code null}
     */
    public String getReason() {
        return reason;
    }
}
