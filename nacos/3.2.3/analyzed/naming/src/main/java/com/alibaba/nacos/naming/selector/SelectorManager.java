/*
 *  Copyright 1999-2021 Alibaba Group Holding Ltd.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package com.alibaba.nacos.naming.selector;

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.alibaba.nacos.api.selector.Selector;
import com.alibaba.nacos.api.selector.context.SelectorContextBuilder;
import com.alibaba.nacos.common.spi.NacosServiceLoader;
import com.alibaba.nacos.common.utils.JacksonUtils;
import com.alibaba.nacos.common.utils.StringUtils;
import com.alibaba.nacos.naming.misc.Loggers;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.alibaba.nacos.api.exception.NacosException.SERVER_ERROR;

/**
 * 实例选择器管理器。
 *
 * <p>通过 SPI 加载 {@link Selector} 与 {@link SelectorContextBuilder}，负责解析选择条件、构建上下文并执行 {@link Selector#select(Object)}，同时为控制台与 OpenAPI 暴露可用选择器类型。</p>
 *
 * @author chenglu
 * @date 2021-07-12 18:42
 */
@Component
public class SelectorManager {
    
    /** 上下文类型到 {@link SelectorContextBuilder} 的映射。 */
    /** 已注册的上下文构建器表。 */
    private Map<String, SelectorContextBuilder> contextBuilders = new HashMap<>(8);
    
    /** 选择器类型到 {@link Selector} 实现类的映射。 */
    /** 已注册的选择器类型表。 */
    private Map<String, Class<? extends Selector>> selectorTypes = new HashMap<>(8);
    
    /** 初始化选择器与上下文构建器 SPI 加载。 */
    @PostConstruct
    /** Spring 启动后加载 SPI 并注册选择器类型。 */
    public void init() {
        initSelectorContextBuilders();
        initSelectorTypes();
    }
    
    /** 通过 SPI 加载并注册 {@link SelectorContextBuilder}。 */
    private void initSelectorContextBuilders() {
        Collection<SelectorContextBuilder> selectorContextBuilders =
            NacosServiceLoader.load(SelectorContextBuilder.class);
        for (SelectorContextBuilder selectorContextBuilder : selectorContextBuilders) {
            if (contextBuilders.containsKey(selectorContextBuilder.getContextType())) {
                Loggers.SRV_LOG.warn(
                    "[SelectorManager] init selectorContextBuilders, SelectorContextBuilder type {} has value, ignore it.",
                    selectorContextBuilder.getContextType());
                continue;
            }
            contextBuilders.put(selectorContextBuilder.getContextType(), selectorContextBuilder);
            Loggers.SRV_LOG.info(
                "[SelectorManager] Load SelectorContextBuilder({}) contextType({}) successfully.",
                selectorContextBuilder.getClass(),
                selectorContextBuilder.getContextType());
        }
    }
    
    /** 加载 {@link Selector} 实现并注册 JSON 子类型；需有无参 public 构造器。 */
    private void initSelectorTypes() {
        Collection<Selector> selectors = NacosServiceLoader.load(Selector.class);
        for (Selector selector : selectors) {
            if (selectorTypes.containsKey(selector.getType())) {
                Loggers.SRV_LOG.warn(
                    "[SelectorManager] init Selectors, Selector type {} has value, ignore it.",
                    selector.getType());
                continue;
            }
            Class<? extends Selector> selectorClass = selector.getClass();
            try {
                Constructor constructor = selectorClass.getConstructor();
                if (Objects.isNull(constructor)) {
                    throw new NoSuchMethodException();
                }
                // 注册 Jackson 子类型序列化
                JacksonUtils.registerSubtype(selectorClass, selector.getType());
                selectorTypes.put(selector.getType(), selectorClass);
                Loggers.SRV_LOG.info(
                    "[SelectorManager] Load Selector({}) type({}) contextType({}) successfully.",
                    selectorClass, selector.getType(),
                    selector.getContextType());
            } catch (Exception e) {
                Loggers.SRV_LOG.warn(
                    "[SelectorManager] Selector {} cannot find public access default constructor, will be ignored.",
                    selectorClass);
            }
        }
    }
    
    /**
     * 返回所有已注册的选择器类型名。
     *
     * @return 选择器类型列表
     */
    /** 列出 {@link #selectorTypes} 中的全部类型键。 */
    public List<String> getAllSelectorTypes() {
        return new ArrayList<>(selectorTypes.keySet());
    }
    
    /**
     * 按类型与条件解析 {@link Selector}；类型不存在或解析失败时抛异常或返回 null。
     *
     * @param type 选择器类型，见 {@link Selector#getType()}
     * @param condition 传给 {@link Selector#parse(Object)} 的条件字符串
     * @return 已解析的选择器实例
     */
    /** 实例化并解析指定类型的选择器。 */
    public Selector parseSelector(String type, String condition) throws NacosException {
        if (StringUtils.isBlank(type)) {
            return null;
        }
        Class<? extends Selector> clazz = selectorTypes.get(type);
        if (Objects.isNull(clazz)) {
            return null;
        }
        try {
            Selector selector = clazz.newInstance();
            selector.parse(condition);
            return selector;
        } catch (Exception e) {
            Loggers.SRV_LOG.warn(
                "[SelectorManager] Parse Selector failed, type: {}, condition: {}.", type,
                condition, e);
            throw new NacosException(SERVER_ERROR, "Selector parses failed: " + e.getMessage());
        }
    }
    
    /**
     * 构建上下文并执行 {@link Selector#select(Object)}。
     *
     * @param selector 选择器实例
     * @param consumerIp 消费者 IP
     * @param providers 待筛选的提供者列表
     * @return 筛选后的实例列表
     */
    /** 查找上下文构建器并执行选择；失败时回退全部提供者。 */
    public <T extends Instance> List<T> select(Selector selector, String consumerIp,
        List<T> providers) {
        if (Objects.isNull(selector)) {
            return providers;
        }
        SelectorContextBuilder selectorContextBuilder =
            contextBuilders.get(selector.getContextType());
        if (Objects.isNull(selectorContextBuilder)) {
            Loggers.SRV_LOG.info("[SelectorManager] cannot find the contextBuilder of type {}.",
                selector.getType());
            return providers;
        }
        try {
            Object context = selectorContextBuilder.build(consumerIp, providers);
            return (List<T>) selector.select(context);
        } catch (Exception e) {
            Loggers.SRV_LOG
                .warn("[SelectorManager] execute select failed, will return all providers.", e);
            return providers;
        }
    }
}
