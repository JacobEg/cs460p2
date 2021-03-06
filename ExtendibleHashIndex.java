import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Arrays;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;

/**
 * @author Jacob Egestad & Cade Marks
 * File: ExtendibleHashIndex.java
 * Course name: CSC 460 - Database Design
 * Assignment Title: Program #2: Extendible Hashing
 * Instructor: Lester McCann
 * TAs: Haris Riaz, Aayush Pinto
 * Due Date: February 9, 2022
 * 
 * Description: Implements an extendible hashing index as a directory of hash bucket addreses and
 *              a file containing different hash buckets to contain up to 50 entries. Each entry
 *              is a HashEntry object containing the projectID, computed keyID, and address in the
 *              database binary file of a record. Whenever an entry is to be added to a full
 *              bucket, the bucket is split into 10 new buckets with the same prefix as the
 *              original with one more digit (0-9). To accomodate this change the directory might
 *              need to expand to have longer prefixes if these new prefixes are not already
 *              stored.
 * 
 * Known deficiencies: N/A
 * 
 * Requirements: Java 16
 * 
 * Constructor:
 * ExtendibleHashIndex(RandomAccessFile hashBucketRAF, RandomAccessFile dbRAF, String mode) - uses
 * RAF for HashBucket file and RAF for db file to construct ExtendibleHashIndex which manages main
 * functionality for Prog2(q)
 * Methods:
 * getProjectSize
 * getBucketSize
 * getNumProjects
 * readProjectValues
 * fillFieldLengths
 * printIndexInfo
 * writeDirectory
 * readDirectory
 * initBuckets
 * printMatches
 * getAddressFromEntry
 * idToKey
 * readBucket
 * addEntry
 * writeEntry
 */

public class ExtendibleHashIndex {
    // some constants and class values copied from Jacob Egestad's Program 1
	private static final int STRING_FIELDS = 9; // number of string fields
	private static final int INT_FIELDS = 4; // number of int fields
	private static final int INT_FIELDS_LENGTHS = INT_FIELDS * Integer.BYTES; // length of int fields
	private static final int ENTRIES_PER_BUCKET = 50; // number of entries per bucket
	private int[] stringFieldLengths = new int[STRING_FIELDS]; // lengths of the string fields
	private int projectSize = -1; // size of projects in bytes
	private int bucketSize = -1; // size of bucket in bytes
	private int numProjects = -1; // number of projects
	private int entrySize = -1; // the size of each entry
    // attributes
    private Directory directory;
    private RandomAccessFile hashBucketRAF; // to be used for writing hashBucket
    private RandomAccessFile dbRAF; // for accessing the db file
    private long newBucketAddress; // address at end of hash bucket file for new bucket

    /**
     * Constructs new ExtendibleHashIndex object give the RAFs for the HashBucket file, the db file,
     * and the mode
     * @param hashBucketRAF RAF for dealing with index file that has the hash buckets
     * @param dbRAF RAF for dealing with db file created with Program 1
     * @param mode "r" for Prog2q and "w" for Prog2
     */
    public ExtendibleHashIndex(RandomAccessFile hashBucketRAF, RandomAccessFile dbRAF, String mode) {
        this.hashBucketRAF = hashBucketRAF;
        this.dbRAF = dbRAF;
        fillFieldLengths();
        if(mode.equals("w")){ // we're writing a hash bucket file so it's cool to create a new directory
            directory = new Directory(bucketSize);
            initBuckets();
        } else{ // we gotta read the directory from the hash bucket file since we're in read mode
            readDirectory();
        }
        newBucketAddress = bucketSize * 10;
    }

    /**
     * getProjectSize: Return size of each project in bytes
     * Pre-conditions: fillFieldLengths has been called
     * Post-conditions: N/A
     * @return project size in bytes
     */
    public int getProjectSize(){
        return projectSize;
    }

    /**
     * getBucketSize: Return size of each bucket in bytes
     * Pre-conditions: fillFieldLengths has been called
     * Post-conditions: N/A
     * @return size of bucket in bytes
     */
    public int getBucketSize(){
        return bucketSize;
    }

