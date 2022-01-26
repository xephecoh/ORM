package org.khamutov.orm;

import org.junit.jupiter.api.Test;
import org.khamutov.orm.entity.NoIdPerson;
import org.khamutov.orm.entity.Person;
import org.khamutov.orm.entity.NonTablePerson;
import org.khamutov.orm.exception.NoIdFieldInsideTableException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class QueryGeneratorTest {
    QueryGenerator queryGenerator = new DefaultQueryGenerator();
    DefaultQueryGenerator generator = new DefaultQueryGenerator();

    @Test
    public void findAllTest() {
        String expectedQuery = "SELECT person_id, name, age FROM Person;";
        String actualQuery = queryGenerator.findAll(Person.class);
        assertEquals(expectedQuery, actualQuery);
    }

    @Test
    public void getColumnsTest() {
        String expectedQuery = "person_id, name, age";
        String actualQuery = generator.getNamesOfColumns(Person.class).toString();
        assertEquals(expectedQuery, actualQuery);
    }

    @Test
    public void findByIdTest()  {
        String expectedQuery = "SELECT person_id, name, age FROM Person WHERE person_id = 5;";
        String actualQuery = queryGenerator.findById(5, Person.class);
        assertEquals(expectedQuery, actualQuery);
    }

    @Test
    public void findByIdExceptionTest() {
        Exception exception = assertThrows(NoIdFieldInsideTableException.class,
                () -> queryGenerator.findById(5, NoIdPerson.class));
        assertEquals("No field id inside class org.khamutov.orm.entity.NoIdPerson", exception.getMessage());
    }

    @Test
    public void findByIdTableMissingAnnotationExceptionTest()  {
        Exception exception = assertThrows(IllegalArgumentException.class,
                () -> queryGenerator.findById(5, NonTablePerson.class));
        assertEquals("class org.khamutov.orm.entity.NonTablePerson is not Table", exception.getMessage());
    }

    @Test
    public void illegalClassExceptionTest() {
        Class<String> stringClass = String.class;
        Exception ex = assertThrows(IllegalArgumentException.class,
                () -> queryGenerator.findById(5, stringClass));
        assertEquals(ex.getMessage(), stringClass.toString() + " is not Table");
    }

    @Test
    public void insertTest()  {
        String expectedQuery = "INSERT INTO Person (person_id, name, age) VALUES ('1', 'Victor', '27');";
        String actualQuery = queryGenerator.insert(new Person(1, "Victor", 27));
        assertEquals(expectedQuery, actualQuery);
    }

    @Test
    public void deleteTest()  {
        String expectedQuery = "DELETE FROM Person WHERE person_id = 1;";
        String actualQuery = queryGenerator.delete(new Person(1, "Victor", 27));
        assertEquals(expectedQuery, actualQuery);
    }

    @Test
    public void deleteWrongPersonTest()  {
        String expectedQuery = "DELETE FROM Person WHERE person_id = 1;";
        String actualQuery = queryGenerator.delete(new Person(1, "Victor", 27));
        assertEquals(expectedQuery, actualQuery);
    }
}