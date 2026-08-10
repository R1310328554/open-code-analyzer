/*
 * Copyright 2024 Red Hat, Inc. and/or its affiliates
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

package org.keycloak.utils;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Map;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

/**
 * FreeMarker 模板加载与渲染工具。
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class FreemarkerUtils {

    /**
     * 从 classpath 加载 FreeMarker 模板并渲染为字符串。
     *
     * @param contextMap 传入模板的属性映射
     * @param templateFile 模板文件名（与 clazz 同包路径）
     * @param clazz 用于定位模板资源的类
     * @return 渲染后的模板内容
     * @throws TemplateException 模板处理失败时抛出
     * @throws IOException 读取模板失败时抛出
     */
    public static String loadTemplateFromClasspath(Map<String, Object> contextMap, String templateFile, Class<?> clazz) throws TemplateException, IOException {
        Configuration freemarkerConfig = new Configuration(Configuration.VERSION_2_3_32);
        freemarkerConfig.setClassForTemplateLoading(clazz, "");
        Template freemarkerTemplate = freemarkerConfig.getTemplate(templateFile);

        Writer out = new StringWriter();
        freemarkerTemplate.process(contextMap, out);
        return out.toString();
    }
}
