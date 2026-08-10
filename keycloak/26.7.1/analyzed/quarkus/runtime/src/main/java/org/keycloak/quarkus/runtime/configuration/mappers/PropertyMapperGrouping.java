package org.keycloak.quarkus.runtime.configuration.mappers;

import java.util.List;

import org.keycloak.quarkus.runtime.cli.Picocli;

/**
 * 属性映射器分组接口：将相关 {@link PropertyMapper} 组织为一组，
 * 并可选地在 CLI 解析后执行分组级配置校验。
 */
public interface PropertyMapperGrouping {

    /** 返回本分组内全部属性映射器。 */
    List<? extends PropertyMapper<?>> getPropertyMappers();

    /** 分组级配置校验钩子，默认无操作。 */
    default void validateConfig(Picocli picocli) {

    }

}
