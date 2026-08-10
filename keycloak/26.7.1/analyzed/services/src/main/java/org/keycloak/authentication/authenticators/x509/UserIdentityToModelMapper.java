/*
 * Copyright 2016 Analytical Graphics, Inc. and/or its affiliates
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
 *
 */

package org.keycloak.authentication.authenticators.x509;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.models.Constants;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.ModelDuplicateException;
import org.keycloak.models.UserModel;
import org.keycloak.models.utils.KeycloakModelUtils;

/**
 * 用户身份到 {@link UserModel} 的映射器：将证书提取的身份字符串匹配到 realm 中的用户（用户名/邮箱或自定义属性）。
 * @author <a href="mailto:pnalyvayko@agi.com">Peter Nalyvayko</a>
 * @version $Revision: 1 $
 * @date 7/30/2016
 */

public abstract class UserIdentityToModelMapper {

    /**
     * 根据提取的用户身份查找 {@link UserModel}。
     * @param context 认证流程上下文
     * @param userIdentity 从证书提取的身份
     * @return 匹配的用户，未找到时返回 null
     */
    public abstract UserModel find(AuthenticationFlowContext context, Object userIdentity) throws Exception;

        /** 按用户名或邮箱查找用户。 */
        static class UsernameOrEmailMapper extends UserIdentityToModelMapper {

        @Override
        public UserModel find(AuthenticationFlowContext context, Object userIdentity) throws Exception {
            return KeycloakModelUtils.findUserByNameOrEmail(context.getSession(), context.getRealm(), userIdentity.toString().trim());
        }
    }

        /** 按一个或多个自定义用户属性查找用户。 */
        static class UserIdentityToCustomAttributeMapper extends UserIdentityToModelMapper {
        private List<String> _customAttributes;
        UserIdentityToCustomAttributeMapper(String customAttributes) {
            _customAttributes = Arrays.asList(Constants.CFG_DELIMITER_PATTERN.split(customAttributes));
        }

        @Override
        public UserModel find(AuthenticationFlowContext context, Object userIdentity) throws Exception {
            KeycloakSession session = context.getSession();
            List<String> userIdentityValues = Arrays.asList(Constants.CFG_DELIMITER_PATTERN.split(userIdentity.toString()));

            if (_customAttributes.isEmpty() || userIdentityValues.isEmpty() || (_customAttributes.size() != userIdentityValues.size())) {
                return null;
            }
            Stream<UserModel> usersStream = session.users().searchForUserByUserAttributeStream(context.getRealm(), _customAttributes.get(0), userIdentityValues.get(0));
            
            for (int i = 1; i <_customAttributes.size(); ++i) {
                String customAttribute = _customAttributes.get(i);
                String userIdentityValue = userIdentityValues.get(i);
                usersStream = usersStream.filter(user -> Objects.equals(user.getFirstAttribute(customAttribute), userIdentityValue));
            }
            List<UserModel> users = usersStream.collect(Collectors.toList());
            if (users.size() > 1) {
                throw new ModelDuplicateException();
            }
            return users.size() == 1 ? users.get(0) : null;
        }
    }

    /** @return 用户名或邮箱映射器实例 */
    public static UserIdentityToModelMapper getUsernameOrEmailMapper() {
        return new UsernameOrEmailMapper();
    }

    /** @return 指定属性名的自定义属性映射器 */
    public static UserIdentityToModelMapper getUserIdentityToCustomAttributeMapper(String attributeName) {
        return new UserIdentityToCustomAttributeMapper(attributeName);
    }
}
