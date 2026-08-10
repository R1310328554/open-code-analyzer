package org.keycloak.ssf.subject;

import org.keycloak.models.OrganizationModel;
import org.keycloak.models.UserModel;

/**
 * {@link SubjectResolver#resolve} 的解析结果。接口未密封（unsealed），
 * 以便第三方代码添加自定义变体（例如 {@code Group(GroupModel)} record），
 * 并在发送方扩展点的 {@code resolveSubject} / {@code applySubjectResolution} 方法中处理。
 */
public interface SubjectResolution {

    SubjectResolution NOT_FOUND = new NotFound();

    SubjectResolution UNSUPPORTED_FORMAT = new UnsupportedFormat();

    record User(UserModel user) implements SubjectResolution {}

    record Organization(OrganizationModel organization) implements SubjectResolution {}

    record NotFound() implements SubjectResolution {}

    record UnsupportedFormat() implements SubjectResolution {}
}
