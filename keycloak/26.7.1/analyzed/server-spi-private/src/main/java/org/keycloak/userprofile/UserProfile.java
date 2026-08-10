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


import org.keycloak.models.UserModel;
import org.keycloak.representations.idm.AbstractUserRepresentation;

/**
 * <p>用户 Profile 入口：在特定 {@link UserProfileContext} 下创建、更新、校验与读取用户属性。
 * <p>An interface that serves an entry point for managing users and their attributes.
 *
 * <p>A {@code UserProfile} provides methods for creating, and updating users as well as for accessing their attributes.
 * All its operations are based the {@link UserProfileContext}. By taking the context into account, the state and behavior of
 * {@link UserProfile} instances depend on the context they are associated with where creating, updating, validating, and
 * accessing the attribute set of a user is based on the configuration (see {@link org.keycloak.representations.userprofile.config.UPConfig})
 * and the constraints associated with a given context.
 *
 * <p>The {@link UserProfileContext} represents the different areas in Keycloak where users, and their attributes are managed.
 * Examples of contexts are: managing users through the Admin API, or through the Account API.
 *
 * <p>A {@code UserProfile} instance can be obtained through the {@link UserProfileProvider}:
 *
 * <pre> {@code
 * // resolve an existing user
 * UserModel user = getExistingUser();
 * // obtain the user profile provider
 * UserProfileProvider provider = session.getProvider(UserProfileProvider.class);
 * // create a instance for managing the user profile through the USER_API context
 * UserProfile profile = provider.create(USER_API, user);
 * }</pre>
 *
 * <p>The {@link UserProfileProvider} provides different methods for creating {@link UserProfile} instances, each one
 * target for a specific scenario such as creating a new user, updating an existing one, or only for accessing the attributes
 * for an existing user as shown in the above example.
 *
 * @see UserProfileContext
 * @see UserProfileProvider
 * @author <a href="mailto:markus.till@bosch.io">Markus Till</a>
 */
public interface UserProfile {

    /** 校验本实例关联的属性；失败抛出 {@link ValidationException}。 */
    /**
     * Validates the attributes associated with this instance.
     *
     * @throws ValidationException in case
     */
    void validate() throws ValidationException;

    /** 根据本 Profile 属性创建新 {@link UserModel}（默认执行校验）。 */
    /**
     * Creates a new {@link UserModel} based on the attributes associated with this instance.
     *
     * @throws ValidationException in case validation fails
     *
     * @return the {@link UserModel} instance created from this profile
     */
    UserModel create() throws ValidationException;

    /** 创建用户，{@code validate} 控制是否自动校验。 */
    /**
     * Creates a new {@link UserModel} based on the attributes associated with this instance.
     *
     * @param validate if {@code true}, execute validations on create. Otherwise, validation should be done programmatically by calling {@link #validate()}
     *
     * @throws ValidationException in case validation fails
     * @return the {@link UserModel} instance created from this profile
     */
    default UserModel create(boolean validate) throws ValidationException {
        return create();
    }

    /** 更新关联的 {@link UserModel}；无关联用户时无效果。未校验时会先 {@link #validate()}。 */
    /**
     * <p>Updates the {@link UserModel} associated with this instance. If no {@link UserModel} is associated with this instance, this operation has no effect.
     *
     * <p>Before updating the {@link UserModel}, this method first checks whether the {@link #validate()} method was previously
     * invoked. If not, the validation step is performed prior to updating the model.
     *
     * @param removeAttributes if attributes should be removed from the {@link UserModel} if they are not among the attributes associated with this instance.
     * @param changeListener a set of one or more listeners to listen for attribute changes
     * @throws ValidationException in case of any validation error
     */
    void update(boolean removeAttributes, AttributeChangeListener... changeListener) throws ValidationException;

    /** 等同 {@link #update(boolean, AttributeChangeListener...)} 且 {@code removeAttributes=true}。 */
    /**
     * <p>The same as {@link #update(boolean, AttributeChangeListener...)}} but forcing the removal of attributes.
     *
     * @param changeListener a set of one or more listeners to listen for attribute changes
     * @throws ValidationException in case of any validation error
     */
    default void update(AttributeChangeListener... changeListener) throws ValidationException {
        update(true, changeListener);
    }

    /** 返回本 Profile 的属性视图（经 UP 配置与上下文过滤，可能与 {@link UserModel} 不完全一致）。 */
    /**
     * Returns the attributes associated with this instance. Note that the attributes returned by this method are not necessarily
     * the same from the {@link UserModel} as they are based on the configurations set in the {@link org.keycloak.representations.userprofile.config.UPConfig} and
     * the context this instance is based on.
     *
     * @return the attributes associated with this instance.
     */
    Attributes getAttributes();

    /** 返回完整用户表示（含 Profile 元数据）。 */
    /**
     * Returns the full user representation
     *
     * @return the user representation
     */
    default <R extends AbstractUserRepresentation> R toRepresentation() {
        return toRepresentation(true);
    }

    /** 返回用户表示；{@code full} 控制是否包含完整 Profile 元数据。 */
    /**
     * Returns the user representation
     *
     * @param full if the full representation should be returned
     * @return the user representation
     */
    <R extends AbstractUserRepresentation> R toRepresentation(boolean full);
}
