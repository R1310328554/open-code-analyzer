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

package org.keycloak.models.utils.reflection;

import java.lang.reflect.Method;

/**
 * 基于 JavaBean getter/setter 方法的 {@link Property} 扩展。
 * <p>暴露底层 {@link Method} 作为注解与反射操作的入口。</p>
 */
public interface MethodProperty<V> extends Property<V> {

    Method getAnnotatedElement();
}