package org.keycloak.client.registration.cli;

import picocli.CommandLine.ITypeConverter;

/**
 * Picocli 命令行参数到 {@link EndpointType} 的类型转换器。
 * <p>
 * 将 {@code -e} / {@code --endpoint} 选项的字符串值解析为注册端点枚举。
 *
 * @author <a href="mailto:mstrukel@redhat.com">Marko Strukelj</a>
 */
public class EndpointTypeConverter implements ITypeConverter<EndpointType> {

    /** {@inheritDoc} */
    @Override
    public EndpointType convert(String value) throws Exception {
        return EndpointType.of(value);
    }

}
