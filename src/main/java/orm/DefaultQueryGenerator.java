package orm;

import orm.annotations.Column;
import orm.annotations.Table;
import orm.entities.Person;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.StringJoiner;

public class DefaultQueryGenerator implements QueryGenerator {
    @Override
    public String findAll(Class<?> clazz) {
        Table tableAnnotation = clazz.getAnnotation(Table.class);
        if (tableAnnotation == null) {
            throw new IllegalArgumentException("");
        }

        StringBuilder result = new StringBuilder("SELECT ");
        String tableName = !tableAnnotation.name().isEmpty() ? tableAnnotation.name() : clazz.getName();
        StringJoiner parameters = new StringJoiner(", ");

        for (Field declaredField : clazz.getDeclaredFields()) {
            Column columnAnnotation = declaredField.getAnnotation(Column.class);
            if (columnAnnotation != null) {
                String fieldName = !columnAnnotation.name().isEmpty() ? columnAnnotation.name() : declaredField.getName();
                parameters.add(fieldName);
            }
        }
        result.append(parameters)
                .append(" FROM ")
                .append(tableName)
                .append(";");
        return result.toString();
    }

    @Override
    public String findById(Serializable id, Class<?> clazz) {
        String idFieldInTable = null;
        Table tableAnnotation = clazz.getAnnotation(Table.class);
        if (tableAnnotation == null) {
            throw new IllegalArgumentException("");
        }
        StringBuilder result = new StringBuilder("SELECT * FROM ");
        String tableName = !tableAnnotation.name().isEmpty() ? tableAnnotation.name() : clazz.getName();
        for (Field declaredField : clazz.getDeclaredFields()) {
            Column annotation = declaredField.getAnnotation(Column.class);
            if (annotation != null & declaredField.getName().equals("id")) {
                idFieldInTable = !annotation.name().isEmpty() ? annotation.name() : declaredField.getName();
            }
        }
        if (idFieldInTable != null) {
            result.append(tableName)
                    .append(" WHERE ")
                    .append(idFieldInTable)
                    .append(" = ")
                    .append(id);
            return result.toString();
        } else {
            throw new RuntimeException("No field id inside table");
        }

    }

    @Override
    public String insert(Object person) {
        if (person instanceof Person) {
            Person p = (Person) person;
            Class clazz = person.getClass();
            Table tableAnnotation = (Table) clazz.getAnnotation(Table.class);
            if (tableAnnotation == null) {
                throw new IllegalArgumentException("Class is not entity");
            }
            String tableName = !tableAnnotation.name().isEmpty() ? tableAnnotation.name() : clazz.getName();
            StringBuilder stringBuilder = new StringBuilder("INSERT INTO " + tableName + " (");
            StringJoiner joiner = new StringJoiner(", ");

            for (Field declaredField : clazz.getDeclaredFields()) {
                Column annotation = declaredField.getAnnotation(Column.class);
                if (annotation != null) {
                    String fieldName = !annotation.name().isEmpty() ? annotation.name() : declaredField.getName();
                    joiner.add(fieldName);
                }
            }
            StringJoiner joiner1 = new StringJoiner(", ");
            stringBuilder.append(joiner)
                    .append(") VALUES (");
            joiner1.add("'" + p.getId() + "'")
                    .add("'" + p.getName() + "'")
                    .add("'" + p.getAge() + "'");

            stringBuilder.append(joiner1).append(");");
            return stringBuilder.toString();
        }
        return person.toString() + " is not instance of Person";
    }

    @Override
    public String delete(Object person) {
        String fieldName = null;
        if (person instanceof Person) {
            Table tableAnnotation = person.getClass().getAnnotation(Table.class);
            if (tableAnnotation == null) {
                throw new IllegalArgumentException("Class is not entity");
            }
            String tableName = !tableAnnotation.name().isEmpty() ? tableAnnotation.name() : person.getClass().getName();
            StringBuilder stringBuilder = new StringBuilder("DELETE FROM ");
            for (Field declaredField : person.getClass().getDeclaredFields()) {
                Column annotation = declaredField.getAnnotation(Column.class);
                if (annotation != null & declaredField.getName().equals("id")) {
                    fieldName = !annotation.name().isEmpty() ? annotation.name() : declaredField.getName();
                }
            }
            stringBuilder.append(tableName)
                    .append(" WHERE ")
                    .append(fieldName)
                    .append(" = ")
                    .append(((Person) person).getId())
                    .append(";");
            return stringBuilder.toString();
        } else return person.toString() + " is not instance of Person";
    }
}