package ca.usask.cs.srlab.correct.core;

import java.util.ArrayList;
import java.util.HashMap;

import ca.usask.cs.srlab.correct.config.StaticData;
import ca.usask.cs.srlab.correct.utility.ContentLoader;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class CorrectLocal {

    String repoName;
    public HashMap<String, String> fileTokenMap;
    String fileTokenJSON;
    public HashMap<Integer, PRObject> prMap;
    String prJSON;
    public HashMap<String, ArrayList<String>> commitFileMap;
    String commitFileJSON;

    public CorrectLocal(String repoName) {
        this.repoName = repoName;
        this.fileTokenMap = new HashMap<>();
        this.fileTokenJSON = StaticData.REPOSITORY + "/JSON/" + repoName
                + ".json";
        this.prMap = new HashMap<>();
        this.prJSON = StaticData.REPOSITORY + "/PR/" + repoName + ".json";
        this.commitFileMap = new HashMap<>();
        this.commitFileJSON = StaticData.REPOSITORY + "/COMMIT/" + repoName
                + ".json";
    }

    public CorrectLocal(String repoName, String fileTokenJSON, String prJSON,
                        String commitFileJSON) {
        this.repoName = repoName;
        this.fileTokenMap = new HashMap<>();
        this.prMap = new HashMap<>();
        this.commitFileMap = new HashMap<>();

        this.fileTokenJSON = fileTokenJSON;
        this.prJSON = prJSON;
        this.commitFileJSON = commitFileJSON;
    }

    public void cacheRepository() {
        this.fileTokenMap = this.loadFileTokenMap();
        this.prMap = this.loadPRMap();
        this.commitFileMap = this.loadCommitFileMap();
    }

    protected HashMap<String, String> loadFileTokenMap() {
        HashMap<String, String> fileTokenMap = new HashMap<>();
        String fileContent = ContentLoader.loadFileContent(this.fileTokenJSON);
        try {
            JSONParser parser = new JSONParser();
            JSONArray items = (JSONArray) parser.parse(fileContent);
            for (Object object : items) {
                JSONObject jsonObject = (JSONObject) object;
                String key = jsonObject.get("file").toString();
                String token = jsonObject.get("token").toString();
                if (!fileTokenMap.containsKey(key)) {
                    fileTokenMap.put(key, token);
                }
            }
        } catch (ParseException e) {
            System.err.println("Failed to load file token info: " + repoName);
        }
        return fileTokenMap;
    }

    protected ArrayList<String> convertToList(Object itemList) {
        ArrayList<String> list = new ArrayList<>();
        JSONArray array = (JSONArray) itemList;
        for (Object o : array) {
            list.add(o.toString());
        }
        return list;
    }

    protected HashMap<Integer, PRObject> loadPRMap() {
        HashMap<Integer, PRObject> prMap = new HashMap<>();
        String fileContent = ContentLoader.loadFileContent(this.prJSON);
        try {
            JSONParser parser = new JSONParser();
            JSONArray items = (JSONArray) parser.parse(fileContent);
            for (Object object : items) {
                JSONObject jsonObject = (JSONObject) object;
                int prNumber = Integer
                        .parseInt(jsonObject.get("prNumber").toString());
                PRObject prObject = new PRObject();
                prObject.prNumber = prNumber;
                prObject.requester = jsonObject.get("requester").toString();
                prObject.commitSHAs = convertToList(jsonObject.get("commitSHAs"));
                prObject.actualRevs = convertToList(jsonObject.get("actualRevs"));
                prObject.recommendedRevs = convertToList(jsonObject
                        .get("recommendedRevs"));
                if (!prMap.containsKey(prNumber)) {
                    prMap.put(prNumber, prObject);
                }
            }
        } catch (ParseException e) {
            System.err.println("Failed to load PR info: " + repoName);
        }
        return prMap;
    }

    protected HashMap<String, ArrayList<String>> loadCommitFileMap() {
        HashMap<String, ArrayList<String>> commitFileMap = new HashMap<>();
        String fileContent = ContentLoader.loadFileContent(this.commitFileJSON);
        try {
            JSONParser parser = new JSONParser();
            JSONObject root = (JSONObject) parser.parse(fileContent);
            for (Object key : root.keySet()) {
                String commitID = key.toString();
                ArrayList<String> changeSet = convertToList(root.get(key));
                if (!commitFileMap.containsKey(commitID)) {
                    commitFileMap.put(commitID, changeSet);
                }
            }
        } catch (Exception exc) {
            System.err.println("Failed to load commit changeset info: " + repoName);
        }
        return commitFileMap;
    }
}
