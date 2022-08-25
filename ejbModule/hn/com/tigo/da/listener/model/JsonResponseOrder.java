package hn.com.tigo.da.listener.model;

public class JsonResponseOrder {

	private String Name;

	private String PaymentType;

	private String IDNumber;

	private String RespCode;

	private String CNumber;

	public void setName(String Name) {
		this.Name = Name;
	}

	public String getName() {
		return this.Name;
	}

	public void setPaymentType(String PaymentType) {
		this.PaymentType = PaymentType;
	}

	public String getPaymentType() {
		return this.PaymentType;
	}

	public void setIDNumber(String IDNumber) {
		this.IDNumber = IDNumber;
	}

	public String getIDNumber() {
		return this.IDNumber;
	}

	public void setRespCode(String RespCode) {
		this.RespCode = RespCode;
	}

	public String getRespCode() {
		return this.RespCode;
	}

	public void setCNumber(String CNumber) {
		this.CNumber = CNumber;
	}

	public String getCNumber() {
		return this.CNumber;
	}
}
