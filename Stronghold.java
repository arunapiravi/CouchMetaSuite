import java.util.HashMap;

import com.couchbase.client.MetaData;


public class Stronghold {

    private int _memQuota;
    private boolean _json;
    private int _itemCount;
    private int _itemSize;
    private double _expRatio;
    private int _expiration;
    private double _delRatio;
    private int _addCount;
    private boolean _replicationFirst_flag;
    private boolean biXDCR;
    private boolean parallelFrontEnds;
    private boolean _doVerify;
    private boolean _writetofile;

    public HashMap<String, Hashstructure> sourceContent = new HashMap<String, Hashstructure>();
    public HashMap<String, Hashstructure> destinationContent = new HashMap<String, Hashstructure>();

    public Stronghold () {
	_memQuota = 2000;
	_json = false;
	_itemCount = 10000;
	_itemSize = 256;
	_expRatio = 0.0;
	_expiration = 0;
	_delRatio = 0.0;
	_addCount = 0;
	_replicationFirst_flag = false;
	biXDCR = false;
	parallelFrontEnds = false;
	_writetofile = false;
	_doVerify = false;
    }

    public void setMemquota(int quota) {
	_memQuota = quota;
    }

    public void setJson(boolean flag) {
	_json = flag;
    }

    public void setItemcount(int count) {
	_itemCount = count;
    }

    public void setSize(int size) {
	_itemSize = size;
    }

    public void setExpRatio(double expires) {
	_expRatio = expires;
    }

    public void setExpiration(int exp) {
	_expiration = exp;
    }

    public void setDelRatio(double del) {
	_delRatio = del;
    }

    public void setAddCount(int addcount) {
	_addCount = addcount;
    }

    public void setReplicationFlag(boolean flag) {
	_replicationFirst_flag = flag;
    }

    public void setbiXDCR (boolean flag) {
	biXDCR = flag;
    }

    public void setparallel (boolean flag) {
	parallelFrontEnds = flag;
    }

    public void setdoVerify (boolean flag) {
	_doVerify = flag;
    }

    public void setaboutwrite (boolean flag) {
	_writetofile = flag;
    }

    public int getMemquota() {
	return _memQuota;
    }

    public boolean isJson() {
	return _json;
    }

    public int getItemcount() {
	return _itemCount;
    }

    public int getItemsize() {
	return _itemSize;
    }

    public double getExpRatio() {
	return _expRatio;
    }

    public int getExpiration() {
	return _expiration;
    }

    public double getDelRatio() {
	return _delRatio;
    }

    public int getAddCount() {
	return _addCount;
    }

    public boolean getReplicationFlag() {
	return _replicationFirst_flag;
    }

    public boolean getbiXDCR() {
	return biXDCR;
    }

    public boolean getparallel() {
	return parallelFrontEnds;
    }

    public boolean getdoVerify() {
	return _doVerify;
    }

    public boolean iswritetofile() { 
	return _writetofile;
    }

    public void storeinSTable(String K, String V, MetaData M) {
	sourceContent.put(K, new Hashstructure(V, M.toString()));
    }

    public Hashstructure retrievefromSTable(String K) {
	return sourceContent.get(K);
    }

    public void storeinDTable(String K, String V, MetaData M) {
	if (M == null)
	    destinationContent.put(K, new Hashstructure(V, null));
	else
	    destinationContent.put(K, new Hashstructure(V, M.toString()));
    }

    public Hashstructure retrievefromDTable(String K) {
	return destinationContent.get(K);
    }
}
