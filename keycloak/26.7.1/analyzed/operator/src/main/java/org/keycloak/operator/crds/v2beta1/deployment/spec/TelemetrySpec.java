package org.keycloak.operator.crds.v2beta1.deployment.spec;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import io.sundr.builder.annotations.Buildable;

/**
 * OpenTelemetry 遥测导出配置，包括端点、协议与资源属性。
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Buildable(editableEnabled = false, builderPackage = "io.fabric8.kubernetes.api.builder")
public class TelemetrySpec {

    /** OpenTelemetry 采集端点地址。 */
    @JsonPropertyDescription("OpenTelemetry endpoint to connect to.")
    private String endpoint;

    /** OpenTelemetry 服务名称，优先于 resourceAttributes 中的 service.name。 */
    @JsonPropertyDescription("OpenTelemetry service name. Takes precedence over 'service.name' defined in the 'resourceAttributes' map.")
    private String serviceName;

    /** 遥测数据传输协议，默认为 grpc。 */
    @JsonPropertyDescription("OpenTelemetry protocol used for the telemetry data (default 'grpc'). For more information, check the OpenTelemetry guide.")
    private String protocol;

    /** 导出遥测数据时携带的资源属性，用于标识遥测生产者。 */
    @JsonPropertyDescription("OpenTelemetry resource attributes present in the exported telemetry data to characterize the telemetry producer.")
    private Map<String, String> resourceAttributes;

    public Map<String, String> getResourceAttributes() {
        if (resourceAttributes == null) {
            resourceAttributes = new LinkedHashMap<>();
        }
        return resourceAttributes;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    /** 将资源属性 Map 序列化为 key=val 逗号分隔字符串（JSON 序列化时忽略）。 */
    @JsonIgnore
    public String getResourceAttributesString() {
        return convertResourceAttributesToString(getResourceAttributes());
    }

    public void setResourceAttributes(Map<String, String> resourceAttributes) {
        this.resourceAttributes = resourceAttributes;
    }

    /**
     * 将资源属性 Map 转换为 key=val 逗号分隔的字符串。
     *
     * @param attributes 资源属性键值对
     * @return 逗号分隔的属性字符串
     */
    public static String convertResourceAttributesToString(Map<String, String> attributes) {
        return attributes.entrySet().stream()
                .map(attr -> String.format("%s=%s", attr.getKey(), attr.getValue()))
                .collect(Collectors.joining(","));
    }
}
