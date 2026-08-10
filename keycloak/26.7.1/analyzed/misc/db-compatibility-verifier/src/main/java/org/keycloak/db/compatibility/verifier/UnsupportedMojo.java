package org.keycloak.db.compatibility.verifier;

import java.io.File;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;

/**
 * Maven 目标 {@code unsupported}：将新变更标记为<strong>不支持滚动升级</strong>并写入 unsupported JSON。
 * <p>
 * 与 {@link SupportedMojo} 参数顺序相反：目标文件为 unsupported，并从 supported 中移除冲突项。
 */
@Mojo(name = "unsupported")
public class UnsupportedMojo extends AbstractNewEntryMojo {

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
            execute(uFile, sFile);
        } catch (Exception e) {
            throw new MojoExecutionException("Error adding entry to unsupported file", e);
        }
    }
}
