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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;


public class Directory {
    // attributes
    HashMap<String, Long> directory;
    int prefixSize;

    // constructor
    public Directory() {
        directory = new HashMap<String, Long>();
        prefixSize = 1;
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
            if (idKey.startsWith(key)) {
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
        directory.replace(prefix, newAddr);
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
        List<String> prefixes = new ArrayList<String>();
        Set<String> prevKeys = directory.keySet();
        // get all key values then loop 10 times on each adding 0-9 to the end of each for new keys
        HashMap<String, Long> newDirectory = new HashMap<String, Long>();
        for (String key : prevKeys) {
            for (int i = 0; i < 10; i++) {
                newDirectory.put(key+i, directory.get(key));
            }
        }
        directory = newDirectory;
        System.out.println(newDirectory.keySet());
    }
}
