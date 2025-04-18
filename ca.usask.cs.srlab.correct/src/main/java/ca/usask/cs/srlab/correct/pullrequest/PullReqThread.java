package ca.usask.cs.srlab.correct.pullrequest;

import ca.usask.cs.srlab.correct.core.PRObject;
import ca.usask.cs.srlab.correct.utility.MiscUtility;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class PullReqThread implements Runnable {

    int prNumber;
    String repoName;
    ArrayList<String> files;
    ArrayList<String> reviewers;
    String tokens;
    ArrayList<String> commits;
    HashMap<String, String> fileTokenMap;
    HashMap<Integer, PRObject> prMap;
    HashMap<String, ArrayList<String>> commitFileMap;
    int MAX_COMMIT_FILES = 10;

    public PullReqThread(String repoName, int prNumber,
                         HashMap<String, String> fileTokenMap,
                         HashMap<Integer, PRObject> prMap,
                         HashMap<String, ArrayList<String>> commitFileMap) {
        this.prNumber = prNumber;
        this.repoName = repoName;

        this.fileTokenMap = fileTokenMap;
        this.prMap = prMap;
        this.commitFileMap = commitFileMap;

        this.files = new ArrayList<>();
        this.reviewers = new ArrayList<>();
        this.tokens = "";
        this.commits = new ArrayList<>();
    }

    protected ArrayList<String> collectCommits(int prNumber) {
        if (this.prMap.containsKey(prNumber)) {
            PRObject prObject = this.prMap.get(prNumber);
            return prObject.commitSHAs;
        }
        return null;
    }

    protected ArrayList<String> collectReviewersBoth(int prNumber) {
        HashSet<String> reviewers = new HashSet<>();
        if (this.prMap.containsKey(prNumber)) {
            PRObject prObject = this.prMap.get(prNumber);
            if (!prObject.actualRevs.isEmpty()) {
                reviewers.addAll(prObject.actualRevs);
            }
            if (!prObject.recommendedRevs.isEmpty()) {
                reviewers.addAll(prObject.recommendedRevs);
            }
        }
        return new ArrayList<>(reviewers);
    }

    protected ArrayList<String> collectCommittedFiles(ArrayList<String> commits) {
        HashSet<String> diffFiles = new HashSet<>();
        for (String key : commits) {
            if (this.commitFileMap.containsKey(key)) {
                ArrayList<String> files = this.commitFileMap.get(key);
                if (files.size() <= MAX_COMMIT_FILES) {
                    diffFiles.addAll(new HashSet<String>(files));
                }
            }
        }
        return new ArrayList<>(diffFiles);
    }

    protected String collectTokens(HashSet<String> committedFiles) {
        ArrayList<String> tokenList = new ArrayList<>();
        for (String url : committedFiles) {
            if (url.endsWith(".py")) {
                if (this.fileTokenMap.containsKey(url.trim())) {
                    String tokens = fileTokenMap.get(url);
                    tokenList.add(tokens);
                }
            }
        }
        return MiscUtility.list2Str(tokenList);
    }

    @Override
    public void run() {
        this.commits = this.collectCommits(this.prNumber);
        this.reviewers = this.collectReviewersBoth(this.prNumber);
        this.files = this.collectCommittedFiles(this.commits);
        this.tokens = this.collectTokens(new HashSet<String>(files));
    }
}
