package ca.usask.cs.srlab.correct.pullrequest;

import ca.usask.cs.srlab.correct.core.CorrectLocal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashSet;

public class PullReqThreadTest {

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
    public void testCollectCommits() {
        int prNumber = 1519;
        PullReqThread prThread = new PullReqThread(this.repoName, prNumber, correctLocal.fileTokenMap, correctLocal.prMap, correctLocal.commitFileMap);
        System.out.println(prThread.collectCommits(prNumber));
    }

    @Test
    public void testCollectCommittedFile() {
        int prNumber = 1519;
        PullReqThread prThread = new PullReqThread(this.repoName, prNumber, correctLocal.fileTokenMap, correctLocal.prMap, correctLocal.commitFileMap);
        ArrayList<String> commits = prThread.collectCommits(prNumber);
        System.out.println(prThread.collectCommittedFiles(commits));
    }

    @Test
    public void testCollectCommittedFileTokens() {
        int prNumber = 1519;
        PullReqThread prThread = new PullReqThread(this.repoName, prNumber, correctLocal.fileTokenMap, correctLocal.prMap, correctLocal.commitFileMap);
        ArrayList<String> commits = prThread.collectCommits(prNumber);
        ArrayList<String> files = prThread.collectCommittedFiles(commits);
        System.out.println(prThread.collectTokens(new HashSet<>(files)));
    }
}
