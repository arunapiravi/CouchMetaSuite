import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutionException;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import net.spy.memcached.internal.OperationFuture;

import com.couchbase.client.CouchbaseMetaClient;
import com.couchbase.client.MetaData;

public class Addrunner {

    public static void adds (Stronghold sh, CouchbaseMetaClient _sclient, CouchbaseMetaClient _dclient, String prefix) 
	throws JSONException, InterruptedException, ExecutionException {
	    /*
	     * Module to create items through addrms' on the source cluster,
	     * and with the retrieved metaData, runs setwithmetas' on the
	     * destination cluster
	     */
	    ArrayList<DelayedOps> delayedadds = new ArrayList<DelayedOps>();
	    Random gen = new Random ( 987654321 );
	    StringBuffer value = new StringBuffer();
	    String CHAR_LIST = "ABCD";
	    while (value.length() < sh.getItemsize()) {
		value.append(CHAR_LIST);
	    }

	    List<OperationFuture<MetaData>> creates = new LinkedList<OperationFuture<MetaData>>();
	    for (int i=sh.getItemcount(); i<(sh.getItemcount() + sh.getAddCount()); i++) {
		OperationFuture<MetaData> addrm = null;
		OperationFuture<Boolean> addm = null;
		String key = String.format("%s%d", prefix, i);
		if (sh.isJson()) {
		    JSONObject _val = Spawner.retrieveJSON(gen, sh.getItemsize());
		    addrm = _sclient.addReturnMeta(key, 0, _val.toString());
		    assert(addrm.get() != null);
		    sh.storeinSTable(key, _val.toString(), addrm.get());
		    if (sh.getReplicationFlag()) {
			delayedadds.add(new DelayedOps(key, _val.toString(), addrm.get()));
		    } else {
			try {
			    addm = _dclient.setWithMeta(key, _val.toString(), addrm.get(), 0);
			} catch (Exception e) {
			    System.out.println("Add failed at destination, either because MetaData wasn't retreived from setrm");
			    if (addm.get().booleanValue() == false)
				System.out.println("Reason: " + addm.getStatus().getMessage());
			}
			assert(addm.get().booleanValue());
			sh.storeinDTable(key, _val.toString(), null);
		    }
		} else {
		    addrm = _sclient.addReturnMeta(key, 0, value.toString());
		    assert(addrm.get() != null);
		    sh.storeinSTable(key, value.toString(), addrm.get());
		    if (sh.getReplicationFlag()) {
			delayedadds.add(new DelayedOps(key, value.toString(), addrm.get()));
		    } else {
			try {
			    addm = _dclient.setWithMeta(key, value.toString(), addrm.get(), 0);
			} catch (Exception e) {
			    System.out.println("Add failed at destination, either because MetaData wasn't retreived from setrm");
			    if (addm.get().booleanValue() == false)
				System.out.println("Reason: " + addm.getStatus().getMessage());
			}
			assert(addm.get().booleanValue());			    
			sh.storeinDTable(key, value.toString(), null);
		    }
		}
		creates.add(addrm);
	    }

	    if (sh.getReplicationFlag()) {
		System.out.println("Wait for 10 seconds, before sending addMetas");
		Thread.sleep(10000);
		for (DelayedOps d : delayedadds) {
		    OperationFuture<Boolean> addm = _dclient.setWithMeta(d.getkey(), d.getval(), d.getmeta(), 0);
		    if (addm.get().booleanValue())
			sh.storeinDTable(d.getkey(), d.getval(), null);
		}
	    }

	    while (!creates.isEmpty()) {
		if (creates.get(0).isDone() == false){
		    System.err.println("Add failed");
		    continue;
		}
		creates.remove(0);
	    }
	}
}
