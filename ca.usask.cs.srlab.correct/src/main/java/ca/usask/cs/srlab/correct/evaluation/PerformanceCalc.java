package ca.usask.cs.srlab.correct.evaluation;

import ca.usask.cs.srlab.correct.config.StaticData;
import ca.usask.cs.srlab.correct.core.CorrectLocal;
import ca.usask.cs.srlab.correct.core.PRObject;
import ca.usask.cs.srlab.correct.utility.ContentLoader;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

public class PerformanceCalc {

    String repoName;
    int TOPK;
    HashMap<Integer, PRObject> prMap;
    String outputFile;
    HashMap<Integer, ArrayList<String>> resultMap;

    public PerformanceCalc(String repoName, int TOPK, String outputFile) {
        this.repoName = repoName;
        this.TOPK = TOPK;
        this.outputFile = outputFile;
        StaticData.REPOSITORY = System.getProperty("user.dir");
        this.prMap = loadGoldset();
        this.resultMap = loadResults();
    }

    protected HashMap<Integer, PRObject> loadGoldset() {
        CorrectLocal localLoader = new CorrectLocal(repoName);
        localLoader.cacheRepository();
        return localLoader.prMap;
    }

    protected HashMap<Integer, ArrayList<String>> loadResults() {
        ArrayList<String> rLines = ContentLoader
                .getAllLinesOptList(this.outputFile);
        HashMap<Integer, ArrayList<String>> resultMap = new HashMap<>();
        for (String line : rLines) {
            String[] parts = line.split(":");
            int prNum = Integer.parseInt(parts[0].trim());
            String[] revs = parts[1].trim().split("\\s+");
            ArrayList<String> recRevs = new ArrayList<String>(
                    Arrays.asList(revs));
            resultMap.put(prNum, recRevs);
        }
        return resultMap;
    }

    public void calculatePerformance() {

        int correct = 0;
        double sumPrec = 0;
        double sumRec = 0;
        double sumRR = 0;

        for (int prNumber : this.resultMap.keySet()) {
            ArrayList<String> recommended = this.resultMap.get(prNumber);
            if (this.prMap.containsKey(prNumber)) {
                ArrayList<String> goldSet = getPRGoldSet(this.prMap
                        .get(prNumber));
                double precision = getAvgPrecision(recommended, goldSet, TOPK);
                sumPrec += precision;
                if (precision > 0) {
                    correct++;
                }
                double recall = getRecall(recommended, goldSet, TOPK);
                sumRec += recall;
                double recRank = getReciprocalRank(recommended, goldSet, TOPK);
                sumRR += recRank;
            }
        }

        System.out.println("Top-" + TOPK + " Accuracy:" + (double) correct
                / this.prMap.size());
        System.out.println("Mean Reciprocal Rank@" + TOPK + ": " + sumRR
                / this.prMap.size());
        System.out.println("Mean Precision@" + TOPK + ": " + sumPrec
                / this.prMap.size());
        System.out.println("Mean Recall@" + TOPK + ": " + sumRec
                / this.prMap.size());
    }

    protected ArrayList<String> getPRGoldSet(PRObject prObject) {
        ArrayList<String> suggested = prObject.recommendedRevs;
        ArrayList<String> actual = prObject.actualRevs;
        HashSet<String> uniques = new HashSet<>();
        uniques.addAll(suggested);
        uniques.addAll(actual);
        return new ArrayList<String>(uniques);
    }

    protected double getPrecision(ArrayList<String> recommended,
                                  ArrayList<String> gold, int topk) {
        double found = 0;
        int tcount = 0;
        for (String rev : recommended) {
            if (gold.contains(rev)) {
                found++;
            }
            tcount++;
            if (tcount == topk)
                break;
        }
        return (double) found / tcount;
    }

    protected double getAvgPrecision(ArrayList<String> recommended,
                                     ArrayList<String> gold, int topk) {
        int found = 0;
        double tempPrec = 0;
        for (int i = 0; i < recommended.size(); i++) {
            String rev = recommended.get(i);
            if (gold.contains(rev)) {
                found++;
                double prec = (double) found / (i + 1);
                tempPrec += prec;
            }

            if (i + 1 == topk)
                break;
        }
        if (found == 0)
            return 0;

        return tempPrec / found;
    }

    protected double getRecall(ArrayList<String> recommended,
                               ArrayList<String> gold, int topk) {
        double found = 0;
        int tcount = 0;
        for (String rev : recommended) {
            if (gold.contains(rev)) {
                found++;
            }
            tcount++;
            if (tcount == topk)
                break;
        }
        return (double) found / gold.size();
    }

    protected double getReciprocalRank(ArrayList<String> recommended,
                                       ArrayList<String> gold, int topk) {
        double found = 0;
        for (String rev : recommended) {
            found++;
            if (gold.contains(rev)) {
                break;
            }
        }
        return found > 0 ? (1 / found) : 0;

    }

}
