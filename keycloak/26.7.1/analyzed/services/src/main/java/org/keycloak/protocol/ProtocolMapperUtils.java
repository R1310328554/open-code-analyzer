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

package org.keycloak.protocol;

import java.lang.reflect.Method;
import java.util.AbstractMap;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.keycloak.models.ClientModel;
import org.keycloak.models.ClientSessionContext;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.models.ProtocolMapperModel;
import org.keycloak.models.UserModel;
import org.keycloak.protocol.oidc.OIDCAdvancedConfigWrapper;
import org.keycloak.protocol.oidc.OIDCLoginProtocol;
import org.keycloak.protocol.oidc.OIDCLoginProtocolFactory;
import org.keycloak.services.util.DPoPUtil;
import org.keycloak.services.util.MtlsHoKTokenUtil;

/**
 * 协议映射器（Protocol Mapper）通用工具类：提供配置键常量、执行优先级及映射器排序/查询辅助方法。
 * <p>用于 OIDC/SAML/Docker 等登录协议在令牌或断言生成阶段按优先级调用已注册的 {@link ProtocolMapper}。</p>
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class ProtocolMapperUtils {

    /** 协议映射器配置：将用户角色写入声明/属性的键名。 */
    public static final String USER_ROLE = "user.role";
    /** 协议映射器配置：用户自定义属性名键。 */
    public static final String USER_ATTRIBUTE = "user.attribute";
    /** 协议映射器配置：用户会话备注键名。 */
    public static final String USER_SESSION_NOTE = "user.session.note";
    /** 协议映射器配置：属性是否多值。 */
    public static final String MULTIVALUED = "multivalued";
    /** 协议映射器配置：是否聚合多个属性值。 */
    public static final String AGGREGATE_ATTRS = "aggregate.attrs";
    public static final String USER_MODEL_PROPERTY_LABEL = "usermodel.prop.label";
    public static final String USER_MODEL_PROPERTY_HELP_TEXT = "usermodel.prop.tooltip";
    public static final String USER_MODEL_ATTRIBUTE_LABEL = "usermodel.attr.label";
    public static final String USER_MODEL_ATTRIBUTE_HELP_TEXT = "usermodel.attr.tooltip";

    public static final String USER_MODEL_CLIENT_ROLE_MAPPING_CLIENT_ID = "usermodel.clientRoleMapping.clientId";
    public static final String USER_MODEL_CLIENT_ROLE_MAPPING_CLIENT_ID_LABEL = "usermodel.clientRoleMapping.clientId.label";
    public static final String USER_MODEL_CLIENT_ROLE_MAPPING_CLIENT_ID_HELP_TEXT = "usermodel.clientRoleMapping.clientId.tooltip";

    public static final String USER_MODEL_CLIENT_ROLE_MAPPING_ROLE_PREFIX = "usermodel.clientRoleMapping.rolePrefix";
    public static final String USER_MODEL_CLIENT_ROLE_MAPPING_ROLE_PREFIX_LABEL = "usermodel.clientRoleMapping.rolePrefix.label";
    public static final String USER_MODEL_CLIENT_ROLE_MAPPING_ROLE_PREFIX_HELP_TEXT = "usermodel.clientRoleMapping.rolePrefix.tooltip";

    public static final String USER_MODEL_REALM_ROLE_MAPPING_ROLE_PREFIX = "usermodel.realmRoleMapping.rolePrefix";
    public static final String USER_MODEL_REALM_ROLE_MAPPING_ROLE_PREFIX_LABEL = "usermodel.realmRoleMapping.rolePrefix.label";
    public static final String USER_MODEL_REALM_ROLE_MAPPING_ROLE_PREFIX_HELP_TEXT = "usermodel.realmRoleMapping.rolePrefix.tooltip";

    public static final String USER_SESSION_MODEL_NOTE_LABEL = "userSession.modelNote.label";
    public static final String USER_SESSION_MODEL_NOTE_HELP_TEXT = "userSession.modelNote.tooltip";
    public static final String MULTIVALUED_LABEL = "multivalued.label";
    public static final String AGGREGATE_ATTRS_LABEL = "aggregate.attrs.label";
    public static final String MULTIVALUED_HELP_TEXT = "multivalued.tooltip";
    public static final String AGGREGATE_ATTRS_HELP_TEXT = "aggregate.attrs.tooltip";

    // SubMapper 优先级：应最先执行，以便后续映射器可覆盖 `sub` 声明
    public static final int SUB_MAPPER = -10;

    // 角色名映射器可在令牌中调整角色顺序
    public static final int PRIORITY_ROLE_NAMES_MAPPER = 10;

    // 硬编码角色映射器用于追加固定角色
    public static final int PRIORITY_HARDCODED_ROLE_MAPPER = 20;

    // 受众（audience）应在所有角色设置完成后解析
    public static final int PRIORITY_AUDIENCE_RESOLVE_MAPPER = 30;

    // 最后将角色写入令牌
    public static final int PRIORITY_ROLE_MAPPER = 40;

    // 脚本映射器最后执行，以便访问令牌中已写入的角色
    public static final int PRIORITY_SCRIPT_MAPPER = 50;

    private static final HashMap<String, Method> ACCESSORS = new HashMap<>();

    // 缓存 UserModel 已知 getter/is 方法，避免运行时重复反射查找与异常开销
    static {
        for (Method method : UserModel.class.getMethods()) {
            String propertyName;
            if (method.getName().startsWith("is") && method.getParameterCount() == 0) {
                propertyName = method.getName().substring(2);
            } else if (method.getName().startsWith("get") && method.getParameterCount() == 0) {
                propertyName = method.getName().substring(3);
            } else {
                continue;
            }
            ACCESSORS.put(getLowerCasedProperty(propertyName), method);
        }
    }

    /**
     * 通过反射读取 {@link UserModel} 属性值（支持 get/is 前缀）。
     * <p>为兼容旧配置，属性名首字母大小写均可。</p>
     * @param user 用户模型
     * @param propertyName 属性名
     * @return 字符串形式的属性值，不存在或调用失败时返回 {@code null}
     */
    public static String getUserModelValue(UserModel user, String propertyName) {
        // 兼容旧版配置：属性名首字母大小写均可接受
        Method m = ACCESSORS.get(getLowerCasedProperty(propertyName));
        if (m == null) {
            return null;
        }

        try {
            Object val = m.invoke(user);
            if (val != null) return val.toString();
        } catch (Exception ignore) {
        }

        return null;
    }

    private static String getLowerCasedProperty(String propertyName) {
        return Character.toLowerCase(propertyName.charAt(0)) + propertyName.substring(1);
    }

    /**
     * 查找内置 locale 协议映射器。
     * @param session Keycloak 会话
     * @return OIDC 协议的内置 locale 映射器，未找到时返回 {@code null}
     */
    public static ProtocolMapperModel findLocaleMapper(KeycloakSession session) {
        return session.getKeycloakSessionFactory().getProviderFactoriesStream(LoginProtocol.class)
                .map(LoginProtocolFactory.class::cast)
                .map(factory -> factory.getBuiltinMappers().get(OIDCLoginProtocolFactory.LOCALE))
                .filter(Objects::nonNull)
                .filter(protocolMapper -> Objects.equals(protocolMapper.getProtocol(), OIDCLoginProtocol.LOGIN_PROTOCOL))
                .findFirst()
                .orElse(null);
    }


    /**
     * 返回按优先级排序的协议映射器流（无额外过滤）。
     * @param session Keycloak 会话
     * @param ctx 客户端会话上下文
     * @return 映射器模型与实现实例的有序流
     */
    public static Stream<Entry<ProtocolMapperModel, ProtocolMapper>> getSortedProtocolMappers(KeycloakSession session, ClientSessionContext ctx) {
        return getSortedProtocolMappers(session, ctx, entry -> true);
    }

    /**
     * 返回按优先级排序且满足过滤条件的协议映射器流。
     * <p>OIDC 客户端还会附加 DPoP 与 mTLS HoK 等临时映射器。</p>
     * @param session Keycloak 会话
     * @param ctx 客户端会话上下文
     * @param filter 映射器条目过滤器
     * @return 排序后的映射器流
     */
    public static Stream<Entry<ProtocolMapperModel, ProtocolMapper>> getSortedProtocolMappers(KeycloakSession session, ClientSessionContext ctx, Predicate<Entry<ProtocolMapperModel, ProtocolMapper>> filter) {
        KeycloakSessionFactory sessionFactory = session.getKeycloakSessionFactory();

        Stream<Entry<ProtocolMapperModel, ProtocolMapper>> protocolMapperStream = //
                ctx.getProtocolMappersStream()
                        .<Entry<ProtocolMapperModel, ProtocolMapper>>map(mapperModel -> {
                            ProtocolMapper mapper = (ProtocolMapper) sessionFactory.getProviderFactory(ProtocolMapper.class, mapperModel.getProtocolMapper());
                            if (mapper == null) {
                                return null;
                            }
                            return new AbstractMap.SimpleEntry<>(mapperModel, mapper);
                        })
                        .filter(Objects::nonNull)
                        .filter(filter);

        ClientModel client = ctx.getClientSession().getClient();
        if (OIDCLoginProtocol.LOGIN_PROTOCOL.equals(client.getProtocol())) {
            protocolMapperStream = Stream.concat(protocolMapperStream, DPoPUtil.getTransientProtocolMapper());

            if (OIDCAdvancedConfigWrapper.fromClientModel(client).isUseMtlsHokToken()) {
                protocolMapperStream = Stream.concat(protocolMapperStream, MtlsHoKTokenUtil.getTransientProtocolMapper());
            }
        }

        return protocolMapperStream.sorted(Comparator.comparing(ProtocolMapperUtils::compare));
    }

    /** 按 {@link ProtocolMapper#getPriority()} 比较映射器执行顺序。 */
    public static int compare(Entry<ProtocolMapperModel, ProtocolMapper> entry) {
        int priority = entry.getValue().getPriority();
        return priority;
    }

    /** 判断映射器对应的 {@link ProtocolMapper} 提供方是否已注册可用。 */
    public static boolean isEnabled(KeycloakSession session, ProtocolMapperModel mapper) {
        return session.getKeycloakSessionFactory().getProviderFactory(ProtocolMapper.class, mapper.getProtocolMapper()) != null;
    }
}
