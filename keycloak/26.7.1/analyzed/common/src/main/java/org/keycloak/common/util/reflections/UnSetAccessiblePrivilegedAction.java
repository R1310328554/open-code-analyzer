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

package org.keycloak.common.util.reflections;

import java.lang.reflect.AccessibleObject;
import java.security.PrivilegedAction;

/**
 * 在特权上下文中调用 {@link AccessibleObject#setAccessible(boolean)} 以撤销可访问性的 {@link PrivilegedAction}。
 *
 * @deprecated for removal in Keycloak 27
 */
@Deprecated
public class UnSetAccessiblePrivilegedAction implements PrivilegedAction<Void> {

    /** 待撤销可访问性的成员对象。 */
    private final AccessibleObject member;

    public UnSetAccessiblePrivilegedAction(AccessibleObject member) {
        this.member = member;
    }

    /** 若成员当前可访问则将其设为不可访问，并返回 null。 */
    public Void run() {
        if (member.isAccessible()) {
            member.setAccessible(false);
        }
        return null;
    }

}
