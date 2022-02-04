import java.io.File;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;

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

    public static void main(String[] args){
        if(args.length == 0) {
            printErrAndExit("Prog2 <path/to/binary_file>");
        }
        File inputFile = new File(args[0]);
        if(!inputFile.isFile()){
            printErrAndExit("Prog2 <path/to/binary_file>");
        }
        try {
            RandomAccessFile inputRAF = new RandomAccessFile(inputFile, "r");
            RandomAccessFile hashBucketRAF = new RandomAccessFile("HashBucket.bin", "w");
            inputRAF.close();
            hashBucketRAF.close();
        } catch (Exception e) {
            e.printStackTrace();
            printErrAndExit("Can't access " + args[0]);
        }
    }

    /**
     * idToKey: Takes a String Project ID and converts it into an integer key. It does this by
     * reversing the String and appending the least significant digit of the ASCII value of
     * each character in the String.
     * Pre-condition: The binary file is being read.
     * Post-condition: The 
     * @param id
     * @return
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
	 * the projects in inputRAF, as well as projectSize, bucketSize and numProjects
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
        bucketSize = (stringFieldLengths[0] + Long.BYTES) * ENTRIES_PER_BUCKET;
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
