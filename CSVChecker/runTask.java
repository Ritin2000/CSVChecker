/*
 * This class implements a worker thread function which returns the error report from a specified file
 * in a string format
 */
public class runTask implements Runnable {
	private String result;
	private String fileName;
	private int startRow;
	public runTask(String fileName, int startRow) // constructor 
	{
		this.result = ""; 
		this.fileName = fileName;
		this.startRow = startRow;
	}
	
	public void run()
	{
		CSVChecker csvChecker = new CSVChecker();
		result = csvChecker.fileChecker(fileName, startRow);
	}
	
	public String getResult()
	{
		return result;
	}
	
}
