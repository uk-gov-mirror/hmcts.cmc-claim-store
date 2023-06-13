package uk.gov.hmcts.cmc.claimstore.services.unstuck;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.claimstore.models.idam.User;
import uk.gov.hmcts.cmc.claimstore.repositories.CaseSearchApi;
import uk.gov.hmcts.cmc.claimstore.services.UserService;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.ClaimState;

import java.util.HashSet;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class SearchStuckClaims {

    private final UserService userService;
    private final CaseSearchApi caseSearchApi;


    public Set<Claim> findStuckClaims(User user) {
        Set<Claim> claims = new HashSet<>(caseSearchApi.getClaims(user, query()));
        log.info(String.format("SearchStuckClaims:: Found %s stack claims:", claims.size()));
        return claims;
    }

    private QueryBuilder query() {
        return QueryBuilders.boolQuery()
            .must(QueryBuilders.termQuery("state", ClaimState.CREATE.getValue()))
            .must(QueryBuilders.existsQuery("data.previousServiceCaseReference"));
    }
}
