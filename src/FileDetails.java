import java.io.File;

public class FileDetails {
	public int lines;
	public String content;
	public File directory;
	
	public FileDetails(int lines, String content, File directory) {
		this.lines = lines;
		this.content = content;
		this.directory = directory;
	}
}
