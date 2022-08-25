package hn.com.tigo.da.listener.model;

import java.util.List;

public class QueryInvoiceEnhancedResult {

	private List<InvoiceInfo> InvoiceInfo;

	private int TotalRowNum;

	public void setInvoiceInfo(List<InvoiceInfo> InvoiceInfo) {
		this.InvoiceInfo = InvoiceInfo;
	}

	public List<InvoiceInfo> getInvoiceInfo() {
		return this.InvoiceInfo;
	}

	public void setTotalRowNum(int TotalRowNum) {
		this.TotalRowNum = TotalRowNum;
	}

	public int getTotalRowNum() {
		return this.TotalRowNum;
	}
}
