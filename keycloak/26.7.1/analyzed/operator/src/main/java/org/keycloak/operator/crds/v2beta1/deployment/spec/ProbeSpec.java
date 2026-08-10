package org.keycloak.operator.crds.v2beta1.deployment.spec;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.sundr.builder.annotations.Buildable;

/**
 * Kubernetes 健康探针（Liveness/Readiness）的通用参数配置。
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Buildable(editableEnabled = false, builderPackage = "io.fabric8.kubernetes.api.builder")
public class ProbeSpec {

    /** 探针执行间隔（秒）。 */
    @JsonProperty("periodSeconds")
    private int probePeriodSeconds;

    /** 连续失败多少次后判定探针失败。 */
    @JsonProperty("failureThreshold")
    private int probeFailureThreshold;

    public int getProbeFailureThreshold() {return probeFailureThreshold;}
    public void setProbeFailureThreshold(int probeFailureThreshold) {this.probeFailureThreshold = probeFailureThreshold;}
    public int getProbePeriodSeconds() {return probePeriodSeconds;}
    public void setProbePeriodSeconds(int probePeriodSeconds) {this.probePeriodSeconds = probePeriodSeconds;}
}
