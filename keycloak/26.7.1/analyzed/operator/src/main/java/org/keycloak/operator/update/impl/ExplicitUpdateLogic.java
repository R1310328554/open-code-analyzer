package org.keycloak.operator.update.impl;

import java.util.Objects;
import java.util.Optional;

import org.keycloak.operator.ContextUtils;
import org.keycloak.operator.crds.v2beta1.CRDUtils;
import org.keycloak.operator.crds.v2beta1.deployment.Keycloak;
import org.keycloak.operator.crds.v2beta1.deployment.spec.UpdateSpec;

import io.javaoperatorsdk.operator.api.reconciler.Context;
import io.javaoperatorsdk.operator.api.reconciler.UpdateControl;

/**
 * 显式更新策略：由外部操作者通过 revision 注解控制是否允许滚动更新。
 *
 * <p>当前 StatefulSet revision 与 CR 中期望 revision 一致时选择滚动，否则重建。
 */
public class ExplicitUpdateLogic extends BaseUpdateLogic {

    public ExplicitUpdateLogic(Context<Keycloak> context, Keycloak keycloak) {
        super(context, keycloak);
    }

    @Override
    Optional<UpdateControl<Keycloak>> onUpdate() {
        var maybeCurrentRevision = CRDUtils.getRevision(ContextUtils.getCurrentStatefulSet(context).orElseThrow());

        if (maybeCurrentRevision.isEmpty()) {
            decideRecreateUpdate("Explicit strategy configured. Revision annotation not present in stateful set.");
            return Optional.empty();
        }
        // CRD 校验保证 revision 在 Explicit 策略下已配置
        var desiredRevision = UpdateSpec.getRevision(keycloak).orElseThrow();
        if (Objects.equals(maybeCurrentRevision.get(), desiredRevision)) {
            decideRollingUpdate("Explicit strategy configured. Revision matches.");
            return Optional.empty();
        }

        decideRecreateUpdate("Explicit strategy configured. Revision (%s) does not match (%s).".formatted(maybeCurrentRevision.get(), desiredRevision));
        return Optional.empty();
    }
}
