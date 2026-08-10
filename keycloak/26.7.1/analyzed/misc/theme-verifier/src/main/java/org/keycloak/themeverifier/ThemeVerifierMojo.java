/*
 * Copyright 2025 Red Hat, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.keycloak.themeverifier;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.maven.model.Resource;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

/**
 * Maven 目标 {@code verify-theme}：在 INSTALL 阶段扫描项目资源目录下全部
 * {@code messages_*.properties}，并调用 {@link VerifyMessageProperties} 做主题文案校验。
 */
@Mojo(name = "verify-theme", defaultPhase = LifecyclePhase.INSTALL, threadSafe = true)
public class ThemeVerifierMojo extends AbstractMojo {

    /** 当前 Maven 项目，用于获取 {@code resources} 目录列表。 */
    @Parameter(defaultValue = "${project}", readonly = true)
    private MavenProject mavenProject;

    /**
     * 是否以后端 MessageFormat 规则校验引号与占位符；
     * false 时按前端展示规则校验。
     */
    @Parameter(defaultValue = "false")
    private boolean validateMessageFormatQuotes;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        Iterator<Resource> resources = mavenProject.getResources().iterator();
        List<String> messages = new ArrayList<>();
        while (resources.hasNext()) {
            Resource resource = resources.next();
            File dir = new File(resource.getDirectory());
            // 递归遍历资源树中的 messages_*.properties
            Iterator<File> fileIterator = FileUtils.iterateFiles(dir, MessagePropertiesFilter.INSTANCE, DirectoryFileFilter.INSTANCE);
            while (fileIterator.hasNext()) {
                File file = fileIterator.next();
                messages.addAll(new VerifyMessageProperties(file).withValidateMessageFormatQuotes(validateMessageFormatQuotes).verify());
            }
        }
        if (!messages.isEmpty()) {
            throw new MojoFailureException("Validation errors: " + messages.stream().collect(Collectors.joining(System.lineSeparator())));
        }
    }
}
