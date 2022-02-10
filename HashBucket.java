/**
 * @author Jacob Egestad & Cade Marks
 * File: HashBucket.java
 * Course name: CSC 460 - Database Design
 * Assignment Title: Program #2: Extendible Hashing
 * Instructor: Lester McCann
 * TAs: Haris Riaz, Aayush Pinto
 * Due Date: February 9, 2022
 *
 * Description: Represents a single bucket, storing all the record entries beginning with a
 *              specific prefix. If the bucket reaches full capacity (50 entries) and another
 *              needs to be inserted, the bucket will be split into 10 new buckets, each with the
 *              same prefix plus an additional digit (0-9). These buckets are all stored in a hash
 *              bucket file, and the addresses are saved in a Directory saved in the Extendible
 *              Hash Index.
 *
 * Known deficiencies: N/A
 *
 * Requirements: Java 16
 * 
 * Constructor:
 * HashBucket(String prefix) - takes in prefix representing entries in the hash bucket and initializes entries to CAPACITY amount
 * Methods:
 * getPrefix
 * getEntries
 * isFull
 * insert
 */
public class HashBucket {
    // attributes
    private final int CAPACITY = 50; // max entries per bucket
    private String prefix; // the initial digits of values stored in this bucket
    private HashEntry[] entries; // array of entrues
    private int numEntries; // curr num entries in bucket

    /**
     * Constructs a HashBucket obj, representing a HashBucket in HashBucket.bin
     * Pre-conditions: HashBucket.bin is being written to
     * Post-conditions: N/A
     * @param prefix prefix associated with Bucket ([0-9]+)
     */
    public HashBucket(String prefix) {
        this.prefix = prefix;
        entries = new HashEntry[CAPACITY];
        numEntries = 0;
    }

    /**
     * getPrefix: returns prefix associated with the bucket
     * Pre-conditions: prefix is initialized
     * Post-conditions: N/A
     * @return prefix associated w/ the bucket
     */
    public String getPrefix() {
        return prefix;
    }

    /**
     * getEntries: returns array of HashEntry objects in this bucket
     * Pre-conditions: entries is initialized
     * Post-conditions: N/A
     * @return entries array
     */
    public HashEntry[] getEntries() {
        return entries;
    }

    /**
     * getNumEntries: returns current number of entries in the HashBucket
     * Pre-conditions: numEntries is initialized
     * Post-conditions: N/A
     * @return
     */
    public int getNumEntries() {
        return numEntries;
    }

    /**
     * isFull()
     * Description: returns true if the bucket is at full capacity, else false
     * Preconditions: constant CAPACITY represents maximum amount of entries per bucket
     *                numEntries is updated each time an entry is added
     * Postconditions: N/A
     * @return numEntries Boolean value representing whether bucket is full
     */
    public Boolean isFull() {
        return numEntries == CAPACITY;
    }

    /**
     * insert(HashEntry newEntry)
     * Description: adds a new entry to the bucket
     * Preconditions: The bucket is not full
     * Postconditions: N/A
     * @param newEntry HashEntry to be added to bucket
     * @return void
     */
    public void insert(HashEntry newEntry) {
        // add the new entry
        entries[numEntries] = newEntry;
        numEntries++;
    }
}
