package org.keycloak.operator.crds.v2beta1.deployment.spec;

import java.util.Map;

import org.keycloak.operator.crds.v2beta1.CRDUtils;
import org.keycloak.operator.crds.v2beta1.deployment.Keycloak;
import org.keycloak.operator.crds.v2beta1.deployment.KeycloakSpec;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import io.fabric8.generator.annotation.Default;
import io.sundr.builder.annotations.Buildable;

/**
 * Prometheus Operator {@code ServiceMonitor} 资源配置，用于抓取 Keycloak 指标。
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Buildable(editableEnabled = false, builderPackage = "io.fabric8.kubernetes.api.builder")
public class ServiceMonitorSpec {

    /** 默认指标抓取间隔。 */
    public static final String DEFAULT_INTERVAL = "30s";
    /** 默认单次抓取超时时间。 */
    public static final String DEFAULT_SCRAPE_TIMEOUT = "10s";

    /** 是否创建 ServiceMonitor 资源，默认为 true。 */
    @JsonPropertyDescription("Enables or disables the creation of the ServiceMonitor.")
    @Default("true")
    private boolean enabled = true;

    /** 指标抓取间隔。 */
    @JsonPropertyDescription("Interval at which metrics should be scraped")
    @Default(DEFAULT_INTERVAL)
    private String interval = DEFAULT_INTERVAL;

    /** 单次抓取超时时间，超时后结束本次抓取。 */
    @JsonPropertyDescription("Timeout after which the scrape is ended")
    @Default(DEFAULT_SCRAPE_TIMEOUT)
    private String scrapeTimeout = DEFAULT_SCRAPE_TIMEOUT;

    /** 追加到 ServiceMonitor 关联 Service 的注解。 */
    @JsonPropertyDescription("Annotations to be appended to the Service object")
    private Map<String, String> annotations;

    /** 追加到 ServiceMonitor 关联 Service 的标签。 */
    @JsonPropertyDescription("Labels to be appended to the Service object")
    private Map<String, String> labels;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getInterval() {
        return interval;
    }

    public void setInterval(String interval) {
        this.interval = interval;
    }

    public String getScrapeTimeout() {
        return scrapeTimeout;
    }

    public void setScrapeTimeout(String scrapeTimeout) {
        this.scrapeTimeout = scrapeTimeout;
    }

    public Map<String, String> getAnnotations() {
        return annotations;
    }

    public void setAnnotations(Map<String, String> annotations) {
        this.annotations = annotations;
    }

    public Map<String, String> getLabels() {
        return labels;
    }

    public void setLabels(Map<String, String> labels) {
        this.labels = labels;
    }

    /** 从 Keycloak CR 获取 ServiceMonitor 配置，未配置时返回默认实例。 */
    public static ServiceMonitorSpec get(Keycloak keycloak) {
        return CRDUtils.keycloakSpecOf(keycloak)
              .map(KeycloakSpec::getServiceMonitorSpec)
              .orElse(new ServiceMonitorSpec());
    }
}
