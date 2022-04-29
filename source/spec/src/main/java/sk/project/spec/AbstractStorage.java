package sk.project.spec;

import java.io.IOException;
import java.util.List;

import sk.project.spec.exception.InvalidCollectionException;
import sk.project.spec.exception.InvalidDataStoreException;
import sk.project.spec.exception.InvalidEntityException;
import sk.project.spec.exception.InvalidQueryProvidedException;

public abstract class AbstractStorage {
	public abstract void save(Collection collection, Entity entity)
			throws InvalidCollectionException, InvalidEntityException, IOException;

	public abstract void save(Collection collection, List<Entity> entitites)
			throws InvalidCollectionException, InvalidEntityException;

	public abstract List<MyPair<Entity, String>> findById(Collection collection, String id)
			throws InvalidCollectionException, IOException;

	public abstract List<MyPair<Entity, String>> findByName(Collection collection, String name)
			throws InvalidCollectionException, IOException;

	public abstract List<MyPair<Entity, String>> find(Collection collection, Query query)
			throws InvalidCollectionException, IOException, InvalidQueryProvidedException;

	public abstract boolean delete(Collection collection, String id) throws InvalidCollectionException, IOException;

	public abstract boolean delete(Collection collection, Query query)
			throws InvalidCollectionException, IOException, InvalidQueryProvidedException;

	public abstract DataStore createDataStore(String path, String name, String format) throws IOException;

	public abstract DataStore loadDataStore(String path)
			throws InvalidDataStoreException, IOException, ClassNotFoundException;

	public abstract void deleteDataStore() throws InvalidDataStoreException;

	public abstract void closeDataStore() throws InvalidDataStoreException, IOException;

	public abstract void setDataStore(DataStore dataStore);

	public abstract DataStore getDataStore();

	public abstract Collection createCollection(String name, int maxCapacity)
			throws IOException, InvalidDataStoreException;

	public abstract void deleteCollection(Collection collection) throws Exception;

	public abstract String getFormat();
}
