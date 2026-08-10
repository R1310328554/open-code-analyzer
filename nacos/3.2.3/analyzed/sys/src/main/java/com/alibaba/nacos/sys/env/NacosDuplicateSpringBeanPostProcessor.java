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

package com.alibaba.nacos.sys.env;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.InstantiationAwareBeanPostProcessor;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * 通用 Spring Bean 重复实例化抑制后处理器。
 *
 * <p>除 Spring 上下文相关 Bean 外，其余已在父容器注册的 Bean 均尝试复用；上下文类 Bean 复用可能导致生命周期问题，故显式跳过。</p>
 *
 * @author xiweng.yy
 */
public class NacosDuplicateSpringBeanPostProcessor extends AbstractNacosDuplicateBeanPostProcessor {
    
    public NacosDuplicateSpringBeanPostProcessor(ConfigurableApplicationContext context) {
        super(context);
    }
    
    @Override
    protected boolean isReUsingBean(Class<?> beanClass, String beanName,
        BeanDefinition beanDefinition) {
        return !isContextBean(beanClass);
    }
    
    /** 判断是否为 Spring 上下文相关 Bean（不可复用）。 */
    private boolean isContextBean(Class<?> beanClass) {
        return isContextClass(beanClass.getCanonicalName());
    }
    
    /** 按类名前缀识别 org.springframework.context / boot.context 包下类。 */
    private boolean isContextClass(String beanClassName) {
        return beanClassName.startsWith("org.springframework.context")
            || beanClassName.startsWith("org.springframework.boot.context");
    }
}
