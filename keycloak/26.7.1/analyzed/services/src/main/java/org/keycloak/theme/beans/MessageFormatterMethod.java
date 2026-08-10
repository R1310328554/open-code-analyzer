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

package org.keycloak.theme.beans;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

import org.keycloak.theme.TemplatingUtil;

import freemarker.template.SimpleDate;
import freemarker.template.SimpleNumber;
import freemarker.template.SimpleScalar;
import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModelException;

import static java.util.Optional.ofNullable;

/**
 * FreeMarker 消息格式化方法。
 * <p>从消息 bundle 查找键值，解析 {@code ${}} 占位符后按区域设置格式化，实现 {@link freemarker.template.TemplateMethodModelEx}。</p>
 *
 * @author <a href="mailto:gerbermichi@me.com">Michael Gerber</a>
 */
public class MessageFormatterMethod implements TemplateMethodModelEx {
    private final Properties messages;
    private final Locale locale;

    /** 使用 Properties 消息 bundle 构造。 */
    public MessageFormatterMethod(Locale locale, Properties messages) {
        this.locale = locale;
        this.messages = messages;
    }

    /** 使用 Map 消息源构造（转为 Properties）。 */
    public MessageFormatterMethod(Locale locale, Map<Object, Object> messages) {
        this.locale = locale;
        this.messages = new Properties();
        this.messages.putAll(ofNullable(messages).orElse(Map.of()));
    }

    /** 首参为消息键，其余为 MessageFormat 占位符参数。 */
    @Override
    public Object exec(List list) throws TemplateModelException {
        if (list.size() >= 1) {
            // 解析参数中残留的 ${} 表达式
            List<Object> resolved = resolve(list.subList(1, list.size()));
            String key = list.get(0).toString();
            String value = messages.getOrDefault(key, key).toString();
            // 若 bundle 值本身含占位符，同样解析
            value = (String) resolve(List.of(value)).get(0);
            return new MessageFormat(value, locale).format(resolved.toArray());
        } else {
            return null;
        }
    }

    /** 将 FreeMarker 模型对象转为 Java 对象并解析变量引用。 */
    private List<Object> resolve(List<Object> list) {
        ArrayList<Object> result = new ArrayList<>();
        for (Object item: list) {
            if (item instanceof SimpleScalar scalar) {
                item = scalar.getAsString();
            } else if (item instanceof SimpleNumber number) {
                item = number.getAsNumber();
            } else if (item instanceof SimpleDate date) {
                item = date.getAsDate();
            }

            if (item instanceof String string) {
                result.add(TemplatingUtil.resolveVariables(string, messages));
            } else {
                result.add(item);
            }
        }
        return result;
    }
}
