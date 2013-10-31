package src;

import com.couchbase.client.MetaData;

public class DelayedOps {
	public final String KEY;
	public final String VAL;
	public final MetaData META;
	
	public DelayedOps(String k, String v, MetaData m) {
		this.KEY = k;
		this.VAL = v;
		this.META = m;
	}
	
	public String getkey() {
		return this.KEY;
	}
	
	public String getval() {
		return this.VAL;
	}
	
	public MetaData getmeta() {
		return this.META;
	}
}
