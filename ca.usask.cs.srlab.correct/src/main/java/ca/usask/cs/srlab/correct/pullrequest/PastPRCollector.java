package ca.usask.cs.srlab.correct.pullrequest;

import ca.usask.cs.srlab.correct.core.CorrectLocal;
import ca.usask.cs.srlab.correct.core.PRObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class PastPRCollector {

    String repoName;
    int currentPRNumber;
    int trainingSize;
    CorrectLocal correctLocal;

    public PastPRCollector(String repoName, int currentPRNumber, int trainingSize, CorrectLocal correctLocal) {
        this.repoName = repoName;
        this.currentPRNumber = currentPRNumber;
        this.trainingSize = trainingSize;
        this.correctLocal = correctLocal;
        this.correctLocal.cacheRepository();
    }

    public HashMap<Integer, String> collectPastPRs() {
        HashMap<Integer, String> pastPRMap = new HashMap<>();
        for (int prNumber = currentPRNumber - 1; prNumber > 0; prNumber--) {
            if (this.correctLocal.prMap.containsKey(prNumber)) {
                PullReqThread prThread = new PullReqThread(repoName, prNumber, this.correctLocal.fileTokenMap, this.correctLocal.prMap, this.correctLocal.commitFileMap);
                ArrayList<String> commits = prThread.collectCommits(prNumber);
                ArrayList<String> changedFiles = prThread.collectCommittedFiles(commits);
                String tokens = prThread.collectTokens(new HashSet<String>(changedFiles));
                pastPRMap.put(prNumber, tokens);
                if (pastPRMap.size() == trainingSize) break;
            }
        }
        return pastPRMap;
    }

    public HashMap<Integer, ArrayList<String>> collectPastReviewers() {
        HashMap<Integer, ArrayList<String>> pastPRReviewerMap = new HashMap<>();
        for (int prNumber = currentPRNumber - 1; prNumber > 0; prNumber--) {
            if (this.correctLocal.prMap.containsKey(prNumber)) {
                PRObject prObject = this.correctLocal.prMap.get(prNumber);
                ArrayList<String> actualRevs = prObject.actualRevs;
                ArrayList<String> recommendedRevs = prObject.recommendedRevs;
                recommendedRevs.addAll(actualRevs);
                pastPRReviewerMap.put(prNumber, recommendedRevs);
                if (pastPRReviewerMap.size() == trainingSize) break;
            }
        }
        return pastPRReviewerMap;
    }
}
