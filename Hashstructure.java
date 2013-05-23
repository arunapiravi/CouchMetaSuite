
public class Hashstructure {
	public final String _data;
	public final String _metadata;
	
	public Hashstructure(String val, String met) {
		this._data = val;
		this._metadata = met;
	}

	public String readdata() {
		return this._data;
	}
	
	public String readmetadata() {
		return this._metadata;
	}
}
