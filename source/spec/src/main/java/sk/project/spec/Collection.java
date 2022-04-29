package sk.project.spec;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

public class Collection implements Serializable {
	private static final long serialVersionUID = 4241078239688196438L;
	private String dataStorePath;
	private String dataStoreFormat;
	private String name;
	private Map<String, Integer> files; // File name, Capacity
	private Map<String, SimpleIndexData> index; // ID, <File, Offset>
	private int maxCapacity;

	public Collection(String dataStorePath, String dataStoreFormat, String name, int maxCapacity) {
		this.dataStorePath = dataStorePath;
		this.dataStoreFormat = dataStoreFormat;
		this.name = name;
		this.maxCapacity = maxCapacity;
		this.files = new HashMap<String, Integer>();
		this.index = new HashMap<String, SimpleIndexData>();
	}

	public boolean doesMetaExists() {
		File file = new File(this.getFullPath(true));
		return file.exists();
	}

	public String getFullPath() {
		return this.getFullPath(false);
	}

	public String getFullPath(boolean meta) {
		return this.getFullPath(this.getDataStorePath(), meta);
	}

	public String getFullPath(String path, boolean meta) {
		StringBuilder stringBuilder = new StringBuilder();

		stringBuilder.append(path);

		if (!path.endsWith(File.separator))
			stringBuilder.append(File.separator);

		stringBuilder.append(this.getName());
		stringBuilder.append(File.separator);

		if (meta) {
			stringBuilder.append(this.getName());
			stringBuilder.append(".cmeta");
		}

		return stringBuilder.toString();
	}

	public void serialze() throws IOException {
		if (this.doesMetaExists()) {
			File file = new File(this.getFullPath(true));
			file.delete();
		}

		File collectionFolder = new File(this.getFullPath(false));

		if (!collectionFolder.exists())
			collectionFolder.mkdir();

		File collectionFile = new File(this.getFullPath(true));

		if (!collectionFile.exists())
			collectionFile.createNewFile();

		FileOutputStream fileOut = new FileOutputStream(collectionFile);
		ObjectOutputStream out = new ObjectOutputStream(fileOut);
		out.writeObject(this);
		out.close();
		fileOut.close();
	}

	public static final Collection deserialize(String path, String name) throws IOException, ClassNotFoundException {
		StringBuilder stringBuilder = new StringBuilder();

		stringBuilder.append(path);

		if (!path.endsWith(File.separator))
			stringBuilder.append(File.separator);

		stringBuilder.append(name);
		stringBuilder.append(File.separator);
		stringBuilder.append(name);
		stringBuilder.append(".cmeta");

		String fullPath = stringBuilder.toString();

		File fullPathFile = new File(fullPath);

		if (!fullPathFile.exists())
			return null;

		FileInputStream fileIn = new FileInputStream(fullPath);
		ObjectInputStream in = new ObjectInputStream(fileIn);

		Collection collection = (Collection) in.readObject();
		in.close();
		fileIn.close();

		return collection;
	}

	public String getAvaliableFile() throws IOException {
		String fileName = "";

		if (files.size() == 0) {
			fileName = this.createNewFile();
			files.put(fileName, 0);
			this.serialze();
			return getFullPath() + "" + fileName;
		}

		Iterator<Entry<String, Integer>> iterator = files.entrySet().iterator();

		while (iterator.hasNext()) {
			Map.Entry<String, Integer> pair = (Map.Entry<String, Integer>) iterator.next();

			if (((int) pair.getValue()) >= this.maxCapacity) {
				fileName = this.createNewFile();
				files.put(fileName, 0);
				this.serialze();
				break;
			}

			fileName = pair.getKey();
			break;
		}

		return getFullPath() + "" + fileName;
	}

	public List<String> getFilesWithFullPath() throws IOException {
		List<String> filesList = new ArrayList<String>();
		Iterator<Entry<String, Integer>> iterator = files.entrySet().iterator();

		while (iterator.hasNext()) {
			Map.Entry<String, Integer> pair = (Map.Entry<String, Integer>) iterator.next();

			StringBuilder stringBuilder = new StringBuilder();
			stringBuilder.append(this.getFullPath());
			stringBuilder.append(pair.getKey());
			filesList.add(stringBuilder.toString());
		}

		return filesList;
	}

	private String createNewFile() throws IOException {
		String fileName = UUID.randomUUID().toString().concat("." + this.getDataStoreFormat());

		File file = new File(getFullPath() + "" + fileName);
		file.createNewFile();

		return fileName;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDataStorePath() {
		return dataStorePath;
	}

	public String getDataStoreFormat() {
		return dataStoreFormat;
	}

	public void setDataStorePath(String dataStorePath) {
		this.dataStorePath = dataStorePath;
	}

	public void setDataStoreFormat(String dataStoreFormat) {
		this.dataStoreFormat = dataStoreFormat;
	}

	public int getMaxCapacity() {
		return maxCapacity;
	}

	public void setMaxCapacity(int maxCapacity) {
		this.maxCapacity = maxCapacity;
	}

	public Map<String, Integer> getFiles() {
		return files;
	}

	public void setFiles(Map<String, Integer> files) {
		this.files = files;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	public Map<String, SimpleIndexData> getIndex() {
		return index;
	}

	public void setIndex(Map<String, SimpleIndexData> index) {
		this.index = index;
	}

	@Override
	public String toString() {
		return "Collection [dataStorePath=" + dataStorePath + ", dataStoreFormat=" + dataStoreFormat + ", name=" + name
				+ ", files=" + files + ", index=" + index + ", maxCapacity=" + maxCapacity + "]";
	}
}
