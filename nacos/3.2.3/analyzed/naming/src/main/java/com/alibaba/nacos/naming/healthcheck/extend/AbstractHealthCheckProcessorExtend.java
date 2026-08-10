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

package com.alibaba.nacos.naming.healthcheck.extend;

import com.alibaba.nacos.common.utils.StringUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.config.SingletonBeanRegistry;

import java.util.Set;

/**
 * 健康检查处理器扩展抽象基类。
 *
 * <p>实现 {@link BeanFactoryAware} 以获取 Spring 单例注册表，供子类将 SPI 加载的扩展处理器注册为 Bean。</p>
 *
 * @author sunmengying
 */
public abstract class AbstractHealthCheckProcessorExtend implements BeanFactoryAware {
    
    /** Spring 单例 Bean 注册表。 */
    protected SingletonBeanRegistry registry;
    
    /**
     * 在内置检查类型集合上追加扩展处理器类型。
     *
     * @param origin Origin Checker Type
     * @return Extend Processor Type
     */
    abstract Set<String> addProcessor(Set<String> origin);
    
    /** 将类名首字母小写，用作 Spring Bean 名称。 */
    protected String lowerFirstChar(String simpleName) {
        if (StringUtils.isBlank(simpleName)) {
            throw new IllegalArgumentException("can't find extend processor class name");
        }
        return String.valueOf(simpleName.charAt(0)).toLowerCase() + simpleName.substring(1);
    }
    
    /** 注入 Bean 工厂并保存单例注册表引用。 */
    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        if (beanFactory instanceof SingletonBeanRegistry) {
            this.registry = (SingletonBeanRegistry) beanFactory;
        }
    }
}
