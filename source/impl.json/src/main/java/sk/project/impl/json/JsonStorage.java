package sk.project.impl.json;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import sk.project.spec.AbstractStorage;
import sk.project.spec.Collection;
import sk.project.spec.Criteria;
import sk.project.spec.DataStore;
import sk.project.spec.Entity;
import sk.project.spec.MyPair;
import sk.project.spec.Query;
import sk.project.spec.StorageManager;
import sk.project.spec.exception.InvalidCollectionException;
import sk.project.spec.exception.InvalidDataStoreException;
import sk.project.spec.exception.InvalidEntityException;
import sk.project.spec.exception.InvalidQueryProvidedException;

public class JsonStorage extends AbstractStorage {
	private ObjectMapper objectMapper;
	private DataStore currentDataStore = null;

	// Register the Storage
	static {
		StorageManager.register(new JsonStorage());
	}

	public JsonStorage() {
		this.objectMapper = new ObjectMapper();
		this.currentDataStore = null;
	}

	private List<Entity> loadEntitiesFromFile(String path) throws IOException {
		// Load all entites from that file
		String text = new String(Files.readAllBytes(Paths.get(path)), StandardCharsets.UTF_8);

		// List for storing the entities
		List<Entity> loadedEntites;

		// Check if there is any data loaded, if so, convert it from JSON to List of
		// Entities
		if (text.length() > 0) {
			loadedEntites = objectMapper.readValue(text, new TypeReference<List<Entity>>() {
			});
		} else // If there is no loaded data, make a new list
			loadedEntites = new ArrayList<Entity>();

		return loadedEntites;
	}

	@Override
	public void save(Collection collection, Entity entity)
			throws InvalidCollectionException, InvalidEntityException, IOException {
		// Check if the valid collection was referenced
		if (collection == null)
			throw new InvalidCollectionException("Invalid collection!");

		boolean updateOperation = false;
		String filePath = "";

		// We need to save entity ID because of filter function, it reqieres objects to
		// be immutable in order to access them outside the scope of the function
		String entityID = entity.getId();

		// Check if the entity is already present in the DB and update it
		List<MyPair<Entity, String>> found = findById(collection, entityID);

		if (found.size() > 0) {
			// Get the first result, since we can not have more entities with same ID
			MyPair<Entity, String> resultPair = found.get(0);

			// Extract information from the pair and replace the current entity with the new
			// one
			entity = resultPair.getFirst();
			filePath = resultPair.getSecond();

			// Mark operation as update operation
			updateOperation = true;
		}

		// If we are insering a new entity
		if (!updateOperation) {
			// Get an avaliable file for storing
			filePath = collection.getAvaliableFile();
		}

		// List for storing the entities
		List<Entity> loadedEntites = loadEntitiesFromFile(filePath);

		// If we are updating the entity, we need to remove the existing one from the
		// list of loaded entities
		if (updateOperation)
			loadedEntites = loadedEntites.stream().filter(ent -> !ent.getId().equals(entityID))
					.collect(Collectors.toList());

		// Add an entity to the list of loaded entities
		loadedEntites.add(entity);

		// Rewrite the file with new data
		FileWriter fileWritter = new FileWriter(filePath, false);
		fileWritter.write(objectMapper.writeValueAsString(loadedEntites));
		fileWritter.close();

		// Update the collection number of entites in file tracking
		if (!updateOperation) {
			String[] pathComponents = filePath.split(Pattern.quote(File.separator));
			String lastComponent = pathComponents[pathComponents.length - 1];

			if (collection.getFiles().containsKey(lastComponent)) {
				int lastNum = collection.getFiles().get(lastComponent);
				collection.getFiles().remove(lastComponent);
				collection.getFiles().put(lastComponent, lastNum + 1);
				collection.serialze();
			}
		}
	}

