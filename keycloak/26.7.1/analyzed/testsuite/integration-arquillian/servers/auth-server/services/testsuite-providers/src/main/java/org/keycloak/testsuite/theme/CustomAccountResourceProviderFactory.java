package org.keycloak.testsuite.theme;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.keycloak.Config.Scope;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.services.resource.AccountResourceProvider;
import org.keycloak.services.resource.AccountResourceProviderFactory;

import org.jboss.resteasy.reactive.NoCache;

/**
 * 自定义账户控制台 {@link AccountResourceProvider} 工厂，提供简单 HTML 测试页面。
 */
public class CustomAccountResourceProviderFactory implements AccountResourceProviderFactory, AccountResourceProvider {
  /** 提供者 ID，用于主题扩展注册。 */
  public static final String ID = "ext-custom-account-console";

  /** {@inheritDoc} 返回 {@link #ID}。 */
  @Override
  public String getId() {
    return ID;
  }

  /** {@inheritDoc} 返回自身作为资源提供者。 */
  @Override
  public AccountResourceProvider create(KeycloakSession session) {
    return this;
  }

  /** {@inheritDoc} JAX-RS 资源根对象。 */
  @Override
  public Object getResource() {
    return this;
  }

  /** 返回自定义账户控制台 HTML 主页。 */
  @GET
  @NoCache
  @Produces(MediaType.TEXT_HTML)
  public Response getMainPage() {
    return Response.ok().entity("<html><head><title>Account</title></head><body><h1>Custom Account Console</h1></body></html>").build();
  }
  
  @Override
  public void init(Scope config) {}

  @Override
  public void postInit(KeycloakSessionFactory factory) {}

  @Override
  public void close() {}
}
