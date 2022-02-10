/**
 * @author Jacob Egestad & Cade Marks
 * File: Prog2.java
 * Course name: CSC 460 - Database Design
 * Assignment Title: Program #2: Extendible Hashing
 * Instructor: Lester McCann
 * TAs: Haris Riaz, Aayush Pinto
 * Due Date: February 9, 2022
 * 
 * Description: Uses the binary db file generated from Jacob Egestad's Program 1 and constructs a file
 * HashBucket.bin that acts as an index file for that db. Creates an ExtendibleHashIndex obj to manage
 * the db and index file creation. Calls addEntry on every project id and byte address for every entry to be added to the index.
 * After the db file is done being processed, calls printIndexInfo to print the info about the index,
 * and then writes the directory to the end of the file.
 * 
 * Known deficiencies: N/A
 * 
 * Requirements: Java 16
 */

import java.io.File;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;

public class Prog2 {

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
		} catch (Exception exception) {
			exception.printStackTrace();
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
		// ExtendibleHashIndex object for managing index file
		ExtendibleHashIndex extendibleHashIndex = new ExtendibleHashIndex(hashBucketRAF, inputRAF, "w");
		try{
			hashBucketRAF.write(new byte[extendibleHashIndex.getBucketSize() * 10]); // write initial size to HashBucket file
			long location = 0; // location (in bytes) of the start of the project
			for(int i = 0; i < extendibleHashIndex.getNumProjects(); i++){
				String projectID = extendibleHashIndex.readProjectValues(location)[0]; // project id value
				extendibleHashIndex.addEntry(projectID, location);
				location += extendibleHashIndex.getProjectSize();
			}
		} catch (Exception exception){
			exception.printStackTrace();
			printErrAndExit("Error with I/O around DB File or Hashbucket File");
		}
		extendibleHashIndex.printIndexInfo();
		extendibleHashIndex.writeDirectory();
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
