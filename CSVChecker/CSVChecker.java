import java.io.*;
import java.util.List;
import java.util.Scanner;
import com.opencsv.*;
import com.opencsv.exceptions.CsvException;
/*
 * This class implements the CSVChecker object which contains the methods relevant to checking a file 
 * to determine whether it has the proper format based on a specification file, and to return a string
 * consisting of all mismatches found.
 */
public class CSVChecker {
	
	/*
	 * This method is given a file and the file is checked for column type mismatches and the error message 
	 * is returned as a string
	 */
	public String fileChecker(String fileName, int startRow)
	{
		// use specs file to get current column format in an array
		String[] parameters;
		String errors = "";
		if ((parameters = sanityParameters(fileName)) == null )
		{
			System.out.println("Error with specificiation file");
			return null;
		}
		
		try {
			FileReader fileReader = new FileReader(fileName);
			CSVParser csvParser = new CSVParserBuilder().withSeparator('|').build();
			CSVReader csvReader = new CSVReaderBuilder(fileReader).withCSVParser(csvParser).build();
			
			// use CSVReader to read file row by row
			String[] row;
			int rowNum = startRow;
			while ((row = csvReader.readNext()) != null) // assign row to next line in the file
			{
				rowNum++;
				errors += rowCheck(row, parameters, rowNum); // go through each row and append errors in each to a string
			}
			fileReader.close();
			return errors;
		}
		catch(Exception e)
		{
			System.out.println("An error occurred when checking the file");
			return null;
		}
		
		
	}

	
	/*
	 * This method takes a file name, and based on the first character of the file returns the parameters
	 * in a string array format
	 */
	public String[] sanityParameters (String fileName)
	{
		char key = resolveFilePath(fileName).charAt(0); // the first letter of the file name acts as a key
		
		try {
			FileReader fileReader = new FileReader("./specs"); // read through "specs.csv" to find parameters
			CSVParser csvParser = new CSVParserBuilder().withSeparator('|').build();
			CSVReader csvReader = new CSVReaderBuilder(fileReader).withCSVParser(csvParser).build();
			String[] line;
			
			// assigns line to next line in the file and only continues the loop while this is not null
			while ((line = csvReader.readNext()) != null) 
			{
				//printLine(line);
				if (line[0].charAt(0) == key) // matching line has been found
				{
					if(line.length > 2) // first two entries are file prefix and number of columns
					{
						int numCol = Integer.parseInt(line[1]);
						String[] result = new String[numCol];
						int count = 0;
						int index = 2;
						while (count < numCol)
						{
							result[count] = line[index];
							count++;
							index++;
						}
						fileReader.close();
						return result;
					}
				}
					
			}
			fileReader.close();
			return null;
		}
		catch (Exception e)
		{
			System.out.println("An exception occured");
			return null;
		}
		
	}
	/*
	 * This method resolves the file path of a file and returns just the file name without the rest of the path
	 */
	public static String resolveFilePath (String fileName) 
	{
		int index = fileName.lastIndexOf("/");
		return fileName.substring(index + 1);
	}
	
	/*
	 * This method is used to format a string array when printed out for testing purposes
	 */
	public void printLine(String[] line)
	{
		for (int i = 0; i < line.length; i++)
		{
			System.out.print(line[i] + "\t");
		}
	}
	
	/*
	 * This method iterates through the columns of a row and generates an error message if any
	 * type mismatches are found
	 */
	public String rowCheck(String[] row, String[] parameters, int rowNum)
	{
		String error = "";
		if (row.length != parameters.length) // check if the row has the correct number of columns
		{
			error += "Row " + rowNum + " has an incorrect number of columns\n";
			return error;
		}
		int blank = 0;
		for (int i = 0; i < row.length; i++) // go through each column and check if it matches
		{
			String status = "";
			status = typeCheck(row[i], parameters[i], rowNum, (i+1));
			if(status.equals("Blank"))
			{
				blank++;
			}
			if (!status.equals("Type match") && !status.equals("Blank")) // if the type does not match the column type
			{
				error += status;
			}	
		}
		if (blank == parameters.length) // entire row is empty 
			return "Row " + rowNum +" is empty\n";
		return error;
		
	}
	
	/*
	 * This method checks if the value at the specified cell matches the expected type
	 * Note: Still need to account for if a cell is empty if it is valid or not (think it's fair to say it is)
	 */
	public String typeCheck(String observed, String expected, int rowNum, int colNum)
	{
		if(observed.isBlank()) // empty cell
		{
			return "Blank";
		}
		if(expected.equals("String")) // not sure if there are any invalid strings yet
		{
			return "Type match";
		}
		else if (expected.equals("int")) // check if it is an integer
		{
			try
			{
				Integer.parseInt(observed.trim());
			}
			catch (NumberFormatException e)
			{
				return "Expected integer at row " + rowNum + " column " + colNum + "\n"; 
			}
		}
		else if (expected.equals("char")) // check if it is a character
		{ 
			// check if character is a letter (only one character long too)
			if ( !Character.isLetter(observed.trim().charAt(0)) || observed.trim().length() != 1 )
			{
				return "Expected letter at row " + rowNum + " column " + colNum + "\n";
			}
		}
		
		return "Type match";
		
	}
	
}
