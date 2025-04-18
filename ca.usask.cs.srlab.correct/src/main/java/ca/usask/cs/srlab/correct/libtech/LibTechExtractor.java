package ca.usask.cs.srlab.correct.libtech;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import ca.usask.cs.srlab.correct.config.StaticData;
import ca.usask.cs.srlab.correct.utility.ContentLoader;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;


public class LibTechExtractor {

	String repoName;
	String repoFolder;
	HashMap<String, String> fileMap;
	HashMap<String, String> fileTokenMap;
	String outFile;

	public LibTechExtractor(String repoName) {
		this.repoName = repoName;
		this.repoFolder = StaticData.REPOSITORY + "/" + repoName;
		this.outFile = repoFolder + "/" + repoName + ".json";
		this.fileMap = new HashMap<>();
		this.fileTokenMap = new HashMap<>();
	}

	protected void collectPyFiles(String folder) {
		// browse through the repository
		File dir = new File(folder);
		if (dir.isDirectory()) {
			File[] files = dir.listFiles();
			for (File f : files) {
				if (f.isDirectory()) {
					collectPyFiles(f.getAbsolutePath());
				} else {
					String fileName = f.getAbsolutePath();
					if (fileName.endsWith(".py")) {
						if (!fileMap.containsKey(fileName)) {
							String filecontent = ContentLoader
									.loadFileContentSC(fileName);
							fileMap.put(fileName, filecontent);
						}
					}
				}
			}
		} else {
			if (folder.endsWith(".py")) {
				if (!fileMap.containsKey(folder)) {
					String filecontent = ContentLoader
							.loadFileContentSC(folder);
					fileMap.put(folder, filecontent);
				}
			}
		}
	}

	protected ArrayList<String> collectImports(String content) {
		// collecting import statements
		ArrayList<String> imports = new ArrayList<>();
		String[] allines = content.split("\n");
		for (String line : allines) {
			if (line.trim().startsWith("import")) {
				imports.add(line);
			} else if (line.trim().startsWith("from")) {
				imports.add(line);
			}
		}
		// returning import statements
		return imports;
	}

	protected ArrayList<String> extractLibTechTokens(ArrayList<String> imports) {
		// extracting lib-tech tokens
		ArrayList<String> libtech = new ArrayList<>();
		for (String stmt : imports) {
			if (stmt.startsWith("import")) {
				String[] parts = stmt.split("\\s+|,");
				for (int i = 1; i < parts.length; i++) {
					if (parts[i].equals("as"))
						continue;
					if (!parts[i].trim().isEmpty())
						libtech.add(parts[i]);
				}
			} else if (stmt.startsWith("from")) {
				String[] parts = stmt.split("\\s+|,");
				for (int i = 1; i < parts.length; i++) {
					if (parts[i].contains("import"))
						continue;
					if (parts[i].equals("as"))
						continue;
					if (!parts[i].trim().isEmpty())
						libtech.add(parts[i]);
				}
			}
		}
		return libtech;
	}

	protected void collectLibTechTokens() {
		// collect file & source code
		this.collectPyFiles(this.repoFolder);
		// collecting library technology tokens from the source code
		for (String fileUri : this.fileMap.keySet()) {
			String pageContent = this.fileMap.get(fileUri);
			ArrayList<String> importStmts = collectImports(pageContent);
			ArrayList<String> tokens = extractLibTechTokens(importStmts);
			String tokencoll = new String();
			for (String token : tokens) {
				tokencoll += token + " ";
			}
			if (!this.fileTokenMap.containsKey(fileUri)) {
				this.fileTokenMap.put(fileUri, tokencoll);
			}
		}
		// now save the tokens
		this.saveLibTechTokens();

	}

	protected String reformURL(String fileUri) {
		// reformat the URL of file path
		int index = fileUri.indexOf(repoName);
		index = index + repoName.length()+1;
		String filename=fileUri.substring(index);
		filename=filename.replace("\\","/");
		return filename;
	}

	protected void saveLibTechTokens() {
		// now save the extracted tokens
		JSONArray items = new JSONArray();
		for (String fileUri : fileTokenMap.keySet()) {
			String key = reformURL(fileUri);
			String tokens=fileTokenMap.get(fileUri);
			
			JSONObject jobj = new JSONObject();
			
			jobj.put("file", key);
			jobj.put("token", tokens);
			
			items.add(jobj);
		}
		// now save the JSON array
		try {
			FileWriter fwriter = new FileWriter(new File(outFile));
			fwriter.write(items.toJSONString());
			fwriter.close();
			System.out.println("Lib Tech Tokens saved to " + outFile);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		String repoName = "VA-vtest";
		LibTechExtractor extractor = new LibTechExtractor(repoName);
		extractor.collectLibTechTokens();
	}
}
