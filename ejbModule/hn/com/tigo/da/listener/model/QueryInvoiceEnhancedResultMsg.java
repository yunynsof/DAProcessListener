package hn.com.tigo.da.listener.model;

public class QueryInvoiceEnhancedResultMsg {

	private String arc;

	private String ars;

	private String cbs;

	private QueryInvoiceEnhancedResult QueryInvoiceEnhancedResult;

	private ResultHeader ResultHeader;

	public void setArc(String arc) {
		this.arc = arc;
	}

	public String getArc() {
		return this.arc;
	}

	public void setArs(String ars) {
		this.ars = ars;
	}

	public String getArs() {
		return this.ars;
	}

	public void setCbs(String cbs) {
		this.cbs = cbs;
	}

	public String getCbs() {
		return this.cbs;
	}

	public void setQueryInvoiceEnhancedResult(QueryInvoiceEnhancedResult QueryInvoiceEnhancedResult) {
		this.QueryInvoiceEnhancedResult = QueryInvoiceEnhancedResult;
	}

	public QueryInvoiceEnhancedResult getQueryInvoiceEnhancedResult() {
		return this.QueryInvoiceEnhancedResult;
	}

	public void setResultHeader(ResultHeader ResultHeader) {
		this.ResultHeader = ResultHeader;
	}

	public ResultHeader getResultHeader() {
		return this.ResultHeader;
	}
}
