package hn.com.tigo.da.listener.model;

public class TaxList {

	private long TaxAmt;
	private long CurrencyId;
	private long TaxCode;

	public long getTaxAmt() {
		return TaxAmt;
	}

	public void setTaxAmt(long taxAmt) {
		TaxAmt = taxAmt;
	}

	public long getCurrencyId() {
		return CurrencyId;
	}

	public void setCurrencyId(long currencyId) {
		CurrencyId = currencyId;
	}

	public long getTaxCode() {
		return TaxCode;
	}

	public void setTaxCode(long taxCode) {
		TaxCode = taxCode;
	}

}
