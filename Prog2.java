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
			fillFieldLengths(inputRAF);
			processDatabase(inputRAF, hashBucketRAF);
			inputRAF.close();
			hashBucketRAF.close();
		} catch (Exception e) {
			e.printStackTrace();
			printErrAndExit("Can't access " + args[0]);
		}
	}

	/**
	 * processDatabase: Reads the projects in the database, adding the project ID to the HashBucket file
	 * Pre-conditions: inputRAF points to a binary file that is the db file full of projects. fillFieldLengths
	 * has been called.
	 * Post-conditions: The HashBucket file has been filled with info from the db file.
	 * @param inputRAF RandomAccessFile for accessing the db file
	 * @param hashBucketRAF RandomAccessFile for accessing the hash bucket file
	 * @return void
	 */
	public static void processDatabase(RandomAccessFile inputRAF, RandomAccessFile hashBucketRAF){
		ExtendibleHashIndex extendibleHashIndex = new ExtendibleHashIndex(hashBucketRAF, inputRAF, bucketSize, "w");
		try{
			hashBucketRAF.write(new byte[bucketSize * 10]); // write initial size to HashBucket file
			long location = 0; // location (in bytes) of the start of the project
			for(int i = 0; i < numProjects; i++){
				String projectID = readProjectValues(inputRAF, location)[0];
				extendibleHashIndex.addEntry(projectID, location);
				location += projectSize;
			}
		} catch (Exception exception){
			exception.printStackTrace();
			printErrAndExit("Error with I/O around DB File or Hashbucket File");
		}
		extendibleHashIndex.printIndexInfo();
		extendibleHashIndex.writeDirectory();
	}

	/**
	 * readProjectValues: given a database file and a bye location, reads the Project into an array of Strings
	 * representing the values of the project.
	 * Pre-conditions: fillFieldLengths has been called. inputRAF is a binary file representing the db file of projects and
	 * is in read mode
	 * Post-conditions: file-pointer offset for inputRAF is set to location
	 * @param inputRAF RandomAccessFile pointing to the db file of projects
	 * @param location the byte # where the project starts
	 * @return Array of strings representing the fields in a project
	 * @throws IOException if inputRAF.seek or .read fails
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
	 * bytesToLong: convert array of bytes to a long
	 * Pre-condition: bytes is of length 8
	 * Post-condition: N/A
	 * @param bytes the long represented as an array of bytes
	 * @return the bytes array represented as a long
	 */
	public static long bytesToLong(byte[] bytes){
		return ByteBuffer.wrap(bytes).getLong();
	}

	/**
	 * longToBytes: converts a long into a length 8 array of bytes
	 * Pre-condition: N/A
	 * Post-condition: N/A
	 * @param longVal long to be converted to an array of bytes
	 * @return An array of bytes representing the long arg
	 */
	public static byte[] longToBytes(long longVal){
		return ByteBuffer.allocate(Long.BYTES).putLong(longVal).array();
	}

	/**
	 * intToBytes: converts an integer into a length 4 array of bytes
	 * Pre-condition: N/A
	 * Post-condition: N/A
	 * @param integer Integer to be converted to an array of bytes
	 * @return An array of bytes representing the integer arg
	 */
	public static byte[] intToBytes(int integer){
		return ByteBuffer.allocate(Integer.BYTES).putInt(integer).array();
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
