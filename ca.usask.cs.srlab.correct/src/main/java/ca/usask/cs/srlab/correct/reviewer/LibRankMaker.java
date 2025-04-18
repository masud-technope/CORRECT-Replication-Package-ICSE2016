package ca.usask.cs.srlab.correct.reviewer;

import ca.usask.cs.srlab.correct.pullrequest.PRReviewer;
import ca.usask.cs.srlab.correct.similarity.CosineSimilarityMeasure;
import ca.usask.cs.srlab.correct.utility.MiscUtility;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class LibRankMaker {

	String targetTokenList;
	HashMap<Integer, String> prTokenMap;
	HashMap<Integer, Double> prRecencyMap;
	HashMap<Integer, ArrayList<String>> pastReviewers;
	ArrayList<String> reviewers;
	HashMap<String, Double> candidates;
	HashMap<String, PRReviewer> candidatesObj;
	ArrayList<String> vlibs;
	ArrayList<String> vatechs;
	ArrayList<String> misclibs;

	@Deprecated
	public LibRankMaker(String targetTokenList,
			HashMap<Integer, String> prTokenMap,
			HashMap<Integer, ArrayList<String>> pastReviewers,
			ArrayList<String> reviewers) {
		// initialization of objects
		this.targetTokenList = targetTokenList;
		this.prTokenMap = prTokenMap;
		this.pastReviewers = pastReviewers;
		this.reviewers = reviewers;
		this.candidates = new HashMap<>();
	}

	public LibRankMaker(String targetTokenList,
			HashMap<Integer, String> prTokenMap,
			HashMap<Integer, ArrayList<String>> pastReviewers) {
		this.targetTokenList = targetTokenList;
		this.prTokenMap = prTokenMap;
		this.pastReviewers = pastReviewers;
		this.candidates = new HashMap<>();
		this.candidatesObj = new HashMap<>();
		// newly added
		this.prRecencyMap = new HashMap<>();
	}

	@Deprecated
	public LibRankMaker(String targetTokenList,
			HashMap<Integer, String> prTokenMap, ArrayList<String> valibs,
			ArrayList<String> vatechs,
			HashMap<Integer, ArrayList<String>> pastReviewers,
			ArrayList<String> reviewers) {
		// initialization of objects
		this.targetTokenList = targetTokenList;
		this.prTokenMap = prTokenMap;
		this.pastReviewers = pastReviewers;
		this.reviewers = reviewers;
		this.candidates = new HashMap<>();
		this.vlibs = valibs;
		this.vatechs = vatechs;
	}

	@Deprecated
	protected String collectVALibTokens(String tokenList) {
		String[] tokens = tokenList.split("\\s+");
		String libtokens = new String();
		for (String token : tokens) {
			if (vlibs.contains(token)) {
				libtokens += " " + token;
			}
		}
		return libtokens.trim();
	}

	protected String collectVALibTokens(String tokenList,
			ArrayList<String> vlibs) {
		String[] tokens = tokenList.split("\\s+");
		String libtokens = new String();
		for (String token : tokens) {
			for (String vlib : vlibs) {
				// token containing vlibs
				if (token.trim().contains(vlib)) {
					libtokens += " " + token;
					break;
				}
			}
		}
		return libtokens.trim();
	}

	@Deprecated
	protected String collectTechTokens(String tokenList) {
		String[] tokens = tokenList.split("\\s+");
		String techtokens = new String();
		for (String token : tokens) {
			if (vatechs.contains(token)) {
				techtokens += " " + token;
			}
		}
		return techtokens.trim();
	}

	protected String collectTechTokens(String tokenList, ArrayList<String> vlibs) {
		String[] tokens = tokenList.split("\\s+");
		String techtokens = new String();
		for (String token : tokens) {
			if (!vlibs.contains(token)) {
				if (token.startsWith("google.appengine")) {
					techtokens += " " + token;
				}
			}
		}
		return techtokens.trim();
	}

	protected String collectMiscLibTokens(String tokenList,
			ArrayList<String> vlibs) {
		String[] tokens = tokenList.split("\\s+");
		String misctokens = new String();
		for (String token : tokens) {
			if (!vlibs.contains(token)) {
				if (token.startsWith("google.appengine")) {
					// do nothing now
				} else {
					misctokens += " " + token;
				}
			}
		}
		return misctokens.trim();
	}

	protected ArrayList<String> collectNCombineRanks() {
		// collecting and combining ranks
		HashMap<String, Double> libcandidateMap = new HashMap<>();
		HashMap<String, Double> techcandidateMap = new HashMap<>();
		String targetLibTokens = collectVALibTokens(targetTokenList);
		String targetTechTokens = collectTechTokens(targetTokenList);

		// now collect token similarity scores
		for (int key : this.prTokenMap.keySet()) {
			String tokenList = this.prTokenMap.get(key);
			String libtoken = collectVALibTokens(tokenList);
			CosineSimilarityMeasure cos1 = new CosineSimilarityMeasure(
					targetLibTokens, libtoken);
			double cos1Score = cos1.get_cosine_similarity_score(true);
			if (cos1Score > 0) {
				if (pastReviewers.containsKey(key)) {
					ArrayList<String> revlist = pastReviewers.get(key);
					for (String reviewer : revlist) {
						if (libcandidateMap.containsKey(reviewer)) {
							double score = libcandidateMap.get(reviewer);
							score += cos1Score;
							libcandidateMap.put(reviewer, score);
						} else {
							libcandidateMap.put(reviewer, cos1Score);
						}
					}
				}
			}

			String techtoken = collectTechTokens(tokenList);
			CosineSimilarityMeasure cos2 = new CosineSimilarityMeasure(
					targetTechTokens, techtoken);
			double cos2Score = cos2.get_cosine_similarity_score(true);
			if (cos2Score > 0) {
				if (pastReviewers.containsKey(key)) {
					ArrayList<String> revlist = pastReviewers.get(key);
					for (String reviewer : revlist) {
						if (techcandidateMap.containsKey(reviewer)) {
							double score = techcandidateMap.get(reviewer);
							score += cos2Score;
							techcandidateMap.put(reviewer, score);
						} else {
							techcandidateMap.put(reviewer, cos2Score);
						}
					}
				}
			}
		}

		ArrayList<String> libranks = rankCodeReviewers(libcandidateMap);
		ArrayList<String> techranks = rankCodeReviewers(techcandidateMap);

		// now use Borda count for the ranking
		HashMap<String, Double> combinedMap = new HashMap<>();
		int TOPK = libranks.size();
		for (int i = 0; i < TOPK; i++) {
			double current_score = TOPK - (i + 1);
			String login = libranks.get(i);
			if (combinedMap.containsKey(login)) {
				double score = combinedMap.get(login) + current_score;
				combinedMap.put(login, score);
			} else {
				combinedMap.put(login, current_score);
			}
		}

		TOPK = techranks.size();
		for (int i = 0; i < TOPK; i++) {
			double current_score = TOPK - (i + 1);
			String login = techranks.get(i);
			if (combinedMap.containsKey(login)) {
				double score = combinedMap.get(login) + current_score;
				combinedMap.put(login, score);
			} else {
				combinedMap.put(login, current_score);
			}
		}

		// now rank use the Borda count for ranking
		ArrayList<String> ranked = rankCodeReviewers(combinedMap);
		return ranked;
	}

	protected void collectReviewerScores() {
		// collecting reviewer scores
		
		
		// collect PR recency scores
		this.getPRRecencyScores();
		
		for (int key : this.prTokenMap.keySet()) {
			String tokenlist = this.prTokenMap.get(key);

			CosineSimilarityMeasure cosmeasure = new CosineSimilarityMeasure(
					targetTokenList, tokenlist);
			double simScore = cosmeasure.get_cosine_similarity_score(true);
			
			//introducing recency with the similar score
			if(this.prRecencyMap.containsKey(key)){
				simScore=simScore* this.prRecencyMap.get(key);
			}
			
			if (pastReviewers.containsKey(key)) {
				ArrayList<String> revlist = pastReviewers.get(key);
				for (String reviewer : revlist) {
					if (this.candidates.containsKey(reviewer)) {
						double score = this.candidates.get(reviewer);
						score += simScore;
						this.candidates.put(reviewer, score);
					} else {
						this.candidates.put(reviewer, simScore);
					}
				}
			}
		}
	}

	protected void calculateOverallExperienceScore() {
		// calculating overall experience
		for (String login : this.candidatesObj.keySet()) {
			PRReviewer myRev = this.candidatesObj.get(login);
			String expertTokens = MiscUtility.list2Str(new ArrayList<String>(
					myRev.expertTokenList));
			CosineSimilarityMeasure cos = new CosineSimilarityMeasure(
					targetTokenList, expertTokens);
			double score = cos.get_cosine_similarity_score(true);
			this.candidates.put(login, score);
		}
	}

	protected void getPRRecencyScores() {
		// estimating recency scores
		ArrayList<Integer> prNumbers = new ArrayList<Integer>(
				this.prTokenMap.keySet());
		Collections.sort(prNumbers, new Comparator<Integer>() {
			@Override
			public int compare(Integer pr1, Integer pr2) {
				// TODO Auto-generated method stub
				return pr2.compareTo(pr1);
			}
		});
		// adding the recency scores
		int index = 0;
		int prcount = prNumbers.size();
		for (int prNumber : prNumbers) {
			double recency = 1 - (double) index / prcount;
			this.prRecencyMap.put(prNumber, recency);
			index++;
		}
	}

	protected void collectReviewerScores(ArrayList<String> valibs) {
		// collecting reviewer scores
		String targetLibs = collectVALibTokens(targetTokenList, valibs);
		String targetTechs = collectTechTokens(targetTokenList, valibs);
		String targetMiscs = collectMiscLibTokens(targetTokenList, valibs);

		// collect PR recency scores
		this.getPRRecencyScores();

		for (int key : this.prTokenMap.keySet()) {
			String tokenlist = this.prTokenMap.get(key);

			// all tokens considered altogether
			String listlib = collectVALibTokens(tokenlist, valibs);

			String listtech = collectTechTokens(tokenlist, valibs);

			String listMiscLib = collectMiscLibTokens(tokenlist, valibs);

			CosineSimilarityMeasure cosmeasure = new CosineSimilarityMeasure(
					targetTokenList, tokenlist);

			// CosineSimilarityMeasure cosmeasure = new CosineSimilarityMeasure(
			// targetLibs, listlib);

			double simScore = 0;
			double libSimScore = 0;
			double techSimScore = 0;
			double miscSimScore = 0;

			simScore = cosmeasure.get_cosine_similarity_score(true);

			libSimScore = new CosineSimilarityMeasure(targetLibs, listlib)
					.get_cosine_similarity_score(true);
			techSimScore = new CosineSimilarityMeasure(targetTechs, listtech)
					.get_cosine_similarity_score(true);
			miscSimScore = new CosineSimilarityMeasure(targetMiscs, listMiscLib)
					.get_cosine_similarity_score(true);

			if (pastReviewers.containsKey(key)) {
				ArrayList<String> revlist = pastReviewers.get(key);
				for (String reviewer : revlist) {
					if (this.candidates.containsKey(reviewer)) {

						double score = this.candidates.get(reviewer);

						// considers only frequency
						/*
						 * if(simScore>0){ score++; }
						 */

						// considers the max score only
						/*
						 * if(simScore>score){ score=simScore; }
						 */

						// considers experience
						// score += simScore;

						// consider recency and experience
						double recency = prRecencyMap.get(key);
						score += simScore * recency;

						this.candidates.put(reviewer, score);

						PRReviewer myRev = this.candidatesObj.get(reviewer);
						myRev.libSimScore += libSimScore * recency;
						myRev.techSimScore += techSimScore * recency;
						myRev.miscSimScore += miscSimScore * recency;
						myRev.totalScore += simScore * recency;

						// storing the experience tokens
						// myRev.expertTokenList.addAll(Arrays.asList(tokenlist
						// .split("\\s+")));

						this.candidatesObj.put(reviewer, myRev);

					} else {

						// considers only frequency
						/*
						 * if(simScore>0){ this.candidates.put(reviewer, 1.0); }
						 */
						// considers experience
						// this.candidates.put(reviewer, simScore);

						// consider PR recency
						double recency = prRecencyMap.get(key);
						this.candidates.put(reviewer, simScore * recency);

						PRReviewer myRev = new PRReviewer();
						myRev.login = reviewer;
						myRev.libSimScore = libSimScore * recency;
						myRev.techSimScore = techSimScore * recency;
						myRev.miscSimScore = miscSimScore * recency;
						myRev.totalScore = simScore * recency;

						// storing the experience tokens
						// myRev.expertTokenList.addAll(Arrays.asList(tokenlist
						// .split("\\s+")));

						this.candidatesObj.put(reviewer, myRev);
					}
				}
			}
		}

		// calculating the overall experience
		// this.calculateOverallExperienceScore();

	}

	protected ArrayList<String> collectCrossProjectRevRank() {
		// collect developer ranks based on cross project experience

		// VA tokens in target PR
		String targetLibTokens = collectVALibTokens(targetTokenList);

		HashMap<String, Double> libcandidates = new HashMap<>();

		for (int key : this.prTokenMap.keySet()) {
			String tokenList = this.prTokenMap.get(key);
			String libtokens = collectVALibTokens(tokenList);
			CosineSimilarityMeasure cosmeasure = new CosineSimilarityMeasure(
					targetLibTokens, libtokens);
			double simScore = cosmeasure.get_cosine_similarity_score(true);
			if (pastReviewers.containsKey(key)) {
				ArrayList<String> revlist = pastReviewers.get(key);
				for (String reviewer : revlist) {
					if (libcandidates.containsKey(reviewer)) {
						double score = libcandidates.get(reviewer);
						score += simScore;
						libcandidates.put(reviewer, score);
					} else {
						libcandidates.put(reviewer, simScore);
					}
				}
			}
		}
		// now rank the lib-candidates
		ArrayList<String> ranked = rankCodeReviewers(libcandidates);
		return ranked;
	}

	protected ArrayList<String> collectTechRevRank() {
		// collect developer ranks based on cross project experience

		// VA tokens in target PR
		String targetTechTokens = collectTechTokens(targetTokenList);

		HashMap<String, Double> techcandidates = new HashMap<>();

		for (int key : this.prTokenMap.keySet()) {
			String tokenList = this.prTokenMap.get(key);
			String techtokens = collectTechTokens(tokenList);
			CosineSimilarityMeasure cosmeasure = new CosineSimilarityMeasure(
					targetTechTokens, techtokens);
			double simScore = cosmeasure.get_cosine_similarity_score(true);
			if (pastReviewers.containsKey(key)) {
				ArrayList<String> revlist = pastReviewers.get(key);
				for (String reviewer : revlist) {
					if (techcandidates.containsKey(reviewer)) {
						double score = techcandidates.get(reviewer);
						score += simScore;
						techcandidates.put(reviewer, score);
					} else {
						techcandidates.put(reviewer, simScore);
					}
				}
			}
		}
		// now rank the tech candidates
		ArrayList<String> ranked = rankCodeReviewers(techcandidates);
		return ranked;
	}

	protected ArrayList<String> getRankedReviewers() {
		// collecting ranked reviewers
		this.collectReviewerScores();
		return this.rankCodeReviewers();
	}

	protected ArrayList<PRReviewer> getRankedPRReviewers(
			ArrayList<String> valibs, String currentLogin) {
		// collecting ranked reviewers
		this.collectReviewerScores(valibs);
		this.discardCurrentLogin(currentLogin);
		this.normalizeLibNTechScoresAbs();
		return this.rankPRCodeReviewers();
	}

	protected void discardCurrentLogin(String currentLogin) {
		// discard current login from analysis
		if (this.candidates.containsKey(currentLogin)) {
			this.candidates.remove(currentLogin);
			this.candidatesObj.remove(currentLogin);
		}
	}

	protected ArrayList<String> getRankedReviewersByLib() {
		return collectCrossProjectRevRank();
	}

	protected ArrayList<String> getRankedReviewersByTech() {
		return collectTechRevRank();
	}

	protected ArrayList<String> getRankedReviewersBy2Ranks() {
		return collectNCombineRanks();
	}

	protected ArrayList<String> rankCodeReviewers(
			HashMap<String, Double> candidates) {
		List<Entry<String, Double>> list = new LinkedList<>(
				candidates.entrySet());
		Collections.sort(list, new Comparator<Entry<String, Double>>() {
			@Override
			public int compare(Entry<String, Double> o1,
					Entry<String, Double> o2) {
				// TODO Auto-generated method stub
				Double v2 = o2.getValue();
				Double v1 = o1.getValue();
				return v2.compareTo(v1);
			}
		});
		ArrayList<String> ranked = new ArrayList<>();
		for (Entry<String, Double> entry : list) {
			ranked.add(entry.getKey());
		}
		return ranked;
	}

	protected ArrayList<String> rankCodeReviewers() {
		// rank the code reviewers
		List<Entry<String, Double>> list = new LinkedList<>(
				this.candidates.entrySet());
		Collections.sort(list, new Comparator<Entry<String, Double>>() {
			@Override
			public int compare(Entry<String, Double> o1,
					Entry<String, Double> o2) {
				// TODO Auto-generated method stub
				Double v2 = o2.getValue();
				Double v1 = o1.getValue();
				return v2.compareTo(v1);
			}
		});
		ArrayList<String> ranked = new ArrayList<>();
		for (Entry<String, Double> entry : list) {
			ranked.add(entry.getKey());
			//System.out.println(entry.getKey() + " " + entry.getValue());
		}
		return ranked;
	}

	protected void normalizeLibNTechScores() {
		// normalizing the lib and tech scores
		double maxLibScore = 0;
		double maxTechScore = 0;
		double maxMiscScore = 0;
		double maxTotalScore = 0;

		// collect max scores
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
		// now normalize the scores.
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
		// normalizing the lib and tech scores
		double maxLibScore = 0;
		double maxTechScore = 0;
		double maxMiscScore = 0;
		double maxTotalScore = 0;

		// collect max scores
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

		// now normalize the scores.
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

	protected ArrayList<PRReviewer> rankPRCodeReviewers() {
		// rank the code reviewers
		List<Entry<String, Double>> list = new LinkedList<>(
				this.candidates.entrySet());
		Collections.sort(list, new Comparator<Entry<String, Double>>() {
			@Override
			public int compare(Entry<String, Double> o1,
					Entry<String, Double> o2) {
				// TODO Auto-generated method stub
				Double v2 = o2.getValue();
				Double v1 = o1.getValue();
				return v2.compareTo(v1);
			}
		});
		ArrayList<PRReviewer> ranked = new ArrayList<>();
		for (Entry<String, Double> entry : list) {
			ranked.add(this.candidatesObj.get(entry.getKey()));
			// System.out.println(entry.getKey() + " " + entry.getValue());
		}
		return ranked;
	}
}
