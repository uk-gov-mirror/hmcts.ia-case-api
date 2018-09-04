package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import java.util.Optional;

public class TimeExtension {

    private Optional<String> dateRequested = Optional.empty();
    private Optional<String> timeRequested = Optional.empty();
    private Optional<String> reasons = Optional.empty();
    private Optional<String> requestedBy = Optional.empty();
    private Optional<String> status = Optional.empty();
    private Optional<String> comment = Optional.empty();

    public Optional<String> getDateRequested() {
        return dateRequested;
    }

    public Optional<String> getTimeRequested() {
        return timeRequested;
    }

    public Optional<String> getReasons() {
        return reasons;
    }

    public Optional<String> getRequestedBy() {
        return requestedBy;
    }

    public Optional<String> getStatus() {
        return status;
    }

    public Optional<String> getComment() {
        return comment;
    }

    public void setDateRequested(String dateRequested) {
        this.dateRequested = Optional.ofNullable(dateRequested);
    }

    public void setTimeRequested(String timeRequested) {
        this.timeRequested = Optional.ofNullable(timeRequested);
    }

    public void setReasons(String reasons) {
        this.reasons = Optional.ofNullable(reasons);
    }

    public void setRequestedBy(String requestedBy) {
        this.requestedBy = Optional.ofNullable(requestedBy);
    }

    public void setStatus(String status) {
        this.status = Optional.ofNullable(status);
    }

    public void setComment(String comment) {
        this.comment = Optional.ofNullable(comment);
    }
}
