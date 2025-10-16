/*
 * Copyright 2025 Pavel Castornii.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.techsenger.maven.plugins.bomproperties;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOCase;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.resolution.ArtifactRequest;
import org.eclipse.aether.resolution.ArtifactResult;

/**
 *
 * @author Pavel Castornii
 */
@Mojo(name = "copy-properties")
public class CopyPropertiesMojo extends AbstractMojo {

    private static String getId(Bom bom) {
        return bom.getGroupId() + ":" + bom.getArtifactId() + ":" + bom.getVersion();
    }

    @Parameter(defaultValue = "${project}", readonly = true)
    private MavenProject project;

    @Parameter(required = true)
    private List<Bom> boms;

    @Component
    private RepositorySystem repositorySystem;

    @Parameter(defaultValue = "${session}", readonly = true)
    private MavenSession session;

    @Override
    public void execute() throws MojoExecutionException {
        if (boms == null || boms.isEmpty()) {
            getLog().info("No BOMs configured");
            return;
        }

        Properties props = project.getProperties();

        for (var bom : boms) {
            getLog().info("Processing BOM: " + getId(bom));
            validateBom(bom);

            IOCase iOCase = IOCase.SENSITIVE;
            if (!bom.isCaseSensitive()) {
                iOCase = IOCase.INSENSITIVE;
            }

            try {
                var properties = getBomProperties(bom);
                var count = 0;

                for (var entry : properties.entrySet()) {
                    var key = (String) entry.getKey();

                    boolean included = false;
                    boolean excluded = false;

                    for (var include : bom.getIncludes()) {
                        if (FilenameUtils.wildcardMatch(key, include, iOCase)) {
                            included = true;
                            break;
                        }
                    }
                    if (!included) {
                        continue;
                    }

                    if (bom.getExcludes() != null) {
                        for (var exclude : bom.getExcludes()) {
                            if (FilenameUtils.wildcardMatch(key, exclude, iOCase)) {
                                excluded = true;
                                break;
                            }
                        }
                    }
                    if (excluded) {
                        continue;
                    }
                    props.put(bom.getPrefix() + "." + key, entry.getValue());
                    count++;
                }
                getLog().info("Added " + count + " properties with prefix: " + bom.getPrefix());
            } catch (Exception ex) {
                getLog().error("Failed to process", ex);
                throw new MojoExecutionException("Failed to process", ex);
            }
        }
    }

    private void validateBom(Bom bom) throws MojoExecutionException {
        if (bom.getGroupId() == null || bom.getGroupId().isBlank()) {
            throw new MojoExecutionException("BOM groupId is required");
        }
        if (bom.getArtifactId() == null || bom.getArtifactId().isBlank()) {
            throw new MojoExecutionException("BOM artifactId is required");
        }
        if (bom.getVersion() == null || bom.getVersion().isBlank()) {
            throw new MojoExecutionException("BOM version is required");
        }
        if (bom.getIncludes() == null || bom.getIncludes().isEmpty()) {
            throw new MojoExecutionException("BOM has no includes");
        }
        if (bom.getPrefix() == null || bom.getPrefix().isBlank()) {
            throw new MojoExecutionException("BOM has no scope");
        }
    }

    private Properties getBomProperties(Bom bom) throws Exception {
        var file = resolveBomFile(bom);
        try (InputStream is = new FileInputStream(file)) {
            return new MavenXpp3Reader().read(is).getProperties();
        }
    }

    private File resolveBomFile(Bom bom) throws Exception {
        Artifact bomArtifact = new DefaultArtifact(
            bom.getGroupId(),
            bom.getArtifactId(),
            "pom",
            bom.getVersion()
        );
        ArtifactRequest request = new ArtifactRequest();
        request.setArtifact(bomArtifact);
        ArtifactResult result = repositorySystem.resolveArtifact(
            session.getRepositorySession(),
            request
        );
        return result.getArtifact().getFile();
    }
}

