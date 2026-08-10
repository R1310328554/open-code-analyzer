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

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.InstantiationAwareBeanPostProcessor;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * 抽象重复 Bean 后处理器：避免子 Spring 上下文重复实例化父容器已有 Bean。
 *
 * <p>实现 {@link InstantiationAwareBeanPostProcessor}，在 Bean 实例化前若核心上下文已存在同名 Bean 且满足复用条件，则直接返回父容器实例。</p>
 *
 * @author xiweng.yy
 */
public abstract class AbstractNacosDuplicateBeanPostProcessor
    implements InstantiationAwareBeanPostProcessor {
    
    private final ConfigurableApplicationContext coreContext;
    
    protected AbstractNacosDuplicateBeanPostProcessor(ConfigurableApplicationContext context) {
        coreContext = null == context.getParent() ? context
            : (ConfigurableApplicationContext) context.getParent();
    }
    
    @Override
    public Object postProcessBeforeInstantiation(Class<?> beanClass, String beanName)
        throws BeansException {
        if (!coreContext.containsBean(beanName)) {
            return null;
        }
        BeanDefinition beanDefinition = coreContext.getBeanFactory().getBeanDefinition(beanName);
        return isReUsingBean(beanClass, beanName, beanDefinition) ? coreContext.getBean(beanName)
            : null;
    }
    
    /**
     * 判断是否从核心上下文复用 Bean。
     *
     * @param beanClass bean class
     * @param beanName bean name
     * @param beanDefinition bean definition
     * @return {@code true} 表示复用核心上下文 Bean，{@code false} 表示在子上下文重新构建。
     */
    protected abstract boolean isReUsingBean(Class<?> beanClass, String beanName,
        BeanDefinition beanDefinition);
}
