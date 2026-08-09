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
package org.redisson.misc;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.redisson.api.RedissonClient;
import org.redisson.api.annotation.RInject;

/**
 * 通过反射向任务对象注入依赖字段。
 * <p>
 * 扫描类层次上标注 {@link RInject} 且类型可赋值的字段并赋值；
 * 常用于 MapReduce 任务、异步回调等需获取 {@link RedissonClient} 的组件。
 *
 * @author Nikita Koksharov
 *
 */
public class Injector {

    /**
     * 将 {@code value} 注入到 {@code task} 上所有匹配类型的 {@link RInject} 字段。
     * <p>
     * 沿继承链向上收集全部 declared fields。
     */
    public static <T> void inject(Object task, Class<T> valueClass, T value) {
        List<Field> allFields = new ArrayList<Field>();
        Class<?> clazz = task.getClass();
        // 沿继承链收集各层 declared fields
        while (true) {
            if (clazz != null) {
                Field[] fields = clazz.getDeclaredFields();
                allFields.addAll(Arrays.asList(fields));
            } else {
                break;
            }
            if (clazz.getSuperclass() != Object.class) {
                clazz = clazz.getSuperclass();
            } else {
                clazz = null;
            }
        }

        for (Field field : allFields) {
            if (valueClass.isAssignableFrom(field.getType())
                    && field.isAnnotationPresent(RInject.class)) {
                field.setAccessible(true);
                try {
                    field.set(task, value);
                } catch (IllegalAccessException e) {
                    throw new IllegalStateException(e);
                }
            }
        }
    }

    /** 便捷方法：向任务注入 {@link RedissonClient} 实例。 */
    public static void inject(Object task, RedissonClient redisson) {
        inject(task, RedissonClient.class, redisson);
    }
    
}
