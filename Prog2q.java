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
import java.util.Scanner;

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
            queryUser(inputRAF, hashBucketRAF);
            inputRAF.close();
            hashBucketRAF.close();
        } catch(Exception exception){
            exception.printStackTrace();
            Prog2.printErrAndExit("Error accessing " + args[0] + " or HashBucket.bin");
        }
    }

    /**
     * queryUser: prompts the user to enter a project id suffix until they enter '-1'. All projects with that same
     * suffix are printed out using the HashBucket file generated in Prog2.
     * Pre-conditions: fillFieldLengths has been called, hashBucketRAF points to the index file created in Prog2,
     * inputRAF points to the db file created in Prog1.
     * Post-conditons: any matching projects are printed out
     * @param inputRAF binary db file of projects
     * @param hashBucketRAF binary index file of project ids and pointers to the db file
     * @return void
     */
    public static void queryUser(RandomAccessFile inputRAF, RandomAccessFile hashBucketRAF){
        ExtendibleHashIndex extendibleHashIndex = new ExtendibleHashIndex(hashBucketRAF, inputRAF, "r");
        Scanner userInput = new Scanner(System.in);
        System.out.println("Enter ProjectID suffix to search for (-1 to quit):");
        while(userInput.hasNextLine()){
            String currInput = userInput.nextLine().strip();
            if(currInput.equals("-1")){
                break;
            }
            extendibleHashIndex.printMatches(currInput);
            System.out.println("Enter ProjectID suffix to search for (-1 to quit):");
        }
        userInput.close();
    }
}
