package sk.project.spec;

public class StorageManager {
	private static AbstractStorage dataStorage = null;
	
	public static void register(AbstractStorage newDataStorage) {
		dataStorage = newDataStorage;
	}
	
	public static AbstractStorage getStorage() {
		return dataStorage;
	}
}
