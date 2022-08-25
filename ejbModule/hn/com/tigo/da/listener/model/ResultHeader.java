package hn.com.tigo.da.listener.model;

public class ResultHeader {

	private int Version;

	private int ResultCode;

	private int MsgLanguageCode;

	private String ResultDesc;

	public void setVersion(int Version) {
		this.Version = Version;
	}

	public int getVersion() {
		return this.Version;
	}

	public void setResultCode(int ResultCode) {
		this.ResultCode = ResultCode;
	}

	public int getResultCode() {
		return this.ResultCode;
	}

	public void setMsgLanguageCode(int MsgLanguageCode) {
		this.MsgLanguageCode = MsgLanguageCode;
	}

	public int getMsgLanguageCode() {
		return this.MsgLanguageCode;
	}

	public void setResultDesc(String ResultDesc) {
		this.ResultDesc = ResultDesc;
	}

	public String getResultDesc() {
		return this.ResultDesc;
	}
}
