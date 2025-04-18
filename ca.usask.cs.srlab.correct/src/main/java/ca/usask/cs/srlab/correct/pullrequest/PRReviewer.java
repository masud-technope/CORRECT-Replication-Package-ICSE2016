package ca.usask.cs.srlab.correct.pullrequest;

import java.util.ArrayList;
import java.util.HashSet;

public class PRReviewer {
    public String login;
    public double libSimScore;
    public double techSimScore;
    public double miscSimScore;
    public double totalScore;
    public HashSet<String> expertTokenList = new HashSet<>();

}
