package ca.usask.cs.srlab.correct.reviewer;

import org.junit.Before;
import org.junit.jupiter.api.BeforeEach;

public class LibRankMakerTest {

    String repoName;
    String COMMIT_FILE;
    String JSON_FILE;
    String PR_FILE;
    String HOME_DIR;
    @BeforeEach
    public void setup() {
        this.repoName = "sample";
        this.HOME_DIR = "C:/MyWorks/MyResearch/CodeReview/Correct/CORRECT-Replication-Package-ICSE2016/ca.usask.cs.srlab.correct";
        this.COMMIT_FILE = this.HOME_DIR + "/COMMIT/" + this.repoName + ".json";
        this.JSON_FILE = this.HOME_DIR + "/JSON/" + this.repoName + ".json";
        this.PR_FILE = this.HOME_DIR + "/PR/" + this.repoName + ".json";
    }


}
