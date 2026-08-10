package org.keycloak.ssf.transmitter.emit;

import org.keycloak.models.OrganizationModel;
import org.keycloak.models.UserModel;

/**
 * 从发射方 {@code sub_id} 解析出的主体元组。对应方面缺失或无法解析时，
 * 各字段可为 {@code null}；两者均为 null 时报告为 {@link EmitEventStatus#SUBJECT_NOT_FOUND}。
 */
record EmitSubjectResolution(UserModel user, OrganizationModel organization) {
}
