import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

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
 * 
 * Constructors:
 * Directory(int bucketSize) - takes in a bucket size to be used in initializing directory and entries
 * directory and entries given 10 initial values
 * Methods:
 * getTotalBuckets
 * setTotalBuckets
 * getPrefixFromAddress
 * getNumEntriesInBucketByAddress
 * incrementNumEntriesAtAddress
 * getNumEntries
 * getUniqueBuckets
 * getPrefixSize
 * getPrefixes
 * getAddresses
 * addAddress
 * changeAddress
 * grow
 */
public class Directory implements Serializable{
    // attributes
    private HashMap<String, Long> directory; // maps directory prefix to address of bucket
    private HashMap<Long, Integer> entries; // maps address of bucket to num entries in that bucket
    private HashMap<String, String> keyMap; // key is directory prefix, val is hashbucket prefix
    private int prefixSize; // size of directory prefix
    private int numEntries; // total number of entries
    //private final int BUCKET_SIZE;
    private int totalBuckets; // total number of buckets

    /**
     * Constructs new directory obj given a bucketSize
     * @param bucketSize size of bucket, to be used for setting entries and directory
     */
    public Directory(int bucketSize) {
        totalBuckets = 10;
        directory = new HashMap<String, Long>();
        entries = new HashMap<Long, Integer>();
        keyMap = new HashMap<String, String>();
        long pos = 0; // byte position representing start of each bucket
        for(int i = 0; i < totalBuckets; i++){
            entries.put(pos, 0);
            directory.put("" + i, pos);
            keyMap.put("" + i, "" + i);
            pos += bucketSize;
        }
        //BUCKET_SIZE = bucketSize;
        prefixSize = 1;
        numEntries = 0;
    }

    /**
     * getTotalBuckets: returns total number of buckets in HashBucket.bin
     * Pre-condition: totalBuckets has been initialized
     * Post-condition: N/A
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
     * @param address the address of the bucket to get the prefix from
     * @return the prefix corresponding to the bucket address, null if not found
     */
     public String getPrefixFromAddress(long address){
        for(String prefix: directory.keySet()){
            if(directory.get(prefix) == address){
                return keyMap.get(prefix);
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
     * incrementNumEntries: adds 1 to numEntries and one to entries
     * at address address
     * Pre-conditions: numEntries and entries have been initialized
     * Post-conditions: numEntries increased by 1 as well as the value of entries[address]
     * @param address address of bucket to increment
     * @return void
     */
    public void incrementNumEntriesAtAddress(long address){
        numEntries++;
        int entriesInBucket = entries.get(address); // curr number of entries in bucket at address address
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
     * getUniqueBuckets: Returns the current number of unique buckets
     * Pre-conditons: directory has been initialized
     * Post-conditions: N/A
     * @return the current number of unique buckets
     */
    public int getUniqueBuckets(){
        Set<Long> uniqueBuckets = new HashSet<Long>(); // set of addresses
        for(long address : directory.values()){ // iterate over bucket addresses
            uniqueBuckets.add(address);
        }
        return uniqueBuckets.size();
    }

    /**
     * getPrefixSize: returns size of directory prefix
     * Pre-conditions: prefixSize is initialized
     * Post-conditions: N/A
     * @return size of directory prefix
     */
    public int getPrefixSize() {
        return prefixSize;
    }

    /**
     * getPrefixes: returns set of prefixes in directory
     * Pre-condition: directory is set
     * Post-condition: N/A
     * @return set of prefixes in directory
     */
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
        ArrayList<Long> addresses = new ArrayList<Long>(); // list of addresses to return
        for(String key : directory.keySet()) { // iterate over prefixes
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
        keyMap.put(prefix, prefix); // is this correct?
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
            for(String key : directory.keySet()) { // iterate over keys
                if (key.startsWith(prefix)) {
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
     * grow(String currPrefix)
     * Description: Grow directory by a factor of 10, making new prefixes by adding digits 0-9 to
     *              the end of each old prefix. This is done to accomodate new hash buckets in the
     *              case a bucket is split after reaching capacity.
     * Preconditions: Directory was initialized with 10 buckets
     * Postconditions: directory is grown by a factor of 10
     * @param currPrefix The prefix of the hash bucket being split
     * @return void
     */
    public void grow(String currPrefix) {
        prefixSize++;
        Set<String> prevKeys = directory.keySet(); // previous set of directory prefixes
        // get all key values then loop 10 times on each adding 0-9 to the end of each for new keys
        HashMap<String, Long> newDirectory = new HashMap<String, Long>(); // new directory to set directory to
        for (String key : prevKeys) {
            for (int i = 0; i < 10; i++) {
                if (key.equals(currPrefix)) {
                    keyMap.put(key+i, key+i);
                } else {
                    keyMap.put(key+i, key);                }
                newDirectory.put(key+i, directory.get(key));
            }
            keyMap.remove(key);
        }
        directory = newDirectory;
        //System.out.println(newDirectory.keySet());
    }

    /**
     * grow()
     * Description: Change the keyMap so each directory key's value is now the updated Hash Bucket
     *              prefix for the bucket being split.
     * Preconditions: Directory was initialized with 10 buckets
     * Postconditions: N/A
     * @param currPrefix The prefix of the hash bucket being split
     * @return void
     */
    public void updateKeyMap(String currPrefix) {
        Set<String> keys = directory.keySet();
        for (String key : keys) {
            for (int i = 0; i < 10; i++) {
                if (key.equals(currPrefix+i)) {
                    keyMap.replace(key, keyMap.get(key)+i);
                }
            }
        }
    }
}
