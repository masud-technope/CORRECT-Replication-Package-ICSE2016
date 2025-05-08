package ca.usask.cs.srlab.correct.reviewer;

import ca.usask.cs.srlab.correct.pullrequest.PRReviewer;
import ca.usask.cs.srlab.correct.similarity.CosineSimilarityMeasure;
import ca.usask.cs.srlab.correct.utility.MiscUtility;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;

public class LibRankMaker {

    String targetTokenList;
    HashMap<Integer, String> prTokenMap;
    HashMap<Integer, Double> prRecencyMap;
    HashMap<Integer, ArrayList<String>> pastReviewers;
    ArrayList<String> reviewers;
    HashMap<String, Double> candidates;
    HashMap<String, PRReviewer> candidatesObj;
    ArrayList<String> vaLibs;
    ArrayList<String> vaTechs;
    ArrayList<String> miscLibs;

    public LibRankMaker(String targetTokenList,
                        HashMap<Integer, String> prTokenMap,
                        HashMap<Integer, ArrayList<String>> pastReviewers) {
        this.targetTokenList = targetTokenList;
        this.prTokenMap = prTokenMap;
        this.pastReviewers = pastReviewers;
        this.vaLibs = loadVALibraries();
        this.vaTechs = loadVATechnologies();
        this.prRecencyMap = getPRRecencyScores();
        this.candidates = new HashMap<>();
        this.candidatesObj = new HashMap<>();
    }

    private ArrayList<String> loadVALibraries() {
        ArrayList<String> vaLibs = new ArrayList<>();
        vaLibs.add("vapi");
        vaLibs.add("vform");
        vaLibs.add("vbackup");
        vaLibs.add("vlogs");
        vaLibs.add("vautil");
        vaLibs.add("vpubsub");
        vaLibs.add("vtest");
        vaLibs.add("vauth");
        vaLibs.add("vmonitor");
        vaLibs.add("vpipeline");
        return vaLibs;
    }

    private ArrayList<String> loadVATechnologies() {
        ArrayList<String> vaTechs = new ArrayList<>();
        vaTechs.add("taskqueue");
        vaTechs.add("mapreduce");
        vaTechs.add("urlfetch");
        vaTechs.add("search");
        vaTechs.add("ndb");
        vaTechs.add("deferred");
        vaTechs.add("blobstore");
        vaTechs.add("jinja2");
        vaTechs.add("modules");
        vaTechs.add("socket");
        return vaTechs;
    }

    protected String collectVALibTokensFromPR(String tokenList) {
        String[] tokens = tokenList.split("\\s+");
        ArrayList<String> libTokens = new ArrayList<>();
        for (String token : tokens) {
            if (this.vaLibs.contains(token)) {
                libTokens.add(token);
            }
        }
        return MiscUtility.list2Str(libTokens);
    }

    protected String collectVATechTokensFromPR(String tokenList) {
        String[] tokens = tokenList.split("\\s+");
        ArrayList<String> techTokens = new ArrayList<>();
        for (String token : tokens) {
            if (this.vaTechs.contains(token)) {
                techTokens.add(token);
            }
        }
        return MiscUtility.list2Str(techTokens);
    }

    protected String collectMiscLibTokens(String tokenList) {
        String[] tokens = tokenList.split("\\s+");
        ArrayList<String> miscTokens = new ArrayList<>();
        for (String token : tokens) {
            if (!this.vaLibs.contains(token) && !this.vaTechs.contains(token)) {
                miscTokens.add(token);
            }
        }
        return MiscUtility.list2Str(miscTokens);
    }

