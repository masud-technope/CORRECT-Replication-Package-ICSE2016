package ca.usask.cs.srlab.correct.utility;

import ca.usask.cs.srlab.correct.config.StaticData;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;

public class MiscUtility {

    public static ArrayList<String> getVALibs() {
        String[] repoNames = new File(StaticData.REPOSITORY).list();
        ArrayList<String> libs = new ArrayList<>();
        for (String repoName : repoNames) {
            if (repoName.startsWith("VA-")) {
                String libName = repoName.substring(3);
                libs.add(libName);
            }
        }
        return libs;
    }

    public static String list2Str(ArrayList<String> list) {
        String temp = "";
        for (String item : list) {
            temp += item + " ";
        }
        return temp.trim();
    }

    public static String set2Str(HashSet<String> list) {
        String temp = "";
        for (String item : list) {
            temp += item + " ";
        }
        return temp.trim();
    }


}
