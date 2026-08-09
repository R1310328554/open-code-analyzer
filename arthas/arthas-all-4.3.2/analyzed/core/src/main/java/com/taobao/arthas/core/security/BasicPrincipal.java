package com.taobao.arthas.core.security;

import java.security.Principal;

/**
 * HTTP Basic 认证对应的 {@link Principal} 实现，持有用户名与密码。
 * <p>
 * {@link #getName()} 返回用户名；{@link #toString()}  deliberately 不输出密码。
 * 
 * @author hengyunabc 2021-03-04
 */
public final class BasicPrincipal implements Principal {

    private final String username;
    private final String password;

    /** @param username Basic 用户名
     *  @param password Basic 密码 */
    public BasicPrincipal(String username, String password) {
        this.username = username;
        this.password = password;
    }

    @Override
    public String getName() {
        return username;
    }

    /** @return Basic 认证用户名 */
    public String getUsername() {
        return username;
    }

    /** @return Basic 认证密码（仅供 {@link SecurityAuthenticatorImpl} 校验） */
    public String getPassword() {
        return password;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((password == null) ? 0 : password.hashCode());
        result = prime * result + ((username == null) ? 0 : username.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        BasicPrincipal other = (BasicPrincipal) obj;
        if (password == null) {
            if (other.password != null)
                return false;
        } else if (!password.equals(other.password))
            return false;
        if (username == null) {
            if (other.username != null)
                return false;
        } else if (!username.equals(other.username))
            return false;
        return true;
    }

    @Override
    public String toString() {
        // 日志与调试输出中不暴露密码
        return "BasicPrincipal[" + username + "]";
    }
}