    protected ArrayList<String> collectNCombineRanks() {
        HashMap<String, Double> libCandidateMap = new HashMap<>();
        HashMap<String, Double> techCandidateMap = new HashMap<>();
        HashMap<String, Double> miscCandidateMap = new HashMap<>();

        String targetLibTokens = collectVALibTokensFromPR(targetTokenList);
        String targetTechTokens = collectVATechTokensFromPR(targetTokenList);
        String targetMiscTokens = collectMiscLibTokens(targetTokenList);

        for (int key : this.prTokenMap.keySet()) {
            String tokenList = this.prTokenMap.get(key);
            String libTokens = collectVALibTokensFromPR(tokenList);
            CosineSimilarityMeasure cos1 = new CosineSimilarityMeasure(
                    targetLibTokens, libTokens);
            double cos1Score = cos1.getCosineSimilarityScore(true);
            if (cos1Score > 0) {
                if (pastReviewers.containsKey(key)) {
                    ArrayList<String> revList = pastReviewers.get(key);
                    for (String reviewer : revList) {
                        if (libCandidateMap.containsKey(reviewer)) {
                            double score = libCandidateMap.get(reviewer);
                            score += cos1Score;
                            libCandidateMap.put(reviewer, score);
                        } else {
                            libCandidateMap.put(reviewer, cos1Score);
                        }
                    }
                }
            }

            String techTokens = collectVATechTokensFromPR(tokenList);
            CosineSimilarityMeasure cos2 = new CosineSimilarityMeasure(
                    targetTechTokens, techTokens);
            double cos2Score = cos2.getCosineSimilarityScore(true);
            if (cos2Score > 0) {
                if (pastReviewers.containsKey(key)) {
                    ArrayList<String> revList = pastReviewers.get(key);
                    for (String reviewer : revList) {
                        if (techCandidateMap.containsKey(reviewer)) {
                            double score = techCandidateMap.get(reviewer);
                            score += cos2Score;
                            techCandidateMap.put(reviewer, score);
                        } else {
                            techCandidateMap.put(reviewer, cos2Score);
                        }
                    }
                }
            }

            String miscTokens = collectMiscLibTokens(tokenList);
            CosineSimilarityMeasure cos3 = new CosineSimilarityMeasure(targetMiscTokens, miscTokens);
            double cos3Score = cos3.getCosineSimilarityScore(true);
            if (cos3Score > 0) {
                if (pastReviewers.containsKey(key)) {
                    ArrayList<String> revList = pastReviewers.get(key);
                    for (String reviewer : revList) {
                        if (miscCandidateMap.containsKey(reviewer)) {
                            double score = miscCandidateMap.get(reviewer);
                            score += cos3Score;
                            miscCandidateMap.put(reviewer, score);
                        } else {
                            miscCandidateMap.put(reviewer, cos3Score);
                        }
                    }
                }
            }
        }

        ArrayList<String> revRanksByLib = rankCodeReviewers(libCandidateMap);
        ArrayList<String> revRanksByTech = rankCodeReviewers(techCandidateMap);
        ArrayList<String> revRanksByMisc = rankCodeReviewers(miscCandidateMap);
        HashMap<String, Double> combinedMap = getCombinedMap(revRanksByLib, revRanksByTech, revRanksByMisc);

        return rankCodeReviewers(combinedMap);
    }

    private HashMap<String, Double> getCombinedMap(ArrayList<String> revRanksByLib, ArrayList<String> revRanksByTech, ArrayList<String> revRanksByMisc) {
        HashMap<String, Double> combinedMap = new HashMap<>();
        int TOPK = revRanksByLib.size();
        for (int i = 0; i < TOPK; i++) {
            double current_score = TOPK - (i + 1);
            String login = revRanksByLib.get(i);
            if (combinedMap.containsKey(login)) {
                double score = combinedMap.get(login) + current_score;
                combinedMap.put(login, score);
            } else {
                combinedMap.put(login, current_score);
            }
        }

        TOPK = revRanksByTech.size();
        for (int i = 0; i < TOPK; i++) {
            double current_score = TOPK - (i + 1);
            String login = revRanksByTech.get(i);
            if (combinedMap.containsKey(login)) {
                double score = combinedMap.get(login) + current_score;
                combinedMap.put(login, score);
            } else {
                combinedMap.put(login, current_score);
            }
        }

        TOPK = revRanksByMisc.size();
        for (int i = 0; i < TOPK; i++) {
            double current_score = TOPK - (i + 1);
            String login = revRanksByMisc.get(i);
            if (combinedMap.containsKey(login)) {
                double score = combinedMap.get(login) + current_score;
                combinedMap.put(login, score);
            } else {
                combinedMap.put(login, current_score);
            }
        }

        return combinedMap;
    }


