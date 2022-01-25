import org.junit.jupiter.api.Test;
import orm.DefaultQueryGenerator;
import orm.QueryGenerator;
import orm.entities.Person;
import org.junit.jupiter.api.Assertions;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class QueryGeneratorTest {


    @Test
    public void findAllTest() {
        String expectedQuery = "SELECT person_id, name, age FROM Person;";
        QueryGenerator queryGenerator = new DefaultQueryGenerator();
        String actualQuery = queryGenerator.findAll(Person.class);

        assertEquals(expectedQuery, actualQuery);
    }

    @Test
    public void findByIdTest() {
        String expectedQuery = "SELECT * FROM Person WHERE person_id = 5;";
        QueryGenerator queryGenerator = new DefaultQueryGenerator();
        String actualQuery = queryGenerator.findById(5, Person.class);
        assertEquals(expectedQuery, actualQuery);
    }

    @Test
    public void insertTest() {
        String expectedQuery = "INSERT INTO Person (person_id, name, age) VALUES ('1', 'Victor', '27');";
        QueryGenerator queryGenerator = new DefaultQueryGenerator();
        String actualQuery = queryGenerator.insert(new Person(1, "Victor", 27));
        assertEquals(expectedQuery, actualQuery);
    }

    @Test
    public void deleteTest() {
        String expectedQuery = "DELETE FROM Person WHERE person_id = 1;";
        QueryGenerator queryGenerator = new DefaultQueryGenerator();
        String actualQuery = queryGenerator.delete(new Person(1, "Victor", 27));
        assertEquals(expectedQuery, actualQuery);
    }
}