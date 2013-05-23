import java.util.Iterator;

import com.couchbase.client.CouchbaseMetaClient;


public class Verification {

	public void comparison(Stronghold sh, CouchbaseMetaClient sclient, CouchbaseMetaClient dclient) {
		populate_destTable(sh, dclient);
	}
	
	public void populate_destTable(Stronghold sh, CouchbaseMetaClient dclient) {
		Iterator it = sh.destinationContent.entrySet().iterator();
		while (it.hasNext()) {
			// TODO
		}
	}
}
