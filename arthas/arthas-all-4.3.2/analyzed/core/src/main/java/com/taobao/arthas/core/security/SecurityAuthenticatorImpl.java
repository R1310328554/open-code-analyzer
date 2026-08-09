package com.taobao.arthas.core.security;

import java.security.Principal;

import javax.security.auth.Subject;
import javax.security.auth.login.LoginException;

import com.alibaba.arthas.deps.org.slf4j.Logger;
import com.alibaba.arthas.deps.org.slf4j.LoggerFactory;
import com.taobao.arthas.common.ArthasConstants;
import com.taobao.arthas.core.util.StringUtils;

/**
 * 默认 {@link SecurityAuthenticator} 实现：单用户 Basic/Bearer/本地连接校验。
 * <p>
 * 仅配置 username 时自动生成随机 password 并打印；Bearer 令牌与 password 字段等同。
 * TODO 支持不同角色不同权限，command 按角色分类。
 * 
 * @author hengyunabc 2021-03-03
 */
public class SecurityAuthenticatorImpl implements SecurityAuthenticator {
    private static final Logger logger = LoggerFactory.getLogger(SecurityAuthenticatorImpl.class);
    private String username;
    private String password;
    private Subject subject;

    /**
     * 构造认证器；仅 username 时生成 32 位随机密码。
     * @param username 登录用户名，可为 null
     * @param password 登录密码或 Bearer 共享密钥
     */
    public SecurityAuthenticatorImpl(String username, String password) {
        // 只配用户名时自动生成密码并输出到日志
        if (username != null && password == null) {
            password = StringUtils.randomString(32);
            logger.info("\nUsing generated security password: {}\n", password);
        }
        if (username == null && password != null) {
            username = ArthasConstants.DEFAULT_USERNAME;
        }

        this.username = username;
        this.password = password;

        subject = new Subject();
    }

    @Override
    public void setName(String name) {
        // TODO Auto-generated method stub

    }

    @Override
    public String getName() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setRoleClassNames(String names) {
        // TODO Auto-generated method stub

    }

    @Override
    /**
     * 校验 BasicPrincipal、BearerPrincipal 或 LocalConnectionPrincipal。
     * @return 成功返回内部共享 Subject，失败返回 null
     */
    public Subject login(Principal principal) throws LoginException {
        if (principal == null) {
            return null;
        }
        if (principal instanceof BasicPrincipal) {
            BasicPrincipal basicPrincipal = (BasicPrincipal) principal;
            if (basicPrincipal.getName().equals(username) && basicPrincipal.getPassword().equals(this.password)) {
                return subject;
            }
        }
        if (principal instanceof BearerPrincipal) {
            BearerPrincipal bearerPrincipal = (BearerPrincipal) principal;
            // Bearer：令牌与配置的 password 字符串完全匹配即通过
            if (bearerPrincipal.getToken().equals(this.password)) {
                return subject;
            }
        }
        if (principal instanceof LocalConnectionPrincipal) {
            return subject;
        }

        return null;
    }

    @Override
    public void logout(Subject subject) throws LoginException {
        // TODO Auto-generated method stub

    }

    @Override
    public String getUserRoles(Subject subject) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    /** 用户名与密码均非 null 时才强制客户端登录 */
    public boolean needLogin() {
        return username != null && password != null;
    }

}
