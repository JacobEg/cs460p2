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
 * Known deficiencies: Does not account for case when bucket needs to be split more than once
 *                     (Ex: Bucket '1' holds '10000','10001','10002',...,'10050', insert '10051')
 *                     Might also need to grow directory more than once in the same case.
 *                     Could this be done recursively?
 * 
 * Requirements: Java 16
 */

public class ExtendibleHashIndex {
    // attributes
    private Directory directory;
    private final long BUCKET_SIZE = 100; // TEMP VALUE! NOT ACCURATE!
    // store name of hash file

    // constructor
    public ExtendibleHashIndex() {
        directory = new Directory();
        initBuckets();
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
            HashBucket bucket = new HashBucket(Integer.toString(i));
            // add location to directory (what is the size of a bucket?)
            directory.addAddress(Integer.toString(i), BUCKET_SIZE * i);
            // write to file
        }
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
     * @param idKey The computed hash index integer value (as a String) of the project ID
     * @param dbAddress long address of record in database binary file
     * @return void
     */
    public void addEntry(String projID, String idKey, long dbAddress) {
        // find address of bucket from directory
        long bucketAddr = directory.getAddress(idKey);
        if (bucketAddr == -1) { // bucket not found!
            System.out.println("Error: Bucket not found.");
            System.exit(-1);
        }
        // read appropriate bucket into memory
        HashBucket currBucket; // read from hashbuckets file at bucketAddr
        // add entry to bucket or add buckets if full (also expand directory if necessary)
        /*if (currBucket.isFull()) {
            String currPrefix = currBucket.getPrefix();
            // expand directory if necessary
            if (currPrefix.length() == directory.getPrefixSize()) {
                directory.grow();
            }
            // split bucket into 10 new buckets
            HashBucket[] newBuckets = new HashBucket[10];
            for (int i = 0; i < 10; i++) {
                newBuckets[i] = new HashBucket(currPrefix+i);
            }
            // copy entries from old bucket into new buckets
            HashEntry[] entries = currBucket.getEntries();
            for (HashEntry entry : entries) {
                for (HashBucket bucket : newBuckets) {
                    if (entry.getIdKey().startsWith(bucket.getPrefix())) {
                        // add entry to this bucket
                        bucket.insert(entry);
                        break;
                    }
                }
            }
            // add new entry to new bucket
            HashEntry newEntry = new HashEntry(projID, dbAddress, idKey);
            for (HashBucket bucket : newBuckets) {
                if (newEntry.getIdKey().startsWith(bucket.getPrefix())) {
                    // add entry to this bucket
                    bucket.insert(newEntry);
                    break;
                }
            }
            // write new buckets to Hash Buckets file
            // update directory to point at new buckets

        } else {
            HashEntry newEntry = new HashEntry(projID, dbAddress, idKey);
            currBucket.insert(newEntry);
            // write bucket back to Hash Buckets file
        }*/
    }
}
