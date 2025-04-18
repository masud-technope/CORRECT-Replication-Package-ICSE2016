package ca.usask.cs.srlab.correct.utility;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Scanner;

public class ContentLoader {

	public static String loadFileContent(String fileName) {
		// code for loading the file name
		String fileContent = new String();
		try {
			File f = new File(fileName);
			if (!f.exists())
				return "";
			BufferedReader bufferedReader = new BufferedReader(
					new FileReader(f));
			while (bufferedReader.ready()) {
				String line = bufferedReader.readLine();
				fileContent += line + "\n";
			}
			bufferedReader.close();
		} catch (Exception ex) {
			// handle the exception
		}
		return fileContent;
	}

	public static String[] getAllLines(String fileName) {
		String content = loadFileContentSC(fileName);
		String[] lines = content.split("\n");
		return lines;
	}
	
	public static ArrayList<String> getAllLinesOptList(String fileName) {
		ArrayList<String> lines = new ArrayList<>();
		try {
			File f = new File(fileName);
			BufferedReader bufferedReader = new BufferedReader(
					new FileReader(f));
			while (bufferedReader.ready()) {
				String line = bufferedReader.readLine().trim();
				lines.add(line);
			}
			bufferedReader.close();
		} catch (Exception ex) {
			// handle the exception
		}
		return lines;
	}
	 

	public static String[] getAllTokens(String fileName) {
		String content = loadFileContentSC(fileName);
		String[] tokens = content.split("\\s+");
		return tokens;
	}
	
	public static String loadFileContentSC(String fileName) {
		// loading content from a file
		String content = new String();
		try {
			Scanner scanner = new Scanner(new File(fileName));
			while (scanner.hasNext()) {
				String line = scanner.nextLine();
				content += line + "\n";
			}
			scanner.close();
		} catch (Exception exc) {
			exc.printStackTrace();
		}
		return content;
	}

	public static String downloadPage(String pageURL) {
		String content = new String();
		try {
			URL u = new URL(pageURL);
			HttpURLConnection connection = (HttpURLConnection) u
					.openConnection();
			connection.setConnectTimeout(2000); // setting connect time out
			connection.setReadTimeout(2000); // setting read time out
			connection.addRequestProperty("Content-Type",
					"text/html;charset=utf-8");
			connection.setRequestMethod("GET");
			if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
				BufferedReader br = new BufferedReader(new InputStreamReader(
						connection.getInputStream()));
				String line = null;
				while ((line = br.readLine()) != null) {
					content += line + "\n";
				}
			}
		} catch (Exception exc) {
			exc.printStackTrace();
		}
		return content;
	}
}
