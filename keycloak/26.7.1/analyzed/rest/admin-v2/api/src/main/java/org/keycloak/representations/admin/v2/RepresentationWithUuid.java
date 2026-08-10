package org.keycloak.representations.admin.v2;

/**
 * 带服务端 UUID 的 Admin v2 表示对象契约。
 * <p>
 * 实现类通过 {@link #getUuid()} 暴露只读标识，供 PUT/PATCH 校验不可变约束使用。
 *
 * @author Vaclav Muzikar <vmuzikar@ibm.com>
 */
public interface RepresentationWithUuid {

    /** 返回服务端生成的 UUID。 */
    String getUuid();
}
