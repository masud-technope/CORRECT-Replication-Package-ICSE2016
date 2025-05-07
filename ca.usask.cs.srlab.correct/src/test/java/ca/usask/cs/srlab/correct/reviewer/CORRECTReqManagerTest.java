package ca.usask.cs.srlab.correct.reviewer;

import ca.usask.cs.srlab.correct.core.CorrectLocal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class CORRECTReqManagerTest {

    String repoName;
    String COMMIT_FILE;
    String JSON_FILE;
    String PR_FILE;
    String HOME_DIR;
    CorrectLocal correctLocal;

    @BeforeEach
    public void setup() {
        this.repoName = "sample";
        this.HOME_DIR = "C:/MyWorks/MyResearch/CodeReview/Correct/CORRECT-Replication-Package-ICSE2016/ca.usask.cs.srlab.correct";
        this.COMMIT_FILE = this.HOME_DIR + "/COMMIT/" + this.repoName + ".json";
        this.JSON_FILE = this.HOME_DIR + "/JSON/" + this.repoName + ".json";
        this.PR_FILE = this.HOME_DIR + "/PR/" + this.repoName + ".json";
        this.correctLocal = new CorrectLocal(this.repoName, this.JSON_FILE, this.PR_FILE, this.COMMIT_FILE);
        this.correctLocal.cacheRepository();
    }

    @Test
    public void testCollectLibTechTokensLocal() {
        CORRECTRequestManager crManager = new CORRECTRequestManager(1039, this.repoName, correctLocal.fileTokenMap, correctLocal.prMap, correctLocal.commitFileMap);
        String tokens = crManager.collectLibTechTokensLocal(1039);
        System.out.println(tokens);
    }
}
