package org.khamutov.orm;

import org.khamutov.orm.annotations.Column;
import org.khamutov.orm.annotations.Id;
import org.khamutov.orm.annotations.Table;
import org.khamutov.orm.exception.NoIdFieldInsideTableException;


import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.StringJoiner;

public class DefaultQueryGenerator implements QueryGenerator {
    @Override
    public String findAll(Class<?> clazz) {
        Table tableAnnotation = clazz.getAnnotation(Table.class);
        if (tableAnnotation == null) {
            throw new IllegalArgumentException(clazz.getName() + " is not a table");
        }
        StringBuilder result = new StringBuilder("SELECT ");
        String tableName = !tableAnnotation.name().isEmpty() ? tableAnnotation.name() : clazz.getName();
        StringJoiner parameters = new StringJoiner(", ");
        extractNameOfColumns(clazz, parameters);
        result.append(parameters)
                .append(" FROM ")
                .append(tableName)
                .append(";");
        return result.toString();
    }

    private void extractNameOfColumns(Class<?> clazz, StringJoiner parameters) {
        for (Field declaredField : clazz.getDeclaredFields()) {
            Column columnAnnotation = declaredField.getAnnotation(Column.class);
            if (columnAnnotation != null) {
                String fieldName = !columnAnnotation.name().isEmpty() ? columnAnnotation.name() : declaredField.getName();
                parameters.add(fieldName);
            }
        }
    }

    @Override
    public String findById(Serializable id, Class<?> clazz) {
        Table tableAnnotation = clazz.getAnnotation(Table.class);
        if (tableAnnotation == null) {
            throw new IllegalArgumentException(clazz.toString() + " is not Table");
        }
        StringBuilder result = new StringBuilder("SELECT ");
        String tableName = !tableAnnotation.name().isEmpty() ? tableAnnotation.name() : clazz.getName();
        String idFieldInTable = null;
        StringJoiner columnNames = new StringJoiner(", ");
        for (Field declaredField : clazz.getDeclaredFields()) {
            Column columnAnnotation = declaredField.getAnnotation(Column.class);
            Id idAnnotation = declaredField.getAnnotation(Id.class);
            if (columnAnnotation != null & idAnnotation != null) {
                idFieldInTable = !columnAnnotation.name().isEmpty() ? columnAnnotation.name() : declaredField.getName();
            }
            if (columnAnnotation != null) {
                String fieldName = !columnAnnotation.name().isEmpty() ? columnAnnotation.name() : declaredField.getName();
                columnNames.add(fieldName);
            }
        }
        if (idFieldInTable != null) {
            result.append(columnNames)
                    .append(" FROM ")
                    .append(tableName)
                    .append(" WHERE ")
                    .append(idFieldInTable)
                    .append(" = ")
                    .append(id.toString())
                    .append(";");
            return result.toString();
        } else {
            throw new NoIdFieldInsideTableException("No field id inside " + clazz.toString());
        }
    }

    @Override
    public String insert(Object person) throws NoSuchFieldException, IllegalAccessException {
        Table tableAnnotation = person.getClass().getAnnotation(Table.class);
        if (tableAnnotation == null) {
            throw new IllegalArgumentException("Class is not entity");
        }
        String tableName = !tableAnnotation.name().isEmpty() ? tableAnnotation.name() : person.getClass().getName();
        StringBuilder stringBuilder = new StringBuilder("INSERT INTO ");
        stringBuilder.append(tableName)
                .append(" (");
        StringJoiner columnNames = new StringJoiner(", ");
        StringJoiner fieldValues = new StringJoiner("', '", "('", "');");
        extractColumnsNamesAndValues(person, person.getClass(), columnNames, fieldValues);
        stringBuilder.append(columnNames)
                .append(") VALUES ")
                .append(fieldValues);
        return stringBuilder.toString();
    }

    private void extractColumnsNamesAndValues(Object person,
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