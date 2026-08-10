package org.keycloak.ssf.transmitter.stream;

/**
 * 流创建后的自动验证配置。
 *
 * @param autoVerifyStream 为 {@code true} 时，发送方在流创建后 shortly 派发验证 SET；
 *                         为 {@code false} 时由接收方通过 {@code /verify} 按需触发
 * @param verificationDelayMillis 自动验证 SET 的延迟毫秒数，仅 {@code autoVerifyStream} 为 true 时生效
 */
public record StreamVerificationConfig(
        boolean autoVerifyStream,
        int verificationDelayMillis) {
}
