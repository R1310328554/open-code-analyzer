package com.taobao.arthas.core.security;

import java.security.Principal;

/**
 * 本地回环连接（127.0.0.1）在开启免认证时的占位 {@link Principal}。
 * <p>
 * 用户名/密码均为 null；{@link SecurityAuthenticatorImpl#login} 对此类型直接放行。
 * 
 * @author hengyunabc 2021-09-01
 */
public final class LocalConnectionPrincipal implements Principal {

    /** 无参构造，表示“本地已信任连接”身份 */
    public LocalConnectionPrincipal() {
    }

    @Override
    public String getName() {
        return null;
    }

    /** 本地免认证场景无用户名 */
    public String getUsername() {
        return null;
    }

    public String getPassword() {
        return null;
    }
}