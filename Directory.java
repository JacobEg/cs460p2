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
     * setTotalBuckets: sets totalBuckets instance var to passed-in val
     * Pre-condition: N/A
     * Post-condition: totalBuckets is set to arg val
     * @param totalBuckets the new value of totalBuckets
     * @return void
     */
    public void setTotalBuckets(int totalBuckets){
        this.totalBuckets = totalBuckets;
    }

    /**
     * getPrefixFromAddress: Given a bucket address, return the corresponding prefix.
     * @param addreses the address of the bucket to get the prefix from
     * @return the prefix corresponding to the bucket address, null if not found
     */
    public String getPrefixFromAddress(long addreses){
        for(String prefix: directory.keySet()){
            if(directory.get(prefix) == addreses){
                return prefix;
            }
        }
        return null;
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

    public Set<String> getPrefixes(){
        return directory.keySet();
    }

    /**
     * getAddresses(String idKey)
     * Description: Returns the addresses of the buckets with the prefix matching the idKey
     * Preconditions: Directory was initialized with 10 buckets
     * Postconditions: N/A
     * @param idKey String index key found from entry's projectID
     * @return arraylist of longs representing  the buckets matching 
     */
    public ArrayList<Long> getAddresses(String idKey) {
        ArrayList<Long> addresses = new ArrayList<Long>();
        for(String key : directory.keySet()) {
            if (idKey.startsWith(key) || key.startsWith(idKey)) {
                addresses.add(directory.get(key));
            }
        }
        return addresses;
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
        // might have to change multiple directory pointers beginning with prefix rather than one if directory entry size > prefix length
        System.out.printf("prefix: %s\noldAddr: %d\nnumEntries: %d\nnewAddr: %d\n", 
        prefix, directory.get(prefix), entries.get(directory.get(prefix)), newAddr);
        try{
            for(String key : directory.keySet()) {
                if (key.equals(prefix) || key.startsWith(prefix)) {
                    //long oldAddr = directory.get(key);
                    directory.replace(key, newAddr);
                    //int numEntries = entries.get(oldAddr);
                    //entries.remove(oldAddr); // we removed this already, so trying to access it for key 41 is impossible
                    entries.put(newAddr, 0); // 0 was numEntries
                }
            }
        } catch(Exception exception){
            exception.printStackTrace();
            System.exit(1);
        }
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
        prefixSize++;
        Set<String> prevKeys = directory.keySet();
        // get all key values then loop 10 times on each adding 0-9 to the end of each for new keys
        HashMap<String, Long> newDirectory = new HashMap<String, Long>();
        for (String key : prevKeys) {
            for (int i = 0; i < 10; i++) {
                newDirectory.put(key+i, directory.get(key));
            }
        }
        directory = newDirectory;
        //System.out.println(newDirectory.keySet());
    }
}
