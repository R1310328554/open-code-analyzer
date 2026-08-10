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

package org.keycloak.admin.client.resource;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import org.keycloak.representations.info.ServerInfoRepresentation;

/**
 * Keycloak 服务器信息 REST 资源。
 * <p>
 * 提供查询服务器版本、内置协议、主题、密码策略及系统配置等元数据的能力。
 *
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
@Path("/admin/serverinfo")
public interface ServerInfoResource {

    /** 获取 Keycloak 服务器的完整信息表示对象。 */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    ServerInfoRepresentation getInfo();

}