    protected void calculateOverallExperienceScore() {
        for (String login : this.candidatesObj.keySet()) {
            PRReviewer myRev = this.candidatesObj.get(login);
            String expertTokens = MiscUtility.list2Str(new ArrayList<String>(
                    myRev.expertTokenList));
            CosineSimilarityMeasure cos = new CosineSimilarityMeasure(
                    targetTokenList, expertTokens);
            double score = cos.getCosineSimilarityScore(true);
            this.candidates.put(login, score);
        }
    }

    protected HashMap<Integer, Double> getPRRecencyScores() {
        ArrayList<Integer> prNumbers = new ArrayList<Integer>(
                this.prTokenMap.keySet());
        prNumbers.sort((pr1, pr2) -> pr2.compareTo(pr1));

        int index = 0;
        int prCount = prNumbers.size();
        HashMap<Integer, Double> recencyMap = new HashMap<>();
        for (int prNumber : prNumbers) {
            double recency = 1 - (double) index / prCount;
            recencyMap.put(prNumber, recency);
            index++;
        }
        return recencyMap;
    }

    protected void collectReviewerScores() {
        String targetLibTokens = collectVALibTokensFromPR(targetTokenList);
        String targetTechTokens = collectVATechTokensFromPR(targetTokenList);
        String targetMiscTokens = collectMiscLibTokens(targetTokenList);

        for (int key : this.prTokenMap.keySet()) {

            String tokenList = this.prTokenMap.get(key);
            String libTokens = collectVALibTokensFromPR(tokenList);
            String techTokens = collectVATechTokensFromPR(tokenList);
            String miscTokens = collectMiscLibTokens(tokenList);

            CosineSimilarityMeasure cosine = new CosineSimilarityMeasure(
                    targetTokenList, tokenList);

            double simScore = 0;
            double libSimScore = 0;
            double techSimScore = 0;
            double miscSimScore = 0;

            simScore = cosine.getCosineSimilarityScore(true);

            libSimScore = new CosineSimilarityMeasure(targetLibTokens, libTokens)
                    .getCosineSimilarityScore(true);
            techSimScore = new CosineSimilarityMeasure(targetTechTokens, techTokens)
                    .getCosineSimilarityScore(true);
            miscSimScore = new CosineSimilarityMeasure(targetMiscTokens, miscTokens)
                    .getCosineSimilarityScore(true);

            if (pastReviewers.containsKey(key)) {
                ArrayList<String> revList = pastReviewers.get(key);
                for (String reviewer : revList) {
                    if (this.candidates.containsKey(reviewer)) {
                        double score = this.candidates.get(reviewer);
                        double recency = prRecencyMap.get(key);
                        score += simScore * recency;

                        this.candidates.put(reviewer, score);

                        PRReviewer myRev = this.candidatesObj.get(reviewer);
                        myRev.libSimScore += libSimScore * recency;
                        myRev.techSimScore += techSimScore * recency;
                        myRev.miscSimScore += miscSimScore * recency;
                        myRev.totalScore += simScore * recency;

                        this.candidatesObj.put(reviewer, myRev);

                    } else {

                        double recency = prRecencyMap.get(key);
                        this.candidates.put(reviewer, simScore * recency);

                        PRReviewer myRev = new PRReviewer();
                        myRev.login = reviewer;
                        myRev.libSimScore = libSimScore * recency;
                        myRev.techSimScore = techSimScore * recency;
                        myRev.miscSimScore = miscSimScore * recency;
                        myRev.totalScore = simScore * recency;

                        this.candidatesObj.put(reviewer, myRev);
                    }
                }
            }
        }
    }

    public ArrayList<String> getRankedReviewers() {
        return this.collectNCombineRanks();
    }


    protected void discardCurrentLogin(String currentLogin) {
        if (this.candidates.containsKey(currentLogin)) {
            this.candidates.remove(currentLogin);
            this.candidatesObj.remove(currentLogin);
        }
    }

