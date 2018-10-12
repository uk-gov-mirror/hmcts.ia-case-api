package uk.gov.hmcts.reform.iacaseapi.forms.domain.service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacaseapi.forms.domain.api.CcdAsylumCaseFetcher;
import uk.gov.hmcts.reform.iacaseapi.shared.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.shared.domain.entities.GroundsOfAppeal;
import uk.gov.hmcts.reform.iacaseapi.shared.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.shared.domain.entities.ccd.IdValue;

@Service
public class GroundsOfAppealFetcher {

    private final CcdAsylumCaseFetcher asylumCaseFetcher;

    public GroundsOfAppealFetcher(
        @Autowired CcdAsylumCaseFetcher asylumCaseFetcher
    ) {
        this.asylumCaseFetcher = asylumCaseFetcher;
    }

    public List<String> fetch(
        String caseId
    ) {
        CaseDetails<AsylumCase> asylumCaseDetails = asylumCaseFetcher.fetch(caseId);

        Optional<GroundsOfAppeal> groundsOfAppeal =
            asylumCaseDetails
                .getCaseData()
                .getCaseArgument()
                .orElseThrow(() -> new IllegalStateException("caseArgument not present"))
                .getGroundsOfAppeal();

        if (!groundsOfAppeal.isPresent()
            || !groundsOfAppeal.get().getGroundsOfAppeal().isPresent()) {
            return Collections.emptyList();
        }

        return groundsOfAppeal
            .get()
            .getGroundsOfAppeal()
            .get()
            .stream()
            .map(IdValue::getId)
            //.map(ground -> {
            //
            //    if (ground.getGround().contains("Refugee Convention")) {
            //        return "refugeeConvention";
            //    }
            //
            //    if (ground.getGround().contains("humanitarian protection")) {
            //        return "humanitarianProtection";
            //    }
            //
            //    if (ground.getGround().contains("Human Rights Convention")) {
            //        return "humanRightsConvention";
            //    }
            //
            //    throw new IllegalStateException("Ground not recognised");
            //
            //})
            .collect(Collectors.toList());
    }
}