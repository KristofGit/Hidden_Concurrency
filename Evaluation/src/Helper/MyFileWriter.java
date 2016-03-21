package Helper;

import java.io.FileWriter;
import java.io.IOException;

import sun.org.mozilla.javascript.tools.shell.Environment;

public class MyFileWriter {

	FileWriter writer;
	
	public MyFileWriter(String path)
	{
		try {
			writer = new FileWriter(path, true);
		} catch (IOException e) {
			e.printStackTrace();
		}		
	}
	
	
	public void writeNewLine(String line)
	{
		try {
			writer.write(line);
			writer.write(System.getProperty("line.separator"));
			writer.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
	}
	
}
