package sk.project.app;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

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

public class TsAppMain {
	private AbstractStorage storage;
	private String FULL_PATH = System.getenv("APPDATA") + File.separator + "Dusan_Milutinovic_SK_Projekat_";

	public static void main(String[] args) throws Exception {
		// Koricenje:
		// "sk.project.impl.json.JsonStorage"
		// "sk.project.impl.yaml.YamlStorage"

		new TsAppMain(args[0]);
	}

	public TsAppMain(String class_) throws Exception {
		if (class_.equals("sk.project.impl.json.JsonStorage"))
			FULL_PATH += "JSON" + File.separator;
		else if (class_.equals("sk.project.impl.yaml.YamlStorage"))
			FULL_PATH += "YAML" + File.separator;
		else {
			throw new Exception("No more implementations!");
		}

		File folder = new File(FULL_PATH);

		if (folder.exists())
			deleteDirectory(folder);

		folder.mkdir();

		Class.forName(class_);
		storage = StorageManager.getStorage();

		Test1(); // Kreiranje Data Storea, Kolekcije, i Dodavanje entiteta u istu
		Test2(); // Ucitavanje data store-a i update eid-1 entiteta
	}

	// Kreiranje Data Storea, Kolekcije, i Dodavanje entiteta u istu
	public void Test1() throws IOException, InvalidDataStoreException, InvalidCollectionException,
			InvalidEntityException, InvalidQueryProvidedException {
		System.out.println("--------------------------------------------------------------");
		System.out.println("TEST 1");
		System.out.println("--------------------------------------------------------------");

		String dataStoreName = "Test_DS_1";
		String collectionName = "Test_Collection_1";

		// Kreiranje Data Store-a
		DataStore dataStore = storage.createDataStore(FULL_PATH + "Prvi_Datastore", dataStoreName, storage.getFormat());
		System.out.println("Kreirani Data Store: \n" + dataStore + "\n");

		storage.setDataStore(dataStore);

		// Kreiranje kolekcije
		System.out.println("Kreiranje kolekcije: \n" + storage.createCollection(collectionName, 3) + "\n");

		// Posto je maksimalni broj entiteta po fajlu 3, trebalo bi da imamo 2 datoteke
		// PS: Ovo ne ukljucuje ugnjezdene entitete

		// Kreiranje i dodavanje entiteta
		Entity entity1 = new Entity("eid-1", "First_Entity");
		entity1.addField("aa", "some value 123");
		entity1.addField("ab", "some value 321");

		Entity entity2 = new Entity(UUID.randomUUID().toString(), "Second_Entity");
		entity2.addField("ba", "test123");
		entity2.addField("bb", "___");

		Entity entity3 = new Entity(UUID.randomUUID().toString(), "Third_Entity");
		entity3.addField("ca", "   awdad  2354  ");
		entity3.addField("cb", "lll 11122 lll fff");

		// Dodavanje entiteta u drugi entitet
		entity2.addEntity(entity3.getName(), entity3);
		entity1.addEntity(entity2.getName(), entity2);

		Entity entity4 = new Entity(UUID.randomUUID().toString(), "Forth_Entity");
		entity4.addField("da", "eeerrttt");
		entity4.addField("db", "zzzzzzzzzz33433");

		Entity entity5 = new Entity(UUID.randomUUID().toString(), "Fifth_Entity");
		entity5.addField("ea", "tttttttt");
		entity5.addField("eb", "vvvvvbbbvbvb");

		Entity entity6 = new Entity(UUID.randomUUID().toString(), "Sixth_Entity");
		entity6.addField("fa", "fffffff");
		entity6.addField("fb", "ggggggg");

		System.out.println("Entiteti: \n");
		System.out.println(entity1 + "\n");
		System.out.println(entity4 + "\n");
		System.out.println(entity5 + "\n");
		System.out.println(entity6 + "\n");

		// Cuvanje entiteta u kolekciji
		Collection collection = storage.getDataStore().getCollectionByName(collectionName);

		// Kolekcija pre cuvanja
		System.out.println("Kolekcija pre cuvanja: \n" + collection + "\n");

		storage.save(collection, entity1);
		storage.save(collection, entity4);
		storage.save(collection, entity5);
		storage.save(collection, entity6);

		// Kolekcija nakon cuvanja
		System.out.println("Kolekcija nakon cuvanja: \n" + collection + "\n");

		// Pretraga po ID-u
		System.out.println("Da li postoji Entitet sa id 'eid-1'?:  \n" + storage.findById(collection, "eid-1") + "\n");

		// Pretraga po imenu
		System.out.println("Da li postoji Entitet sa imenom 'Forth_Entity'?:  \n"
				+ storage.findByName(collection, "Forth_Entity") + "\n");

		// Pretraga po kljucu ugnjezdenog entiteta
		System.out.println(
				"Da li postoji Entitet sa pod entitetom sa imenom 'Second_Entity' i poljem 'ba' koje ima vrednost 'test123'?:  \n"
						+ storage.find(collection,
								new Query().meetCriteria("Second_Entity.ba", "test123", Criteria.EQUALS))
						+ "\n");

		System.out.println(
				"Da li postoji Entitet sa pod entitetom sa imenom 'Second_Entity' koji ima pod entitet 'Third_Entity' koji ima polje 'cb' koje sadrzi 'd'?:  \n"
						+ storage.find(collection,
								new Query().meetCriteria("Second_Entity.Third_Entity.cb", "f", Criteria.CONTAINS))
						+ "\n");

		System.out.println(
				"Da li postoji Entitet sa pod entitetom sa imenom 'Second_Entity' koji ima pod entitet 'Third_Entity' koji ima polje 'ert' koje ima vrednost '22'?:  \n"
						+ storage.find(collection,
								new Query().meetCriteria("Second_Entity.Third_Entity.ert", "22", Criteria.CONTAINS))
						+ "\n");

		// Mogucnosti za Query
		// .hasField(name)
		// .withId(id)
		// .withName(name)
		// .withoutId(id)
		// .withoutName(name)
		// .meetCriteria(field, value, criteria)
		// field u meetCriteria moze biti ili polje ili polje ugnjezdenog entiteta
		// (Granjanje moze biti beskonacno ali je eksponencijalno vreme)

		// Kriterijumi:
		// Criteria.EQUALS
		// Criteria.NOT_EQUALS
		// Criteria.CONTAINS
		// Criteria.NOT_CONTAINS

		// Zatvaranje storage-a
		storage.closeDataStore();

		System.out.println("--------------------------------------------------------------");
	}

