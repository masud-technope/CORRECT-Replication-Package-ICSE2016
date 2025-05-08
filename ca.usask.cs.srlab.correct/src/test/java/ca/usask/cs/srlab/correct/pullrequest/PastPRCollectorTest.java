package ca.usask.cs.srlab.correct.pullrequest;

import ca.usask.cs.srlab.correct.core.CorrectLocal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class PastPRCollectorTest {

    String repoName;
    int currentPRNumber;
    CorrectLocal correctLocal;
    PastPRCollector pprCollector;

    @BeforeEach
    public void setup() {
        this.repoName = "sample";
        this.currentPRNumber = 1046;
        this.correctLocal = new CorrectLocal(this.repoName);
        this.pprCollector = new PastPRCollector(this.repoName, this.currentPRNumber, 30, correctLocal);
    }

    @Test
    public void testCollectPastPRs() {
        System.out.println(pprCollector.collectPastPRs().size());
    }

    @Test
    public void testCollectPastReviewers() {
        System.out.println(pprCollector.collectPastReviewers().size());
    }
}
