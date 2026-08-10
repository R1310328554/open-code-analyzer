/*
 * Copyright 2002-2007 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alibaba.nacos.common.packagescan.resource;

/**
 * Copy from https://github.com/spring-projects/spring-framework.git, with less modifications
 * <p>上下文资源扩展接口：表示相对于 enclosing context（如 ServletContext、ResourceLoader 根路径）加载的资源，提供 context 内相对路径视图。</p>
 * Extended interface for a resource that is loaded from an enclosing
 * 'context', e.g. from a {@link jakarta.servlet.ServletContext} but also
 * from plain classpath paths or relative file system paths (specified
 * without an explicit prefix, hence applying relative to the local
 * {@link ResourceLoader}'s context).
 *
 * @author Juergen Hoeller
 * @since 2.5
 */
public interface ContextResource extends Resource {

    /**
     * Return the path within the enclosing 'context'.
     * <p>返回相对于 enclosing context 根目录的路径（如 Web 应用根下的路径）。</p>
     *
    String getPathWithinContext();

}
