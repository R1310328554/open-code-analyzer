package org.keycloak.testframework.realm;

/**
 * {@link RealmConfig} 的默认空实现，不修改构建器中的任何选项。
 */
public class DefaultRealmConfig implements RealmConfig {

    /** {@inheritDoc} 原样返回传入的构建器。 */
    @Override
    public RealmBuilder configure(RealmBuilder realm) {
        return realm;
    }

}
