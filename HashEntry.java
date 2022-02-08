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
    String projID;
    long dbAddress;

    // constructor
    public HashEntry(String projID, long dbAddress) {
        this.projID = projID;
        this.dbAddress = dbAddress;
    }

    // getter method for projID
    public String getProjID() {
        return projID;
    }
}