package ca.usask.cs.srlab.correct.reviewer;

import ca.usask.cs.srlab.correct.core.PRObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class CORRECTRequestManager {

    String branchURL;
    public String repoName;
    int currentPRNumber;
    String branch1;
    String branch2;
    ArrayList<String> fileList;
    public ArrayList<String> reviewers;
    HashMap<String, String> fileTokenMap;
    HashMap<Integer, PRObject> prMap;
    HashMap<String, ArrayList<String>> commitFileMap;
    int MAX_FILES_PER_COMMIT = 10;

    String GHAccessToken;
    String currentLogin = "";

    @Deprecated
    public CORRECTRequestManager(String branchURL,
                                 HashMap<String, String> fileTokenMap, String GHAccessToken) {
        this.branchURL = branchURL;
        this.fileList = new ArrayList<>();
        this.fileTokenMap = fileTokenMap;
        this.GHAccessToken = GHAccessToken;
        this.extractBranchesFromURL();
    }

    @Deprecated
    public CORRECTRequestManager(int targetPRNumber, String prURL,
                                 HashMap<String, String> fileTokenMap, String GHAccessToken) {
        this.branchURL = prURL;
        this.currentPRNumber = targetPRNumber;
        this.fileList = new ArrayList<>();
        this.fileTokenMap = fileTokenMap;
        this.GHAccessToken = GHAccessToken;
        this.extractBranchesFromURL();
    }

    public CORRECTRequestManager(int targetPRNumber, String repoName,
                                 HashMap<String, String> fileTokenMap,
                                 HashMap<Integer, PRObject> prMap,
                                 HashMap<String, ArrayList<String>> commitFileMap) {
        this.currentPRNumber = targetPRNumber;
        this.repoName = repoName;
        this.fileTokenMap = fileTokenMap;
        this.prMap = prMap;
        this.commitFileMap = commitFileMap;
        this.reviewers = new ArrayList<>();
    }

    protected void extractRepoFromPRURL() {
        String[] parts = this.branchURL.split("/");
        this.repoName = parts[4]; // fixed position
    }

    protected void extractBranchesFromURL() {
        try {
            String[] parts = this.branchURL.split("/");
            this.repoName = parts[4]; // fixed position
            int lastCompIndex = this.branchURL.indexOf("compare/") + 8;
            String branchNames = this.branchURL.substring(lastCompIndex);

            String[] branchParts = branchNames.split("\\...");
            if (branchParts.length == 2) {
                branch1 = branchParts[0];
                branch2 = branchParts[1];
            } else {
                if (repoName.startsWith("VA-")) { // VA libraries
                    branch1 = "master";
                } else
                    branch1 = "develop"; // projects
                branch2 = branchNames;
            }
        } catch (Exception exc) {
            exc.printStackTrace();
        }
    }

    public String collectLibTechTokensLocal(int targetNumber) {
        String currentPRTokens = "";
        if (this.prMap.containsKey(targetNumber)) {
            PRObject prObject = this.prMap.get(targetNumber);
            ArrayList<String> commitIDs = prObject.commitSHAs;
            this.currentLogin = prObject.requester;
            ArrayList<String> files = new ArrayList<>();
            for (String commitID : commitIDs) {
                if (this.commitFileMap.containsKey(commitID)) {
                    HashSet<String> commitFiles = new HashSet<String>(
                            this.commitFileMap.get(commitID));
                    files.addAll(commitFiles);
                }
            }

            for (String fileURL : files) {
                if (this.fileTokenMap.containsKey(fileURL)) {
                    currentPRTokens += this.fileTokenMap.get(fileURL) + "\t";
                }
            }

            this.reviewers.addAll(prObject.actualRevs);
            this.reviewers.addAll(prObject.recommendedRevs);
        }

        return currentPRTokens.trim();
    }

    protected String extractFileURLFromBlob(String blobURL) {
        String[] parts = blobURL.split("/");
        String fileURL = new String();
        for (int i = 7; i < parts.length; i++) {
            fileURL += "/" + parts[i];
        }
        return fileURL.substring(1);
    }
}
