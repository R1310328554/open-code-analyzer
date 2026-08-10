package org.keycloak.scim.resource.user;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * SCIM 企业用户扩展属性（RFC 7643 第 4.3 节）。
 * <p>包含员工编号、组织层级与上级经理等企业相关信息。</p>
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EnterpriseUser {

    /** 员工编号。 */
    @JsonProperty("employeeNumber")
    private String employeeNumber;

    /** 成本中心。 */
    @JsonProperty("costCenter")
    private String costCenter;

    /** 所属组织。 */
    @JsonProperty("organization")
    private String organization;

    /** 所属事业部。 */
    @JsonProperty("division")
    private String division;

    /** 所属部门。 */
    @JsonProperty("department")
    private String department;

    /** 上级经理信息。 */
    @JsonProperty("manager")
    private Manager manager;

    public String getEmployeeNumber() {
        return employeeNumber;
    }

    public void setEmployeeNumber(String employeeNumber) {
        this.employeeNumber = employeeNumber;
    }

    public String getCostCenter() {
        return costCenter;
    }

    public void setCostCenter(String costCenter) {
        this.costCenter = costCenter;
    }

    public String getOrganization() {
        return organization;
    }

    public void setOrganization(String organization) {
        this.organization = organization;
    }

    public String getDivision() {
        return division;
    }

    public void setDivision(String division) {
        this.division = division;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public Manager getManager() {
        return manager;
    }

    public void setManager(Manager manager) {
        this.manager = manager;
    }

    /**
     * 用户的上级经理（RFC 7643 第 4.3 节）。
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Manager {

        /** 经理资源的标识符或用户名。 */
        @JsonProperty("value")
        private String value;

        /** 经理资源的 URI 引用。 */
        @JsonProperty("$ref")
        private String ref;

        /** 经理的显示名称。 */
        @JsonProperty("displayName")
        private String displayName;

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public String getRef() {
            return ref;
        }

        public void setRef(String ref) {
            this.ref = ref;
        }

        public String getDisplayName() {
            return displayName;
        }

        public void setDisplayName(String displayName) {
            this.displayName = displayName;
        }
    }
}
