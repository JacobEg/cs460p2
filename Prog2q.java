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
import java.io.RandomAccessFile;
import java.util.Scanner;

public class Prog2q {
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
        if(args.length == 0){
            Prog2.printErrAndExit("Prog2q <path/to/binary_file>");
        }
        File inputFile = new File(args[0]);
        if(!inputFile.isFile()){
            System.err.println(args[0] + " either doesn't exist or isn't a file");
            Prog2.printErrAndExit("Prog2q <path/to/binary_file>");
        }
        File hashBucketFile = new File("HashBucket.bin");
        if(!hashBucketFile.isFile()){
            Prog2.printErrAndExit("HashBucket.bin either doesn't exist or isn't a file");
        }
        try{
            RandomAccessFile inputRAF = new RandomAccessFile(inputFile, "r");
            fillFieldLengths(inputRAF);
            RandomAccessFile hashBucketRAF = new RandomAccessFile(hashBucketFile, "r");
            queryUser(inputRAF, hashBucketRAF);
            inputRAF.close();
            hashBucketRAF.close();
        } catch(Exception exception){
            exception.printStackTrace();
            Prog2.printErrAndExit("Error accessing " + args[0] + " or HashBucket.bin");
        }
    }

    /**
     * queryUser: prompts the user to enter a project id suffix until they enter '-1'. All projects with that same
     * suffix are printed out using the HashBucket file generated in Prog2.
     * Pre-conditions: fillFieldLengths has been called, hashBucketRAF points to the index file created in Prog2,
     * inputRAF points to the db file created in Prog1.
     * Post-conditons: any matching projects are printed out
     * @param inputRAF binary db file of projects
     * @param hashBucketRAF binary index file of project ids and pointers to the db file
     * @return void
     */
    public static void queryUser(RandomAccessFile inputRAF, RandomAccessFile hashBucketRAF){
        ExtendibleHashIndex extendibleHashIndex = new ExtendibleHashIndex(hashBucketRAF, inputRAF, bucketSize, "r");
        Scanner userInput = new Scanner(System.in);
        System.out.println("Enter ProjectID suffix to search for (-1 to quit):");
        while(userInput.hasNextLine()){
            String currInput = userInput.nextLine().strip();
            if(currInput.equals("-1")){
                break;
            }
            extendibleHashIndex.printMatches(currInput);
            System.out.println("Enter ProjectID suffix to search for (-1 to quit):");
        }
        userInput.close();
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
			Prog2.printErrAndExit("Error reading input file");
		}
		projectSize = INT_FIELDS_LENGTHS; // 4*4 to start with
		for(int i = 0; i < STRING_FIELDS; i++){
			int j = i  * Integer.BYTES; // the index of the byte to be gotten from lengths to fill toInt
			byte[] toInt = new byte[Integer.BYTES]; // array representing int value of field i
			for(int k = 0; k < Integer.BYTES; k++){
				toInt[k] = lengths[j++];
			}
			stringFieldLengths[i] = Prog2.bytesToInt(toInt);
			projectSize += stringFieldLengths[i];
			//System.out.println("Field " + i + " length: " + stringFieldLengths[i]);
		}
		numProjects = (int) (startOfLengths / projectSize);
		entrySize = stringFieldLengths[0] + Long.BYTES;
		bucketSize = entrySize * ENTRIES_PER_BUCKET;
	}
}
