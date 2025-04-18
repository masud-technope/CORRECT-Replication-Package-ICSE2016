package ca.usask.cs.srlab.correct.core;

import java.util.ArrayList;

public class PRObject {
	public int prNumber;
	public String requester;
	public ArrayList<String> commitSHAs=new ArrayList<>();
	public ArrayList<String> recommendedRevs=new ArrayList<>();
	public ArrayList<String> actualRevs=new ArrayList<>();
}
