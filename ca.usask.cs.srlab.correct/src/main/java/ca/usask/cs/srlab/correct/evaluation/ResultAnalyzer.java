package ca.usask.cs.srlab.correct.evaluation;

import ca.usask.cs.srlab.correct.utility.ContentLoader;

import java.util.ArrayList;
import java.util.HashMap;

public class ResultAnalyzer {

	ArrayList<Double> accuracy;
	ArrayList<Double> precisions;
	ArrayList<Double> recalls;
	ArrayList<Double> recranks;
	//String EXPDIR = "C:/MyWorks/Thesis Works/Crowdsource_Knowledge_Base/CoRRec/Experiment";
	String EXPDIR = "C:/My MSc/ThesisWorks/Crowdsource_Knowledge_Base/CoRRec/Experiment";

	public ResultAnalyzer() {
		// default constructor
		this.accuracy = new ArrayList<>();
		this.precisions = new ArrayList<>();
		this.recalls = new ArrayList<>();
		this.recranks = new ArrayList<>();
	}

	protected void compareResults() {
		String correct = this.EXPDIR + "/result-nov-5-1.txt";
		String pick = this.EXPDIR + "/pick.txt";
		String[] clines = ContentLoader.getAllLines(correct);
		String[] plines = ContentLoader.getAllLines(pick);
		HashMap<String, Double> correctAcc = new HashMap<>();
		HashMap<String, Double> pickAcc = new HashMap<>();
		HashMap<String, Integer> correctTPR = new HashMap<>();
		HashMap<String, Integer> pickTPR = new HashMap<>();

		boolean repostarted = false;
		String lastRepo = new String();

		// collecting info from correct

		for (String line : clines) {
			if (line.contains("Repo")) {
				String repo = line.split(":")[1].trim();
				repostarted = true;
				lastRepo = repo;
			} else if (line.contains("Total PR")) {
				int tprcount = Integer.parseInt(line.split(":")[1]);
				if (repostarted) {
					if (!correctTPR.containsKey(lastRepo)) {
						correctTPR.put(lastRepo, tprcount);
					}
				}
			} else if (line.contains("Accuracy")) {
				double acc = Double.parseDouble(line.split(":")[1]);
				if (repostarted) {
					if (!correctAcc.containsKey(lastRepo)) {
						correctAcc.put(lastRepo, acc);
					}
				}
				repostarted = false;
			} else if(line.contains("===="))break;
		}

		// correct info from pick
		repostarted=false;
		lastRepo=new String();
		for (String line : plines) {
			if (line.contains("repo")) {
				String repo = line.split(":")[1].trim();
				repostarted = true;
				lastRepo = repo;
			} else if (line.contains("Total PR")) {
				int tprcount = Integer.parseInt(line.split(":")[1]);
				if (repostarted) {
					if (!pickTPR.containsKey(lastRepo)) {
						pickTPR.put(lastRepo, tprcount);
					}
				}
			} else if (line.contains("Accuracy")) {
				double acc = Double.parseDouble(line.split(":")[1]);
				if (repostarted) {
					if (!pickAcc.containsKey(lastRepo)) {
						pickAcc.put(lastRepo, acc);
					}
				}
				repostarted = false;
			}
		}

		// now show the comparison
		double sum_acc_correct=0;
		double sum_acc_pick=0;
		
		System.out.println("Repo\tCORRECT\tPICK");
		for (String repo : correctAcc.keySet()) {
			System.out.println(repo + "\t" + correctAcc.get(repo) + "\t"
					+ correctTPR.get(repo) + "\t" + pickAcc.get(repo) + "\t"
					+ pickTPR.get(repo));
			sum_acc_correct+=correctAcc.get(repo);
			if(pickAcc.containsKey(repo))
			sum_acc_pick+=pickAcc.get(repo);
		}
		System.out.println("Correct:"+ sum_acc_correct/correctAcc.size());
		System.out.println("Pick:"+ sum_acc_pick/pickAcc.size());

	}

	protected void analyzeResults() {
		// analyze the results
		String resFile = EXPDIR + "/correct-tse.txt";
		String[] lines = ContentLoader.getAllLines(resFile);

		double sum_acc = 0;
		double sum_prec = 0;
		double sum_rec = 0;
		double sum_rr = 0;

		int index = 2;

		for (String line : lines) {
			if (line.startsWith("Accuracy")) {
				double acc = Double.parseDouble(line.split(":")[1].trim());
				accuracy.add(acc);
				sum_acc += acc;
			}
			if (line.contains("Precision")) {
				double prec = Double.parseDouble(line.split(":")[1].trim());
				precisions.add(prec);
				sum_prec += prec;
			}
			if (line.contains("Recall")) {
				double rec = Double.parseDouble(line.split(":")[1].trim());
				recalls.add(rec);
				sum_rec += rec;
			}
			if (line.contains("RR")) {
				double rr = Double.parseDouble(line.split(":")[1].trim());
				recranks.add(rr);
				sum_rr += rr;
			}

			if (line.startsWith("===")) {
				break;
			}
		}

		System.out.println(accuracy);
		System.out.println(precisions);
		System.out.println(recalls);

		System.out.println("Mean accuracy:" + sum_acc / accuracy.size());
		System.out.println("Mean precision:" + sum_prec / precisions.size());
		System.out.println("Mean recall:" + sum_rec / recalls.size());
		System.out.println("Mean RR:" + sum_rr / recranks.size());
	}

	public static void main(String[] args) {
		ResultAnalyzer analyzer = new ResultAnalyzer();
		//analyzer.analyzeResults();
		analyzer.compareResults();
	}
}
