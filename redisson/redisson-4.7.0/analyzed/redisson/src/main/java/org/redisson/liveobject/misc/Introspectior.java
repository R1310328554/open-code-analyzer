/**
 * Copyright (c) 2013-2026 Nikita Koksharov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.redisson.liveobject.misc;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import net.bytebuddy.description.field.FieldDescription;
import net.bytebuddy.description.field.FieldList;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatchers;
import org.redisson.api.annotation.RId;
import org.redisson.cache.LRUCacheMap;

/**
 * Live Object 实体 introspection：ByteBuddy 字段描述与 {@link RId} 字段名解析。
 * <p>
 * 类名拼写保留历史 typo（Introspectior）。
 *
 * @author Rui Gu (https://github.com/jackygurui)
 */
public class Introspectior {

    /** 将已加载 Class 包装为 ByteBuddy {@link TypeDescription}。 */
    public static TypeDescription.ForLoadedType getTypeDescription(Class<?> c) {
        return new TypeDescription.ForLoadedType(c);
    }

    /** 返回类及其父类上带指定注解的字段列表。 */
    public static FieldList<FieldDescription.InDefinedShape> getFieldsWithAnnotation(Class<?> c, Class<? extends Annotation> a) {
        return getAllFields(c)
                .filter(ElementMatchers.isAnnotatedWith(a));
    }

    /** 收集类层次中所有 declared 字段。 */
    public static FieldList<FieldDescription.InDefinedShape> getAllFields(Class<?> cls) {
        List<Field> fields = new ArrayList<Field>();
        for (Class<?> c = cls; c != null; c = c.getSuperclass()) {
            Collections.addAll(fields, c.getDeclaredFields());
        }
        return new FieldList.ForLoadedFields(fields);
    }


    /** 实体类 → {@link RId} 字段名的 LRU 缓存。 */
    private static final Map<Class<?>, String> ID_FIELD_NAME_CACHE = new LRUCacheMap<>(500, 0, 0);

    /** 解析并缓存 {@code @REntity} 上 {@link RId} 标注的字段名（必须唯一）。 */
    public static String getREntityIdFieldName(Class<?> cls) {
        String name = ID_FIELD_NAME_CACHE.get(cls);
        if (name == null) {
            name = Introspectior
                    .getFieldsWithAnnotation(cls, RId.class)
                    .getOnly()
                    .getName();
            ID_FIELD_NAME_CACHE.put(cls, name);
        }
        return name;
    }

}
