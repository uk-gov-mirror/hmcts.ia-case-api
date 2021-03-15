package uk.gov.hmcts.reform.iacaseapi.domain.service;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.UserDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.roleassignment.ActorIdType;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.roleassignment.Classification;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.roleassignment.GrantType;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.roleassignment.QueryRequest;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.roleassignment.RequestedRoles;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.roleassignment.RoleAssignment;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.roleassignment.RoleAssignmentResource;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.roleassignment.RoleCategory;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.roleassignment.RoleRequest;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.roleassignment.RoleType;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.roleassignment.RoleAssignmentApi;

import java.util.HashMap;
import java.util.Map;

import static java.util.Collections.singletonList;

@Component
public class RoleAssignmentService {
    public static final String ROLE_NAME = "senior-tribunal-caseworker";
    private final AuthTokenGenerator serviceAuthTokenGenerator;
    private final UserDetails userDetails;
    private final RoleAssignmentApi roleAssignmentApi;

    public RoleAssignmentService(AuthTokenGenerator serviceAuthTokenGenerator,
                                 RoleAssignmentApi roleAssignmentApi,
                                 UserDetails userDetails) {
        this.serviceAuthTokenGenerator = serviceAuthTokenGenerator;
        this.roleAssignmentApi = roleAssignmentApi;
        this.userDetails = userDetails;
    }

    public void assignRole(long caseDetailsId, String assigneeId) {
        String accessToken = userDetails.getAccessToken();
        String currentUserIdamId = userDetails.getId();
        String serviceAuthorizationToken = serviceAuthTokenGenerator.generate();

        RoleAssignment body =
            getRoleAssignment(caseDetailsId, assigneeId, currentUserIdamId);

        roleAssignmentApi.assignRole(accessToken, serviceAuthorizationToken, body);
    }

    public RoleAssignment getRoleAssignment(long caseDetailsId, String assigneeId, String currentUserIdamId) {

        Map<String, String> attributes = new HashMap<>();
        attributes.put("caseId", Long.toString(caseDetailsId));

        RoleAssignment body = new RoleAssignment(
            new RoleRequest(
                currentUserIdamId,
                "case-allocation",
                caseDetailsId + "/" + ROLE_NAME,
                true
            ),
            singletonList(new RequestedRoles(
                ActorIdType.IDAM,
                assigneeId,
                RoleType.CASE,
                ROLE_NAME,
                RoleCategory.STAFF,
                Classification.RESTRICTED,
                GrantType.SPECIFIC,
                false,
                attributes
            ))
        );
        return body;
    }


    public RoleAssignmentResource queryRoleAssignments(QueryRequest queryRequest) {
        return roleAssignmentApi.queryRoleAssignments(
            userDetails.getAccessToken(),
            serviceAuthTokenGenerator.generate(),
            queryRequest
        );
    }

}
