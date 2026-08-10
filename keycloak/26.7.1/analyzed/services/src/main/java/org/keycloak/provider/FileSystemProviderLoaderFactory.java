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
package org.keycloak.provider;

import java.io.File;
import java.io.FilenameFilter;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.LinkedList;
import java.util.List;

import org.jboss.logging.Logger;

/**
 * 文件系统 classpath 提供者加载器工厂。
 * <p>支持 {@code classpath} 类型资源描述，从 JAR 文件或目录构建 {@link URLClassLoader} 并创建 {@link DefaultProviderLoader}。</p>
 *
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
public class FileSystemProviderLoaderFactory implements ProviderLoaderFactory {

    /** 日志记录器 */
    private static final Logger logger = Logger.getLogger(FileSystemProviderLoaderFactory.class);

    /** 是否支持 classpath 资源类型 @param type 资源类型字符串 @return type 为 classpath 时 true */
    @Override
    public boolean supports(String type) {
        return "classpath".equals(type);
    }

    /** 根据分号分隔的路径/JAR 列表创建加载器 @param resource classpath 资源描述 @return 提供者加载器 */
    @Override
    public ProviderLoader create(KeycloakDeploymentInfo info, ClassLoader baseClassLoader, String resource) {
        return new DefaultProviderLoader(info, createClassLoader(baseClassLoader, resource.split(";")));
    }

    /** 将路径或目录下 JAR 转为 URL 并构建类加载器 @param parent 父类加载器 @param files 路径数组 @return URLClassLoader */
    private static URLClassLoader createClassLoader(ClassLoader parent, String... files) {
        try {
            List<URL> urls = new LinkedList<URL>();

            for (String f : files) {
                if (f.endsWith("*")) {
                    File dir = new File(f.substring(0, f.length() - 1));
                    if (dir.isDirectory()) {
                        for (File file : dir.listFiles(new JarFilter())) {
                            urls.add(file.toURI().toURL());
                        }
                    }
                } else {
                    urls.add(new File(f).toURI().toURL());
                }
            }

            logger.debugf("Loading providers from %s", urls);

            return new URLClassLoader(urls.toArray(new URL[urls.size()]), parent);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /** 目录扫描时仅接受 .jar 文件 */
    private static class JarFilter implements FilenameFilter {

        /** @param dir 目录 @param name 文件名 @return 扩展名为 .jar 时 true */
        @Override
        public boolean accept(File dir, String name) {
            return name.toLowerCase().endsWith(".jar");
        }

    }

}
