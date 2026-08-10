package org.keycloak.models;

import java.util.HashMap;
import java.util.Map;

/**
 * 身份提供方查询构建器：按类型、能力与附加选项筛选 IdP。
 */
public class IdentityProviderQuery {

    IdentityProviderType type;
    IdentityProviderCapability capability;
    Map<String, String> options;

    /** @return 匹配任意类型 IdP 的查询 */
    public static IdentityProviderQuery any() {
        IdentityProviderQuery query = new IdentityProviderQuery();
        query.type = IdentityProviderType.ANY;
        return query;
    }

    /** @return 用户认证类 IdP 查询（默认联邦登录场景） */
    public static IdentityProviderQuery userAuthentication() {
        IdentityProviderQuery query = new IdentityProviderQuery();
        query.type = IdentityProviderType.USER_AUTHENTICATION;
        return query;
    }

    /** @param type IdP 类型
     * @return 按类型筛选的查询 */
    public static IdentityProviderQuery type(IdentityProviderType type) {
        IdentityProviderQuery query = new IdentityProviderQuery();
        query.type = type;
        return query;
    }

    /** @param capability IdP 能力
     * @return 按能力筛选的查询 */
    public static IdentityProviderQuery capability(IdentityProviderCapability capability) {
        IdentityProviderQuery query = new IdentityProviderQuery();
        query.capability = capability;
        return query;
    }

    /** @param key 搜索选项键
     * @param value 搜索选项值
     * @return 当前查询（链式） */
    public IdentityProviderQuery with(String key, String value) {
        if (this.options == null) {
            this.options = new HashMap<>();
        }
        this.options.put(key, value);
        return this;
    }

    /** @param options 搜索选项映射
     * @return 当前查询（链式） */
    public IdentityProviderQuery with(Map<String, String> options) {
        if (this.options == null) {
            this.options = new HashMap<>(options);
        } else {
            this.options.putAll(options);
        }
        return this;
    }

    /** @return IdP 类型筛选条件 */
    public IdentityProviderType getType() {
        return type;
    }

    /** @return IdP 能力筛选条件 */
    public IdentityProviderCapability getCapability() {
        return capability;
    }

    /** @return 附加搜索选项 */
    public Map<String, String> getOptions() {
        return options;
    }

}
