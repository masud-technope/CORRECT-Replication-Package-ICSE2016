package ca.usask.cs.srlab.correct.reviewer;

import ca.usask.cs.srlab.correct.pullrequest.PRReviewer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

public class ReviewerRecommenderTest {

    int prNumber;
    String repoName;
    String prListFile;
    String revOutputFile;
    int topK, trainingSize;

    @BeforeEach
    public void setup() {
        this.repoName = "sample";
        this.prListFile = "./sample-prlist.txt";
        this.revOutputFile = "./sample-output.txt";
        this.topK = 5;
        this.trainingSize = 30;
    }

    @Test
    public void testRecommendCodeReviewersSingle() {
        ReviewerRecommenderLocal recommender = new ReviewerRecommenderLocal(this.repoName, this.prListFile, this.trainingSize, this.topK, this.revOutputFile);
        System.out.println(recommender.recommendCodeReviewers(1137));
    }

    @Test
    public void testRecommendCodeReviewers(){
        ReviewerRecommenderLocal recommender = new ReviewerRecommenderLocal(this.repoName, this.prListFile, this.trainingSize, this.topK, this.revOutputFile);
        ArrayList<String> recLines = recommender.recommendCodeReviewers();
        System.out.println(recLines);
    }

    @Test
    public void testRecommendReviewersObj(){
        ReviewerRecommenderLocal recommender = new ReviewerRecommenderLocal(this.repoName, this.prListFile, this.trainingSize, this.topK, this.revOutputFile);
        ArrayList<PRReviewer> recommended = recommender.recommendCodeReviewersObj(1095);
        for(PRReviewer reviewer: recommended){
            System.out.println(reviewer.login);
        }
    }
}
