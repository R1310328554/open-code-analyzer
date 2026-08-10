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

package com.alibaba.nacos.naming.interceptor;

import com.alibaba.nacos.common.spi.NacosServiceLoader;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

/**
 * Naming 拦截器链抽象基类。
 *
 * <p>通过 {@link NacosServiceLoader} 加载 SPI 拦截器并按 {@link NacosNamingInterceptor#order()} 排序，依次调用 {@link #doInterceptor(Interceptable)} 执行拦截逻辑。</p>
 *
 * @author xiweng.yy
 */
public abstract class AbstractNamingInterceptorChain<T extends Interceptable>
    implements NacosNamingInterceptorChain<T> {
    
    /** 已加载并排序的拦截器列表。 */
    private final List<NacosNamingInterceptor<T>> interceptors;
    
    /** 通过 SPI 加载指定类型的拦截器并排序。 */
    protected AbstractNamingInterceptorChain(Class<? extends NacosNamingInterceptor<T>> clazz) {
        this.interceptors = new LinkedList<>();
        interceptors.addAll(NacosServiceLoader.load(clazz));
        interceptors.sort(Comparator.comparingInt(NacosNamingInterceptor::order));
    }
    
    /**
     * 获取所有已注册的拦截器。
     *
     * @return interceptors list
     */
    protected List<NacosNamingInterceptor<T>> getInterceptors() {
        return interceptors;
    }
    
    /** 动态追加拦截器并重新排序。 */
    @Override
    public void addInterceptor(NacosNamingInterceptor<T> interceptor) {
        interceptors.add(interceptor);
        interceptors.sort(Comparator.comparingInt(NacosNamingInterceptor::order));
    }
    
    /** 按顺序遍历拦截器，首个命中则调用 {@link Interceptable#afterIntercept()} 并返回。 */
    @Override
    public void doInterceptor(T object) {
        for (NacosNamingInterceptor<T> each : interceptors) {
            if (!each.isInterceptType(object.getClass())) {
                continue;
            }
            if (each.intercept(object)) {
                object.afterIntercept();
                return;
            }
        }
        object.passIntercept();
    }
}
