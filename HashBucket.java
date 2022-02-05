public class HashBucket {
    private final int CAPACITY = 50;
    private String prefix; // the initial digits of values stored in this bucket
    HashEntry[] entries;
    int numEntries;

    public HashBucket(String prefix) {
        this.prefix = prefix;
        entries = new HashEntry[CAPACITY];
        numEntries = 0;
    }

    public String getPrefix() {
        return prefix;
    }

    public HashEntry[] getEntries() {
        return entries;
    }

    public void insert(String projID, long dbAddress) {
        if (numEntries == CAPACITY) {
            // check if directory needs to grow
            // split the bucket into 10 new buckets
        } else {
            // add the new entry
            HashEntry newEntry = new HashEntry(projID, dbAddress);
            entries[numEntries] = newEntry;
            numEntries++;
        }
    }


    private class HashEntry {
        String projID;
        long dbAddress;

        private HashEntry(String projID, long dbAddress) {
            this.projID = projID;
            this.dbAddress = dbAddress;
        }
    }
}
