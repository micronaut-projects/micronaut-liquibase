/*
 * Copyright 2017-2021 original authors
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
package io.micronaut.liquibase.endpoint;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.annotation.Introspected;
import liquibase.change.CheckSum;
import liquibase.changelog.ChangeSet;
import liquibase.changelog.RanChangeSet;

import java.time.Instant;
import java.util.Set;

/**
 * Serialization wrapper around {@link RanChangeSet}.
 *
 * @author Jonas Konrad
 * @since 5.0.1
 */
@Internal
@Introspected
@JsonInclude(JsonInclude.Include.ALWAYS)
final class RanChangeSetWrapper {
    private final RanChangeSet delegate;

    public RanChangeSetWrapper(RanChangeSet delegate) {
        this.delegate = delegate;
    }

    public String getAuthor() {
        return delegate.getAuthor();
    }

    public String getChangeLog() {
        return delegate.getChangeLog();
    }

    public String getComments() {
        return delegate.getComments();
    }

    public Set<String> getContexts() {
        return delegate.getContextExpression().getContexts();
    }

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    public Instant getDateExecuted() {
        return delegate.getDateExecuted().toInstant();
    }

    public String getDeploymentId() {
        return delegate.getDeploymentId();
    }

    public String getDescription() {
        return delegate.getDescription();
    }

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    public ChangeSet.ExecType getExecType() {
        return delegate.getExecType();
    }

    public String getId() {
        return delegate.getId();
    }

    public Set<String> getLabels() {
        return delegate.getLabels().getLabels();
    }

    public String getStoredChangeLog() {
        return delegate.getStoredChangeLog();
    }

    public String getChecksum() {
        CheckSum cs = delegate.getLastCheckSum();
        return cs == null ? null : cs.toString();
    }

    public Integer getOrderExecuted() {
        return delegate.getOrderExecuted();
    }

    public String getTag() {
        return delegate.getTag();
    }
}
