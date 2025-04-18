package ca.usask.cs.srlab.correct.pullrequest;

import ca.usask.cs.srlab.correct.core.PRObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PullReqThread implements Runnable {

	int prNumber;
	String repoName;
	ArrayList<String> files;
	ArrayList<String> reviewers;
	String tokens;
	ArrayList<String> commits;
	HashMap<String, String> fileTokenMap;
	HashMap<String, String> url2FileMap;
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

		// item initialization
		this.files = new ArrayList<>();
		this.reviewers = new ArrayList<>();
		this.tokens = new String();
		this.commits = new ArrayList<>();
		this.fileTokenMap = fileTokenMap;
		this.url2FileMap = new HashMap<>();
	}

	protected void collectCommit(boolean local) {
		// collecting the commits
		if (this.prMap.containsKey(this.prNumber)) {
			PRObject prObject = this.prMap.get(this.prNumber);
			this.commits = prObject.commitSHAs;
		}
	}

	protected void collectReviewersBoth(boolean local) {
		// collecting reviewers for a PR
		if (this.prMap.containsKey(this.prNumber)) {
			PRObject prObject = this.prMap.get(this.prNumber);
			if (!prObject.actualRevs.isEmpty()) {
				reviewers.addAll(prObject.actualRevs);
			}
			if (!prObject.recommendedRevs.isEmpty()) {
				reviewers.addAll(prObject.recommendedRevs);
			}
		}
	}

	protected ArrayList<String> parseReviewersFromBody(String body) {
		// parsing reviewers from body
		ArrayList<String> reviewers = new ArrayList<>();
		String regex = "@\\w+-va";
		Pattern p = Pattern.compile(regex);
		Matcher matcher = p.matcher(body);
		while (matcher.find()) {
			String userID = body.substring(matcher.start(), matcher.end());
			reviewers.add(userID.substring(1));
		}
		return reviewers;
	}

	protected void collectFilesNTokens() {
		// collect committed files and their tokens
		HashMap<String, ArrayList<String>> commitMap  = null; //collectCommittedFiles(commits);
		HashSet<String> dfiles = new HashSet<>();
		for (String key : commitMap.keySet()) {
			ArrayList<String> files = commitMap.get(key);
			dfiles.addAll(files);
		}
		this.files.addAll(dfiles);

		// collecting tokens
		this.tokens = collectTokens();
		System.out.println("Tokens extracted:" + this.tokens);
	}

	protected void collectFilesNTokens(boolean local) {
		// collecting tokens
		HashSet<String> dfiles = new HashSet<>();
		for (String key : this.commits) {
			if (this.commitFileMap.containsKey(key)) {
				ArrayList<String> files = this.commitFileMap.get(key);
				if (files.size() <= MAX_COMMIT_FILES) {
					dfiles.addAll(new HashSet<String>(files));
				}
			}
		}
		this.files.addAll(dfiles);
		// collecting tokens
		this.tokens = collectTokens();
		// System.out.println("Tokens extracted:" + this.tokens);
	}

	protected String reformURL(String sha, String url) {
		// reformulate the URL
		int shaIndex = url.indexOf(sha);
		return url.substring(shaIndex + sha.length() + 1);
	}

	protected String collectTokens() {

		String tokenlist = new String();
		for (String url : files) {
			if (url.endsWith(".py")) {
				String pyFileName = url;
				if (this.fileTokenMap.containsKey(pyFileName.trim())) {
					String tokens = fileTokenMap.get(pyFileName);
					tokenlist += tokens + " ";
				} else {
					// nothing for now.
				}
			}
		}
		// returning tokens
		return tokenlist;
	}

	@Override
	public void run() {
		this.collectCommit(true);
		this.collectReviewersBoth(true);
		this.collectFilesNTokens(true);
	}
}
