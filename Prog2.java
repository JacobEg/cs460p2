/**
 * @author Jacob Egestad & Cade Marks
 * File: Prog2.java
 * Course name: CSC 460 - Database Design
 * Assignment Title: Program #2: Extendible Hashing
 * Instructor: Lester McCann
 * TAs: Haris Riaz, Aayush Pinto
 * Due Date: February 9, 2022
 * 
 * Description: 
 * 
 * Known deficiencies: N/A
 * 
 * Requirements: Java 16
 */

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class Prog2 {
	// some constants and class values copied from Jacob Egestad's Program 1
	private static final int STRING_FIELDS = 9; // number of string fields
	private static final int INT_FIELDS = 4; // number of int fields
	private static final int INT_FIELDS_LENGTHS = INT_FIELDS * Integer.BYTES; // length of int fields
	private static final int ENTRIES_PER_BUCKET = 50; // number of entries per bucket
	private static int[] stringFieldLengths = new int[STRING_FIELDS]; // lengths of the string fields
	private static int projectSize = -1; // size of projects in bytes
	private static int bucketSize = -1; // size of bucket in bytes
	private static int numProjects = -1; // number of projects
	public static int entrySize = -1;

	public static void main(String[] args){
		if(args.length == 0) {
			printErrAndExit("Prog2 <path/to/binary_file>");
		}
		File inputFile = new File(args[0]); // database file created in Part 1
		if(!inputFile.isFile()){
			printErrAndExit("Prog2 <path/to/binary_file>");
		}
		File hashBucketFile = new File("HashBucket.bin"); // hash bucket file to be used in RAF
		try {
			hashBucketFile.delete();
			hashBucketFile.createNewFile();
			RandomAccessFile inputRAF = new RandomAccessFile(inputFile, "r"); // RAF for working with DB file
			RandomAccessFile hashBucketRAF = new RandomAccessFile(hashBucketFile, "rw"); // RAF for working with Hash Bucket file
			processDatabase(inputRAF, hashBucketRAF);
			inputRAF.close();
			hashBucketRAF.close();
		} catch (Exception e) {
			e.printStackTrace();
			printErrAndExit("Can't access " + args[0]);
		}
	}

	/**
	 * 
	 * @param inputRAF
	 * @param hashBucketRAF
	 */
	public static void processDatabase(RandomAccessFile inputRAF, RandomAccessFile hashBucketRAF){
		try{
			hashBucketRAF.write(new byte[bucketSize * 10]); // write initial size to HashBucket file
			long location = 0; //
			for(int i = 0; i < numProjects; i++){
				String projectID = readProjectValues(inputRAF, location)[0];
				// insert into HashBucket here
				location += projectSize;
			}
		} catch (Exception exception){
			exception.printStackTrace();
			printErrAndExit("Error with I/O around DB File or Hashbucket File");
		}
	}

	/**
	 * 
	 * @param inputRAF
	 * @param location
	 * @return
	 * @throws IOException
	 */
	public static String[] readProjectValues(RandomAccessFile inputRAF, long location) throws IOException{
		String[] projectFields = new String[STRING_FIELDS + INT_FIELDS]; // project represented as an arr of strings
		byte[] project = new byte[projectSize]; // project represented as an array of bytes
		inputRAF.seek(location);
		inputRAF.read(project);
		int i; //index to be used in looping
		int startIndex = 0;
		for(i = 0; i < STRING_FIELDS; i++){
			projectFields[i] = bytesToString(Arrays.copyOfRange(project, startIndex, startIndex + stringFieldLengths[i]));
			startIndex += stringFieldLengths[i];
		}
		for(; i < STRING_FIELDS + INT_FIELDS; i++){
			projectFields[i] = "" + bytesToInt(Arrays.copyOfRange(project, startIndex, startIndex + Integer.BYTES));
			startIndex += Integer.BYTES;
		}
		return projectFields;
	}

	/**
	 * idToKey: Takes a String Project ID and converts it into an integer key. It does this by
	 * reversing the String and appending the least significant digit of the ASCII value of
	 * each character in the String.
	 * Pre-condition: The binary file is being read.
	 * Post-condition: N/A
	 * @param id the Project ID for a project
	 * @return That project id integerized represented as a string
	 */
	public static String idToKey(String id){
		String key = ""; // key to be converted to an integer for the key
		for(int i = id.length() - 1; i >= 0; i--){
			key += ((int) id.charAt(i)) % 10;
		}
		return key;
	}
	
	/**
	 * fillFieldLengths: fills stringFieldLengths array with lengths of each of the string fields of
	 * the projects in inputRAF, as well as projectSize, bucketSize, entrySize and numProjects
	 * Pre-conditions: inputRAF is an existing, properly formatted binary file
	 * Post-conditions: stringFieldLengths, projectSize, numProjects are filled with values
	 * from end of file inputRAF
	 * Note: Copied from Jacob Egestad's Program 1
	 * @param inputRAF RandomAccessFile for dealing with binary file from user
	 * @return void
	 */
	public static void fillFieldLengths(RandomAccessFile inputRAF){
		byte[] lengths = new byte[STRING_FIELDS * Integer.BYTES]; // 9 lengths in a binary string
		long startOfLengths = -1; // to be used to mark the beginning of the string field lengths
		try {
			startOfLengths = inputRAF.length() - (STRING_FIELDS * Integer.BYTES);
			inputRAF.seek(startOfLengths);
			inputRAF.read(lengths);
			inputRAF.seek(0);
		} catch (Exception e) {
			printErrAndExit("Error reading input file");
		}
		projectSize = INT_FIELDS_LENGTHS; // 4*4 to start with
		for(int i = 0; i < STRING_FIELDS; i++){
			int j = i  * Integer.BYTES; // the index of the byte to be gotten from lengths to fill toInt
			byte[] toInt = new byte[Integer.BYTES]; // array representing int value of field i
			for(int k = 0; k < Integer.BYTES; k++){
				toInt[k] = lengths[j++];
			}
			stringFieldLengths[i] = bytesToInt(toInt);
			projectSize += stringFieldLengths[i];
			//System.out.println("Field " + i + " length: " + stringFieldLengths[i]);
		}
		numProjects = (int) (startOfLengths / projectSize);
		entrySize = stringFieldLengths[0] + Long.BYTES;
		bucketSize = entrySize * ENTRIES_PER_BUCKET;
	}

	/**
	 * bytesToString: Convert an array of bytes to a string
	 * Pre-condition: N/A
	 * Post-condition: N/A
	 * Note: Copied from Jacob Egestad's Program 1
	 * @param bytes string field length to be converted to string
	 * @return bytes but represented as a String
	 */
	public static String bytesToString(byte[] bytes){
		String result = ""; // string to be returned
		for(byte b : bytes){
			result += (char) b;
		}
		return result;
	}

	/**
	 * bytesToInt: converts an array of bytes to an int
	 * Pre-condition: bytes is of length 4
	 * Post-condition: N/A
	 * Note: Copied from Jacob Egestad's Program1
	 * @param bytes array of bytes representing a tring
	 * @return int value of the the bytes
	 */
	public static int bytesToInt(byte[] bytes){
		return ByteBuffer.wrap(bytes).getInt();
	}

	/**
	 * printErrAndExit: prints provided string to stderr and exits with error code 1
	 * Pre-conditions: The user has given bad input by not including a valid command line argument
	 * Post-conditions: This method exits the program
	 * Note: copied from Jacob Egestad's Program 1
	 * @param message string to print to stderr
	 * @return void
	 */
	public static void printErrAndExit(String message){
		System.err.println(message);
		System.exit(1);
	}

}
