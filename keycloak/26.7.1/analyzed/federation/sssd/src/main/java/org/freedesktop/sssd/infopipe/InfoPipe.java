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

package org.freedesktop.sssd.infopipe;

import java.util.List;
import java.util.Map;

import org.freedesktop.dbus.annotations.DBusInterfaceName;
import org.freedesktop.dbus.annotations.DBusMemberName;
import org.freedesktop.dbus.interfaces.DBusInterface;
import org.freedesktop.dbus.types.Variant;

/**
 * SSSD InfoPipe D-Bus 接口：通过 org.freedesktop.sssd.infopipe 服务查询用户属性与组信息。
 *
 * @author <a href="mailto:bruno@abstractj.org">Bruno Oliveira</a>.
 */
@DBusInterfaceName("org.freedesktop.sssd.infopipe")
public interface InfoPipe extends DBusInterface {

    /** InfoPipe 对象路径。 */
    String OBJECTPATH = "/org/freedesktop/sssd/infopipe";
    /** InfoPipe D-Bus 总线名。 */
    String BUSNAME = "org.freedesktop.sssd.infopipe";

    /** 按属性名列表获取用户 LDAP/SSSD 属性。 */
    @DBusMemberName("GetUserAttr")
    Map<String, Variant> getUserAttributes(String user, List<String> attr);

    /** 获取用户所属组名列表。 */
    @DBusMemberName("GetUserGroups")
    List<String> getUserGroups(String user);

    /** 健康检查 ping。 */
    @DBusMemberName("Ping")
    String ping(String ping);

}
