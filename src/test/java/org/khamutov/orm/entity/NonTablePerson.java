package org.khamutov.orm.entity;

import org.khamutov.orm.annotations.Column;
import org.khamutov.orm.annotations.Id;


public class NonTablePerson {

    public NonTablePerson(int id, String name, int age) {
        this.id = id;
        this.name = name;
        this.age = age;
    }
    @Id
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