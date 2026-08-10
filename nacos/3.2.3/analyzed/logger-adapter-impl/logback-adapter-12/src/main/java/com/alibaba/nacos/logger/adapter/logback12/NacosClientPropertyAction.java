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

package com.alibaba.nacos.logger.adapter.logback12;

import ch.qos.logback.core.joran.action.Action;
import ch.qos.logback.core.joran.action.ActionUtil;
import ch.qos.logback.core.joran.spi.ActionException;
import ch.qos.logback.core.joran.spi.InterpretationContext;
import ch.qos.logback.core.util.OptionHelper;
import com.alibaba.nacos.common.logging.NacosLoggingProperties;
import org.xml.sax.Attributes;

/**
 * Logback Joran 自定义动作 {@code nacosClientProperty}，从 Nacos 客户端属性注入配置。
 *
 * <p>用法类似 Spring Boot 的 {@code springProperty}，例如： {@code <nacosClientProperty scope="context" name="logPath" source="system.log.path" defaultValue="/root" />}。</p>
 *
 * @author onewe
 */
class NacosClientPropertyAction extends Action {
    
    /** XML 属性名：默认值。 */
    private static final String DEFAULT_VALUE_ATTRIBUTE = "defaultValue";
    
    /** XML 属性名：Nacos 客户端属性源 key。 */
    private static final String SOURCE_ATTRIBUTE = "source";
    
    /** 当前加载配置时绑定的 Nacos 日志属性。 */
    private final NacosLoggingProperties loggingProperties;
    
    /** 构造动作并注入属性查找源。 */
    NacosClientPropertyAction(NacosLoggingProperties loggingProperties) {
        this.loggingProperties = loggingProperties;
    }
    
    /** 解析 {@code nacosClientProperty} 元素并将属性写入指定 scope。 */
    @Override
    public void begin(InterpretationContext ic, String elementName, Attributes attributes)
        throws ActionException {
        String name = attributes.getValue(NAME_ATTRIBUTE);
        String source = attributes.getValue(SOURCE_ATTRIBUTE);
        ActionUtil.Scope scope = ActionUtil.stringToScope(attributes.getValue(SCOPE_ATTRIBUTE));
        String defaultValue = attributes.getValue(DEFAULT_VALUE_ATTRIBUTE);
        if (OptionHelper.isEmpty(name)) {
            addError(
                "The \"name\" and \"source\"  attributes of <nacosClientProperty> must be set");
        }
        ActionUtil.setProperty(ic, name, getValue(source, defaultValue), scope);
    }
    
    @Override
    public void end(InterpretationContext ic, String name) throws ActionException {
        
    }
    
    private String getValue(String source, String defaultValue) {
        return null == loggingProperties ? defaultValue
            : loggingProperties.getValue(source, defaultValue);
    }
}
