package sk.project.spec.exception;

@SuppressWarnings("serial")
public class InvalidQueryProvidedException extends Exception {
	public InvalidQueryProvidedException(String message) {
		super(message);
	}
}
