import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import com.couchbase.client.CouchbaseMetaClient;


public class Verification {

    @SuppressWarnings("rawtypes")
	public static void comparison(Stronghold sh, CouchbaseMetaClient sclient, CouchbaseMetaClient dclient) 
	throws InterruptedException, ExecutionException, IOException {
	    populate_destTable(sh, dclient);

	    if (sh.iswritetofile()) {
		System.out.println("Writing data to file data_log.txt");
		File file1 = new File("source_data_log.txt");
		File file2 = new File("destination_data_log.txt");
		if (file1.exists())
		    file1.delete();
		if (file2.exists())
		    file2.delete();
		file1.createNewFile();
		file2.createNewFile();
		FileWriter fw1 = new FileWriter(file1.getAbsolutePath());
		BufferedWriter bw1 = new BufferedWriter(fw1);
		FileWriter fw2 = new FileWriter(file1.getAbsolutePath());
		BufferedWriter bw2 = new BufferedWriter(fw2);
		Iterator it1 = sh.sourceContent.entrySet().iterator();
		Iterator it2 = sh.destinationContent.entrySet().iterator();
		while(it1.hasNext()) {
		    Map.Entry p1 = (Map.Entry) it1.next();
		    String key = (String) p1.getKey();
		    Hashstructure val = (Hashstructure) p1.getValue();
		    //System.out.println(key + " -- " + val._data + " -- " + val._metadata);
		    bw1.write(key + " -- " + val._data + " -- " + val._metadata + "\n");
		}
		while (it2.hasNext()) {
		    Map.Entry p2 = (Map.Entry) it2.next();
		    String key = (String) p2.getKey();
		    Hashstructure val = (Hashstructure) p2.getValue();
		    //System.out.println(key + " -- " + val._data + " -- " + val._metadata);
		    bw2.write(key + " -- " + val._data + " -- " + val._metadata + "\n");
		}
		bw1.close();
		bw2.close();
	    }

	    int BADflag = 0;
	    for (Map.Entry<String, Hashstructure> htEntries : sh.sourceContent.entrySet()) {
		if (sh.sourceContent.size() != sh.destinationContent.size()) {
		    System.out.println("- Item count didn't match!");
		    BADflag = 1;
		} else {
		    if (sh.destinationContent.containsKey(htEntries.getKey())) {
			if (!(sh.destinationContent.get(htEntries.getKey()).readdata().equals(htEntries.getValue().readdata()))
				&& (sh.destinationContent.get(htEntries.getKey()).readmetadata().equals(htEntries.getValue().readmetadata()))) {
			    BADflag = 1;
			    break;
			}
		    }
		}
	    }
	    if (BADflag == 1) {
		System.out.println("VERIFICATION FAILED");
	    } else {
		System.out.println("- Item count matched, so did the content!");
		System.out.println("PASSED VERIFICATION");
	    }
	}

    @SuppressWarnings("rawtypes")
	public static void populate_destTable(Stronghold sh, CouchbaseMetaClient dclient) 
	throws InterruptedException, ExecutionException {
	    Iterator it = sh.destinationContent.entrySet().iterator();
	    while (it.hasNext()) {
		Map.Entry pair = (Map.Entry)it.next();
		String key = (String) pair.getKey();
		Hashstructure val = (Hashstructure) pair.getValue();
		sh.storeinDTable(key, val._data, dclient.getReturnMeta(key).get());
	    }
	}
}
