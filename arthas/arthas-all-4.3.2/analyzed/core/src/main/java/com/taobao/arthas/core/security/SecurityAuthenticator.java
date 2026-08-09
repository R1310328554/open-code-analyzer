package com.taobao.arthas.core.security;

import java.security.Principal;

import javax.security.auth.Subject;
import javax.security.auth.login.LoginException;

/**
 * Arthas 安全认证器 SPI：可插拔 JAAS 或自定义实现。
 * <p>
 * 负责 realm 名称、角色类名配置，以及 {@link Principal} 的 login/logout 与角色提取。
 */
public interface SecurityAuthenticator {
    
    
    /** @return 是否要求客户端提供凭据（用户名密码或 Bearer） */
    boolean needLogin();
    
    /**
     * 设置安全域（realm）名称。
     */
    void setName(String name);

    /**
     * @return 当前 realm 名称
     */
    String getName();

    /**
     * Sets the role class names (separated by comma)
     * <p/>
     * By default if no explicit role class names has been configured, then this
     * implementation will assume the {@link Subject}
     * {@link java.security.Principal}s is a role if the classname contains the word
     * <tt>role</tt> (lower cased).
     *
     * @param names a list of FQN class names for role
     *              {@link java.security.Principal} implementations.
     */
    void setRoleClassNames(String names);

    /**
     * 尝试将 {@link java.security.Principal} 登录到本 realm。
     * <p>
     * 成功时不抛异常并返回非 null {@link Subject}。
     * <p/>
     * The login is a success if no Exception is thrown, and a {@link Subject} is
     * returned.
     *
     * @param principal the principal
     * @return the subject for the logged in principal, must <b>not</b> be
     *         <tt>null</tt>
     * @throws LoginException is thrown if error logging in the
     *                        {@link java.security.Principal}
     */
    Subject login(Principal principal) throws LoginException;

    /**
     * 注销已登录的 {@link Subject}。
     *
     * @param subject subject to logout
     * @throws LoginException is thrown if error logging out subject
     */
    void logout(Subject subject) throws LoginException;

    /**
     * 从 {@link Subject} 提取用户角色列表。
     *
     * @param subject the subject
     * @return <tt>null</tt> if no roles, otherwise a String with roles separated by
     *         comma.
     */
    String getUserRoles(Subject subject);

}