package org.khamutov.orm;

import java.io.Serializable;

public interface QueryGenerator {
    String findAll(Class<?> clazz);

    String findById(Serializable id, Class<?> clazz) throws NoSuchFieldException;

    String insert(Object value) throws NoSuchFieldException, IllegalAccessException;

    String delete(Object value) throws NoSuchFieldException, IllegalAccessException;
}