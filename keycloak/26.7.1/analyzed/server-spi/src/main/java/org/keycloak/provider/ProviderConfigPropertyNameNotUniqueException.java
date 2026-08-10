package org.keycloak.provider;

import org.keycloak.models.ModelException;

/**
 * Provider 配置属性名不唯一异常：同名属性已存在时抛出。
 * Exception thrown when a provider configuration property name is not unique.
 * This is used to indicate that a property with the same name already exists
 * in the configuration, which violates the uniqueness constraint.
 */
public class ProviderConfigPropertyNameNotUniqueException extends ModelException {

  /** @param message 错误消息 */
  public ProviderConfigPropertyNameNotUniqueException(String message) {
    super(message);
  }
}
