import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import org.apache.commons.*;

/*
 * This is the main driver class of this project and should be used to run the test cases.
 */
public class runTrial {
	
	/*
	 * This is a static method that can be used in other classes as well to count the number of rows in a file
	 * The return is an integer number of rows, or in the case the file can not be found -1
	 */
	public static int countRows(String fileName)
	{
		Scanner reader = null;
		try {
			reader = new Scanner(new File(fileName));
		}
		catch( FileNotFoundException e)
		{
			System.out.println("The file: " + fileName + " was not found");
			return -1; // if the file is not found return -1
		}
		
		int numRows = 0; // count number of rows in the file
		while (reader.hasNextLine())
		{
			reader.nextLine();
			numRows++;
		}
		reader.close(); 
		return numRows;
	}
	
	/*
	 * This method takes a file name as input and outputs 4 separate files after partitioning the original
	 * If the number of rows is not divisible by 4 then extra lines are written to the fourth file
	 */
	public static void splitFile(String fileName, int totalRows)
	{
		int rowsPerFile = totalRows/4;
		long maxFileSize = 10000000; // maximum file size is 10 MB
		StringBuilder buffer = new StringBuilder((int)(maxFileSize)); // set string size capacity equal to 10 MB
		String subFileName;
		List<String> rows = null;
		
		try 
		{
			rows = Files.readAllLines(new File(fileName).toPath(), Charset.defaultCharset() );
		}
		catch(IOException e)
		{
			System.out.println("The file was not able to be opened");
		}
		int fileNum =  1;
		int currentRows = 0;
		for (String row: rows)
		{
			buffer.append(row + "\n"); // add row to buffer to be written
			currentRows++; // increment current number of rows
			if (currentRows >= rowsPerFile) // once there are rowsPerFile amount of rows in the file increment the file number
			{
				subFileName = fileName + fileNum;
				File subFile = new File(subFileName); // ex: ./A_test -> ./A_test1 
				try (PrintWriter writer = new PrintWriter(subFile)) 
				{
		              writer.println(buffer.toString().substring(0, (buffer.toString().length() - 1))); 
		              // note: we want to not write the last new line so taking the above substring
		              // excludes the last '\n' character
		        }
				catch(FileNotFoundException e)
				{
					System.out.println("The file was not able to be found");
				}
				if (fileNum < 4)
					fileNum++; // wrote to subfile so increment file number
				buffer = new StringBuilder(); // reset buffer to empty string
				currentRows = 0;
			}
		}
		
		// write the remaining rows to the last file
		// note: this will be 0 - 3 extra lines with 4 subfiles
		
		subFileName = fileName + fileNum; // note: this will be to the subfile with suffix "4"
		File subFile = new File(subFileName);
		try (FileWriter writer = new FileWriter(subFile, true)) // this allows appending to the existing file
		{
            writer.write(buffer.toString());
		}
		catch(IOException e)
		{
			System.out.println("The file was not able to be written");
		}
	}	
	
	/*
	 * This method is used to remove the additional subfiles created from partitioning the original
	 * after the result has been calculated.
	 */
	public static void removeSubFiles(String fileName)
	{
		for (int i = 1 ; i <= 4; i++)
		{
			try
			{
				Files.delete(Paths.get(fileName + i));
			}
			catch(IOException e)
			{
				System.out.println("Unable to delete files");
			}

		}
	}

	public static void main(String[] args) throws InterruptedException {
		Scanner in = new Scanner(System.in);
		System.out.println("Please enter the path of the file to be checked");
		String fileName = in.nextLine(); // read file to be checked by user
		in.close();
		CSVChecker csvChecker = new CSVChecker();
		// check how many rows are in the file
		int numRows = countRows(fileName);
		String result = null;
		// if number of rows is < 100 no need for multithreading, otherwise multithread
		// this amount is arbitrarily picked but the idea is that this threshold should be
		// wherever the overhead from creating and running the threads is less than the
		// improvement gained from parallel processing
		
		if (numRows < 100) // no need for multithreading
		{
			result = csvChecker.fileChecker(fileName, 0);
		}
		else // split file into 4 subfiles and run each one on its own thread
		{
			splitFile(fileName, numRows);
			int rowsPerFile = numRows/4;
			runTask task1 = new runTask(fileName + "1", 0);
			runTask task2 = new runTask(fileName + "2", rowsPerFile);
			runTask task3 = new runTask(fileName + "3", 2 * rowsPerFile);
			runTask task4 = new runTask(fileName + "4", 3 * rowsPerFile);
			
			Thread t1 = new Thread(task1);
			Thread t2 = new Thread(task2);
			Thread t3 = new Thread(task3);
			Thread t4 = new Thread(task4);
			
			// start threads
			t1.start();
			t2.start();
			t3.start();
			t4.start();
			
			// have main method wait for threads to finish
			t1.join();
			t2.join();
			t3.join();
			t4.join();

			
			// compile result 
			result = "";
			result += task1.getResult();
			result += task2.getResult();
			result += task3.getResult();
			result += task4.getResult();
			
			removeSubFiles(fileName);
			
		}
		
		
		if (result == null)
		{
			//System.out.println("An error occurred when checking the file");
			return;
		}
		//System.out.println(result);
		if ((result.trim()).equals(""))
		{
			System.out.println("No errors found");
			return;
		}
		
		// write errors to separate file 
		File file = new File("./" + CSVChecker.resolveFilePath(fileName) + ".error");
		try
		{
			FileOutputStream fileStream = new FileOutputStream (file);
			BufferedOutputStream bufferStream = new BufferedOutputStream(fileStream);
			byte[] bytes = result.getBytes(); // convert string to bytes to be written
			bufferStream.write(bytes);
			bufferStream.close();
			fileStream.close();
			System.out.println("written to file successfully");
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
		

	}

}
