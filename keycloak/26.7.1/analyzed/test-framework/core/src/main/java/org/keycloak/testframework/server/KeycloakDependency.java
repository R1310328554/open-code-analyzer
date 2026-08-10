package org.keycloak.testframework.server;

import io.quarkus.maven.dependency.ArtifactDependency;
import io.quarkus.maven.dependency.DependencyBuilder;

/**
 * Keycloak 测试服务器 Provider 依赖描述，扩展 Quarkus {@link ArtifactDependency}。
 * <p>
 * 标记是否支持热部署及是否来自当前 Maven 工程模块。
 */
public class KeycloakDependency extends ArtifactDependency {

    /** 是否可在运行中的服务器上热部署。 */
    private final boolean hotDeployable;
    /** 是否对应当前工程的 Maven 模块。 */
    private final boolean dependencyCurrentProject;

    /** @param dependencyBuilder 构建器状态 */
    private KeycloakDependency(Builder dependencyBuilder) {
        super(dependencyBuilder);
        this.hotDeployable = dependencyBuilder.hotDeployable;
        this.dependencyCurrentProject = dependencyBuilder.dependencyCurrentProject;
    }

    /** @return 是否支持热部署 */
    public boolean isHotDeployable() {
        return this.hotDeployable;
    }

    /** @return 是否为当前工程模块依赖 */
    public boolean dependencyCurrentProject() {
        return this.dependencyCurrentProject;
    }

    /** {@link KeycloakDependency} 的流式构建器。 */
    public static class Builder extends DependencyBuilder {

        /** 热部署标志，默认 false。 */
        private boolean hotDeployable = false;
        /** 当前工程模块标志，默认 false。 */
        private boolean dependencyCurrentProject = false;

        /** 设置热部署标志并返回 {@code this}。 */
        public Builder hotDeployable(boolean hotDeployable) {
            this.hotDeployable = hotDeployable;
            return this;
        }

        /** 标记依赖来自当前 Maven 工程并返回 {@code this}。 */
        public Builder dependencyCurrentProject(boolean dependencyCurrentProject) {
            this.dependencyCurrentProject = dependencyCurrentProject;
            return this;
        }

        /** {@inheritDoc} */
        @Override
        public Builder setGroupId(String groupId) {
            super.setGroupId(groupId);
            return this;
        }

        /** {@inheritDoc} */
        @Override
        public Builder setArtifactId(String artifactId) {
            super.setArtifactId(artifactId);
            return this;
        }

        /** {@inheritDoc} */
        @Override
        public Builder setVersion(String version) {
            super.setVersion(version);
            return this;
        }

        /** @return 不可变的 {@link KeycloakDependency} 实例 */
        @Override
        public KeycloakDependency build() {
            return new KeycloakDependency(this);
        }

    }
}
