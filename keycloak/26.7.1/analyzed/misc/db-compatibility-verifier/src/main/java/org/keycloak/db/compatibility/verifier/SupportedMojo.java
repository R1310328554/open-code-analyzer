package org.keycloak.db.compatibility.verifier;

import java.io.File;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;

/**
 * Maven 目标 {@code supported}：将新变更标记为<strong>支持滚动升级</strong>并写入 supported JSON。
 * <p>
 * 继承 {@link AbstractNewEntryMojo}，在 supported 与 unsupported 文件间维护互斥条目。
 */
@Mojo(name = "supported")
public class SupportedMojo extends AbstractNewEntryMojo {

    @Override
    public void execute() throws MojoExecutionException {
        if (skip) {
            getLog().info("Skipping execution");
            return;
        }

        File root = project.getBasedir();
        File sFile = new File(root, supportedFile);
        File uFile = new File(root, unsupportedFile);
        checkFileExist("supported", sFile);
        checkFileExist("unsupported", uFile);

        try {
            execute(sFile, uFile);
        } catch (Exception e) {
            throw new MojoExecutionException("Error adding entry to supported file", e);
        }
    }
}
