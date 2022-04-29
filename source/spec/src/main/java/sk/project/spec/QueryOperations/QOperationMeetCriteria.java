package sk.project.spec.QueryOperations;

import sk.project.spec.Criteria;

public class QOperationMeetCriteria implements IQueryOperation {
	private String field;
	private String value;
	private Criteria criteria;

	public QOperationMeetCriteria(String field, String value, Criteria criteria) {
		this.field = field;
		this.value = value;
		this.criteria = criteria;
	}

	public String getField() {
		return field;
	}

	public String getValue() {
		return value;
	}

	public Criteria getCriteria() {
		return criteria;
	}

	public void setField(String field) {
		this.field = field;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public void setCriteria(Criteria criteria) {
		this.criteria = criteria;
	}
}
