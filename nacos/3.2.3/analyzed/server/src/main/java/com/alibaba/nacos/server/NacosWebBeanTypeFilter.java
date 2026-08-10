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
 * 仅保留 Web Bean 的组件扫描过滤器。
 *
 * <p>用于 {@link NacosServerBasicApplication}：匹配 Web Bean（{@code isWebBean}）， 在 excludeFilters 中排除，使基础进程不加载 REST 控制器等 Web 组件。</p>
 *
 * @author xiweng.yy
 */
public class NacosWebBeanTypeFilter extends AbstractNacosWebBeanTypeFilter {
    
    @Override
    /** Web Bean 时返回 true，配合 excludeFilter 排除注册。 */
    public boolean match(MetadataReader metadataReader, MetadataReaderFactory metadataReaderFactory)
        throws IOException {
        return super.isWebBean(metadataReader, metadataReaderFactory);
    }
}
