package org.keycloak.testframework.realm;

/**
 * {@link UserConfig} 的默认空实现，不修改构建器中的任何选项。
 */
public class DefaultUserConfig implements UserConfig {

    /** {@inheritDoc} 原样返回传入的构建器。 */
    @Override
    public UserBuilder configure(UserBuilder user) {
        return user;
    }

}