    protected ArrayList<String> rankCodeReviewers(
            HashMap<String, Double> candidates) {
        List<Entry<String, Double>> list = new LinkedList<>(
                candidates.entrySet());
        list.sort((o1, o2) -> {
            Double v2 = o2.getValue();
            Double v1 = o1.getValue();
            return v2.compareTo(v1);
        });
        ArrayList<String> ranked = new ArrayList<>();
        for (Entry<String, Double> entry : list) {
            ranked.add(entry.getKey());
        }
        return ranked;
    }


    protected void normalizeLibNTechScores() {
        double maxLibScore = 0;
        double maxTechScore = 0;
        double maxMiscScore = 0;
        double maxTotalScore = 0;

        for (String key : this.candidatesObj.keySet()) {
            PRReviewer rev = this.candidatesObj.get(key);
            double libScore = rev.libSimScore;
            if (libScore > maxLibScore) {
                maxLibScore = libScore;
            }
            double techSimScore = rev.techSimScore;
            if (techSimScore > maxTechScore) {
                maxTechScore = techSimScore;
            }

            double miscSimScore = rev.miscSimScore;
            if (miscSimScore > maxMiscScore) {
                maxMiscScore = miscSimScore;
            }

            double totalScore = rev.totalScore;
            if (totalScore > maxTotalScore) {
                maxTotalScore = totalScore;
            }
        }

        for (String key : this.candidatesObj.keySet()) {
            PRReviewer rev = this.candidatesObj.get(key);
            if (maxLibScore > 0) {
                rev.libSimScore = rev.libSimScore / maxLibScore;
            }
            if (maxTechScore > 0) {
                rev.techSimScore = rev.techSimScore / maxTechScore;
            }
            if (maxMiscScore > 0) {
                rev.miscSimScore = rev.miscSimScore / maxMiscScore;
            }
            if (maxTotalScore > 0) {
                rev.totalScore = rev.totalScore / maxTotalScore;
            }
            this.candidatesObj.put(key, rev);
        }
    }

    protected void normalizeLibNTechScoresAbs() {
        double maxLibScore = 0;
        double maxTechScore = 0;
        double maxMiscScore = 0;
        double maxTotalScore = 0;

        for (String key : this.candidatesObj.keySet()) {
            PRReviewer rev = this.candidatesObj.get(key);
            double libScore = rev.libSimScore;
            if (libScore > maxLibScore) {
                maxLibScore = libScore;
            }
            double techSimScore = rev.techSimScore;
            if (techSimScore > maxTechScore) {
                maxTechScore = techSimScore;
            }

            double miscSimScore = rev.miscSimScore;
            if (miscSimScore > maxMiscScore) {
                maxMiscScore = miscSimScore;
            }

            double totalScore = rev.totalScore;
            if (totalScore > maxTotalScore) {
                maxTotalScore = totalScore;
            }
        }

        int prCount = this.prTokenMap.size();

        for (String key : this.candidatesObj.keySet()) {
            PRReviewer rev = this.candidatesObj.get(key);
            if (maxLibScore > 0) {
                rev.libSimScore = rev.libSimScore / prCount;
            }
            if (maxTechScore > 0) {
                rev.techSimScore = rev.techSimScore / prCount;
            }
            if (maxMiscScore > 0) {
                rev.miscSimScore = rev.miscSimScore / prCount;
            }
            if (maxTotalScore > 0) {
                rev.totalScore = rev.totalScore / prCount;
            }
            this.candidatesObj.put(key, rev);
        }
    }

    protected ArrayList<PRReviewer> rankPRCodeReviewers(HashMap<String, Double> candidates) {
        List<Entry<String, Double>> list = new LinkedList<>(
                candidates.entrySet());
        list.sort((o1, o2) -> {
            Double v2 = o2.getValue();
            Double v1 = o1.getValue();
            return v2.compareTo(v1);
        });

        ArrayList<PRReviewer> ranked = new ArrayList<>();
        for (Entry<String, Double> entry : list) {
            ranked.add(this.candidatesObj.get(entry.getKey()));
        }
        return ranked;
    }
}
