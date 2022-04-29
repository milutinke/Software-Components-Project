package sk.project.spec.QueryOperations;

public class QOperationHasField implements IQueryOperation {
	private String field;

	public QOperationHasField(String field) {
		this.field = field;
	}

	public String getField() {
		return field;
	}

	public void setField(String field) {
		this.field = field;
	}
}