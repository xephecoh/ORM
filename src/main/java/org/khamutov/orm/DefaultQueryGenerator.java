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

    private String getTableName(Class<?> clazz) {
        Table tableAnnotation = clazz.getAnnotation(Table.class);
        String getTableName = !tableAnnotation.name().isEmpty() ? tableAnnotation.name() : clazz.getName();
        return getTableName;
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
        String fieldName = !columnAnnotation.name().isEmpty() ? columnAnnotation.name() : field.getName();
        return fieldName;
    }


    @Override
    public String findById(Serializable id, Class<?> clazz) {
        checkIfTable(clazz);
        StringBuilder result = new StringBuilder("SELECT ");
        String tableName = getTableName(clazz);
        String idFieldInTable = null;
        StringJoiner columnNames = new StringJoiner(", ");
        for (Field declaredField : clazz.getDeclaredFields()) {
            Column columnAnnotation = declaredField.getAnnotation(Column.class);
            Id idAnnotation = declaredField.getAnnotation(Id.class);
            if (columnAnnotation != null & idAnnotation != null) {
                idFieldInTable = getFieldName(columnAnnotation, declaredField);
            }
            if (idFieldInTable == null) {
                throw new NoIdFieldInsideTableException("No field id inside " + clazz.toString());
            }
            if (columnAnnotation != null) {
                String fieldName = getFieldName(columnAnnotation, declaredField);
                columnNames.add(fieldName);
            }
        }
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
        Table tableAnnotation = person.getClass().getAnnotation(Table.class);
        if (tableAnnotation == null) {
            throw new IllegalArgumentException("Class is not entity");
        }
        checkIfTable(person.getClass());
        //String tableName = !tableAnnotation.name().isEmpty() ? tableAnnotation.name() : person.getClass().getName();
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
        Table tableAnnotation = person.getClass().getAnnotation(Table.class);
        /*if (tableAnnotation == null) {
            throw new IllegalArgumentException("Class is not entity");
        }*/
        checkIfTable(person.getClass());
        String tableName = getTableName(person.getClass());
        StringBuilder stringBuilder = new StringBuilder("DELETE FROM ");
        stringBuilder.append(tableName);
        extractIdColumnNameAndValue(person, stringBuilder);
        return stringBuilder.toString();
    }

    private void extractIdColumnNameAndValue(Object person, StringBuilder stringBuilder) {
        Object fieldValue = null;
        for (Field declaredField : person.getClass().getDeclaredFields()) {
            Id idAnnotation = declaredField.getAnnotation(Id.class);
            Column annotation = declaredField.getAnnotation(Column.class);
            String fieldName = getFieldName(annotation, declaredField);
            if (idAnnotation != null) {
                try {
                    declaredField.setAccessible(true);
                    fieldValue = declaredField.get(person);
                } catch (IllegalAccessException e) {
                    System.out.println(e.getMessage());
                }
                stringBuilder.append(" WHERE ")
                        .append(fieldName)
                        .append(" = ")
                        .append(fieldValue.toString())
                        .append(";");
                break;
            }
        }
        if(fieldValue==null){
            throw new NoIdFieldInsideTableException("No field id inside " + person.getClass().toString());
        }
    }

    private void checkIfTable(Class<?> clazz) {
        Table annotation = clazz.getAnnotation(Table.class);
        if (annotation == null) {
            throw new IllegalArgumentException(clazz.toString() + " is not Table");
        }
    }
}