/**
 * Copyright (c) 2013-2026 Nikita Koksharov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.redisson.tomcat;

import java.io.IOException;
import java.security.Principal;
import org.apache.catalina.Realm;
import org.apache.catalina.Session;
import org.apache.catalina.authenticator.SingleSignOn;
import org.apache.catalina.authenticator.SingleSignOnEntry;
import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.tomcat.util.res.StringManager;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;

/**
 * 扩展 Tomcat {@link org.apache.catalina.authenticator.SingleSignOn} Valve，
 * 以 Redis/Valkey 持久化 SSO 条目，实现无粘性 Session 的 Tomcat 集群单点登录。
 * <p>本地 {@code cache} 与 Redis Map {@code redisson:tomcat_sso} 双向同步。
 */
public class RedissonSingleSignOn extends SingleSignOn {

  private static final StringManager sm = StringManager.getManager(RedissonSingleSignOn.class);
  private static final String SSO_SESSION_ENTRIES = "redisson:tomcat_sso";

  private RedissonSessionManager manager;

  /** 注入 {@link RedissonSessionManager} 以访问 Redis Map。 */
  void setSessionManager(RedissonSessionManager manager) {
    if (containerLog != null && containerLog.isTraceEnabled()) {
        containerLog.trace(sm.getString("redissonSingleSignOn.trace.setSessionManager", manager));
    }
    this.manager = manager;
  }

  /** 请求前从 Redis 同步 SSO 条目，再委托父类 Valve 链。 */
  @Override
  public void invoke(Request request, Response response) throws IOException, ServletException {
      if (containerLog.isTraceEnabled()) {
          containerLog.trace(sm.getString("redissonSingleSignOn.trace.invoke"));
      }
      String ssoSessionId = getSsoSessionId(request);
      syncAndGetSsoEntry(ssoSessionId);
      super.invoke(request, response);
  }

  /** Session 销毁时清理 Redis 中对应 SSO 条目。 */
  @Override
  public void sessionDestroyed(String ssoId, Session session) {
      if (containerLog.isTraceEnabled()) {
          containerLog.trace(sm.getString("redissonSingleSignOn.trace.sessionDestroyed"));
      }
      super.sessionDestroyed(ssoId, session);
      manager.getMap(SSO_SESSION_ENTRIES).fastRemove(ssoId);
  }

  /** 关联 Session 与 SSO ID 后，将条目写入 Redis。 */
  @Override
  protected boolean associate(String ssoId, Session session) {
      if (containerLog.isTraceEnabled()) {
          containerLog.trace(sm.getString("redissonSingleSignOn.trace.associate", ssoId, session));
      }
      syncAndGetSsoEntry(ssoId);
      boolean associated = super.associate(ssoId, session);
      if (associated) {
          manager.getMap(SSO_SESSION_ENTRIES).fastPut(ssoId, cache.get(ssoId));
      }
      return associated;
  }

  /** 重新认证前同步 Redis 中的 SSO 状态。 */
  @Override
  protected boolean reauthenticate(String ssoId, Realm realm, Request request) {
      if (containerLog.isTraceEnabled()) {
          containerLog.trace(sm.getString("redissonSingleSignOn.trace.reauthenticate"));
      }
      syncAndGetSsoEntry(ssoId);
      return super.reauthenticate(ssoId, realm, request);
  }

  /** 注册新 SSO 条目并持久化到 Redis。 */
  @Override
  protected void register(String ssoId, Principal principal, String authType, String username, String password) {
      if (containerLog.isTraceEnabled()) {
          containerLog.trace(sm.getString("redissonSingleSignOn.trace.register"));
      }  
      super.register(ssoId, principal, authType, username, password);
      manager.getMap(SSO_SESSION_ENTRIES).fastPut(ssoId, cache.get(ssoId));
  }

  /** 注销 SSO 并从 Redis 删除条目。 */
  @Override
  protected void deregister(String ssoId) {
      if (containerLog.isTraceEnabled()) {
          containerLog.trace(sm.getString("redissonSingleSignOn.trace.deregister"));
      }
      super.deregister(ssoId);
      manager.getMap(SSO_SESSION_ENTRIES).fastRemove(ssoId);
  }

  /** 更新 SSO 凭证；成功时写回 Redis。 */
  @Override
  protected boolean update(String ssoId, Principal principal, String authType, String username, String password) {
      if (containerLog.isTraceEnabled()) {
          containerLog.trace(sm.getString("redissonSingleSignOn.trace.update"));
      }
      syncAndGetSsoEntry(ssoId);
      boolean updated = super.update(ssoId, principal, authType, username, password);
      if (updated) {
          manager.getMap(SSO_SESSION_ENTRIES).fastPut(ssoId, cache.get(ssoId));
      }
      return updated;
  }

  /** 移除 Session 关联；若无剩余 Session 则注销 SSO。 */
  @Override
  protected void removeSession(String ssoId, Session session) {
      if (containerLog.isTraceEnabled()) {
          containerLog.trace(sm.getString("redissonSingleSignOn.trace.removeSession", session, ssoId));
      }
      SingleSignOnEntry sso = this.syncAndGetSsoEntry(ssoId);
      super.removeSession(ssoId, session);
      if (sso != null && sso.findSessions().isEmpty()) {
        deregister(ssoId);
      }
  }

  /**
   * 按 SSO ID 从 Redis 查找 {@link org.apache.catalina.authenticator.SingleSignOnEntry}，
   * 并同步本地 cache（包括条目不存在时移除缓存项）。
   *
   * @param ssoSessionId 目标 SSO Session ID
   * @return 匹配的条目，未找到时返回 {@code null}
   */
  private SingleSignOnEntry syncAndGetSsoEntry(String ssoSessionId) {
      if (containerLog.isTraceEnabled()) {
          containerLog.trace(sm.getString("redissonSingleSignOn.trace.getSsoEntry", ssoSessionId));
      }
      if (ssoSessionId == null) {
          return null;
      }
      SingleSignOnEntry entry = (SingleSignOnEntry) manager.getMap(SSO_SESSION_ENTRIES).get(ssoSessionId);
      if (entry == null) {
        this.cache.remove(ssoSessionId);
      } else {
          this.cache.put(ssoSessionId, entry);
      }
      return entry;
  }

  /**
   * 从请求 Cookie 中解析 SSO Session ID。
   *
   * @param request 入站请求
   * @return Cookie 中的 SSO ID，未提供时返回 {@code null}
   */
  private String getSsoSessionId(Request request) {
      if (containerLog.isTraceEnabled()) {
          containerLog.trace(sm.getString("redissonSingleSignOn.trace.getSsoSessionId", request.getRequestURI()));
      }
      Cookie cookie = null;
      Cookie[] cookies = request.getCookies();
      if (cookies != null) {
          for (Cookie value : cookies) {
              if (getCookieName().equals(value.getName())) {
                  cookie = value;
                  break;
              }
          }
      }
      if (cookie != null) {
          return cookie.getValue();
      }
      return null;
  }

}
