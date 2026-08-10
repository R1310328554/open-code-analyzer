package org.keycloak.services.resource;

import org.keycloak.provider.Provider;


/**
 * 账户资源提供者：为 Account 端点创建 JAX-RS 资源实例。
 * <p>A {@link AccountResourceProvider} creates JAX-RS resource instances for the Account endpoints, allowing
 * an implementor to override the behavior of the entire Account console.</p>
 */
public interface AccountResourceProvider extends Provider {
  /** @return JAX-RS 资源实例 */
  /** Returns a JAX-RS resource instance. */
  Object getResource();
}
