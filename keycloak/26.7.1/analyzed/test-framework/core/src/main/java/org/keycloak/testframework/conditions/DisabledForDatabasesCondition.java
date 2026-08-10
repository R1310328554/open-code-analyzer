package org.keycloak.testframework.conditions;

import java.lang.annotation.Annotation;

import org.keycloak.testframework.database.TestDatabase;

/** {@link DisabledForDatabases} 的执行条件实现，针对 {@link org.keycloak.testframework.database.TestDatabase}。 */
class DisabledForDatabasesCondition extends AbstractDisabledForSupplierCondition {

    /** @return {@link org.keycloak.testframework.database.TestDatabase} 类型 */
    @Override
    Class<?> valueType() {
        return TestDatabase.class;
    }

    /** @return {@link DisabledForDatabases} 注解类型 */
    Class<? extends Annotation> annotation() {
        return DisabledForDatabases.class;
    }

}
