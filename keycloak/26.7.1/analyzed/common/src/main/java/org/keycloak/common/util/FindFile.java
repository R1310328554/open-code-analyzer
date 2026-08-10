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

package org.keycloak.common.util;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import org.keycloak.common.constants.GenericConstants;

/**
 * 按 classpath 或文件系统路径定位并打开 Keycloak 配置文件输入流。
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class FindFile {
    /**
     * 解析配置路径：{@code classpath:} 前缀从类路径加载，否则按文件路径打开。
     */
    public static InputStream findFile(String keycloakConfigFile) {
        if (keycloakConfigFile.startsWith(GenericConstants.PROTOCOL_CLASSPATH)) {
            String classPathLocation = keycloakConfigFile.replace(GenericConstants.PROTOCOL_CLASSPATH, "");
            // 优先当前类加载器
            InputStream is = FindFile.class.getClassLoader().getResourceAsStream(classPathLocation);
            if (is == null) {
                is = Thread.currentThread().getContextClassLoader().getResourceAsStream(classPathLocation);
            }

            if (is != null) {
                return is;
            } else {
                throw new RuntimeException("Unable to find config from classpath: " + keycloakConfigFile);
            }
        } else {
            // 回退到文件系统
            try {
                return new FileInputStream(keycloakConfigFile);
            } catch (FileNotFoundException fnfe) {
                throw new RuntimeException(fnfe);
            }
        }
    }
}
