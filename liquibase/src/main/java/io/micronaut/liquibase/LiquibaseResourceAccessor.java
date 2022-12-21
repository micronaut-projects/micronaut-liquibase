/*
 * Copyright 2017-2020 original authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.micronaut.liquibase;

import io.micronaut.context.env.Environment;
import jakarta.inject.Singleton;
import liquibase.resource.ClassLoaderResourceAccessor;
import liquibase.resource.CompositeResourceAccessor;
import liquibase.resource.FileSystemResourceAccessor;
import liquibase.resource.Resource;
import liquibase.resource.ResourceAccessor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;

/**
 * Micronaut bean implementing {@link liquibase.resource.ResourceAccessor}.
 *
 * @author Sergio del Amo
 * @since 1.0.0
 */
@Singleton
public class LiquibaseResourceAccessor extends CompositeResourceAccessor {

    /**
     * @param environment The Micronaut environment
     */
    public LiquibaseResourceAccessor(Environment environment) {
        super(buildResourceAccessors(environment));
    }

    @Override
    public SortedSet<String> list(String relativeTo, String path, boolean includeFiles, boolean includeDirectories, boolean recursive) throws IOException {
        return super.list(normalize(relativeTo), path, includeFiles, includeDirectories, recursive);
    }

    @Override
    public List<Resource> getAll(String path) throws IOException {
        return super.getAll(normalize(path));
    }

    private String normalize(String path) {
        if (path != null) {
            if (path.startsWith("classpath:")) {
                path = path.substring(10);
            }
            if (path.startsWith("file:")) {
                path = path.substring(5);
            }
        }
        return path;
    }

    /**
     * @param environment The environment
     * @return A list of {@link ResourceAccessor} to look for migrations
     */
    protected static List<ResourceAccessor> buildResourceAccessors(Environment environment) {
        List<ResourceAccessor> resourceAccessors = new ArrayList<>(2);
        resourceAccessors.add(new ClassLoaderResourceAccessor(environment.getClassLoader()));
        resourceAccessors.add(new FileSystemResourceAccessor());
        return resourceAccessors;
    }
}
