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
		this.loadFileTokenMap();
		this.loadPRMap();
		this.loadCommitFileMap();
	}

	protected void loadFileTokenMap() {
		String fileContent = ContentLoader.loadFileContent(this.fileTokenJSON);
		try {
			JSONParser parser = new JSONParser();
			JSONArray items = (JSONArray) parser.parse(fileContent);
			for (Object object : items) {
				JSONObject jobj = (JSONObject) object;
				String key = jobj.get("file").toString();
				String token = jobj.get("token").toString();
				if (!this.fileTokenMap.containsKey(key)) {
					this.fileTokenMap.put(key, token);
				}
			}
			System.out.println(repoName + ": Files loaded:"
					+ this.fileTokenMap.size());
		} catch (ParseException e) {
			// handle the exception
		}
	}

	protected ArrayList<String> convertToList(Object itemList) {
		ArrayList<String> list = new ArrayList<>();
		JSONArray array = (JSONArray) itemList;
		for (int i = 0; i < array.size(); i++) {
			list.add(array.get(i).toString());
		}
		return list;
	}

	protected void loadPRMap() {
		String fileContent = ContentLoader.loadFileContent(this.prJSON);
		try {
			JSONParser parser = new JSONParser();
			JSONArray items = (JSONArray) parser.parse(fileContent);
			for (Object object : items) {
				JSONObject jobj = (JSONObject) object;
				int prNumber = Integer
						.parseInt(jobj.get("prNumber").toString());
				PRObject prObject = new PRObject();
				prObject.prNumber = prNumber;
				prObject.requester = jobj.get("requester").toString();
				prObject.commitSHAs = convertToList(jobj.get("commitSHAs"));
				prObject.actualRevs = convertToList(jobj.get("actualRevs"));
				prObject.recommendedRevs = convertToList(jobj
						.get("recommendedRevs"));
				if (!this.prMap.containsKey(prNumber)) {
					this.prMap.put(prNumber, prObject);
				}
			}
			System.out.println(repoName + ": PR loaded:" + this.prMap.size());
		} catch (ParseException e) {
			System.err.println("Failed to load PR info: " + repoName);
		}
	}

	protected void loadCommitFileMap() {
		String fileContent = ContentLoader.loadFileContent(this.commitFileJSON);
		try {
			JSONParser parser = new JSONParser();
			JSONObject root = (JSONObject) parser.parse(fileContent);
			for (Object key : root.keySet()) {
				String commitID = key.toString();
				ArrayList<String> changeSet = convertToList(root.get(key));
				if (!this.commitFileMap.containsKey(commitID)) {
					this.commitFileMap.put(commitID, changeSet);
				}
			}
			System.out.println(repoName + ": Commits loaded:"
					+ this.commitFileMap.size());
		} catch (Exception exc) {
			//handle the exception
		}
	}

	public static void main(String[] args) {
		String repoName="st2";
		CorrectLocal local=new CorrectLocal(repoName);
		local.loadPRMap();
	}
}
