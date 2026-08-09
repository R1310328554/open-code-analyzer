/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.rocketmq.filter;

import java.util.HashMap;
import java.util.Map;

/**
 * 消息过滤器工厂：支持注册多种 {@link FilterSpi} 实现（默认注册 SQL92）。
 */
public class FilterFactory {

    /** 全局单例工厂实例。 */
    public static final FilterFactory INSTANCE = new FilterFactory();

    /** 过滤器类型到 SPI 实现的映射表。 */
    protected static final Map<String, FilterSpi> FILTER_SPI_HOLDER = new HashMap<>(4);

    /** 静态块：默认注册 SQL92 过滤器。 */
    static {
        FilterFactory.INSTANCE.register(new SqlFilter());
    }

    /**
     * 注册自定义过滤器。
     * <br>
     * 注意：过滤器在 Broker 侧运行，需保证可靠性与性能。
     */
    /** 注册过滤器，同类型重复注册将抛出异常。 */
    public void register(FilterSpi filterSpi) {
        if (FILTER_SPI_HOLDER.containsKey(filterSpi.ofType())) {
            throw new IllegalArgumentException(String.format("Filter spi type(%s) already exist!", filterSpi.ofType()));
        }

        FILTER_SPI_HOLDER.put(filterSpi.ofType(), filterSpi);
    }

    /**
     * 注销指定类型的过滤器。
     */
    public FilterSpi unRegister(String type) {
        return FILTER_SPI_HOLDER.remove(type);
    }

    /**
     * 按类型获取已注册过滤器，不存在时返回 null。
     */
    public FilterSpi get(String type) {
        return FILTER_SPI_HOLDER.get(type);
    }

}
