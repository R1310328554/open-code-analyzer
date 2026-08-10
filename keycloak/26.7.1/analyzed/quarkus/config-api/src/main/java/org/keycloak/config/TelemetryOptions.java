package org.keycloak.config;

import java.util.List;

/**
 * OpenTelemetry 遥测（日志、指标、通用端点与资源属性）相关配置选项。
 */
public class TelemetryOptions {

    /** 配置选项：telemetry-endpoint，OpenTelemetry 采集端点。 */
    public static final Option<String> TELEMETRY_ENDPOINT = new OptionBuilder<>("telemetry-endpoint", String.class)
            .category(OptionCategory.TELEMETRY)
            .description("OpenTelemetry endpoint to connect to.")
            .defaultValue("http://localhost:4317")
            .build();

    /** 配置选项：telemetry-service-name，OpenTelemetry 服务名。 */
    public static final Option<String> TELEMETRY_SERVICE_NAME = new OptionBuilder<>("telemetry-service-name", String.class)
            .category(OptionCategory.TELEMETRY)
            .description("OpenTelemetry service name. Takes precedence over 'service.name' defined in the 'telemetry-resource-attributes' property.")
            .defaultValue("keycloak")
            .build();

    /** 配置选项：telemetry-protocol，与采集器通信的 OpenTelemetry 协议。 */
    public static final Option<String> TELEMETRY_PROTOCOL = new OptionBuilder<>("telemetry-protocol", String.class)
            .category(OptionCategory.TELEMETRY)
            .description("OpenTelemetry protocol used for the communication between server and OpenTelemetry collector.")
            .defaultValue("grpc")
            .expectedValues("grpc", "http/protobuf")
            .build();

    /** 配置选项：telemetry-resource-attributes，OpenTelemetry 资源属性列表。 */
    public static final Option<List<String>> TELEMETRY_RESOURCE_ATTRIBUTES = OptionBuilder.listOptionBuilder("telemetry-resource-attributes", String.class)
            .category(OptionCategory.TELEMETRY)
            .description("OpenTelemetry resource attributes characterize the telemetry producer. Values in format 'key1=val1,key2=val2'.")
            .build();

    /** 配置选项：telemetry-header-<header>，导出器请求的通用 HTTP 头。 */
    public static final Option<String> TELEMETRY_HEADER = new OptionBuilder<>("telemetry-header-<header>", String.class)
            .category(OptionCategory.TELEMETRY)
            .description("General OpenTelemetry header that will be part of the exporter request (mainly useful for providing Authorization header). Check the documentation on how to set environment variables for headers containing special characters or custom case-sensitive headers.")
            .build();

    // 遥测日志配置
    /** 配置选项：telemetry-logs-enabled，是否导出 OpenTelemetry 日志。 */
    public static final Option<Boolean> TELEMETRY_LOGS_ENABLED = new OptionBuilder<>("telemetry-logs-enabled", Boolean.class)
            .category(OptionCategory.TELEMETRY)
            .description("Enables exporting logs to a destination handling OpenTelemetry logs.")
            .defaultValue(Boolean.FALSE)
            .buildTime(true)
            .build();

    /** 配置选项：telemetry-logs-endpoint，日志导出端点。 */
    public static final Option<String> TELEMETRY_LOGS_ENDPOINT = new OptionBuilder<>("telemetry-logs-endpoint", String.class)
            .category(OptionCategory.TELEMETRY)
            .description("OpenTelemetry endpoint to export logs to. If not given, the value is inherited from the '%s' option.".formatted(TELEMETRY_ENDPOINT.getKey()))
            .build();

    /** 配置选项：telemetry-logs-protocol，日志导出协议。 */
    public static final Option<String> TELEMETRY_LOGS_PROTOCOL = new OptionBuilder<>("telemetry-logs-protocol", String.class)
            .category(OptionCategory.TELEMETRY)
            .description("OpenTelemetry protocol used for exporting logs. If not given, the value is inherited from the '%s' option.".formatted(TELEMETRY_PROTOCOL.getKey()))
            .expectedValues("grpc", "http/protobuf")
            .build();

