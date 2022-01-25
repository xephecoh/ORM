package org.khamutov.orm;

import org.khamutov.orm.annotations.Column;
import org.khamutov.orm.annotations.Id;
import org.khamutov.orm.annotations.Table;


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
        Table tableAnnotation = clazz.getAnnotation(Table.class);
        if (tableAnnotation == null) {
            throw new IllegalArgumentException("");
        }
        StringBuilder result = new StringBuilder("SELECT * FROM ");
        String tableName = !tableAnnotation.name().isEmpty() ? tableAnnotation.name() : clazz.getName();
        String idFieldInTable = null;
        for (Field declaredField : clazz.getDeclaredFields()) {
            Column columnAnnotation = declaredField.getAnnotation(Column.class);
            Id idAnnotation = declaredField.getAnnotation(Id.class);
            if (columnAnnotation != null & idAnnotation != null) {
                idFieldInTable = !columnAnnotation.name().isEmpty() ? columnAnnotation.name() : declaredField.getName();
            }
        }
        if (idFieldInTable != null) {
            result.append(tableName)
                    .append(" WHERE ")
                    .append(idFieldInTable)
                    .append(" = ")
                    .append(id)
                    .append(";");
            return result.toString();
        } else {
            throw new RuntimeException("No field id inside table");
        }
    }

    @Override
    public String insert(Object person) throws NoSuchFieldException, IllegalAccessException {
        Class clazz = person.getClass();
        Table tableAnnotation = (Table) clazz.getAnnotation(Table.class);
        if (tableAnnotation == null) {
            throw new IllegalArgumentException("Class is not entity");
        }
        String tableName = !tableAnnotation.name().isEmpty() ? tableAnnotation.name() : clazz.getName();
        StringBuilder stringBuilder = new StringBuilder("INSERT INTO ");
        stringBuilder.append(tableName)
                .append(" (");
        StringJoiner columnNames = new StringJoiner(", ");
        StringJoiner fieldValues = new StringJoiner("', '", "('", "');");
        extractObjectColumnsNamesAndValues(person, clazz, columnNames, fieldValues);
        stringBuilder.append(columnNames)
                .append(") VALUES ")
                .append(fieldValues);

        return stringBuilder.toString();
    }

    private void extractObjectColumnsNamesAndValues(Object person,
                                                    Class clazz,
                                                    StringJoiner columnNames,
                                                    StringJoiner fieldValues) throws IllegalAccessException, NoSuchFieldException {
        for (Field declaredField : clazz.getDeclaredFields()) {
            declaredField.setAccessible(true);
            Column annotation = declaredField.getAnnotation(Column.class);
            if (annotation != null) {
                String fieldName = !annotation.name().isEmpty() ? annotation.name() : declaredField.getName();
                columnNames.add(fieldName);
                Object value = declaredField.get(person);
                fieldValues.add(value.toString());
            }
        }
    }

    @Override
    public String delete(Object person) throws NoSuchFieldException, IllegalAccessException {
        Table tableAnnotation = person.getClass().getAnnotation(Table.class);
        if (tableAnnotation == null) {
            throw new IllegalArgumentException("Class is not entity");
        }
        String tableName = !tableAnnotation.name().isEmpty() ? tableAnnotation.name() : person.getClass().getName();
        StringBuilder stringBuilder = new StringBuilder("DELETE FROM ");
        stringBuilder.append(tableName);
        for (Field declaredField : person.getClass().getDeclaredFields()) {
            Id idAnnotation = declaredField.getAnnotation(Id.class);
            Column annotation = declaredField.getAnnotation(Column.class);
            declaredField.setAccessible(true);
            String fieldName = !annotation.name().isEmpty() ? annotation.name() : declaredField.getName();
            Object fieldValue = declaredField.get(person);
            if (idAnnotation != null) {
                stringBuilder.append(" WHERE ")
                        .append(fieldName)
                        .append(" = ")
                        .append(fieldValue.toString())
                        .append(";");
            }
        }
        return stringBuilder.toString();
    }
}