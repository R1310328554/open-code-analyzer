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

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * 按属性类型（含子类型/超类型选项）匹配的 {@link PropertyCriteria} 实现。
 *
 * @see PropertyCriteria
 */
public class TypedPropertyCriteria implements PropertyCriteria {

    /** 类型匹配选项：精确相等；SUB_TYPE 含子类型；SUPER_TYPE 含超类型；ALL 二者皆可。 */
    public enum MatchOption {
        SUB_TYPE, SUPER_TYPE, ALL
    }

    private final Class<?> propertyClass;
    private final MatchOption matchOption;

    /** 仅匹配与 propertyClass 完全相等的类型。 */
    public TypedPropertyCriteria(Class<?> propertyClass) {
        this(propertyClass, null);
    }

    /** @param propertyClass 目标类型；@param matchOption 子/超类型匹配策略 */
    public TypedPropertyCriteria(Class<?> propertyClass, MatchOption matchOption) {
        if (propertyClass == null) {
            throw new IllegalArgumentException("Property class can not be null.");
        }
        this.propertyClass = propertyClass;
        this.matchOption = matchOption;
    }

    public boolean fieldMatches(Field f) {
        return match(f.getType());
    }

    public boolean methodMatches(Method m) {
        return match(m.getReturnType());
    }

    private boolean match(Class<?> type) {
        if (propertyClass.equals(type)) {
            return true;
        } else {
            boolean matchSubType = propertyClass.isAssignableFrom(type);

            if (MatchOption.SUB_TYPE == this.matchOption) {
                return matchSubType;
            }

            boolean matchSuperType = type.isAssignableFrom(propertyClass);

            if (MatchOption.SUPER_TYPE == this.matchOption) {
                return matchSuperType;
            }

            if (MatchOption.ALL == this.matchOption) {
                return matchSubType || matchSuperType;
            }
        }

        return false;
    }
}