    /** 配置选项：telemetry-logs-level，导出至遥测端点的最详细日志级别。 */
    public static final Option<LoggingOptions.Level> TELEMETRY_LOGS_LEVEL = new OptionBuilder<>("telemetry-logs-level", LoggingOptions.Level.class)
            .category(OptionCategory.TELEMETRY)
            .description("The most verbose log level exported to the telemetry endpoint. For more information, check the Telemetry guide.")
            .defaultValue(LoggingOptions.Level.ALL)
            .caseInsensitiveExpectedValues(true)
            .build();

    /** 配置选项：telemetry-logs-header-<header>，日志导出器请求的 HTTP 头。 */
    public static final Option<String> TELEMETRY_LOGS_HEADER = new OptionBuilder<>("telemetry-logs-header-<header>", String.class)
            .category(OptionCategory.TELEMETRY)
            .description("OpenTelemetry header that will be part of the log exporter request (mainly useful for providing Authorization header). Check the documentation on how to set environment variables for headers containing special characters or custom case-sensitive headers.")
            .build();

    /** 合成选项：telemetry-logs-headers，聚合所有日志导出 HTTP 头。 */
    public static final Option<List<String>> TELEMETRY_LOGS_HEADERS = OptionBuilder.listOptionBuilder("telemetry-logs-headers", String.class)
            .category(OptionCategory.TELEMETRY)
            .synthetic()
            .build();

    // 遥测指标配置
    /** 配置选项：telemetry-metrics-enabled，是否导出 OpenTelemetry 指标。 */
    public static final Option<Boolean> TELEMETRY_METRICS_ENABLED = new OptionBuilder<>("telemetry-metrics-enabled", Boolean.class)
            .category(OptionCategory.TELEMETRY)
            .description("Enables exporting metrics to a destination handling OpenTelemetry metrics.")
            .defaultValue(Boolean.FALSE)
            .buildTime(true)
            .build();

    /** 配置选项：telemetry-metrics-endpoint，指标导出端点。 */
    public static final Option<String> TELEMETRY_METRICS_ENDPOINT = new OptionBuilder<>("telemetry-metrics-endpoint", String.class)
            .category(OptionCategory.TELEMETRY)
            .description("OpenTelemetry endpoint to connect to for Metrics. If not given, the value is inherited from the '%s' option.".formatted(TelemetryOptions.TELEMETRY_ENDPOINT.getKey()))
            .build();

    /** 配置选项：telemetry-metrics-protocol，指标遥测数据协议。 */
    public static final Option<String> TELEMETRY_METRICS_PROTOCOL = new OptionBuilder<>("telemetry-metrics-protocol", String.class)
            .category(OptionCategory.TELEMETRY)
            .description("OpenTelemetry protocol used for the metrics telemetry data. If not given, the value is inherited from the '%s' option.".formatted(TelemetryOptions.TELEMETRY_PROTOCOL.getKey()))
            .expectedValues("grpc", "http/protobuf")
            .build();

    /** 配置选项：telemetry-metrics-interval，指标导出间隔。 */
    public static final Option<String> TELEMETRY_METRICS_INTERVAL = new OptionBuilder<>("telemetry-metrics-interval", String.class)
            .category(OptionCategory.TELEMETRY)
            .description("The interval between the start of two metric export attempts to the destination handling OpenTelemetry metrics data. It accepts simplified format for time units as java.time.Duration (like 5000ms, 30s, 5m, 1h). If the value is only a number, it represents time in seconds.")
            .defaultValue("60s")
            .build();

    /** 配置选项：telemetry-metrics-header-<header>，指标导出器请求的 HTTP 头。 */
    public static final Option<String> TELEMETRY_METRICS_HEADER = new OptionBuilder<>("telemetry-metrics-header-<header>", String.class)
            .category(OptionCategory.TELEMETRY)
            .description("OpenTelemetry header that will be part of the metrics exporter request (mainly useful for providing Authorization header). Check the documentation on how to set environment variables for headers containing special characters or custom case-sensitive headers.")
            .build();

    /** 合成选项：telemetry-metrics-headers，聚合所有指标导出 HTTP 头。 */
    public static final Option<List<String>> TELEMETRY_METRICS_HEADERS = OptionBuilder.listOptionBuilder("telemetry-metrics-headers", String.class)
            .category(OptionCategory.TELEMETRY)
            .synthetic()
            .build();
}
