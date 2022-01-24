package orm.entities;

import orm.annotations.Column;
import orm.annotations.Table;

@Table(name = "Person")
public class Person {

    public Person(int id, String name, int age) {
        this.id = id;
        this.name = name;
        this.age = age;
    }

    @Column(name = "person_id")
    private  int id;

    @Column
    private String name;

    @Column
    private int age;

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getAge() {
        return age;
    }
}