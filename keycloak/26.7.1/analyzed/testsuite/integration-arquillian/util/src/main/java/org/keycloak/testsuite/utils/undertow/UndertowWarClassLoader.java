/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.keycloak.testsuite.utils.undertow;



import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.Node;

/**
 * 基于 ShrinkWrap {@link Archive} 的 Undertow WAR 类加载器，从归档内加载类与资源。
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class UndertowWarClassLoader extends ClassLoader {

    /** ShrinkWrap 归档，作为类与资源的来源。 */
    private final Archive<?> archive;

    /**
     * 构造 WAR 类加载器。
     *
     * @param parent  父类加载器
     * @param archive ShrinkWrap 归档
     */
    public UndertowWarClassLoader(ClassLoader parent, Archive<?> archive) {
        super(parent);
        this.archive = archive;
    }

    /** 从归档中查找并定义指定类。 */
    @Override
    protected Class<?> findClass(String name) {
        try (InputStream resourceAsStream = getResourceAsStream(name.replace('.', '/') + ".class")) {
            byte[] bytes = IOUtils.toByteArray(resourceAsStream);
            return defineClass(name, bytes, 0, bytes.length);            
        } catch (IOException e) {
            throw new RuntimeException("Failed to find class [" + name + "]", e);
        }
    }

    /** 优先从父加载器获取资源，否则从 WAR 归档的 {@code WEB-INF/classes} 路径读取。 */
    @Override
    public InputStream getResourceAsStream(String name) {
        InputStream is = super.getResourceAsStream(name);
        if (is == null) {
            // 构建 WEB-INF/classes 下的资源路径
            String resourcePath = "/WEB-INF/classes";
            if (!name.startsWith("/")) {
                resourcePath = resourcePath + "/";
            }
            resourcePath = resourcePath + name;

            Node node = archive.get(resourcePath);
            if (node == null) {
                return null;
            } else {
                return node.getAsset().openStream();
            }
        } else {
            return is;
        }
    }

}
