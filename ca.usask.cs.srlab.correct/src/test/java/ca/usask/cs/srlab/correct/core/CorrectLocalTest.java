package ca.usask.cs.srlab.correct.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.HashMap;

public class CorrectLocalTest {

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

    @Test
    public void testLoadFileTokens() {
        CorrectLocal correctLocal = new CorrectLocal(this.repoName, this.JSON_FILE, this.PR_FILE, this.COMMIT_FILE);
        HashMap<String, String> tokenMap = correctLocal.loadFileTokenMap();
        for (String key : tokenMap.keySet()) {
            System.out.println(key + " " + tokenMap.get(key).length());
        }
    }

    @Test
    public void testLoadPR() {
        CorrectLocal correctLocal = new CorrectLocal(this.repoName, this.JSON_FILE, this.PR_FILE, this.COMMIT_FILE);
        HashMap<Integer, PRObject> prMap = correctLocal.loadPRMap();
        for (int key : prMap.keySet()) {
            PRObject prObject = prMap.get(key);
            System.out.println(key + " " + prObject.commitSHAs);
        }
    }


    @Test
    public void testLoadCommits(){
        CorrectLocal correctLocal = new CorrectLocal(this.repoName, this.JSON_FILE, this.PR_FILE, this.COMMIT_FILE);
        HashMap<String, ArrayList<String>> commitMap = correctLocal.loadCommitFileMap();
        for(String key: commitMap.keySet()){
            System.out.println(key+" "+commitMap.get(key).size());
        }
    }
}
