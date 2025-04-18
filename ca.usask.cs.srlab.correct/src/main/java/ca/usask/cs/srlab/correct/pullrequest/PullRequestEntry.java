package ca.usask.cs.srlab.correct.pullrequest;

import java.util.ArrayList;
import java.util.HashMap;

public class PullRequestEntry {
	public int PRNumber;
	public ArrayList<String> changedFiles;
	public HashMap<String,String> changedFilesMap;
	public ArrayList<String> vaLibs;
	public ArrayList<String> technologies;
	public ArrayList<String> libTech;
	public ArrayList<String> codeReviewers;
}
