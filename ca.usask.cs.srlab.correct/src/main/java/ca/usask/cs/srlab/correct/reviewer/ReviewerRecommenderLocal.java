package ca.usask.cs.srlab.correct.reviewer;

import ca.usask.cs.srlab.correct.config.StaticData;
import ca.usask.cs.srlab.correct.core.CorrectLocal;
import ca.usask.cs.srlab.correct.pullrequest.PastPRCollector;
import ca.usask.cs.srlab.correct.utility.ContentLoader;
import ca.usask.cs.srlab.correct.utility.MiscUtility;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

public class ReviewerRecommenderLocal {

    String repoName;
    int TOPK;
    CorrectLocal localLoader;
    String outputFile;
    ArrayList<Integer> prList;
    int trainingSize = 30;
    HashMap<Integer, ArrayList<String>> pastReviewerMap;
    HashMap<Integer, String> prTokenMap;

    public ReviewerRecommenderLocal(String repoName, String prListFile,
                                    int trainingSize, int TOP_K, String outputFile) {
        this.repoName = repoName;
        this.prList = getPRList(prListFile);
        this.TOPK = TOP_K;
        this.trainingSize = trainingSize;
        StaticData.REPOSITORY = System.getProperty("user.dir");
        localLoader = new CorrectLocal(repoName);
        localLoader.cacheRepository();
        this.pastReviewerMap = new HashMap<>();
        this.prTokenMap = new HashMap<>();
        this.outputFile = outputFile;
    }

    protected ArrayList<Integer> getPRList(String prListFile) {
        ArrayList<String> lines = ContentLoader.getAllLinesOptList(prListFile);
        ArrayList<Integer> temp = new ArrayList<>();
        for (String line : lines) {
            temp.add(Integer.parseInt(line.trim()));
        }
        Collections.sort(temp);
        return temp;
    }

    protected ArrayList<String> getTopKOnly(ArrayList<String> ranked) {
        ArrayList<String> temp = new ArrayList<String>();
        for (String rev : ranked) {
            temp.add(rev);
            if (temp.size() == TOPK) {
                break;
            }
        }
        return temp;
    }

    protected void loadPastPRDetails(int currentPRNumber) {
        PastPRCollector pprCollector = new PastPRCollector(repoName, currentPRNumber, trainingSize, localLoader);
        this.prTokenMap = pprCollector.collectPastPRs();
        this.pastReviewerMap = pprCollector.collectPastReviewers();
    }

    public ArrayList<String> recommendCodeReviewers(int prNumber) {
        CORRECTRequestManager requestManager = new CORRECTRequestManager(
                prNumber, repoName, localLoader.fileTokenMap,
                localLoader.prMap, localLoader.commitFileMap);
        String targetTokenList = requestManager
                .collectLibTechTokensLocal(prNumber);
        this.loadPastPRDetails(prNumber);
        LibRankMaker maker = new LibRankMaker(targetTokenList, prTokenMap,
                pastReviewerMap);
        return maker.getRankedReviewers();
    }

    public ArrayList<String> recommendCodeReviewers() {
        ArrayList<String> results = new ArrayList<>();
        for (int prNumber : this.prList) {
            ArrayList<String> ranked = recommendCodeReviewers(prNumber);
            ArrayList<String> topKRevs = getTopKOnly(ranked);
            if (!topKRevs.isEmpty()) {
                results.add(prNumber + ":\t" + MiscUtility.list2Str(topKRevs));
                System.out.println("Done: PR#" + prNumber);
            }
        }
        return results;
    }
}
