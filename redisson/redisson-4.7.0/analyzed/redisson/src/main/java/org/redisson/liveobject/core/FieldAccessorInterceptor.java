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
package org.redisson.liveobject.core;

import java.lang.reflect.Method;
import java.util.Locale;

import org.redisson.api.RMap;
import org.redisson.api.annotation.RGetter;
import org.redisson.api.annotation.RSetter;
import org.redisson.liveobject.misc.ClassUtils;

import net.bytebuddy.implementation.bind.annotation.AllArguments;
import net.bytebuddy.implementation.bind.annotation.FieldValue;
import net.bytebuddy.implementation.bind.annotation.Origin;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import net.bytebuddy.implementation.bind.annotation.This;

/**
 * 动态字段名访问拦截器，支持 {@code get("fieldName")} / {@code set("fieldName", value)} 形式。
 * <p>
 * 通过反射调用实体上对应的 JavaBean getter/setter，
 * 配合 {@link org.redisson.api.annotation.RGetter}/{@link RSetter} 使用。
 *
 * @author Rui Gu (https://github.com/jackygurui)
 */
public class FieldAccessorInterceptor {

    /**
     * 拦截动态字段访问：args[0] 为字段名字符串，
     * getter 调用 {@code getXxx()}，setter 调用 {@code setXxx(value)}。
     */
    @RuntimeType
    public static Object intercept(
            @Origin Method method,
            @AllArguments Object[] args,
            @This Object me,
            @FieldValue("liveObjectLiveMap") RMap<?, ?> map
    ) throws Exception {
        if (args.length >= 1 && args[0] != null && String.class.isAssignableFrom(args[0].getClass())) {
            String fieldName = (String) args[0];
            String name = fieldName.substring(0, 1).toUpperCase(Locale.ENGLISH) + fieldName.substring(1);
            if (isGetter(method) && args.length == 1) {
                try {
                    return me.getClass().getMethod("get" + name).invoke(me);
                } catch (NoSuchMethodException noSuchMethodException) {
                    throw new NoSuchFieldException(fieldName);
                }
            } else if (isSetter(method) && args.length == 2) {
                Method m = ClassUtils.searchForMethod(me.getClass(), "set" + name, new Class[]{args[1].getClass()});
                if (m != null) {
                    return m.invoke(me, args[1]);
                } else {
                    throw new NoSuchFieldException(fieldName);
                }
            }
        }
        throw new NoSuchMethodException(method.getName() + " has wrong signature");

    }

    /** 方法名为 get 或标注 {@link RGetter} 时视为 getter。 */
    private static boolean isGetter(Method method) {
        if (method.isAnnotationPresent(RGetter.class)) {
            return true;
        }
        return "get".equals(method.getName());
    }

    /** 方法名为 set 或标注 {@link RSetter} 时视为 setter。 */
    private static boolean isSetter(Method method) {
        if (method.isAnnotationPresent(RSetter.class)) {
            return true;
        }
        return "set".equals(method.getName());
    }
}
