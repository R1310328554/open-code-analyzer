package org.keycloak.testframework.realm;

import java.util.HashMap;
import java.util.Map;

import org.keycloak.authentication.actiontoken.resetcred.ResetCredentialsActionToken;
import org.keycloak.authentication.actiontoken.verifyemail.VerifyEmailActionToken;

import static org.keycloak.models.jpa.entities.RealmAttributes.ACTION_TOKEN_GENERATED_BY_USER_LIFESPAN;

/**
 * 领域自定义属性的流式构建器，集中配置操作令牌（Action Token）生命周期等键值。
 */
public class RealmAttributesBuilder {

    private final Map<String, String> attributes;

    /** 基于已有属性映射构造构建器。 */
    private RealmAttributesBuilder(HashMap<String, String> attributes) {
        this.attributes = attributes;
    }

    /** 创建空的领域属性构建器。 */
    public static RealmAttributesBuilder create() {
        return new RealmAttributesBuilder(new HashMap<>());
    }

    /** 设置重置密码操作令牌的有效期（秒）。 */
    public RealmAttributesBuilder resetCredentialsLifespan(int lifespan) {
        attributes.put(ACTION_TOKEN_GENERATED_BY_USER_LIFESPAN + "." + ResetCredentialsActionToken.TOKEN_TYPE, String.valueOf(lifespan));
        return this;
    }

    /** 设置验证邮箱操作令牌的有效期（秒）。 */
    public RealmAttributesBuilder verifyEmailLifespan(int lifespan) {
        attributes.put(ACTION_TOKEN_GENERATED_BY_USER_LIFESPAN + "." + VerifyEmailActionToken.TOKEN_TYPE, String.valueOf(lifespan));
        return this;
    }

    /** 返回构建完成的属性映射。 */
    public Map<String, String> build() {
        return attributes;
    }

}
