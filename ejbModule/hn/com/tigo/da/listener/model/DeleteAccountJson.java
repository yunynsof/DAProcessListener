package hn.com.tigo.da.listener.model;

import java.util.List;

public class DeleteAccountJson {

	private List<DeleteList> deleteList;

	public void setDeleteList(List<DeleteList> deleteList) {
		this.deleteList = deleteList;
	}

	public List<DeleteList> getDeleteList() {
		return this.deleteList;
	}
}
