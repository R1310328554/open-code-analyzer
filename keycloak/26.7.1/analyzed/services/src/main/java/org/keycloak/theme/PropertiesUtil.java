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

package org.keycloak.theme;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.Enumeration;
import java.util.Properties;
import java.util.PropertyResourceBundle;

/**
 * 属性文件读取工具。
 * <p>支持 UTF-8 与 ISO-8859-1 自动检测，行为与 {@link PropertyResourceBundle} 一致。</p>
 *
 * @author <a href="mailto:wadahiro@gmail.com">Hiroyuki Wada</a>
 */
public class PropertiesUtil {

    /**
     * 以字符集感知方式读取 properties 流并合并到目标 {@link Properties}。
     * <p>可通过系统属性 {@code java.util.PropertyResourceBundle.encoding} 指定编码。</p>
     *
     * @param properties 目标属性表
     * @param stream 输入流
     * @see PropertyResourceBundle
     */
    public static void readCharsetAware(Properties properties, InputStream stream) throws IOException {
        PropertyResourceBundle propertyResourceBundle = new PropertyResourceBundle(stream);
        Enumeration<String> keys = propertyResourceBundle.getKeys();
        while(keys.hasMoreElements()) {
            String s = keys.nextElement();
            properties.put(s, propertyResourceBundle.getString(s));
        }
    }

}
