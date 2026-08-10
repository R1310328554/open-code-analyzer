package org.keycloak.device;

import org.keycloak.provider.Provider;
import org.keycloak.representations.account.DeviceRepresentation;

/**
 * 设备表示 SPI：从当前 HTTP 请求（如 User-Agent）解析 {@link DeviceRepresentation}。
 */
public interface DeviceRepresentationProvider extends Provider {

    /** @return 当前请求对应的设备描述，无法解析时可为 {@code null} */
    DeviceRepresentation deviceRepresentation();

    /** 默认空实现。 */
    @Override
    default void close() {
    }
}
