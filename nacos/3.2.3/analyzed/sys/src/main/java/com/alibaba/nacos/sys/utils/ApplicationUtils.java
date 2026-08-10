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

package com.alibaba.nacos.sys.utils;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.util.function.Consumer;

/**
 * Nacos 全局 Spring 应用上下文工具类。
 *
 * <p>实现 {@link ApplicationContextInitializer}，在容器启动时注入 {@link ApplicationContext}，供非 Spring 管理的静态代码获取 Bean、发布事件与加载资源。</p>
 *
 * @author <a href="mailto:liaochuntao@live.com">liaochuntao</a>
 */
@SuppressWarnings("all")
public class ApplicationUtils
    implements ApplicationContextInitializer<ConfigurableApplicationContext> {
    
    /** 全局持有的 Spring 应用上下文。 */
    private static ApplicationContext applicationContext;
    
    /** Nacos 进程是否已完成启动的标志位。 */
    private static boolean started = false;
    
    /** 返回 Nacos 是否已标记为启动完成。 */
    public static boolean isStarted() {
        return started;
    }
    
    /** 设置 Nacos 启动完成标志。 */
    public static void setStarted(boolean started) {
        ApplicationUtils.started = started;
    }
    
    /** 按 Bean 名称获取实例。 */
    public static Object getBean(String name) throws BeansException {
        return applicationContext.getBean(name);
    }
    
    /** 按名称与类型获取 Bean。 */
    public static <T> T getBean(String name, Class<T> requiredType) throws BeansException {
        return applicationContext.getBean(name, requiredType);
    }
    
    public static Object getBean(String name, Object... args) throws BeansException {
        return applicationContext.getBean(name, args);
    }
    
    /** 按类型获取唯一 Bean。 */
    public static <T> T getBean(Class<T> requiredType) throws BeansException {
        return applicationContext.getBean(requiredType);
    }
    
    /** Bean 存在时执行 consumer，不存在则静默跳过。 */
    public static <T> void getBeanIfExist(Class<T> requiredType, Consumer<T> consumer)
        throws BeansException {
        try {
            T bean = applicationContext.getBean(requiredType);
            consumer.accept(bean);
        } catch (NoSuchBeanDefinitionException ignore) {
        }
    }
    
    public static <T> T getBean(Class<T> requiredType, Object... args) throws BeansException {
        return applicationContext.getBean(requiredType, args);
    }
    
    /** 判断容器中是否注册了指定名称的 Bean。 */
    public static boolean containsBean(String name) {
        return applicationContext.containsBean(name);
    }
    
    public static Class<?> getType(String name) throws NoSuchBeanDefinitionException {
        return applicationContext.getType(name);
    }
    
    /** 向 Spring 容器发布应用事件。 */
    public static void publishEvent(Object event) {
        applicationContext.publishEvent(event);
    }
    
    /** 按 Ant 风格路径模式批量加载资源。 */
    public static Resource[] getResources(String locationPattern) throws IOException {
        return applicationContext.getResources(locationPattern);
    }
    
    public static Resource getResource(String location) {
        return applicationContext.getResource(location);
    }
    
    public static ClassLoader getClassLoader() {
        return applicationContext.getClassLoader();
    }
    
    /** 返回当前持有的 ApplicationContext。 */
    public static ApplicationContext getApplicationContext() {
        return applicationContext;
    }
    
    /** 手动注入应用上下文（测试或特殊启动场景）。 */
    public static void injectContext(ConfigurableApplicationContext context) {
        ApplicationUtils.applicationContext = context;
    }
    
    @Override
    public void initialize(ConfigurableApplicationContext context) {
        if (null == applicationContext) {
            // 首次初始化，直接保存根上下文
            applicationContext = context;
        } else if (context.getParent() == applicationContext) {
            // 子上下文初始化时，仅保留第一个子上下文引用
            applicationContext = context;
        }
    }
}
