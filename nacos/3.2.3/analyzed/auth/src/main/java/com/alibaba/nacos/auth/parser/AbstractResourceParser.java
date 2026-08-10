/*
 * Copyright 1999-2021 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.auth.parser;

import com.alibaba.nacos.auth.annotation.Secured;
import com.alibaba.nacos.plugin.auth.api.Resource;
import com.alibaba.nacos.plugin.auth.constant.Constants;

import java.util.Properties;

/**
 * 资源解析器抽象基类。
 *
 * <p>模板方法模式：子类提供命名空间、分组、资源名等字段，统一组装 {@link Resource}。</p>
 *
 * @author xiweng.yy
 * @since 2.1.0
 */
public abstract class AbstractResourceParser<R> implements ResourceParser<R> {
    
    /** 模板方法：聚合子类字段并构造 {@link Resource}。 */
    @Override
    public Resource parse(R request, Secured secured) {
        String namespaceId = getNamespaceId(request, secured);
        String group = getGroup(request);
        String name = getResourceName(request);
        Properties properties = getProperties(request);
        String action = secured.action().toString();
        properties.putIfAbsent(Constants.Resource.ACTION, action);
        injectTagsToProperties(properties, secured);
        return new Resource(namespaceId, group, name, secured.signType(), properties);
    }
    
    /**
     * 从请求中提取命名空间 ID。
     *
     * @param request 协议请求
     * @return 命名空间 ID
     */
    protected abstract String getNamespaceId(R request);
    
    /**
     * 结合 {@link Secured} 从请求提取命名空间 ID；默认委托 {@link #getNamespaceId(Object)}，子类可覆盖。
     *
     * @param request 协议请求
     * @param secured 鉴权注解
     * @return 命名空间 ID
     */
    protected String getNamespaceId(R request, Secured secured) {
        return getNamespaceId(request);
    }
    
    /**
     * 从请求中提取分组名。
     *
     * @param request 协议请求
     * @return 分组名
     */
    protected abstract String getGroup(R request);
    
    /**
     * 从请求中提取资源名。
     *
     * @param request 协议请求
     * @return 资源名
     */
    protected abstract String getResourceName(R request);
    
    /**
     * 从请求中提取附加属性。
     *
     * @param request 协议请求
     * @return 自定义属性
     */
    protected abstract Properties getProperties(R request);
    
    /**
     * 将 {@link Secured#tags()} 以键值对形式注入资源 properties。
     *
     * @param properties 资源属性容器
     * @param secured    鉴权注解
     */
    protected void injectTagsToProperties(Properties properties, Secured secured) {
        for (String each : secured.tags()) {
            properties.put(each, each);
        }
    }
}
