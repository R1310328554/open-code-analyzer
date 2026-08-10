package org.keycloak.testframework.injection;

/**
 * 常用 {@link Supplier#order()} 常量，控制实例部署与销毁顺序。
 * <p>
 * 数值越小越先部署；销毁时按相反顺序执行。
 */
public interface SupplierOrder {

    /** Keycloak 服务器之前的预备供应器顺序。 */
    int BEFORE_KEYCLOAK_SERVER = 100;
    /** {@link org.keycloak.testframework.server.KeycloakServer} 供应器顺序。 */
    int KEYCLOAK_SERVER = 250;
    /** Realm 创建之前的供应器顺序。 */
    int BEFORE_REALM = 500;
    /** Realm 相关供应器顺序。 */
    int REALM = 750;
    /** 未指定顺序时的默认值。 */
    int DEFAULT = 1000;

}
