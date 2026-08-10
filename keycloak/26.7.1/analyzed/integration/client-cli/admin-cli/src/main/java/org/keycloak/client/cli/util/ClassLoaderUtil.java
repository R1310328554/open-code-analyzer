/*
 * Copyright 2022 Red Hat, Inc. and/or its affiliates
 *  and other contributors as indicated by the @author tags.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.keycloak.client.cli.util;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.stream.Stream;

/**
 * Keycloak 客户端 CLI 的类加载器解析工具。
 * <p>
 * 根据 {@code client/lib} 目录中是否存在 BC FIPS JAR，选择 FIPS 或默认加密实现所需的依赖 JAR 构建 {@link URLClassLoader}。
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class ClassLoaderUtil {

    /**
     * 检测 lib 目录中的 BC FIPS JAR 并构建包含合适加密依赖的类加载器。
     *
     * @param libDir 客户端 lib 目录路径（由 {@code kc.lib.dir} 系统属性指定）
     * @return 加载了 keycloak-crypto 与 BouncyCastle JAR 的 {@link URLClassLoader}
     */
    public static ClassLoader resolveClassLoader(String libDir) {
        File[] jarsInDir = new File(libDir).listFiles(file -> file.getName().endsWith(".jar"));

        // 检测 client/lib 目录中是否存在 BC FIPS JAR
        boolean bcFipsJarPresent = Stream.of(jarsInDir).anyMatch(file -> file.getName().startsWith("bc-fips"));
        String[] validJarPrefixes = bcFipsJarPresent ?  new String[] {"keycloak-crypto-fips1402", "bc-fips", "bctls-fips","bcutil-fips"} : new String[] {"keycloak-crypto-default", "bcprov-jdk18on"};
        URL[] usedJars = Stream.of(jarsInDir)
                .filter(file -> {
                    for (String prefix : validJarPrefixes) {
                        if (file.getName().startsWith(prefix + "-")) return true;
                    }
                    return false;
                })
                .map(file -> {
                    try {
                        return file.toURI().toURL();
                    } catch (MalformedURLException ex) {
                        throw new IllegalStateException("Error when converting file into URL. Please check the files in the directory " + jarsInDir, ex);
                    }
                }).toArray(URL[]::new);

        return new URLClassLoader(usedJars, ClassLoaderUtil.class.getClassLoader());
    }

}
