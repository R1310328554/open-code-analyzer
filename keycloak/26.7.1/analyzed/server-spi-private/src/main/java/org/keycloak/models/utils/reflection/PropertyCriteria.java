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
 * <p>用于 {@link PropertyQuery} 过滤匹配属性的条件接口。</p> <p/>
 * <p>内置实现包括 {@link TypedPropertyCriteria}、{@link NamedPropertyCriteria}、 {@link AnnotatedPropertyCriteria}，也可自定义实现。</p>
 *
 * @see PropertyQuery#addCriteria(PropertyCriteria)
 * @see PropertyQueries
 * @see TypedPropertyCriteria
 * @see AnnotatedPropertyCriteria
 * @see NamedPropertyCriteria
 */
public interface PropertyCriteria {

    /** 判断 getter 方法是否满足此条件。 */
    /**
     * Tests whether the specified method matches the criteria
     *
     * @param m
     *
     * @return true if the method matches
     */
    boolean methodMatches(Method m);
}
