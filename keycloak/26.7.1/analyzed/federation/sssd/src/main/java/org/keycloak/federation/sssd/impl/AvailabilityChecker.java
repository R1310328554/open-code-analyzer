/*
 * Copyright 2023 Red Hat, Inc. and/or its affiliates
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
package org.keycloak.federation.sssd.impl;

import org.freedesktop.dbus.connections.impl.DBusConnection;
import org.freedesktop.dbus.connections.impl.DBusConnectionBuilder;
import org.freedesktop.sssd.infopipe.InfoPipe;
import org.jboss.logging.Logger;

/**
 * 检测系统中 SSSD 是否可用的工具类，适用于原生 Java D-Bus 传输。
 *
 * @author rmartinc
 */
public class AvailabilityChecker {

    private static final Logger logger = Logger.getLogger(AvailabilityChecker.class);

    /**
     * 检测系统中 SSSD 是否可用。
     * <p>
     * 通过系统 D-Bus 连接 {@link InfoPipe} 并发送 ping 请求进行探测；
     * 若连接失败或 ping 无响应，则判定 SSSD 不可用，联邦提供程序将被禁用。
     *
     * @return 若 SSSD 可用则返回 {@code true}，否则返回 {@code false}
     */
    public static boolean isAvailable() {
        boolean sssdAvailable = false;
        try (DBusConnection connection = DBusConnectionBuilder.forSystemBus().build()) {
            InfoPipe infoPipe = connection.getRemoteObject(InfoPipe.BUSNAME, InfoPipe.OBJECTPATH, InfoPipe.class);

            if (infoPipe.ping("PING") == null || infoPipe.ping("PING").isEmpty()) {
                logger.debug("SSSD is not available in your system. Federation provider will be disabled.");
            } else {
                sssdAvailable = true;
            }
        } catch (Exception e) {
            logger.debug("SSSD is not available in your system. Federation provider will be disabled.", e);
        }
        return sssdAvailable;
    }
}
