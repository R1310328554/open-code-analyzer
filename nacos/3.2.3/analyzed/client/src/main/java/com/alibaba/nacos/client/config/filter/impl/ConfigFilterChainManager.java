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

package com.alibaba.nacos.client.config.filter.impl;

import com.alibaba.nacos.api.config.filter.IConfigFilter;
import com.alibaba.nacos.api.config.filter.IConfigFilterChain;
import com.alibaba.nacos.api.config.filter.IConfigRequest;
import com.alibaba.nacos.api.config.filter.IConfigResponse;
import com.alibaba.nacos.api.exception.NacosException;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.ServiceLoader;

/**
 * 配置过滤器链管理器，加载 SPI 过滤器并按 order 排序执行。
 *
 * <p>通过 {@link ServiceLoader} 发现 {@link IConfigFilter} 实现，支持运行时 {@link #addFilter} 追加。</p>
 *
 * @author Nacos
 */
public class ConfigFilterChainManager implements IConfigFilterChain {
    
    /** 已注册的过滤器列表（按 order 排序）。 */
    private final List<IConfigFilter> filters = new ArrayList<>();
    
    /** 过滤器初始化属性。 */
    private final Properties initProperty;
    
    /** 构造管理器并加载 SPI 过滤器。 */
    public ConfigFilterChainManager(Properties properties) {
        this.initProperty = properties;
        ServiceLoader<IConfigFilter> configFilters = ServiceLoader.load(IConfigFilter.class);
        for (IConfigFilter configFilter : configFilters) {
            addFilter(configFilter);
        }
    }
    
    /**
     * 添加过滤器并按 order 插入合适位置；同名过滤器不重复添加。
     *
     * @param filter 待添加的过滤器
     * @return 当前管理器实例
     */
    public synchronized ConfigFilterChainManager addFilter(IConfigFilter filter) {
        // 初始化过滤器
        filter.init(this.initProperty);
        // 按 order 值升序插入
        int i = 0;
        while (i < this.filters.size()) {
            IConfigFilter currentValue = this.filters.get(i);
            if (currentValue.getFilterName().equals(filter.getFilterName())) {
                break;
            }
            if (filter.getOrder() >= currentValue.getOrder() && i < this.filters.size()) {
                i++;
            } else {
                this.filters.add(i, filter);
                break;
            }
        }
        
        if (i == this.filters.size()) {
            this.filters.add(i, filter);
        }
        return this;
    }
    
    @Override
    /** 启动虚拟过滤链，依次执行各过滤器。 */
    public void doFilter(IConfigRequest request, IConfigResponse response) throws NacosException {
        new VirtualFilterChain(this.filters).doFilter(request, response);
    }
    
    /** 虚拟过滤链，递归驱动过滤器依次执行。 */
    private static class VirtualFilterChain implements IConfigFilterChain {
        
        private final List<? extends IConfigFilter> additionalFilters;
        
        private int currentPosition = 0;
        
        public VirtualFilterChain(List<? extends IConfigFilter> additionalFilters) {
            this.additionalFilters = additionalFilters;
        }
        
        @Override
        public void doFilter(final IConfigRequest request, final IConfigResponse response)
            throws NacosException {
            if (this.currentPosition != this.additionalFilters.size()) {
                this.currentPosition++;
                IConfigFilter nextFilter = this.additionalFilters.get(this.currentPosition - 1);
                nextFilter.doFilter(request, response, this);
            }
        }
    }
    
}