	@Override
	public void save(Collection collection, List<Entity> entitites)
			throws InvalidCollectionException, InvalidEntityException {
		if (entitites == null)
			throw new InvalidEntityException("Invalid entities list!");

		entitites.forEach(entity -> {
			try {
				this.save(collection, entity);
			} catch (InvalidCollectionException e) {
				e.printStackTrace();
			} catch (InvalidEntityException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		});
	}

	@Override
	public List<MyPair<Entity, String>> findById(Collection collection, String id)
			throws InvalidCollectionException, IOException {
		if (collection == null)
			throw new InvalidCollectionException("Invalid collection provided!");

		// A list to pairs to store found entities to
		List<MyPair<Entity, String>> results = new ArrayList<MyPair<Entity, String>>();

		// Get a list of files from collection (full path to each file)
		List<String> collectionFiles = collection.getFilesWithFullPath();

		// Loop trought all files
		for (String path : collectionFiles) {
			// Load all entities from the file into a list
			List<Entity> loadedEntites = loadEntitiesFromFile(path);

			// Skip if we do not have any entites in the file
			if (loadedEntites.size() == 0)
				continue;

			// Store the filtered results
			List<Entity> foundEntities = new Query(loadedEntites).withId(id).execute();

			// Skip if we have not found the entity
			if (foundEntities.size() == 0)
				continue;

			// Get the first one since we are searching by ID and there can not be more
			// entites with the same ID
			Entity foundEntity = foundEntities.get(0);

			// Add the found entity to the list
			results.add(new MyPair<Entity, String>(foundEntity, path));
		}

		return results;
	}

	@Override
	public List<MyPair<Entity, String>> findByName(Collection collection, String name)
			throws InvalidCollectionException, IOException {
		if (collection == null)
			throw new InvalidCollectionException("Invalid collection provided!");

		// A list to pairs to store found entities to
		List<MyPair<Entity, String>> results = new ArrayList<MyPair<Entity, String>>();

		// Get a list of files from collection (full path to each file)
		List<String> collectionFiles = collection.getFilesWithFullPath();

		// Loop trought all files
		for (String path : collectionFiles) {
			// Load all entities from the file into a list
			List<Entity> loadedEntites = loadEntitiesFromFile(path);

			// Skip if we do not have any entites in the file
			if (loadedEntites.size() == 0)
				continue;

			// Store the filtered results
			List<Entity> foundEntities = new Query(loadedEntites).withName(name).execute();

			// Skip if we have not found the entity
			if (foundEntities.size() == 0)
				continue;

			foundEntities.forEach(ent -> results.add(new MyPair<Entity, String>(ent, path)));
		}

		return results;
	}

	@Override
	public List<MyPair<Entity, String>> find(Collection collection, Query query)
			throws InvalidCollectionException, IOException, InvalidQueryProvidedException {
		if (collection == null)
			throw new InvalidCollectionException("Invalid collection provided!");

		if (query == null)
			throw new InvalidQueryProvidedException("Invalid query povided!");

		// A list to pairs to store found entities to
		List<MyPair<Entity, String>> results = new ArrayList<MyPair<Entity, String>>();

		// Get a list of files from collection (full path to each file)
		List<String> collectionFiles = collection.getFilesWithFullPath();

		// Loop trought all files
		for (String path : collectionFiles) {
			// Load all entities from the file into a list
			List<Entity> loadedEntites = loadEntitiesFromFile(path);

			// Skip if we do not have any entites in the file
			if (loadedEntites.size() == 0)
				continue;

			// Pass the entities to the query
			query.setEntities(loadedEntites);

			// Execute the query
			List<Entity> foundEntities = query.execute();

			// Skip if we have not found the entity
			if (foundEntities.size() == 0)
				continue;

			foundEntities.forEach(ent -> results.add(new MyPair<Entity, String>(ent, path)));
		}

		return results;
	}

	@Override
	public boolean delete(Collection collection, String id) throws InvalidCollectionException, IOException {
		try {
			return this.delete(collection, new Query().meetCriteria(">ID", id, Criteria.NOT_EQUALS));
		} catch (InvalidQueryProvidedException e) {
			return false;
		}
	}

	@Override
	public boolean delete(Collection collection, Query query)
			throws InvalidCollectionException, IOException, InvalidQueryProvidedException {
		if (collection == null)
			throw new InvalidCollectionException("Invalid collection provided!");

		if (query == null)
			throw new InvalidQueryProvidedException("Invalid query povided!");

		// Get a list of files from collection (full path to each file)
		List<String> collectionFiles = collection.getFilesWithFullPath();

		// Loop trought all files
		for (String path : collectionFiles) {
			// Load all entities from the file into a list
			List<Entity> loadedEntites = loadEntitiesFromFile(path);

			// Skip if we do not have any entites in the file
			if (loadedEntites.size() == 0)
				continue;

			query.setEntities(loadedEntites);
			List<Entity> newList = query.execute();

			// If the entity has not been found in this file, skip
			if (newList.size() == loadedEntites.size())
				continue;

			// Rewrite the file with new data
			FileWriter fileWritter = new FileWriter(path, false);
			fileWritter.write(objectMapper.writeValueAsString(newList));
			fileWritter.close();

			// Update the collection number of entites in file tracking
			String[] pathComponents = path.split(Pattern.quote(File.separator));
			String lastComponent = pathComponents[pathComponents.length - 1];

			if (collection.getFiles().containsKey(lastComponent)) {
				collection.getFiles().remove(lastComponent);
				collection.getFiles().put(lastComponent, newList.size());
				collection.serialze();
			}

			return true;
		}

		return false;
	}

	@Override
	public DataStore createDataStore(String path, String name, String format) throws IOException {
		// Create the object
		DataStore newDataStore = new DataStore(path, name, format);

		// Save it in the file system
		newDataStore.serialize();

		// Return the object
		return newDataStore;
	}

	@Override
	public DataStore loadDataStore(String path) throws ClassNotFoundException, InvalidDataStoreException, IOException {
		return DataStore.deserialize(path);
	}

	@Override
	public void deleteDataStore() throws InvalidDataStoreException {
		// Check if the data store object exists
		if (this.currentDataStore == null)
			throw new InvalidDataStoreException("Invalid data store, please load one using loadDataStore method!");

		// Delete it from the file system
		this.currentDataStore.delete();

		// Destroy the object
		this.currentDataStore = null;
	}

	@Override
	public void closeDataStore() throws InvalidDataStoreException, IOException {
		// Check if the data store object exists
		if (this.currentDataStore == null)
			throw new InvalidDataStoreException("Invalid data store, please load one using loadDataStore method!");

		// Save it to the file system
		this.currentDataStore.serialize();

		// Delete the object
		this.currentDataStore = null;
	}

	@Override
	public Collection createCollection(String name, int maxCapacity) throws IOException, InvalidDataStoreException {
		// Check if the data store object exists
		if (this.currentDataStore == null)
			throw new InvalidDataStoreException("Invalid data store, please load one using loadDataStore method!");

		return this.currentDataStore.createNewCollection(name, maxCapacity);
	}

	@Override
	public void deleteCollection(Collection collection) throws Exception {
		// TODO: Implement
		throw new Exception("Unimplemented");
	}

	@Override
	public void setDataStore(DataStore dataStore) {
		this.currentDataStore = dataStore;
	}

	@Override
	public DataStore getDataStore() {
		return currentDataStore;
	}

	@Override
	public String getFormat() {
		return "json";
	}
}
