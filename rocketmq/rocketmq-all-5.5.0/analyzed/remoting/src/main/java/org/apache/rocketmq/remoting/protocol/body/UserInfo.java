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
package org.apache.rocketmq.remoting.protocol.body;

/**
 * ACL 用户信息：用户名、密码、类型及状态，用于权限认证与账户管理。
 */
public class UserInfo {

    /** 用户名。 */
    private String username;

    /** 密码（传输或存储时通常已加密）。 */
    private String password;

    /** 用户类型（如 SUPER、NORMAL）。 */
    private String userType;

    /** 账户状态（启用/禁用等）。 */
    private String userStatus;

    /** 三字段工厂方法。 */
    public static UserInfo of(String username, String password, String userType) {
        UserInfo userInfo = new UserInfo();
        userInfo.setUsername(username);
        userInfo.setPassword(password);
        userInfo.setUserType(userType);
        return userInfo;
    }

    /** 四字段工厂方法，含账户状态。 */
    public static UserInfo of(String username, String password, String userType, String userStatus) {
        UserInfo userInfo = new UserInfo();
        userInfo.setUsername(username);
        userInfo.setPassword(password);
        userInfo.setUserType(userType);
        userInfo.setUserStatus(userStatus);
        return userInfo;
    }

    /** 返回用户名。 */
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    /** 返回用户类型。 */
    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }

    public String getUserStatus() {
        return userStatus;
    }

    public void setUserStatus(String userStatus) {
        this.userStatus = userStatus;
    }
}
