package com.taobao.arthas.core.security;

import java.security.Principal;

/**
 * Bearer Token 认证对应的 {@link Principal} 实现。
 * <p>
 * {@link #getName()} 固定返回 {@code bearer}；实际令牌通过 {@link #getToken()} 获取，
 * 由 {@link SecurityAuthenticatorImpl} 与配置的 password 字段比对。
 */
public final class BearerPrincipal implements Principal {

    private final String token;

    /** @param token Authorization: Bearer 头中的令牌字符串 */
    public BearerPrincipal(String token) {
        this.token = token;
    }

    @Override
    public String getName() {
        return "bearer";
    }

    /** @return Bearer 令牌原文 */
    public String getToken() {
        return token;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((token == null) ? 0 : token.hashCode());
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
        BearerPrincipal other = (BearerPrincipal) obj;
        if (token == null) {
            if (other.token != null)
                return false;
        } else if (!token.equals(other.token))
            return false;
        return true;
    }

    @Override
    public String toString() {
        // 安全原因：toString 不输出令牌内容
        return "BearerPrincipal[***]";
    }
}
