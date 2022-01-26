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

        checkIfTable(clazz);
        StringBuilder result = new StringBuilder("SELECT ");
        String tableName = getTableName(clazz);
        StringJoiner parameters = getNamesOfColumns(clazz);
        result.append(parameters)
                .append(" FROM ")
                .append(tableName)
                .append(";");
        return result.toString();
    }

    @Override
    public String findById(Serializable id, Class<?> clazz) {
        checkIfTable(clazz);
        StringBuilder result = new StringBuilder("SELECT ");
        String tableName = getTableName(clazz);
        StringJoiner columnNames = new StringJoiner(", ");
        String idFieldInTable = idFieldNameInTable(clazz);
        if (idFieldInTable == null) {
            throw new NoIdFieldInsideTableException("No field id inside " + clazz.toString());
        }
        extractColumnsNames(clazz, columnNames);
        result.append(columnNames)
                .append(" FROM ")
                .append(tableName)
                .append(" WHERE ")
                .append(idFieldInTable)
                .append(" = ")
                .append(id.toString())
                .append(";");
        return result.toString();
    }



    @Override
    public String insert(Object person) {
        checkIfTable(person.getClass());
        String tableName = getTableName(person.getClass());
        StringBuilder stringBuilder = new StringBuilder("INSERT INTO ");
        stringBuilder.append(tableName)
                .append(" (");
        StringJoiner columnNames = new StringJoiner(", ");
        StringJoiner fieldValues = new StringJoiner("', '", "('", "');");
        try {
            extractColumnsNamesAndValues(person, columnNames, fieldValues);
        } catch (IllegalAccessException e) {
            System.out.println(e.getMessage());
        }
        stringBuilder.append(columnNames)
                .append(") VALUES ")
                .append(fieldValues);
        return stringBuilder.toString();
    }

    private void extractColumnsNamesAndValues(Object person,
                                              StringJoiner columnNames,
                                              StringJoiner fieldValues) throws IllegalAccessException {

        for (Field declaredField : person.getClass().getDeclaredFields()) {
            Column annotation = declaredField.getAnnotation(Column.class);
            if (annotation != null) {
                declaredField.setAccessible(true);
                String fieldName = getFieldName(annotation, declaredField);
                columnNames.add(fieldName);
                Object value = declaredField.get(person);
                fieldValues.add(value.toString());
            }
        }
    }


    @Override
    public String delete(Object person) {
        checkIfTable(person.getClass());
        StringBuilder stringBuilder = new StringBuilder("DELETE FROM ");
        String tableName = getTableName(person.getClass());
        String idFieldName = idFieldNameInTable(person.getClass());
        if (idFieldName == null) {
            throw new NoIdFieldInsideTableException("No field id inside " + person.getClass().toString());
        }
        String fieldValue = getFieldValue(person);
        stringBuilder.append(tableName)
                .append(" WHERE ")
                .append(idFieldName)
                .append(" = ")
                .append(fieldValue)
                .append(";");
        return stringBuilder.toString();
    }


    private void checkIfTable(Class<?> clazz) {
        Table annotation = clazz.getAnnotation(Table.class);
        if (annotation == null) {
            throw new IllegalArgumentException(clazz.toString() + " is not Table");
        }
    }

    private String getTableName(Class<?> clazz) {
        Table tableAnnotation = clazz.getAnnotation(Table.class);
        return !tableAnnotation.name().isEmpty() ? tableAnnotation.name() : clazz.getName();
    }

    StringJoiner getNamesOfColumns(Class<?> clazz) {
        StringJoiner parameters = new StringJoiner(", ");
        for (Field declaredField : clazz.getDeclaredFields()) {
            Column columnAnnotation = declaredField.getAnnotation(Column.class);
            if (columnAnnotation != null) {
                String fieldName = getFieldName(columnAnnotation, declaredField);
                parameters.add(fieldName);
            }
        }
        return parameters;
    }

    private String getFieldName(Column columnAnnotation, Field field) {
        return !columnAnnotation.name().isEmpty() ? columnAnnotation.name() : field.getName();
    }

    public String idFieldNameInTable(Class<?> clazz) {
        for (Field declaredField : clazz.getDeclaredFields()) {
            Id annotation = declaredField.getAnnotation(Id.class);
            Column columnAnnotation = declaredField.getAnnotation(Column.class);
            if (annotation != null) {
                return getFieldName(columnAnnotation, declaredField);
            }
        }
        return null;
    }

    public String getFieldValue(Object person) {
        Class<?> clazz = person.getClass();
        for (Field field : clazz.getDeclaredFields()) {
            Id idAnnotation = field.getAnnotation(Id.class);
            if (idAnnotation != null) {
                field.setAccessible(true);
                try {
                    Object o = field.get(person);
                    return String.valueOf(o);
                } catch (IllegalAccessException e) {
                    System.out.println(e.getMessage());
                }
            }
        }
        return null;
    }

    private void extractColumnsNames(Class<?> clazz, StringJoiner columnNames) {
        for (Field declaredField : clazz.getDeclaredFields()) {
            Column columnAnnotation = declaredField.getAnnotation(Column.class);
            if (columnAnnotation != null) {
                columnNames.add(getFieldName(columnAnnotation, declaredField));
            }
        }
    }

}