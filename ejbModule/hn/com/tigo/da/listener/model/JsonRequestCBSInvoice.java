package hn.com.tigo.da.listener.model;

public class JsonRequestCBSInvoice {

	private QueryObj queryObj;

	private InvoiceHeaderFilter invoiceHeaderFilter;

	private String retrieveDetail;

	private String totalRowNum;

	private String beginRowNum;

	private String fetchRowNum;

	public void setQueryObj(QueryObj queryObj) {
		this.queryObj = queryObj;
	}

	public QueryObj getQueryObj() {
		return this.queryObj;
	}

	public void setInvoiceHeaderFilter(InvoiceHeaderFilter invoiceHeaderFilter) {
		this.invoiceHeaderFilter = invoiceHeaderFilter;
	}

	public InvoiceHeaderFilter getInvoiceHeaderFilter() {
		return this.invoiceHeaderFilter;
	}

	public void setRetrieveDetail(String retrieveDetail) {
		this.retrieveDetail = retrieveDetail;
	}

	public String getRetrieveDetail() {
		return this.retrieveDetail;
	}

	public void setTotalRowNum(String totalRowNum) {
		this.totalRowNum = totalRowNum;
	}

	public String getTotalRowNum() {
		return this.totalRowNum;
	}

	public void setBeginRowNum(String beginRowNum) {
		this.beginRowNum = beginRowNum;
	}

	public String getBeginRowNum() {
		return this.beginRowNum;
	}

	public void setFetchRowNum(String fetchRowNum) {
		this.fetchRowNum = fetchRowNum;
	}

	public String getFetchRowNum() {
		return this.fetchRowNum;
	}
}
