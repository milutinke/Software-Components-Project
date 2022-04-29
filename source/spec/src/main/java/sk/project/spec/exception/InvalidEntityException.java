package sk.project.spec.exception;

@SuppressWarnings("serial")
public class InvalidEntityException extends Exception {
	public InvalidEntityException(String message) {
		super(message);
	}
}
