package uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd;

public enum EventId {

    START_DRAFT_APPEAL("startDraftAppeal"),
    COMPLETE_DRAFT_APPEAL("completeDraftAppeal"),
    UPDATE_DRAFT_APPEAL("updateDraftAppeal"),
    SUBMIT_APPEAL("submitAppeal"),
    EDIT_GROUNDS_OF_APPEAL("editGroundsOfAppeal"),
    BUILD_CASE_ARGUMENT("buildCaseArgument"),
    UPLOAD_DOCUMENT("uploadDocument"),
    SEND_DIRECTION("sendDirection"),
    COMPLETE_DIRECTION("completeDirection"),
    REQUEST_TIME_EXTENSION("requestTimeExtension"),
    REVIEW_TIME_EXTENSION("reviewTimeExtension"),
    ADD_CORRESPONDENCE("addCorrespondence"),
    ADD_HOME_OFFICE_RESPONSE("addHomeOfficeResponse"),
    ADD_HEARING_SUMMARY("addHearingSummary");

    private final String id;

    EventId(String id) {
        this.id = id;
    }

    public String toString() {
        return id;
    }
}
