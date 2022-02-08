/**
 * @author Jacob Egestad & Cade Marks
 * File: Directory.java
 * Course name: CSC 460 - Database Design
 * Assignment Title: Program #2: Extendible Hashing
 * Instructor: Lester McCann
 * TAs: Haris Riaz, Aayush Pinto
 * Due Date: February 9, 2022
 *
 * Description: A directory of addresses to hash buckets in the hash bucket file. Stores the
 *              prefixes and addresses of each bucket in the file. When buckets split, if there
 *              isn't already a prefix for the new buckets in the directory, the directory grows
 *              by a factor of 10 to fit new buckets/prefixes.
 *
 * Known deficiencies: N/A
 *
 * Requirements: Java 16
 */

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;


public class Directory implements Serializable{
    // attributes
    HashMap<String, Long> directory;
    HashMap<Long, Integer> entries; // keep track of number of entries per bucket
    int prefixSize;
    private int numEntries;
    final int BUCKET_SIZE;
    private int totalBuckets;

    // constructor
    public Directory(int bucketSize) {
        totalBuckets = 10;
        directory = new HashMap<String, Long>();
        entries = new HashMap<Long, Integer>();
        long pos = 0; // byte position representing start of each bucket
        for(int i = 0; i < totalBuckets; i++){
            entries.put(pos, 0);
            directory.put("" + i, pos);
            pos += bucketSize;
        }
        BUCKET_SIZE = bucketSize;
        prefixSize = 1;
        numEntries = 0;
    }

    /**
     * getTotalBuckets: returns total number of buckets in HashBucket.bin
     * @return total number of buckets in HashBucket.bin
     */
    public int getTotalBuckets(){
        return totalBuckets;
    }

    /**
     * getNumEntriesInBucketByAddress: returns the number of entries in a bucket using address
     * @param address the address pointing to the start of the bucket
     * @return number of entries in bucket @ given address
     */
    public int getNumEntriesInBucketByAddress(long address){
        return entries.get(address);
    }

    /**
     * incrementNumEntries: adds 1 to numEntries
     * Pre-conditions: numEntries has been initialized
     * Post-conditions: numEntries increased by 1
     * @return void
     */
    public void incrementNumEntriesAtAddress(long address){
        numEntries++;
        int entriesInBucket = entries.get(address);
        entries.put(address, entriesInBucket+1);
    }

    /**
     * getNumEntries: returns total number of entries in the index
     * Pre-condition: numEntries has been initialized
     * Post-condition: N/A
     * @return numEntries total number of entries in the Hash Bucket file
     */
    public int getNumEntries(){
        return numEntries;
    }

    /**
     * Returns the current number of buckets
     * Pre-conditons: directory has been initialized
     * Post-conditions: N/A
     * @return the current number of buckets
     */
    public int getBuckets(){
        return directory.size();
    }

    // getter method for prefixSize
    public int getPrefixSize() {
        return prefixSize;
    }

    /**
     * getAddress(String idKey)
     * Description: Returns the address of the bucket with the prefix matching the idKey
     * Preconditions: Directory was initialized with 10 buckets
     * Postconditions: N/A
     * @param idKey String index key found from entry's projectID
     * @return long representing address of correct hash bucket, -1 if not found
     */
    public long getAddress(String idKey) {
        for(String key : directory.keySet()) {
            if (key.length() == prefixSize && idKey.startsWith(key)) {
                return directory.get(key);
            }
        }
        return -1;
    }

    /**
     * addAddress(String prefix, long addr)
     * Description: Inserts a new prefix and address key/value pair
     * Preconditions: Directory was initialized with 10 buckets
     *                Given prefix is not already a key in the map
     * Postconditions: N/A
     * @param prefix String prefix key to add to map
     * @param addr long address value to add to map
     * @return void
     */
    public void addAddress(String prefix, long addr) {
        directory.put(prefix, addr);
        entries.put(addr, 0);
    }

    /**
     * changeAddress(String prefix, long newAddr)
     * Description: Changes the address value for a given prefix
     * Preconditions: Directory was initialized with 10 buckets
     *                Given prefix is already a key in the map
     * Postconditions: N/A
     * @param prefix String prefix key to modfiy in map
     * @param newAddr long address value to mopdify to map
     * @return void
     */
    public void changeAddress(String prefix, long newAddr) {
        long oldAddr = directory.get(prefix);
        directory.replace(prefix, newAddr);
        int numEntries = entries.get(oldAddr);
        entries.remove(oldAddr);
        entries.put(newAddr, numEntries);
    }

    /**
     * grow()
     * Description: Grow directory by a factor of 10, making new prefixes by adding digits 0-9 to
     *              the end of each old prefix. This is done to accomodate new hash buckets in the
     *              case a bucket is split after reaching capacity.
     * Preconditions: Directory was initialized with 10 buckets
     * Postconditions: N/A
     * @return void
     */
    public void grow() {
        totalBuckets *= 10;
        prefixSize++;
        List<String> prefixes = new ArrayList<String>();
        Set<String> prevKeys = directory.keySet();
        // get all key values then loop 10 times on each adding 0-9 to the end of each for new keys
        HashMap<String, Long> newDirectory = new HashMap<String, Long>();
        HashMap<Long, Integer> newEntries = new HashMap<Long, Integer>();
        for (String key : prevKeys) {
            for (int i = 0; i < 10; i++) {
                newEntries.put(directory.get(key), entries.get(directory.get(key))); // this might not work
                newDirectory.put(key+i, directory.get(key));
            }
        }
        directory = newDirectory;
        entries = newEntries;
        System.out.println(newDirectory.keySet());
    }
}
