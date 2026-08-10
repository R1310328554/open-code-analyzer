/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates
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

import java.util.HashMap;
import java.util.Map;

/**
 * <p>提供获取 {@code KeycloakSession} 的线程上下文机制（已弃用）。</p>
 *
 * @author <a href="mailto:psilva@redhat.com">Pedro Igor</a>
 *
 * @deprecated use org.keycloak.util.KeycloakSessionUtil instead
 */
@Deprecated
public final class Resteasy {

    /** 当前线程的上下文数据映射。 */
    private static final ThreadLocal<Map<Class<?>, Object>> contextualData = new ThreadLocal<Map<Class<?>, Object>>() {
        @Override
        protected Map<Class<?>, Object> initialValue() {
            return new HashMap<>(1);
        };
    };

    /**
     * 将 {@code instance} 以类型/键 {@code type} 压入当前线程上下文。
     * <br>不应直接调用
     *
     * @param type the type/key to associate the {@code instance} with
     * @param instance the instance
     */
    public static <R> R pushContext(Class<R> type, R instance) {
        return (R) contextualData.get().put(type, instance);
    }

    /**
     * 清除当前线程关联的上下文。
     * <br>不应直接调用
     */
    public static void clearContextData() {
        contextualData.remove();
    }

    /**
     * 从当前线程上下文查找与类型/键 {@code type} 关联的实例。
     * <br> 仅应用于获取 KeycloakSession
     *
     * @param type the type/key to lookup
     * @return the instance associated with the given {@code type} or null if non-existent.
     */
    public static <R> R getContextData(Class<R> type) {
        return (R) contextualData.get().get(type);
    }

    /**
     * 将 {@code instance} 以类型/键 {@code type} 压入 Resteasy 全局上下文（已弃用，等同 {@link #pushContext}）。
     *
     * @param type the type/key to associate the {@code instance} with
     * @param instance the instance
     */
    @Deprecated
    public static void pushDefaultContextObject(Class type, Object instance) {
        pushContext(type, instance);
    }

}
