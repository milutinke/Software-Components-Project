package sk.project.spec;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import sk.project.spec.QueryOperations.IQueryOperation;
import sk.project.spec.QueryOperations.QOperationHasField;
import sk.project.spec.QueryOperations.QOperationMeetCriteria;
import sk.project.spec.QueryOperations.QOperationWithId;
import sk.project.spec.QueryOperations.QOperationWithName;
import sk.project.spec.QueryOperations.QOperationWithoutId;
import sk.project.spec.QueryOperations.QOperationWithoutName;

public class Query {
	private List<IQueryOperation> operations = new ArrayList<IQueryOperation>();
	private List<Entity> entities;
	private StringBuilder textualFrom;

	public Query() {
		this.textualFrom = new StringBuilder();
		this.textualFrom.append("FETCH ");
	}

	public Query(List<Entity> entities) {
		this.entities = entities;
		this.textualFrom = new StringBuilder();
		this.textualFrom.append("FETCH ");
	}

	public Query withId(String id) {
		operations.add(new QOperationWithId(id));
		return this;
	}

	public Query withoutId(String id) {
		operations.add(new QOperationWithoutId(id));
		return this;
	}

	public Query withName(String name) {
		operations.add(new QOperationWithName(name));
		return this;
	}

	public Query withoutName(String name) {
		operations.add(new QOperationWithoutName(name));
		return this;
	}

	public Query hasField(String name) {
		operations.add(new QOperationHasField(name));
		return this;
	}

	public Query meetCriteria(String field, String value, Criteria criteria) {
		operations.add(new QOperationMeetCriteria(field, value, criteria));
		return this;
	}

	private void filter(Predicate<? super Entity> predicate) {
		if (this.entities == null)
			return;

		this.entities = this.entities.stream().filter(predicate).collect(Collectors.toList());
	}

	private boolean processCriteria(Entity entity, String field, String value, Criteria criteria) {
		String first = null;

		textualFrom.append("CRITERIA ( HAS FIELD = '");

		// Trying to access nested entity field
		if (field.contains(".")) {
			MyPair<String, String> newValuePair = getValueIfExists(entity, field, 0);

			if (newValuePair != null) {
				field = newValuePair.getFirst();
				first = newValuePair.getSecond();
			}

			textualFrom.append(field);
			textualFrom.append("'; ");
		} else if (field.contains(">")) {
			field = field.substring(field.lastIndexOf(">") + 1).trim().toLowerCase();

			if (field.equals("id"))
				first = entity.getId();
			else if (field.equals("name"))
				first = entity.getName();
			else
				return false;

		} else {
			if (!entity.getFields().containsKey(field)) {

				textualFrom.append(field);
				textualFrom.append(" '; ");
				return false;
			}

			first = entity.getFields().get(field);
		}

		if (first == null) {
			textualFrom.append(field);
			textualFrom.append(" '; ");
			return false;
		}

		boolean result = false;

		textualFrom.append("FIRST = '");
		textualFrom.append(first);
		textualFrom.append("' ");

		switch (criteria) {
		case EQUALS:
			textualFrom.append("EQUALS");

			result = first.equals(value);
			break;

		case EQUALS_IGNORE_CASE:
			textualFrom.append("EQUALS IGNORE CASE");
			result = first.equalsIgnoreCase(value);
			break;

		case NOT_EQUALS:
			textualFrom.append("NOT EQUALS");
			result = !first.equals(value);
			break;

		case CONTAINS:
			textualFrom.append("CONTAINS");
			result = first.contains(value);
			break;

		case NOT_CONTAINS:
			textualFrom.append("NOT CONTAINS");
			result = !first.contains(value);
			break;
		}

		textualFrom.append(" ");
		textualFrom.append("SECOND = '");
		textualFrom.append(value);
		textualFrom.append("'; );");

		return result;
	}

	public MyPair<String, String> getValueIfExists(Entity entity, String field, int level) {
		if (entity.getFields().size() == 0)
			return null;

		List<String> path = Arrays.asList(field.split("\\."));

		if (path.size() == 0)
			return null;

		if (level > path.size())
			return null;

		String currentPart = path.get(level).trim();

		if (entity.getEntities().size() > 0) {
			Iterator<Entry<String, Entity>> iterator = entity.getEntities().entrySet().iterator();

			while (iterator.hasNext()) {
				Map.Entry<String, Entity> pair = (Map.Entry<String, Entity>) iterator.next();

				if (pair.getKey().equals(currentPart)) {
					Entity subEntity = pair.getValue();

					if (level + 1 >= path.size())
						return null;

					String subfield = path.get(level + 1);

					if (subEntity.getFields().containsKey(subfield))
						return new MyPair<String, String>(subfield, subEntity.getFields().get(subfield));

					return getValueIfExists(subEntity, field, level + 1);
				}
			}
		}

		return null;
	}

	public List<Entity> execute() {
		for (IQueryOperation operation : operations) {
			if (operation instanceof QOperationMeetCriteria) {
				QOperationMeetCriteria currentOperation = (QOperationMeetCriteria) operation;
				filter(ent -> processCriteria(ent, currentOperation.getField(), currentOperation.getValue(),
						currentOperation.getCriteria()));
			}

			if (operation instanceof QOperationHasField) {
				QOperationHasField currentOperation = (QOperationHasField) operation;

				textualFrom.append("HAS FIELD = '");
				textualFrom.append(currentOperation.getField());
				textualFrom.append("'; ");

				filter(ent -> ent.getFields().containsKey(currentOperation.getField()));
			}

			if (operation instanceof QOperationHasField) {
				QOperationHasField currentOperation = (QOperationHasField) operation;

				textualFrom.append("HAS FIELD = '");
				textualFrom.append(currentOperation.getField());
				textualFrom.append("'; ");

				filter(ent -> ent.getFields().containsKey(currentOperation.getField()));
			}

			if (operation instanceof QOperationWithId) {
				QOperationWithId currentOperation = (QOperationWithId) operation;

				textualFrom.append("WITH ID = '");
				textualFrom.append(currentOperation.getId());
				textualFrom.append("'; ");

				filter(ent -> ent.getId().equals(currentOperation.getId()));
			}

			if (operation instanceof QOperationWithoutId) {
				QOperationWithId currentOperation = (QOperationWithoutId) operation;

				textualFrom.append("WITHOUT ID = '");
				textualFrom.append(currentOperation.getId());
				textualFrom.append("'; ");

				filter(ent -> !ent.getId().equals(currentOperation.getId()));
			}

			if (operation instanceof QOperationWithName) {
				QOperationWithName currentOperation = (QOperationWithName) operation;

				textualFrom.append("WITH NAME = '");
				textualFrom.append(currentOperation.getName());
				textualFrom.append("'; ");

				filter(ent -> ent.getName().equals(currentOperation.getName()));
			}

			if (operation instanceof QOperationWithoutName) {
				QOperationWithoutName currentOperation = (QOperationWithoutName) operation;

				textualFrom.append("WITHOUT NAME = '");
				textualFrom.append(currentOperation.getName());
				textualFrom.append("'; ");

				filter(ent -> !ent.getName().equals(currentOperation.getName()));
			}
		}

		return entities;
	}

	public String textual() {
		return textualFrom.toString();
	}

	public List<Entity> getEntities() {
		return entities;
	}

	public void setEntities(List<Entity> entities) {
		this.entities = entities;
	}
}
