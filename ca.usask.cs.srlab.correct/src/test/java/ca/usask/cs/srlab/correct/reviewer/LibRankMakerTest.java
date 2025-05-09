package ca.usask.cs.srlab.correct.reviewer;

import ca.usask.cs.srlab.correct.core.CorrectLocal;
import ca.usask.cs.srlab.correct.core.PRObject;
import ca.usask.cs.srlab.correct.pullrequest.PastPRCollector;
import org.junit.Before;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class LibRankMakerTest {

    String repoName;
    CorrectLocal correctLocal;
    String targetTokenList;
    LibRankMaker lrMaker;

    @BeforeEach
    public void setup() {
        this.repoName = "sample";
        int prNumber = 1519;
        this.correctLocal = new CorrectLocal(this.repoName);
        this.correctLocal.cacheRepository();
        this.targetTokenList = "google.appengine.ext ndb enum unique Enum vobject.objects.partner SubscriptionLevel vdatastore vobject vobject.base.property CoercionPolicy  logging google.appengine.ext ndb .model FeatureAccess FeatureAccessModel .client get_config_async .sdk Client4XXError";
        PastPRCollector pprCollector = new PastPRCollector(this.repoName, prNumber, 30, this.correctLocal);
        this.lrMaker = new LibRankMaker(this.targetTokenList, pprCollector.collectPastPRs(), pprCollector.collectPastReviewers());
    }

    @Test
    public void testLoadVALibTechTokensFromPR() {
        System.out.println(this.lrMaker.collectVALibTokensFromPR(this.targetTokenList));
        System.out.println(this.lrMaker.collectVATechTokensFromPR(this.targetTokenList));
        System.out.println(this.lrMaker.collectMiscLibTokens(this.targetTokenList));
    }


    @Test
    public void testGetPRRecencyScores(){
        System.out.println(this.lrMaker.getPRRecencyScores());
    }

    @Test
    public void collectNCombineRanks(){
        System.out.println(this.lrMaker.collectNCombineRanks());
    }

}