    /**
     * getNumProjects: Return num projects in dbRAF
     * Pre-conditions: fillFieldLengths has been called
     * Post-conditions: N/A
     * @return num projects in db file
     */
    public int getNumProjects(){
        return numProjects;
    }

	/**
	 * readProjectValues: given a database file and a bye location, reads the Project into an array of Strings
	 * representing the values of the project.
	 * Pre-conditions: fillFieldLengths has been called. inputRAF is a binary file representing the db file of projects and
	 * is in read mode
	 * Post-conditions: file-pointer offset for inputRAF is set to location
	 * @param location the byte # where the project starts
	 * @return Array of strings representing the fields in a project
	 * @throws IOException if inputRAF.seek or .read fails
	 */
	public String[] readProjectValues(long location) throws IOException{
		String[] projectFields = new String[STRING_FIELDS + INT_FIELDS]; // project represented as an arr of strings
		byte[] project = new byte[projectSize]; // project represented as an array of bytes
		dbRAF.seek(location);
		dbRAF.read(project);
		int i; // index to be used in looping
		int startIndex = 0; // starting index of string field
		for(i = 0; i < STRING_FIELDS; i++){
			projectFields[i] = Prog2.bytesToString(Arrays.copyOfRange(project, startIndex, startIndex + stringFieldLengths[i]));
			startIndex += stringFieldLengths[i];
		}
		for(; i < STRING_FIELDS + INT_FIELDS; i++){
			projectFields[i] = "" + Prog2.bytesToInt(Arrays.copyOfRange(project, startIndex, startIndex + Integer.BYTES));
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
	 * @return void
	 */
	private void fillFieldLengths(){
		byte[] lengths = new byte[STRING_FIELDS * Integer.BYTES]; // 9 lengths in a binary string
		long startOfLengths = -1; // to be used to mark the beginning of the string field lengths
		try {
			startOfLengths = dbRAF.length() - (STRING_FIELDS * Integer.BYTES);
			dbRAF.seek(startOfLengths);
		    dbRAF.read(lengths);
			dbRAF.seek(0);
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
		}
		numProjects = (int) (startOfLengths / projectSize);
		entrySize = stringFieldLengths[0] + Long.BYTES;
		bucketSize = entrySize * ENTRIES_PER_BUCKET;
	}

    /**
     * printIndexInfo: prints the final global depth of the directory, the number of unique bucket pointer values,
     * the number of buckets in the hash bucket file, and the avg bucket occupancy
     * Pre-conditions: The db file is done being read, directory is initialized
     * Post-conditions: The above info is printed to stdout
     * @return void
     */
    public void printIndexInfo(){
        int prefixSize = directory.getPrefixSize(); // the number of digits in the directory prefixes
        System.out.printf("Global depth of directory: %d\n", prefixSize);
        int uniqueBuckets = directory.getUniqueBuckets(); // the number of unique buckets pointer values
        int totalBuckets = directory.getTotalBuckets(); // the total number of buckets in the hash bucket file
        System.out.printf("Number of unique bucket pointers: %d\n", uniqueBuckets);
        System.out.printf("Number of buckets in HashBucket.bin: %d\n", totalBuckets);
        System.out.printf("Average bucket capacity: %f\n", directory.getNumEntries() * 1.0 / uniqueBuckets);
    }

    /**
     * writeDirectory write directory to the end of the HashBucket binary file.
     * Pre-conditons: hashBucketRAF has been filled with all the projects in dbRAF,
     * hashBucketRAF is in write mode.
     * Post-conditons: the directory is written to the end of the hasbucket binary file.
     * User should be done writing to the hash bucket file
     * @return void
     */
    public void writeDirectory(){
        try {
            ByteArrayOutputStream byteStream = new ByteArrayOutputStream(); // byte stream for converting directory to byte arr
            ObjectOutputStream objectStream = new ObjectOutputStream(byteStream); // obj stream for converting directory to byte arr
            objectStream.writeObject(directory);
            objectStream.flush();
            byte[] directoryBytes = byteStream.toByteArray(); // array of bytes representing directory
            byte[] outputBytes = new byte[directoryBytes.length+Integer.BYTES]; // array of bytes to write to the end of the file
            byte[] directoryLength = Prog2.intToBytes(directoryBytes.length); // array of bytes representing length of directroy byte array
            for(int i = 0; i < directoryBytes.length; i++){
                outputBytes[i] = directoryBytes[i];
            }
            for(int i = 0; i < directoryLength.length; i++){
                outputBytes[i+directoryBytes.length] = directoryLength[i];
            }
            hashBucketRAF.seek(hashBucketRAF.length());
            hashBucketRAF.write(outputBytes);
            objectStream.close();
            byteStream.close();
        } catch (IOException e) {
            e.printStackTrace();
            Prog2.printErrAndExit("Error writing directory to file.");
        }
        
    }

    /**
     * readDirectory: reads directory from bottom of Hash Bucket file and sets this.directory to it
     * Pre-conditions: User has specified that we're in read mode, and hashBucketRAF is in read mode
     * Post-conditions: directory has been read from hashBucketRAF
     * @return void
     */
    private void readDirectory(){
        try {
            long startOfDirectoryLength = hashBucketRAF.length() - Integer.BYTES; // pos in hashBucketRAF where directory length starts
            byte[] directoryLengthBytes = new byte[Integer.BYTES]; // byte array representing length of directory in bytes
            hashBucketRAF.seek(startOfDirectoryLength);
            hashBucketRAF.read(directoryLengthBytes);
            int directoryLength = Prog2.bytesToInt(directoryLengthBytes); // length of directory in bytes as an int
            byte[] directory = new byte[directoryLength]; // directory represented as an array of bytes
            hashBucketRAF.seek(startOfDirectoryLength - directoryLength);
            hashBucketRAF.read(directory);
            ByteArrayInputStream byteStream = new ByteArrayInputStream(directory); // byte stream for converting array of bytes to directory
            ObjectInputStream objectStream = new ObjectInputStream(byteStream); // object stream for converting array of bytes to directory
            this.directory = (Directory) objectStream.readObject();
            objectStream.close();
            byteStream.close();
        } catch (IOException e) {
            e.printStackTrace();
            Prog2.printErrAndExit("Error reading directory from hash bucket file.");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            Prog2.printErrAndExit("Couldn't find class 'directory'");
        }
    }

    /**
     * initBuckets()
     * Description: Initializes the first 10 hash buckets and writes them to the hash bucket file.
     * Preconditions: Bucket file has been created.
     * Postconditions: First 10 buckets are written to file.
     * @return void
     */
    private void initBuckets() {
        // create initial 10 buckets and write to file (and add addresses to directory)
        for (int i = 0; i < 10; i++) {
            // add location to directory (what is the size of a bucket?)
            directory.addAddress(Integer.toString(i), bucketSize * i);
        }
    }

    /**
     * printMatches: prints out all projects that have a suffix matching suffix
     * Pre-conditions: hashBucketRAF is in read mode, as is dbRAF since we are to be using both to handle this query.
     * Post-conditions: All entries with Project ID matching suffix are printed.
     * @param suffix The suffix entered by the user to search for
     * @return void
     */
    public void printMatches(String suffix){
        String key = idToKey(suffix); // key generated from the suffix
        try{
            ArrayList<Long> addresses = directory.getAddresses(key); // addresses beginning with the key
            int matches = 0; // number of matches found
            for(long address: addresses){
                int numEntries = directory.getNumEntriesInBucketByAddress(address); // number of entries in the bucket at current address
                for (int i = 0; i < numEntries; i++) {
                    long dbAddr = getAddressFromEntry(address); // address of record in db file stored by entry
                    String[] values = readProjectValues(dbAddr); // values of record in db file
                    if(!values[0].strip().endsWith(suffix)){ // just making sure
                        address += entrySize;
                        continue;
                    }
                    matches++;
                    System.out.printf("[%s][%s][%s]\n", values[0], values[1], values[9]);
                    address += entrySize;
                }
            }
            System.out.printf("%d records match your query\n", matches);
        } catch(IOException ioException){
            ioException.printStackTrace();
            Prog2.printErrAndExit("Error getting address from entry at in HashBucket File. Or error reading from DB file.");
        }
    }

    /**
     * getAddressFromEntry(long address): Takes the address of an entry in the Hash Bucket file
     * and returns the address of the record stored in that entry from the database binary file.
     * @param address The address of the entry in the Hash Bucket file
     * @return The address of the record in the database file
     * @throws IOException
     */
    private long getAddressFromEntry(long address) throws IOException{
        byte[] entry = new byte[entrySize]; // byte array representing entry in hash buckets file
        hashBucketRAF.seek(address);
        hashBucketRAF.read(entry);
        return Prog2.bytesToLong(Arrays.copyOfRange(entry, entrySize - Long.BYTES, entrySize));
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
    private String idToKey(String id){
        id = id.strip();
        String key = ""; // key to be converted to an integer for the key
        for(int i = id.length() - 1; i >= 0; i--){
            key += ((int) id.charAt(i)) % 10;
        }
        return key;
    }

    /**
     * readBucket: returns the bucket at the given bucket address
     * Pre-condition: Directory is initialized
     * Post-condition: N/A
     * @param bucketAddr address of the start of the bucket
     * @return HashBucket obj representing bucket at bucketAddr
     */
    private HashBucket readBucket(long bucketAddr) {
        // gotta get entries from bucket file somehow
        String prefix = directory.getPrefixFromAddress(bucketAddr); // prefix of bucket at bucketAddr
        HashBucket fileBucket = new HashBucket(prefix); // HashBucket obj to be returned
        int startOfAddr = entrySize - Long.BYTES; // represents start of byte location that the entry points to
        int numEntries = directory.getNumEntriesInBucketByAddress(bucketAddr); // current number of entries in the bucket
        try{
            for(int i = 0; i < numEntries; i++){
                byte[] entry = new byte[entrySize]; // entry as an array of bytes
                hashBucketRAF.seek(bucketAddr);
                hashBucketRAF.read(entry);
                String projID = Prog2.bytesToString(Arrays.copyOfRange(entry, 0, startOfAddr)); // project id gotten from entry
                long projAddr = Prog2.bytesToLong(Arrays.copyOfRange(entry, startOfAddr, entrySize)); // byte location in db file from entry
                fileBucket.insert(new HashEntry(projID, projAddr));
                bucketAddr += entrySize;
            }
        } catch(IOException ioException){
            Prog2.printErrAndExit("Error reading bucket from HashBucket.bin");
        }
        return fileBucket;
    }

    /**
     * addEntry(String projID, String idKey, long dbAddress)
     * Description: Adds a new HashEntry to the appropriate Hash Bucket. If the bucket is full,
     *              first add 10 new buckets to account for an additional digit, growing the
     *              directory if necessary, then copy over all the entries to the new buckets, add
     *              the new entry, and update directory addresses.
     * Preconditions: Bucket file has been created.
     * Postconditions: Directory addresses are up to date.
     * @param projID The project ID string for the record
     * @param dbAddress long address of record in database binary file
     * @return void
     */
    public void addEntry(String projID, long dbAddress) {
        // find address of bucket from directory
        long bucketAddr = directory.getAddresses(idToKey(projID)).get(0); // address of bucket in hash bucket file
        if (bucketAddr == -1) { // bucket not found!
            System.out.println("Error: Bucket not found.");
            System.exit(-1);
        }
        // read appropriate bucket into memory
        HashBucket currBucket = readBucket(bucketAddr); // bucket read from hashbuckets file at bucketAddr
        // add entry to bucket or add buckets if full (also expand directory if necessary)
        if (currBucket.isFull()) {
            directory.setTotalBuckets(directory.getTotalBuckets() + 10);
            String currPrefix = currBucket.getPrefix(); // prefix of currBucket
            // expand directory if necessary
            if (currPrefix.length() == directory.getPrefixSize()) {
                directory.grow(currPrefix);
            } else {
                directory.updateKeyMap(currPrefix);
            }
            // split bucket into 10 new buckets
            HashBucket[] newBuckets = new HashBucket[10]; // array of new buckets being added
            try{
                hashBucketRAF.seek(hashBucketRAF.length());
                hashBucketRAF.write(new byte[bucketSize*10]);
            } catch(IOException ioException){
                ioException.printStackTrace();
                Prog2.printErrAndExit("Couldn't allocate space to HashBucket.bin");
            }
            for (int i = 0; i < 10; i++) {
                newBuckets[i] = new HashBucket(currPrefix+i);
                // update directory to point at new buckets
                directory.changeAddress(currPrefix+i, newBucketAddress);
                newBucketAddress += bucketSize;
            }
            // copy entries from old bucket into new buckets
            HashEntry[] entries = currBucket.getEntries(); // entries from currBucket, before split
            for (HashEntry entry : entries) {
                for (HashBucket bucket : newBuckets) {
                    if (idToKey(entry.getProjID()).startsWith(bucket.getPrefix())) {
                        // add entry to this bucket
                        bucket.insert(entry);
                        break;
                    }
                }
            }
            // add new entry to new bucket
            // recursive call should handle edge case of multiple bucket splits?
            addEntry(projID, dbAddress);
            // write new buckets to Hash Buckets file
            for (HashBucket bucket : newBuckets) {
                HashEntry[] bucketEntries = bucket.getEntries(); // entries from new bucket to write to file
                for (int i = 0; i < bucket.getNumEntries(); i++) {
                    String bucketProjID = bucketEntries[i].getProjID(); // project ID of writing entry
                    long bucketDBAddr = bucketEntries[i].getDbAddress(); // DB address of writing entry
                    long bucketBucketAddr = directory.getAddresses(idToKey(bucketProjID)).get(0); // bucket file address of writing entry
                    writeEntry(bucketProjID, bucketDBAddr, bucketBucketAddr);
                }
            }

        } else {
            HashEntry newEntry = new HashEntry(projID, dbAddress); // new entry added to bucket file
            currBucket.insert(newEntry);
            // write bucket back to Hash Buckets file
            writeEntry(projID, dbAddress, bucketAddr);
        }
    }

    /**
     * writeEntry: writes a given Project ID and DB address to the bucket at bucketADDr
     * Pre-conditions: the bucket at bucketAddr is not full
     * Post-conditions: The project id and db address is written to the hash bucket file
     * @param projID String representing the project id of the project to add
     * @param dbAddr byte location where the project represented by projID can be accessed
     * @param bucketAddr address of bucket where info will be added
     * @return void
     */
    private void writeEntry(String projID, long dbAddr, long bucketAddr){
        int numEntriesInBucket = directory.getNumEntriesInBucketByAddress(bucketAddr); // number of entries in current bucket
        directory.incrementNumEntriesAtAddress(bucketAddr);
        long insertAddr = bucketAddr + (numEntriesInBucket * bucketSize / ENTRIES_PER_BUCKET); // address to insert new entry
        byte[] writeBytes = new byte[projID.length() + Long.BYTES]; // bytes to write at the byte location insertAddr
        for(int i = 0; i < projID.length(); i++){ // byte-ify the string
            writeBytes[i] = (byte) projID.charAt(i);
        }
        byte[] dbAddrBytes = Prog2.longToBytes(dbAddr); // convert pointer to db file to array of bytes
        for(int i = 0; i < Long.BYTES; i++){
            writeBytes[i+projID.length()] = dbAddrBytes[i];
        }
        try{
            hashBucketRAF.seek(insertAddr);
            hashBucketRAF.write(writeBytes);
        } catch(IOException ioException){
            ioException.printStackTrace();
            Prog2.printErrAndExit("Error writing Proj ID " + projID + " to db file.");
        }
    }
}
