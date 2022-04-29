package sk.project.spec;

import java.util.HashMap;
import java.util.Map;

public class Entity {
	private String id;
	private String name;
	private Map<String, String> fields = new HashMap<String, String>();
	private Map<String, Entity> entities = new HashMap<String, Entity>();

	// Needed because of JACKSON
	public Entity() {
	}

	public Entity(String id, String name) {
		this.id = id;
		this.name = name;
	}

	public void addField(String name, String value) {
		if (fields.get(name) != null) {
			fields.remove(name);
			fields.put(name, value);
			return;
		}

		fields.put(name, value);
	}

	public void addEntity(String name, Entity entity) {
		if (entities.get(name) != null) {
			entities.remove(name);
			entities.put(name, entity);
			return;
		}

		entities.put(name, entity);
	}

	public void removeField(String name) {
		if (fields.get(name) != null)
			fields.remove(name);
	}

	public void removeEntity(String name) {
		if (entities.get(name) != null)
			entities.remove(name);
	}

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public Map<String, String> getFields() {
		return fields;
	}

	public Map<String, Entity> getEntities() {
		return entities;
	}

	public void setId(String id) {
		this.id = id;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setFields(Map<String, String> fields) {
		this.fields = fields;
	}

	public void setEntities(Map<String, Entity> entities) {
		this.entities = entities;
	}

	@Override
	public String toString() {
		return "Entity [id=" + id + ", name=" + name + ", fields=" + fields + ", entities=" + entities + "]";
	}
}
