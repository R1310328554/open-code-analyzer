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

package org.keycloak.federation.sssd.api;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.keycloak.models.UserModel;

import org.freedesktop.dbus.connections.impl.DBusConnection;
import org.freedesktop.dbus.types.DBusListType;
import org.freedesktop.dbus.types.Variant;
import org.freedesktop.sssd.infopipe.InfoPipe;
import org.jboss.logging.Logger;

/**
 * 通过 SSSD InfoPipe D-Bus 接口查询 Unix 用户属性与组 membership 的客户端。
 *
 * @author <a href="mailto:bruno@abstractj.org">Bruno Oliveira</a>
 * @version $Revision: 1 $
 */
public class Sssd {

    private final DBusConnection dBusConnection;
    private final String username;
    private static final Logger logger = Logger.getLogger(Sssd.class);

    /**
     * 创建 SSSD 客户端。
     *
     * @param username 要查询的 Unix 用户名
     * @param dbusConnection 系统 D-Bus 连接
     * @throws SSSDException 连接或初始化失败时抛出
     */
    public Sssd(String username, DBusConnection dbusConnection) throws SSSDException {
        this.username = username;
        this.dBusConnection = dbusConnection;
    }

    /**
     * 从 D-Bus {@link Variant} 中提取首个字符串属性值。
     *
     * @param variant D-Bus 变体（通常为字符串列表）
     * @return 首个元素字符串，无法解析时返回 null
     */
    public static String getRawAttribute(Variant variant) {
        if (variant != null && variant.getType() instanceof DBusListType) {
            List<?> value = (List) variant.getValue();
            if (!value.isEmpty()) {
                return value.iterator().next().toString();
            }
        }
        return null;
    }

    /**
     * 查询用户所属组列表。
     *
     * @return 组名列表
     * @throws SSSDException SSSD 服务不可用或 D-Bus 调用失败时抛出
     */
    public List<String> getGroups() {
        List<String> userGroups;
        try {
            InfoPipe infoPipe = dBusConnection.getRemoteObject(InfoPipe.BUSNAME, InfoPipe.OBJECTPATH, InfoPipe.class);
            userGroups = infoPipe.getUserGroups(username);
        } catch (Exception e) {
            throw new SSSDException("Failed to retrieve user's groups from SSSD. Check if SSSD service is active.", e);
        }
        return userGroups;
    }

    /**
     * 从 SSSD 拉取用户邮件、姓名等属性。
     *
     * @return 用户属性对象，查询失败时返回 null
     */
    public User getUser() {
        String[] attr = {"mail", "givenname", "sn", "telephoneNumber"};
        User user = null;
        try {
            InfoPipe infoPipe = dBusConnection.getRemoteObject(InfoPipe.BUSNAME, InfoPipe.OBJECTPATH, InfoPipe.class);
            user = new User(infoPipe.getUserAttributes(username, Arrays.asList(attr)));
        } catch (Exception e) {
            logger.debugf(e, "Failed to retrieve attributes for user '%s'. Check if SSSD service is active.", username);
        }
        return user;
    }

    /** SSSD 返回的用户属性快照，用于与 {@link UserModel} 比对 */
    public class User {

        private final String email;
        private final String firstName;
        private final String lastName;

        /**
         * 从 InfoPipe 返回的属性映射构造用户对象。
         *
         * @param userAttributes 属性名到 D-Bus Variant 的映射
         */
        public User(Map<String, Variant> userAttributes) {
            this.email = getRawAttribute(userAttributes.get("mail"));
            this.firstName = getRawAttribute(userAttributes.get("givenname"));
            this.lastName = getRawAttribute(userAttributes.get("sn"));

        }

        /** 邮箱地址 */
        public String getEmail() {
            return email;
        }

        /** 名 */
        public String getFirstName() {
            return firstName;
        }

        /** 姓 */
        public String getLastName() {
            return lastName;
        }

        /**
         * 与 {@link UserModel} 比较姓名与邮箱是否一致（用于联邦用户校验）。
         */
        @Override
        public boolean equals(Object o) {
            if (o == null) return false;

            UserModel userModel = (UserModel) o;
            if (firstName != null && !firstName.equals(userModel.getFirstName())) {
                return false;
            }
            if (lastName != null && !lastName.equals(userModel.getLastName())) {
                return false;
            }
            if (email != null) {
                return email.equalsIgnoreCase(userModel.getEmail());
            }
            return userModel.getEmail() == null;
        }

        @Override
        public int hashCode() {
            int result = email != null ? email.hashCode() : 0;
            result = 31 * result + (firstName != null ? firstName.hashCode() : 0);
            result = 31 * result + (lastName != null ? lastName.hashCode() : 0);
            return result;
        }
    }
}
