import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

import javax.swing.JFileChooser;


public class Main {

	private String filename;
	
	public static void main(String[] args) {
		Main main = new Main();
		main.run();
	}
	
	public void run() {
		File file = getFile();
		
		if (file != null) {
			diffAllLikeFiles(file);
		} else {
			System.out.println("No file chosen. All done.");
		}
		// make sure the program fully terminates
		System.exit(0);
	}
	
	private void diffAllLikeFiles(File file) {
		this.filename = file.getName();
		File parent = file.getParentFile().getParentFile();

		File[] allSubdirs = parent.listFiles(f -> f.isDirectory());
		List<FileDetails> files = allFiles(allSubdirs);
		// get all but the last
		for (int indexLeft = 0; indexLeft < files.size()-1; indexLeft++) {
			
			FileDetails leftFile = files.get(indexLeft);
			// compare with the next file to the last file
			for (int indexRight = indexLeft + 1; indexRight < files.size(); indexRight++) {
				createDiff(leftFile, files.get(indexRight));
			}
		}
	}

	private void createDiff(FileDetails left, FileDetails right) {
		diff_match_patch diffChecker = new diff_match_patch();
		LinkedList<diff_match_patch.Diff> diffs = diffChecker.diff_main(left.content, right.content); 
		diffChecker.diff_cleanupSemantic(diffs);
		
		File rootDir = left.directory.getParentFile();
		double similar = calcSimilarity(diffs, left.lines) * 100;
		String filename = String.format("(%2.0f%%) %s-%s-%s.html", 
				similar, left.directory.getName(), right.directory.getName(), 
				this.filename);
		File differencesFile = Paths.get(rootDir.getAbsolutePath(), filename).toFile();
		
		try {
			writeDiffContentToFile(diffChecker, diffs, differencesFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private double calcSimilarity(LinkedList<diff_match_patch.Diff> diffs, int lines) {
		double same = 0;
		for (diff_match_patch.Diff diff : diffs) {
			if (diff.operation == diff_match_patch.Operation.EQUAL) {
				same += diff.getLineCount();
			}
		}
		return same / lines;
	}

	/**
	 * Writes the differences to the differencesFile.
	 * @throws IOException If there is any error when opening or writing to the file, an IOException is thrown.
	 */
	private void writeDiffContentToFile(diff_match_patch diffChecker, 
			LinkedList<diff_match_patch.Diff> diffs, 
			File differencesFile) throws IOException {
		
		differencesFile.createNewFile();
		
		BufferedWriter diffWriter = new BufferedWriter(new FileWriter(differencesFile));
		diffWriter.write(diffChecker.diff_prettyHtml(diffs));
		diffWriter.close();
	}

	private List<FileDetails> allFiles(File[] subdirs) {
		List<FileDetails> files = new ArrayList<FileDetails>();
		for (File subdir : subdirs) {
			File[] subdirFiles = subdir.listFiles(f -> 
				f.isFile() && f.getName().equalsIgnoreCase(filename)
			);
			if (subdirFiles.length == 0) {
				System.out.printf("No file with name '%s' for student '%s'\n", filename, subdir.getName());
			} else {
				files.add(readFile(subdirFiles[0]));
			}
		}
		return files;
	}
	
	private FileDetails readFile(File file) {
		// read the entire file into a single string
		StringBuilder contents = new StringBuilder();
		int lines = 0;
		try {
			Scanner s = new Scanner(file);
			while (s.hasNextLine()) {
				contents.append(s.nextLine());
				contents.append("\n");
				lines++;
			}
			s.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return new FileDetails(lines, contents.toString(), file.getParentFile());
	}


	private File getFile() {
		JFileChooser chooser = new JFileChooser();
		chooser.setDialogTitle("Pick any file of any student");
	    chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		int retValue = chooser.showDialog(null, "Select");
		if (retValue == JFileChooser.APPROVE_OPTION) {
			return chooser.getSelectedFile();
		}
		return null;
	}

}
