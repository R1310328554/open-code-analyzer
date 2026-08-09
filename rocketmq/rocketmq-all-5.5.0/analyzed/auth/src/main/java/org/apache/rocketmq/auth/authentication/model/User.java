/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.rocketmq.auth.authentication.model;

import org.apache.rocketmq.auth.authentication.enums.SubjectType;
import org.apache.rocketmq.auth.authentication.enums.UserStatus;
import org.apache.rocketmq.auth.authentication.enums.UserType;
import org.apache.rocketmq.common.constant.CommonConstants;

/**
 * 认证用户实体：实现 {@link Subject}，承载用户名、密码、类型与状态。
 */
public class User implements Subject {

    private String username;

    private String password;

    private UserType userType;

    private UserStatus userStatus;

    /** 仅指定用户名的工厂方法。 */
    public static User of(String username) {
        User user = new User();
        user.setUsername(username);
        return user;
    }

    /** 指定用户名与密码的工厂方法。 */
    public static User of(String username, String password) {
        User user = new User();
        user.setUsername(username);
        user.setPassword(password);
        return user;
    }

    /** 指定用户名、密码与用户类型的工厂方法。 */
    public static User of(String username, String password, UserType userType) {
        User user = new User();
        user.setUsername(username);
        user.setPassword(password);
        user.setUserType(userType);
        return user;
    }

    /** 返回 "User:username" 格式的主体键。 */
    @Override
    public String getSubjectKey() {
        return this.getSubjectType().getName() + CommonConstants.COLON + this.username;
    }

    /** 固定返回 {@link SubjectType#USER}。 */
    @Override
    public SubjectType getSubjectType() {
        return SubjectType.USER;
    }

    /** 返回用户名（accessKey）。 */
    public String getUsername() {
        return username;
    }

    /** 设置用户名。 */
    public void setUsername(String username) {
        this.username = username;
    }

    /** 返回密码/secretKey。 */
    public String getPassword() {
        return password;
    }

    /** 设置密码/secretKey。 */
    public void setPassword(String password) {
        this.password = password;
    }

    /** 返回用户类型。 */
    public UserType getUserType() {
        return userType;
    }

    /** 设置用户类型。 */
    public void setUserType(UserType userType) {
        this.userType = userType;
    }

    /** 返回用户状态。 */
    public UserStatus getUserStatus() {
        return userStatus;
    }

    /** 设置用户状态。 */
    public void setUserStatus(UserStatus userStatus) {
        this.userStatus = userStatus;
    }
}
