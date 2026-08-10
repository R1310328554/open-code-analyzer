package org.keycloak.quarkus.runtime.configuration.mappers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.keycloak.config.HttpAccessLogOptions;
import org.keycloak.quarkus.runtime.configuration.Configuration;
import org.keycloak.utils.StringUtil;

import io.smallrye.config.ConfigSourceInterceptorContext;

import static org.keycloak.quarkus.runtime.configuration.mappers.PropertyMapper.fromOption;

/**
 * HTTP 访问日志（Quarkus access log）相关 {@link PropertyMapper} 分组：
 * 支持控制台/文件输出、掩码头与 Cookie、日志轮转等配置映射。
 */
public class HttpAccessLogPropertyMappers implements PropertyMapperGrouping {
    /** 访问日志已启用时的条件描述。 */
    private static final String ACCESS_LOG_ENABLED_MSG = "HTTP Access log is enabled";
    /** 访问日志写入文件已启用时的条件描述。 */
    private static final String ACCESS_LOG_FILE_ENABLED_MSG = "HTTP Access logging to file is enabled";

    @Override
    public List<PropertyMapper<?>> getPropertyMappers() {
        return List.of(
                fromOption(HttpAccessLogOptions.HTTP_ACCESS_LOG_ENABLED)
                        .to("quarkus.http.access-log.enabled")
                        .build(),
                fromOption(HttpAccessLogOptions.HTTP_ACCESS_LOG_PATTERN)
                        .isEnabled(HttpAccessLogPropertyMappers::isHttpAccessLogEnabled, ACCESS_LOG_ENABLED_MSG)
                        .paramLabel("<pattern>")
                        .to("quarkus.http.access-log.pattern")
                        .build(),
                fromOption(HttpAccessLogOptions.HTTP_ACCESS_LOG_EXCLUDE)
                        .isEnabled(HttpAccessLogPropertyMappers::isHttpAccessLogEnabled, ACCESS_LOG_ENABLED_MSG)
                        .paramLabel("<exclusions>")
                        .to("quarkus.http.access-log.exclude-pattern")
                        .build(),
                fromOption(HttpAccessLogOptions.HTTP_ACCESS_LOG_MASKED_HEADERS)
                        .isEnabled(HttpAccessLogPropertyMappers::isHttpAccessLogEnabled, ACCESS_LOG_ENABLED_MSG)
                        .paramLabel("<headers>")
                        .to("quarkus.http.access-log.masked-headers")
                        .transformer(HttpAccessLogPropertyMappers::transformMaskedHeaders)
                        .build(),
                fromOption(HttpAccessLogOptions.HTTP_ACCESS_LOG_MASKED_COOKIES)
                        .isEnabled(HttpAccessLogPropertyMappers::isHttpAccessLogEnabled, ACCESS_LOG_ENABLED_MSG)
                        .paramLabel("<cookies>")
                        .to("quarkus.http.access-log.masked-cookies")
                        .transformer(HttpAccessLogPropertyMappers::transformMaskedCookies)
                        .build(),
                // 文件输出
                fromOption(HttpAccessLogOptions.HTTP_ACCESS_LOG_FILE_ENABLED)
                        .isEnabled(HttpAccessLogPropertyMappers::isHttpAccessLogEnabled, ACCESS_LOG_ENABLED_MSG)
                        .to("quarkus.http.access-log.log-to-file")
                        .build(),
                fromOption(HttpAccessLogOptions.HTTP_ACCESS_LOG_FILE_NAME)
                        .isEnabled(HttpAccessLogPropertyMappers::isHttpAccessLogFileEnabled, ACCESS_LOG_FILE_ENABLED_MSG)
                        .paramLabel("<name>")
                        .to("quarkus.http.access-log.base-file-name")
                        .build(),
                fromOption(HttpAccessLogOptions.HTTP_ACCESS_LOG_FILE_SUFFIX)
                        .isEnabled(HttpAccessLogPropertyMappers::isHttpAccessLogFileEnabled, ACCESS_LOG_FILE_ENABLED_MSG)
                        .paramLabel("<suffix>")
                        .to("quarkus.http.access-log.log-suffix")
                        .build(),
                fromOption(HttpAccessLogOptions.HTTP_ACCESS_LOG_FILE_ROTATE)
                        .isEnabled(HttpAccessLogPropertyMappers::isHttpAccessLogFileEnabled, ACCESS_LOG_FILE_ENABLED_MSG)
                        .to("quarkus.http.access-log.rotate")
                        .build()
        );
    }

    /** 合并用户配置的掩码头与默认隐藏头列表。 */
    private static String transformMaskedHeaders(String value, ConfigSourceInterceptorContext context) {
        return transformMaskedElements(value, HttpAccessLogOptions.DEFAULT_HIDDEN_HEADERS);
    }

    /** 合并用户配置的掩码 Cookie 与默认隐藏 Cookie 列表。 */
    private static String transformMaskedCookies(String value, ConfigSourceInterceptorContext context) {
        return transformMaskedElements(value, HttpAccessLogOptions.DEFAULT_HIDDEN_COOKIES);
    }

    /** 将用户额外指定的元素追加到默认掩码列表并以逗号连接。 */
    private static String transformMaskedElements(String value, List<String> defaultMaskedElements) {
        var defaultMasked = new ArrayList<>(defaultMaskedElements);
        if (StringUtil.isNotBlank(value)) {
            Arrays.stream(value.split(","))
                    .filter(f -> !defaultMasked.contains(f))
                    .forEach(defaultMasked::add);
        }
        return String.join(",", defaultMasked);
    }

    /** HTTP 访问日志是否已启用。 */
    static boolean isHttpAccessLogEnabled() {
        return Configuration.isTrue(HttpAccessLogOptions.HTTP_ACCESS_LOG_ENABLED);
    }

    /** HTTP 访问日志文件输出是否已启用。 */
    static boolean isHttpAccessLogFileEnabled() {
        return Configuration.isTrue(HttpAccessLogOptions.HTTP_ACCESS_LOG_FILE_ENABLED);
    }
}
