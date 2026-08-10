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

package com.alibaba.nacos.server;

import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;

import java.io.IOException;

/**
 * 排除 Web Bean 的组件扫描过滤器。
 *
 * <p>用于 {@link NacosServerWebApplication}：匹配非 Web Bean（{@code !isWebBean}）， 使 Web 进程不加载后台-only 组件。</p>
 *
 * @author xiweng.yy
 */
public class NacosNormalBeanTypeFilter extends AbstractNacosWebBeanTypeFilter {
    
    @Override
    /** 非 Web Bean 时返回 true，允许扫描注册。 */
    public boolean match(MetadataReader metadataReader, MetadataReaderFactory metadataReaderFactory)
        throws IOException {
        return !super.isWebBean(metadataReader, metadataReaderFactory);
    }
}
