/*
 * Copyright 2021 Red Hat, Inc. and/or its affiliates
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

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import org.keycloak.representations.idm.UserProfileMetadata;
import org.keycloak.representations.userprofile.config.UPConfig;

/**
 * 用户配置文件（User Profile）的管理 REST 资源。
 * <p>
 * 用于读取和更新领域内用户属性定义、校验规则及元数据。
 *
 * @author Vlastimil Elias <velias@redhat.com>
 */
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public interface UserProfileResource {

    /**
     * 获取用户配置文件配置。
     *
     * @return 用户配置文件配置
     */
    @GET
    @Consumes(MediaType.APPLICATION_JSON)
    UPConfig getConfiguration();

    /** 获取用户配置文件的元数据（字段定义、校验规则等）。 */
    @GET
    @Path("/metadata")
    @Consumes(MediaType.APPLICATION_JSON)
    UserProfileMetadata getMetadata();

    /**
     * 更新用户配置文件配置。传入 null 可能表示将配置重置为默认值
     * （具体行为取决于服务端实现）。
     *
     * @param config 新的配置文件配置；可为 null 以恢复默认配置
     */
    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    void update(UPConfig config);
}
