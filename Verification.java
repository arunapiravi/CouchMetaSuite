import java.util.Iterator;
import java.util.Map;

import com.couchbase.client.CouchbaseMetaClient;


public class Verification {

	public void comparison(Stronghold sh, CouchbaseMetaClient sclient, CouchbaseMetaClient dclient) {
		populate_destTable(sh, dclient);
		
		int BADflag = 0;
		for (Map.Entry<String, Hashstructure> htEntries : sh.sourceContent.entrySet()) {
			if (sh.destinationContent.containsKey(htEntries.getKey())) {
				if (!(sh.destinationContent.get(htEntries.getKey()).readdata().equals(htEntries.getValue().readdata()))
						&& (sh.destinationContent.get(htEntries.getKey()).readmetadata().equals(htEntries.getValue().readmetadata()))) {
					BADflag = 1;
					break;
				}
			}
		}
		if (BADflag == 1) {
			System.out.println("VERIFICATION FAILED");
		} else {
			System.out.println("PASSED VERIFICATION");
		}
	}

	public void populate_destTable(Stronghold sh, CouchbaseMetaClient dclient) {
		Iterator it = sh.destinationContent.entrySet().iterator();
		while (it.hasNext()) {
			// TODO
		}
	}
}
