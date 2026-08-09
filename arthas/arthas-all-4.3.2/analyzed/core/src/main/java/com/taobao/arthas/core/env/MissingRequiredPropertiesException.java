/*
 * Copyright 2002-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.taobao.arthas.core.env;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * 必填属性校验失败时抛出的异常。
 * <p>
 * 当 {@link ConfigurablePropertyResolver#validateRequiredProperties()} 发现
 * 通过 {@link ConfigurablePropertyResolver#setRequiredProperties(String...)} 声明的
 * 属性在环境中无法解析时抛出；{@link #getMissingRequiredProperties()} 返回缺失键集合。
 *
 * @author Chris Beams
 * @since 3.1
 * @see ConfigurablePropertyResolver#setRequiredProperties(String...)
 * @see ConfigurablePropertyResolver#validateRequiredProperties()
 * @see org.springframework.context.support.AbstractApplicationContext#prepareRefresh()
 */
@SuppressWarnings("serial")
public class MissingRequiredPropertiesException extends IllegalStateException {

    private final Set<String> missingRequiredProperties = new LinkedHashSet<String>();

    /** 内部方法：记录一个未能解析的必填属性键 */
    void addMissingRequiredProperty(String key) {
        this.missingRequiredProperties.add(key);
    }

    @Override
    public String getMessage() {
        // 英文消息列出全部缺失的必填属性
        return "The following properties were declared as required but could not be resolved: "
                + getMissingRequiredProperties();
    }

    /**
     * 返回校验时被标记为必填但未能解析的属性名集合。
     * 
     * @see ConfigurablePropertyResolver#setRequiredProperties(String...)
     * @see ConfigurablePropertyResolver#validateRequiredProperties()
     */
    public Set<String> getMissingRequiredProperties() {
        return this.missingRequiredProperties;
    }

}
