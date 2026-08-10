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

import java.util.List;
import java.util.Map;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

/**
 * 领域本地化（Localization）文本的管理 REST 资源。
 * <p>
 * 支持按语言环境查询、创建、更新与删除 UI 及消息本地化条目。
 */
public interface RealmLocalizationResource {

    /** 列出领域已配置的所有语言环境代码。 */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    List<String> getRealmSpecificLocales();

    /**
     * 获取指定语言环境的全部本地化文本。
     *
     * @param locale 语言环境代码
     * @return 键值对形式的本地化文本映射
     */
    @Path("{locale}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    Map<String, String> getRealmLocalizationTexts(final @PathParam("locale") String locale);


    /**
     * 获取指定语言环境的本地化文本（已弃用）。
     *
     * @param locale 语言环境代码
     * @param useRealmDefaultLocaleFallback 是否将领域默认语言环境的文本作为回退合并到结果中
     * @return 本地化文本映射
     * @deprecated 请改用 {@link #getRealmLocalizationTexts(String)} 获取不含回退的文本。
     *             若需回退，请多次调用端点并传入所有相关语言环境（如 {@code de-CH} 对应 {@code de}）——
     *             领域默认语言环境并非唯一回退来源。
     */
    @Deprecated
    @Path("{locale}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    Map<String, String> getRealmLocalizationTexts(final @PathParam("locale") String locale,
            @QueryParam("useRealmDefaultLocaleFallback") Boolean useRealmDefaultLocaleFallback);


    /**
     * 获取指定语言环境下单个本地化条目的文本。
     *
     * @param locale 语言环境代码
     * @param key 本地化键名
     * @return 纯文本形式的本地化内容
     */
    @Path("{locale}/{key}")
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    String getRealmLocalizationText(final @PathParam("locale") String locale, final @PathParam("key") String key);


    /** 删除指定语言环境的全部本地化文本。 */
    @Path("{locale}")
    @DELETE
    void deleteRealmLocalizationTexts(@PathParam("locale") String locale);

    /** 删除指定语言环境下的单个本地化条目。 */
    @Path("{locale}/{key}")
    @DELETE
    void deleteRealmLocalizationText(@PathParam("locale") String locale, @PathParam("key") String key);

    /** 保存或覆盖指定语言环境下单个本地化条目的文本。 */
    @Path("{locale}/{key}")
    @PUT
    @Consumes(MediaType.TEXT_PLAIN)
    void saveRealmLocalizationText(@PathParam("locale") String locale, @PathParam("key") String key, String text);

    /** 批量创建或更新指定语言环境的本地化文本。 */
    @Path("{locale}")
    @POST
    @Consumes("application/json")
    void createOrUpdateRealmLocalizationTexts(@PathParam("locale") String locale, Map<String, String> localizationTexts);
}
