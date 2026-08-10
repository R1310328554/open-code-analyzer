/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates
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

package org.keycloak.models;

/**
 * {@link UserModel} 默认方法实现：基于单值属性的名、姓、邮箱访问。
 * <p>供存储适配器继承，减少重复的属性读写逻辑。</p>
 *
 * @author <a href="mailto:external.Martin.Idel@bosch.io">Martin Idel</a>
 * @version $Revision: 1 $
 */
public abstract class UserModelDefaultMethods implements UserModel {

    /** @return 用户名字（{@link UserModel#FIRST_NAME} 属性） */
    @Override
    public String getFirstName() {
        return getFirstAttribute(FIRST_NAME);
    }

    /** @param firstName 用户名字 */
    @Override
    public void setFirstName(String firstName) {
        setSingleAttribute(FIRST_NAME, firstName);
    }

    /** @return 用户姓氏（{@link UserModel#LAST_NAME} 属性） */
    @Override
    public String getLastName() {
        return getFirstAttribute(LAST_NAME);
    }

    /** @param lastName 用户姓氏 */
    @Override
    public void setLastName(String lastName) {
        setSingleAttribute(LAST_NAME, lastName);
    }

    /** @return 用户邮箱（{@link UserModel#EMAIL} 属性） */
    @Override
    public String getEmail() {
        return getFirstAttribute(EMAIL);
    }

    /** 设置邮箱；空字符串转为 {@code null}，非空时转为小写。
     * @param email 邮箱地址 */
    @Override
    public void setEmail(String email) {
        email = email == null || email.trim().isEmpty() ? null : email.toLowerCase();
        setSingleAttribute(EMAIL, email);
    }

    /** @return 类名与 {@link #getId()} 的调试字符串 */
    @Override
    public String toString() {
        return getClass().getName() + "@" + getId();
    }
}
