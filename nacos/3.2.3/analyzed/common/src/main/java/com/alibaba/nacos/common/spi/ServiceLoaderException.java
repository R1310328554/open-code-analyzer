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

package com.alibaba.nacos.common.spi;

/**
 * Nacos SPI 服务加载异常：当 {@link com.alibaba.nacos.common.spi.NacosServiceLoader}
 * 无法通过 Java {@link java.util.ServiceLoader} 机制实例化指定 SPI 实现类时抛出。
 * 异常消息包含目标类全限定名，并保留原始 {@link Exception} 作为 cause。
 * Nacos service loader exception.
 *
 * @author xiweng.yy
 */
public class ServiceLoaderException extends RuntimeException {
    
    private static final long serialVersionUID = -4133484884875183141L;
    
    /** 加载失败的 SPI 实现类对象 */
    private final Class<?> clazz;
    
    /**
     * 构造 SPI 加载失败异常。
     *
     * @param clazz  无法加载的 SPI 实现类
     * @param caused 底层实例化或反射异常
     */
    public ServiceLoaderException(Class<?> clazz, Exception caused) {
        super(String.format("Can not load class `%s` by SPI ", clazz.getName()), caused);
        this.clazz = clazz;
    }
    
    /** 返回加载失败的 SPI 实现类 */
    public Class<?> getClazz() {
        return clazz;
    }
}
