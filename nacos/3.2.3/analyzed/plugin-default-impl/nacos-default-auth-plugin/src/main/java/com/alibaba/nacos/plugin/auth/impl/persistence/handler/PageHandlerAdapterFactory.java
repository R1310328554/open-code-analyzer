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

package com.alibaba.nacos.plugin.auth.impl.persistence.handler;

import com.alibaba.nacos.plugin.auth.impl.persistence.handler.support.DefaultPageHandlerAdapter;
import com.alibaba.nacos.plugin.auth.impl.persistence.handler.support.DerbyPageHandlerAdapter;
import com.alibaba.nacos.plugin.auth.impl.persistence.handler.support.MysqlPageHandlerAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Collections;
import java.util.function.Consumer;

/**
 * 分页适配器工厂（单例）。
 *
 * <p>启动时注册 MySQL、Derby 与默认三种 {@link PageHandlerAdapter}， 供内嵌与外部分页助手按类名或 {@link PageHandlerAdapter#supports} 选取。</p>
 *
 * @author huangKeMing
 */
public class PageHandlerAdapterFactory {
    
    /** 已注册的适配器有序列表。 */
    private final List<PageHandlerAdapter> handlerAdapters;
    
    /** 类全名到适配器实例的映射。 */
    private final Map<String, PageHandlerAdapter> handlerAdapterMap;
    
    /** 返回全部适配器列表（不可变）。 */
    public List<PageHandlerAdapter> getHandlerAdapters() {
        return handlerAdapters;
    }
    
    /** 返回类名到适配器的映射（不可变）。 */
    public Map<String, PageHandlerAdapter> getHandlerAdapterMap() {
        return handlerAdapterMap;
    }
    
    private PageHandlerAdapterFactory() {
        List<PageHandlerAdapter> handlerAdapters = new ArrayList<>(3);
        Map<String, PageHandlerAdapter> handlerAdapterMap = new HashMap<>(3);
        Consumer<PageHandlerAdapter> addHandlerAdapter = handlerAdapter -> {
            handlerAdapters.add(handlerAdapter);
            handlerAdapterMap.put(handlerAdapter.getClass().getName(), handlerAdapter);
        };
        // 注册 MySQL LIMIT 分页适配器
        addHandlerAdapter.accept(new MysqlPageHandlerAdapter());
        // 注册 Derby OFFSET/FETCH 适配器
        addHandlerAdapter.accept(new DerbyPageHandlerAdapter());
        // 注册默认（无分页改写）适配器
        addHandlerAdapter.accept(new DefaultPageHandlerAdapter());
        this.handlerAdapters = Collections.unmodifiableList(handlerAdapters);
        this.handlerAdapterMap = Collections.unmodifiableMap(handlerAdapterMap);
    }
    
    private static final class InstanceHolder {
        
        static final PageHandlerAdapterFactory INSTANCE = new PageHandlerAdapterFactory();
    }
    
    /** 获取工厂单例。 */
    public static PageHandlerAdapterFactory getInstance() {
        return InstanceHolder.INSTANCE;
    }
    
}
