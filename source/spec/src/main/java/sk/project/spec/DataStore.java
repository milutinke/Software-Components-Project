package sk.project.spec;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import sk.project.spec.exception.InvalidDataStoreException;

public class DataStore implements Serializable {
	private static final long serialVersionUID = 8784646600998791455L;
	private String path;
	private String name;
	private String format;
	private Map<String, Collection> collections = new HashMap<String, Collection>();

	public DataStore() {
	}

	public DataStore(String path, String name, String format) {
		this.path = path;
		this.name = name;
		this.format = format;
	}

	public Collection getCollectionByName(String collection) {
		return collections.get(collection);
	}

	private String getMetaFullPath() {
		return this.getFullPath(true);
	}

	public String getFullPath(boolean meta) {
		String path = this.getPath();
		StringBuilder stringBuilder = new StringBuilder();

		stringBuilder.append(path);

		if (!path.endsWith(File.separator))
			stringBuilder.append(File.separator);

		if (meta) {
			stringBuilder.append(this.name);
			stringBuilder.append(".dstore");
		}

		return stringBuilder.toString();
	}

	public String getPath() {
		return path;
	}

	public String getName() {
		return name;
	}

	public String getFormat() {
		return format;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setFormat(String format) {
		this.format = format;
	}

	public Map<String, Collection> getCollections() {
		return collections;
	}

	public void setCollections(Map<String, Collection> collections) {
		this.collections = collections;
	}

	public Collection createNewCollection(String name, int maxCapacity) throws IOException {
		Collection newCollection = new Collection(this.getFullPath(false), this.getFormat(), name, maxCapacity);
		newCollection.serialze();
		collections.put(name, newCollection);
		this.serialize();
		return newCollection;
	}

	public void serialize() throws IOException {
		JsonObject jsonObject = new JsonObject();
		jsonObject.addProperty("name", this.name);
		jsonObject.addProperty("path", this.path);
		jsonObject.addProperty("format", this.format);

		// Serialize all collection objects
		Iterator<Entry<String, Collection>> iterator = this.collections.entrySet().iterator();

		while (iterator.hasNext()) {
			Map.Entry<String, Collection> pair = (Map.Entry<String, Collection>) iterator.next();
			pair.getValue().serialze();
		}

		List<String> collectionsList = new ArrayList<String>(this.collections.keySet());
		JsonArray collectionsArray = new JsonArray();
		collectionsList.forEach(item -> collectionsArray.add(item));

		jsonObject.add("collections", collectionsArray);

		File dataStoreDirectory = new File(this.getFullPath(false));

		if (!dataStoreDirectory.exists())
			dataStoreDirectory.mkdir();

		File metaFile = new File(this.getMetaFullPath());

		if (metaFile.exists())
			metaFile.delete();

		File metaFileNew = new File(this.getMetaFullPath());
		FileWriter fileWriter = new FileWriter(metaFileNew);
		fileWriter.write(jsonObject.toString());
		fileWriter.close();
	}

	public static DataStore deserialize(String dataStorePath)
			throws InvalidDataStoreException, IOException, ClassNotFoundException {
		DataStore dataStore = new DataStore();

		String[] pathComponents = dataStorePath.split(Pattern.quote(File.separator));
		String lastComponent = pathComponents[pathComponents.length - 1];

		if (!lastComponent.contains(".dstore"))
			throw new InvalidDataStoreException("Provided path does is not a valid Data Store Meta Object file!");

		String meta = new String(Files.readAllBytes(Paths.get(dataStorePath)), Charset.defaultCharset());
		JsonObject metaObject = new JsonParser().parse(meta).getAsJsonObject();

		if (!metaObject.has("name"))
			throw new InvalidDataStoreException("Data Store Meta Object does not contain the name field!");

		if (!metaObject.has("path"))
			throw new InvalidDataStoreException("Data Store Meta Object does not contain the path field!");

		if (!metaObject.has("format"))
			throw new InvalidDataStoreException("Data Store Meta Object does not contain the format field!");

		if (!metaObject.has("collections"))
			throw new InvalidDataStoreException("Data Store Meta Object does not contain the collections field!");

		dataStore.setName(metaObject.get("name").getAsString());
		dataStore.setPath(metaObject.get("path").getAsString());
		dataStore.setFormat(metaObject.get("format").getAsString());

		JsonArray metaCollections = metaObject.getAsJsonArray("collections");
		Map<String, Collection> collections = new HashMap<String, Collection>();

		for (JsonElement jsonElement : metaCollections) {
			String name = jsonElement.getAsString();
			Collection collection = Collection.deserialize(dataStore.getPath(), name);

			if (!collection.getDataStorePath().equals(dataStore.getPath())) {
				collection.setDataStorePath(dataStore.getPath());
				collection.serialze();
			}

			collections.put(name, collection);
		}

		dataStore.setCollections(collections);

		return dataStore;
	}

	public void delete() throws InvalidDataStoreException {
		File dir = new File(this.getFullPath(false));

		if (!dir.exists())
			throw new InvalidDataStoreException("Data Store alerady does not exists!");

		this.deleteDirectory(dir);
	}

	private boolean deleteDirectory(File directoryToBeDeleted) {
		File[] allContents = directoryToBeDeleted.listFiles();
		if (allContents != null) {
			for (File file : allContents) {
				deleteDirectory(file);
			}
		}
		return directoryToBeDeleted.delete();
	}

	@Override
	public String toString() {
		return "DataStore [path=" + path + ", name=" + name + ", format=" + format + ", collections=" + collections
				+ "]";
	}
}
