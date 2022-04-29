package sk.project.spec.QueryOperations;

public class QOperationWithName implements IQueryOperation {
	private String name;

	public QOperationWithName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
