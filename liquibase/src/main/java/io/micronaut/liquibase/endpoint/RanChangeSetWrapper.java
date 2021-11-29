package io.micronaut.liquibase.endpoint;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.micronaut.core.annotation.Introspected;
import liquibase.change.CheckSum;
import liquibase.changelog.ChangeSet;
import liquibase.changelog.RanChangeSet;

import java.time.Instant;
import java.util.Set;

@Introspected
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

    public String getLastCheckSum() {
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
