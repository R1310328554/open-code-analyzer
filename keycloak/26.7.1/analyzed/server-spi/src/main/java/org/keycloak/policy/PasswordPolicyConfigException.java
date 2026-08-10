package org.keycloak.policy;

import org.keycloak.models.ModelException;

/**
 * 密码策略配置异常：策略配置值无效时抛出。
 *
 * Created by st on 23/05/17.
 */
public class PasswordPolicyConfigException extends ModelException {

    /** @param message 错误消息 */
    public PasswordPolicyConfigException(String message) {
        super(message);
    }

}
