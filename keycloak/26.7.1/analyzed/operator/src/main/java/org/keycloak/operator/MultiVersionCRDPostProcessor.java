package org.keycloak.operator;

import io.fabric8.crdv2.generator.CRDPostProcessor;
import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.apiextensions.v1.CustomResourceDefinition;

/**
 * 多版本 CRD 后处理器：在生成 CRD 时保留 Alpha 版本并标记为已弃用，引导用户迁移至稳定版本。
 */
public class MultiVersionCRDPostProcessor implements CRDPostProcessor {

    /**
     * 处理 CRD 定义，必要时追加已弃用的 Alpha 版本条目。
     *
     * @param crd CRD 资源对象
     * @param crdSpecVersion CRD 规范版本
     * @return 处理后的 CRD
     */
    @Override
    public HasMetadata process(HasMetadata crd, String crdSpecVersion) {
        CustomResourceDefinition v1crd = (CustomResourceDefinition) crd;
        var mainVersion = v1crd.getSpec().getVersions().get(0);
        // 若主版本不是 Alpha，则追加 Alpha 版本并标记为弃用
        if (!Constants.CRDS_VERSION_ALPHA.equals(mainVersion.getName())) {
            v1crd.getSpec().getVersions()
                    .add(mainVersion.edit().withDeprecated()
                            .withDeprecationWarning("Please migrate to " + Constants.CRDS_VERSION)
                            .withName(Constants.CRDS_VERSION_ALPHA).withStorage(false).build());
        }
        return v1crd;
    }

}
