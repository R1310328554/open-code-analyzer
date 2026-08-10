package org.keycloak.policy;

import org.keycloak.models.KeycloakContext;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.policy.DenylistPasswordPolicyProviderFactory.FileBasedPasswordDenylist;
import org.keycloak.policy.DenylistPasswordPolicyProviderFactory.PasswordDenylist;

/**
 * 密码黑名单策略提供者：校验密码是否出现在已配置的黑名单文件中。
 * <p>使用 {@link DenylistPasswordPolicyProviderFactory.PasswordDenylist} 进行大小写不敏感的包含检查。</p>
 *
 * @author <a href="mailto:thomas.darimont@gmail.com">Thomas Darimont</a>
 */
public class DenylistPasswordPolicyProvider implements PasswordPolicyProvider {

  public static final String ERROR_MESSAGE = "invalidPasswordBlacklistedMessage";

  private final KeycloakContext context;

  private final DenylistPasswordPolicyProviderFactory factory;

  /** @param context Keycloak 上下文
   * @param factory 黑名单策略工厂 */
  public DenylistPasswordPolicyProvider(KeycloakContext context, DenylistPasswordPolicyProviderFactory factory) {
    this.context = context;
    this.factory = factory;
  }

  /**
   * 检查密码是否命中已配置的黑名单。
   *
   * @param username
   * @param password
   * @return {@literal null} if the password is not blacklisted otherwise a {@link PolicyError}
   */
  @Override
  public PolicyError validate(String username, String password) {

    Object policyConfig = context.getRealm().getPasswordPolicy().getPolicyConfig(DenylistPasswordPolicyProviderFactory.ID);
    if (policyConfig == null) {
      return null;
    }

    if (!(policyConfig instanceof PasswordDenylist)) {
      return null;
    }

    PasswordDenylist denylist = (FileBasedPasswordDenylist) policyConfig;

    if (!denylist.contains(password.toLowerCase())) {
      return null;
    }

    return new PolicyError(ERROR_MESSAGE);
  }

  @Override
  public PolicyError validate(RealmModel realm, UserModel user, String password) {
    return validate(user.getUsername(), password);
  }

  /**
   * 解析 {@link DenylistPasswordPolicyProvider} 的策略配置。
   * 支持语法 {@¢ode passwordBlacklist(fileName)}
   *
   * Example configurations:
   * <ul>
   *     <li>{@code passwordBlacklist(test-password-blacklist.txt)}</li>
   * </ul>
   *
   * @param denylistName
   * @return
   */
  @Override
  public Object parseConfig(String denylistName) {

    if (denylistName == null) {
      return null;
    }

    return factory.resolvePasswordDenylist(denylistName);
  }

  @Override
  public void close() {
    // 无资源需释放
  }
}
