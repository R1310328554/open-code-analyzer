package org.keycloak.operator.crds.v2beta1.deployment.spec;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.fabric8.kubernetes.api.model.Affinity;
import io.fabric8.kubernetes.api.model.Toleration;
import io.fabric8.kubernetes.api.model.TopologySpreadConstraint;
import io.sundr.builder.annotations.Buildable;

/**
 * Pod 调度策略配置，包括亲和性、容忍度与拓扑分布约束。
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Buildable(editableEnabled = false, builderPackage = "io.fabric8.kubernetes.api.builder")
public class SchedulingSpec {

    /** Pod 亲和性与反亲和性规则。 */
    private Affinity affinity;
    /** 允许 Pod 调度到带有特定污点的节点。 */
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<Toleration> tolerations = new ArrayList<Toleration>();
    /** 跨拓扑域均匀分布 Pod 的约束。 */
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<TopologySpreadConstraint> topologySpreadConstraints = new ArrayList<TopologySpreadConstraint>();
    /** 关联的 PriorityClass 名称。 */
    private String priorityClassName;

    public Affinity getAffinity() {
        return affinity;
    }

    public void setAffinity(Affinity affinity) {
        this.affinity = affinity;
    }

    public List<Toleration> getTolerations() {
        return tolerations;
    }

    public void setTolerations(List<Toleration> tolerations) {
        this.tolerations = tolerations;
    }

    public List<TopologySpreadConstraint> getTopologySpreadConstraints() {
        return topologySpreadConstraints;
    }

    public void setTopologySpreadConstraints(List<TopologySpreadConstraint> topologySpreadConstraints) {
        this.topologySpreadConstraints = topologySpreadConstraints;
    }

    public String getPriorityClassName() {
        return priorityClassName;
    }

    public void setPriorityClassName(String priorityClassName) {
        this.priorityClassName = priorityClassName;
    }

}
