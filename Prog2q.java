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

import java.io.File;
import java.io.RandomAccessFile;

public class Prog2q {

    public static void main(String[] args){
        if(args.length == 0){
            Prog2.printErrAndExit("Prog2q <path/to/binary_file>");
        }
        File inputFile = new File(args[0]);
        if(!inputFile.isFile()){
            System.err.println(args[0] + " either doesn't exist or isn't a file");
            Prog2.printErrAndExit("Prog2q <path/to/binary_file>");
        }
        File hashBucketFile = new File("HashBucket.bin");
        if(!hashBucketFile.isFile()){
            Prog2.printErrAndExit("HashBucket.bin either doesn't exist or isn't a file");
        }
        try{
            RandomAccessFile inputRAF = new RandomAccessFile(inputFile, "r");
            RandomAccessFile hashBucketRAF = new RandomAccessFile(hashBucketFile, "r");
        } catch(Exception exception){
            exception.printStackTrace();
            Prog2.printErrAndExit("Error accessing " + args[0] + " or HashBucket.bin");
        }
    }
}
