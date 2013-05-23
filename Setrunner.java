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

public class Setrunner {

    public static void sets (Stronghold sh, CouchbaseMetaClient _sclient, CouchbaseMetaClient _dclient) 
	throws JSONException, InterruptedException, ExecutionException {
	    /*
	     * Module to create items through setrms' on the source cluster,
	     * and with the retrieved metaData, runs setwithmetas' on the
	     * destination cluster
	     */
	    ArrayList<DelayedOps> delayedsets = new ArrayList<DelayedOps>();
	    Random gen = new Random ( 987654321 );
	    StringBuffer value = new StringBuffer();
	    String CHAR_LIST = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
	    while (value.length() < sh.getItemsize()) {
		value.append(CHAR_LIST);
	    }

	    List<OperationFuture<MetaData>> creates = new LinkedList<OperationFuture<MetaData>>();
	    for (int i=0; i<(sh.getItemcount() - (int)((double)(sh.getItemcount()) * sh.getExpRatio())); i++) {
		OperationFuture<MetaData> setrm = null;
		OperationFuture<Boolean> setm = null;
		String key = String.format("%s%d", sh.getPrefix(), i);
		if (sh.isJson()) {
		    JSONObject _val = Spawner.retrieveJSON(gen, sh.getItemsize());
		    setrm = _sclient.setReturnMeta(key, 0, 0, _val.toString());
		    assert(setrm.get() != null);
		    //		    sh.storeinSTable(key, _val.toString(), setrm.get());
		    if (sh.getReplicationFlag()) {
			delayedsets.add(new DelayedOps(key, _val.toString(), setrm.get()));
		    } else {
			try {
			    setm = _dclient.setWithMeta(key, _val.toString(), setrm.get(), 0);
			} catch (Exception e) {
			    System.out.println("Set failed at destination, either because MetaData wasn't retreived from setrm");
			    if (setm.get().booleanValue() == false)
				System.out.println("Reason: " + setm.getStatus().getMessage());
			}
			assert(setm.get().booleanValue());
			//			    if (setm.get().booleanValue())
			//			    	sh.storeinDTable(key, _val.toString(), null);
		    }
		} else {
		    setrm = _sclient.setReturnMeta(key, 0, 0, value.toString());
		    assert(setrm.get() != null);
		    sh.storeinSTable(key, value.toString(), setrm.get());
		    if (sh.getReplicationFlag()) {
			delayedsets.add(new DelayedOps(key, value.toString(), setrm.get()));
		    } else {
			try {
			    setm = _dclient.setWithMeta(key, value.toString(), setrm.get(), 0);
			} catch (Exception e) {
			    System.out.println("Set failed at destination, either because MetaData wasn't retreived from setrm");
			    if (setm.get().booleanValue() == false)
				System.out.println("Reason: " + setm.getStatus().getMessage());
			}
			assert(setm.get().booleanValue());
			//			    if (setm.get().booleanValue())
			//			    	sh.storeinDTable(key, value.toString(), null);
		    }
		}
		creates.add(setrm);
	    }
	    for (int i=(sh.getItemcount() - (int)((double)(sh.getItemcount()) * sh.getExpRatio())); i<sh.getItemcount(); i++) {
		OperationFuture<MetaData> setrm = null;
		OperationFuture<Boolean> setm = null;
		String key = String.format("%s%d", sh.getPrefix(), i);
		if (sh.isJson()) {
		    JSONObject _val = Spawner.retrieveJSON(gen, sh.getItemsize());
		    setrm = _sclient.setReturnMeta(key, sh.getExpiration(), 0, _val.toString());
		    assert(setrm.get() != null);
		    //		    sh.storeinSTable(key, _val.toString(), setrm.get());
		    if (sh.getReplicationFlag()) {
			delayedsets.add(new DelayedOps(key, _val.toString(), setrm.get()));
		    } else {
			try {
			    setm = _dclient.setWithMeta(key, _val.toString(), setrm.get(), 0);
			} catch (Exception e) {
			    System.out.println("Set failed at destination, either because MetaData wasn't retreived from setrm");
			    if (setm.get().booleanValue() == false)
				System.out.println("Reason: " + setm.getStatus().getMessage());
			}
			assert(setm.get().booleanValue());
			//			    if (setm.get().booleanValue())
			//			    	sh.storeinDTable(key, _val.toString(), null);
		    }
		} else {
		    setrm = _sclient.setReturnMeta(key, sh.getExpiration(), 0, value.toString());
		    assert(setrm.get() != null);
		    //		    sh.storeinSTable(key, value.toString(), setrm.get());
		    if (sh.getReplicationFlag()) {
			delayedsets.add(new DelayedOps(key, value.toString(), setrm.get()));
		    } else {
			try {
			    setm = _dclient.setWithMeta(key, value.toString(), setrm.get(), 0);
			} catch (Exception e) {
			    System.out.println("Set failed at destination, either because MetaData wasn't retreived from setrm");
			    if (setm.get().booleanValue() == false)
				System.out.println("Reason: " + setm.getStatus().getMessage());
			}
			assert(setm.get().booleanValue());
			//			    if (setm.get().booleanValue())
			//			    	sh.storeinDTable(key, value.toString(), null);
		    }
		}
		creates.add(setrm);
	    }

	    if (sh.getReplicationFlag()) {
		System.out.println("Wait for 10 seconds, before sending setMetas");
		Thread.sleep(10000);
		for (DelayedOps d : delayedsets) {
		    OperationFuture<Boolean> setm = _dclient.setWithMeta(d.getkey(), d.getval(), d.getmeta(), 0);
		    assert(setm.get().booleanValue());
		    //	    		if (setm.get().booleanValue())
		    //	    			sh.storeinDTable(d.getkey(), d.getval(), null);
		}
	    }

	    while (!creates.isEmpty()) {
		if (creates.get(0).isDone() == false){
		    System.err.println("Set failed");
		    continue;
		}
		creates.remove(0);
	    }
	}
}
