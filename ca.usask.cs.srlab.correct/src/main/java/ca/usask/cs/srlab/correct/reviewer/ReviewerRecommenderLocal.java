package ca.usask.cs.srlab.correct.reviewer;

import ca.usask.cs.srlab.correct.config.ErrorCodes;
import ca.usask.cs.srlab.correct.config.StaticData;
import ca.usask.cs.srlab.correct.core.CorrectLocal;
import ca.usask.cs.srlab.correct.utility.ContentLoader;
import ca.usask.cs.srlab.correct.utility.ContentWriter;
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
			int trainingSize, int TOPK, String outputFile) {
		this.repoName = repoName;
		this.prList = getPRList(prListFile);
		this.TOPK = TOPK;
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
		// now do the sorting in the ascending order
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

	/*public void collectPastRequestsLocalParallel(int targetPRNumber) {
		// collecting past pull requests
		PullRequestCollectThreaded collector = new PullRequestCollectThreaded(
				targetPRNumber, repoName, localLoader.fileTokenMap,
				localLoader.prMap, localLoader.commitFileMap);
		// setting the training size
		collector.MAXREQS = this.trainingSize;
		collector.collectPastPRsWithTokens(targetPRNumber, true);
		this.pastReviewerMap = collector.pastReviewers;
		if (pastReviewerMap.isEmpty()) {
			// no past reviewers collected
			ErrorCodes.CUSTOM_ERROR_MSG = ErrorCodes.NO_PAST_REVIWERS;
			return;
		}
		this.prTokenMap = collector.prTokenMap;
		if (this.prTokenMap.isEmpty()) {
			// no library information found in PY files
			ErrorCodes.CUSTOM_ERROR_MSG = ErrorCodes.NO_LIB_IN_FILE;
			return;
		}
	} */


	public void recommendCodeReviewers() {
		// suggesting code reviewers
		ArrayList<String> results = new ArrayList<String>();
		for (int prNumber : this.prList) {
			CORRECTRequestManager requestManager = new CORRECTRequestManager(
					prNumber, repoName, localLoader.fileTokenMap,
					localLoader.prMap, localLoader.commitFileMap);
			String targetTokenList = requestManager
					.collectLibTechTokensLocal(prNumber);
			//this.collectPastRequestsLocalParallel(prNumber);
			LibRankMaker maker = new LibRankMaker(targetTokenList, prTokenMap,
					pastReviewerMap);
			ArrayList<String> ranked = maker.getRankedReviewers();
			ArrayList<String> topk = getTopKOnly(ranked);
			if (!topk.isEmpty()) {
				results.add(prNumber + ":\t" + MiscUtility.list2Str(topk));
				System.out.println("Done: PR#" + prNumber);
			}
		}
		// now save the output file
		ContentWriter.writeContent(outputFile, results);
		System.out.println("Code reviewers recommended successfully!");
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String repoName = "SM";
		String inputFile = "./sample-prlist.txt";
		String outputFile = "./sample-output.txt";
		int TOPK = 5;
		int trainingSize = 30;

		new ReviewerRecommenderLocal(repoName, inputFile, trainingSize, TOPK,
				outputFile).recommendCodeReviewers();
	}
}