	// Ucitavanje data store-a i kolekcija putem putanje
	public void Test2() throws IOException, InvalidDataStoreException, InvalidCollectionException,
			InvalidEntityException, InvalidQueryProvidedException, ClassNotFoundException {
		System.out.println("--------------------------------------------------------------");
		System.out.println("TEST 2");
		System.out.println("--------------------------------------------------------------");

		String dataStoreName = "Test_DS_1";
		String collectionName = "Test_Collection_1";

		DataStore dataStore = storage
				.loadDataStore(FULL_PATH + "Prvi_Datastore" + File.separator + dataStoreName + ".dstore");
		System.out.println("Ucitani data store: \n" + dataStore + "\n");

		storage.setDataStore(dataStore);

		Collection collection = storage.getDataStore().getCollectionByName(collectionName);
		// Kolekcija pre cuvanja
		System.out.println("Kolekcija pre cuvanja: \n" + collection + "\n");

		// MyPair oznacava par Entiteta i lokacije fajla u kojem se nalazi
		List<MyPair<Entity, String>> results = storage.findById(collection, "eid-1");
		Entity entity1 = results.get(0).getFirst();

		System.out.println("Trazeni entitet (pre izmene): \n" + entity1 + "\n");

		entity1.addField("3rd_field", "33333");

		System.out.println("Trazeni entitet (nakon izmene): \n" + entity1 + "\n");

		// Apdejt entiteta 1
		storage.save(collection, entity1);

		System.out.println("Trazeni entitet (nakon ponovljene pretrage): \n" + entity1 + "\n");

		// Kolekcija nakon cuvanja
		System.out.println("Kolekcija nakon cuvanja: \n" + collection + "\n");

		storage.closeDataStore();

		System.out.println("--------------------------------------------------------------");
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
}
