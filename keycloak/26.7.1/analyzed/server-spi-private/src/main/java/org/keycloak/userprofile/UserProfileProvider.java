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

package org.keycloak.userprofile;

import java.util.Map;

import org.keycloak.models.UserModel;
import org.keycloak.provider.Provider;
import org.keycloak.representations.userprofile.config.UPConfig;

/**
 * 用户配置提供者：负责创建 {@link UserProfile} 实例。
 * <p>提供按上下文与属性创建、校验及更新用户配置的能力。</p>
 *
 * @see UserProfile
 * @author <a href="mailto:markus.till@bosch.io">Markus Till</a>
 */
public interface UserProfileProvider extends Provider {

    /**
     * 基于已有用户创建 {@link UserProfile} 实例，仅用于校验属性是否符合给定 {@code context} 与配置。
     * <p>Creates a new {@link UserProfile} instance only for validation purposes to check whether its attributes are in conformance
     * with the given {@code context} and profile configuration.</p>
     *
     * @param context the context
     * @param user an existing user
     *
     * @return the user profile instance
     */
    UserProfile create(UserProfileContext context, UserModel user);

    /**
     * 基于 {@code context} 与 {@code attributes} 创建 {@link UserProfile} 实例，用于校验。
     * <p>适用于校验与更新分步执行，或根据属性创建新用户的场景。</p>
     * <p>Instances created from this method are usually related to contexts where validation and updates are performed in different
     * steps, or when creating new users based on the given {@code attributes}.</p>
     *
     * @param context the context
     * @param attributes the attributes to associate with the instance returned from this method
     *
     * @return the user profile instance
     */
    UserProfile create(UserProfileContext context, Map<String, ?> attributes);

    /**
     * 基于 {@code context}、{@code attributes} 与 {@code user} 创建 {@link UserProfile} 实例，用于更新。
     * <p>实例将基于给定用户执行校验并应用属性更新，适用于更新已有用户。</p>
     * <p>Instances created from this method are going to run validations and updates based on the given {@code user}. This
     * might be useful when updating an existing user.</p>
     *
     * @param context the context
     * @param attributes the attributes to associate with the instance returned from this method
     * @param user the user to eventually update with the given {@code attributes}
     *
     * @return the user profile instance
     */
    UserProfile create(UserProfileContext context, Map<String, ?> attributes, UserModel user);

    /**
     * 获取当前 UserProfile 配置。
     *
     * @return current UserProfile configuration
     * @see #setConfiguration(UPConfig)
     */
    UPConfig getConfiguration();

    /**
     * 设置新的 UserProfile 配置并持久化于提供者内部。
     * <p>{@code configuration} 可为 {@code null}，此时将切换为默认配置。</p>
     * Set new UserProfile configuration. It is persisted inside of the provider.
     *
     * @param configuration to be set. It can be null and in this case, userProfile implementation will switch to use the default configuration
     * @throws RuntimeException if configuration is invalid (exact exception class
     *                          depends on the implementation) or configuration
     *                          can't be persisted.
     * @see #getConfiguration()
     */
    void setConfiguration(UPConfig configuration);
}
