/**
 * @author Jacob Egestad & Cade Marks
 * File: HashEntry.java
 * Course name: CSC 460 - Database Design
 * Assignment Title: Program #2: Extendible Hashing
 * Instructor: Lester McCann
 * TAs: Haris Riaz, Aayush Pinto
 * Due Date: February 9, 2022
 *
 * Description: Represents a single record from the database file to be stored in a Hash Bucket.
 *              Contains fields for the project ID of the record and the address of the record in
 *              the database binary file.
 *
 * Known deficiencies: N/A
 *
 * Requirements: Java 16
 */
public class HashEntry {
    // attributes
    private String projID; // project id of the project this entry represents
    private long dbAddress; // byte position in db file this entry represents

    /**
     * Constructs new HashEntry obj representing entry in HashBucket.bin
     * @param projID Project ID associated with the entry
     * @param dbAddress entry in db created in Prog1 associated with the entry
     */
    public HashEntry(String projID, long dbAddress) {
        this.projID = projID;
        this.dbAddress = dbAddress;
    }

    /**
     * getProjID: returns project id of project this entry points to
     * Pre-conditon: projID is initialized
     * Post-condition: N/A
     * @return project id associated with this entry
     */
    public String getProjID() {
        return projID;
    }

    /**
     * getDbAddress: returns project id of project this entry points to
     * Pre-conditon: dbAddress is initialized
     * Post-condition: N/A
     * @return byte location in db file associated with this entry
     */
    public long getDbAddress() {
        return dbAddress;
    }
}