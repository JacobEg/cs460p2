import java.util.HashMap;

public class Directory {
    HashMap<String, Long> directory;

    public Directory() {
        directory = new HashMap<String, Long>();
        // add initial 10 buckets to HashBucket file
    }

    // use if directory must grow to include an additional digit for each prefix
    public void grow() {

    }
}
