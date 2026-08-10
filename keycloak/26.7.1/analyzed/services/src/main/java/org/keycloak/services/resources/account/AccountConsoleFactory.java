package org.keycloak.services.resources.account;

import java.io.IOException;

import jakarta.ws.rs.InternalServerErrorException;
import jakarta.ws.rs.NotFoundException;

import org.keycloak.Config.Scope;
import org.keycloak.models.ClientModel;
import org.keycloak.models.Constants;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.models.RealmModel;
import org.keycloak.services.resource.AccountResourceProvider;
import org.keycloak.services.resource.AccountResourceProviderFactory;
import org.keycloak.theme.Theme;

/**
 * 提供基于 {@code account} 管理客户端的 {@code default} {@link AccountConsole} 实现。
 */
public class AccountConsoleFactory implements AccountResourceProviderFactory {

  /** {@inheritDoc} 返回 {@code default} */
  @Override
  public String getId() {
    return "default";
  }

  /** {@inheritDoc} 创建 {@link AccountConsole} 实例 */
  @Override
  public AccountResourceProvider create(KeycloakSession session) {
    RealmModel realm = session.getContext().getRealm();
    ClientModel client = getAccountManagementClient(realm);
    Theme theme = getTheme(session);
    return createAccountConsole(session, client, theme);
  }

  /** 工厂方法，子类可覆盖以提供自定义控制台实现 */
  protected AccountConsole createAccountConsole(KeycloakSession session, ClientModel client, Theme theme) {
    return new AccountConsole(session, client, theme);
  }

  @Override
  public void init(Scope config) {}

  @Override
  public void postInit(KeycloakSessionFactory factory) {}

  @Override
  public void close() {}

  /** 加载账户主题，失败时抛出 500 */
  protected Theme getTheme(KeycloakSession session) {
    try {
      return session.theme().getTheme(Theme.Type.ACCOUNT);
    } catch (IOException e) {
      throw new InternalServerErrorException(e);
    }
  }

  /** 获取已启用的账户管理客户端，未配置时抛出 404 */
  protected  ClientModel getAccountManagementClient(RealmModel realm) {
    ClientModel client = realm.getClientByClientId(Constants.ACCOUNT_MANAGEMENT_CLIENT_ID);
    if (client == null || !client.isEnabled()) {
      throw new NotFoundException("account management not enabled");
    }
    return client;
  }
}
