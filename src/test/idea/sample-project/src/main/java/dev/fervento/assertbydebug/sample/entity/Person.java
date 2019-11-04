package dev.fervento.assertbydebug.sample.entity;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Person {
    private UUID uuid;
    private Long insuranceId;
    private String name;
    private String surname;
    private int age;
    private Instant lastAccess;
    private Gender gender;
    private List<Car> cars;
    private Person father;

    public Person(UUID uuid, Long insuranceId, String name, String surname, int age, Instant lastAccess, Gender gender, Person father) {
        this.uuid = uuid;
        this.insuranceId = insuranceId;
        this.name = name;
        this.surname = surname;
        this.age = age;
        this.lastAccess = lastAccess;
        this.gender = gender;
        this.father = father;
        this.cars = new ArrayList<>();
    }

    public enum Gender {
        MALE, FEMALE
    }

    public Person addCar(Car car) {
        this.cars.add(car);
        return this;
    }

    public UUID getUuid() {
        return uuid;
    }

    public Long getInsuranceId() {
        return insuranceId;
    }

    public String getName() {
        return name;
    }

    public String getSurname() {
        return surname;
    }

    public int getAge() {
        return age;
    }

    public Instant getLastAccess() {
        return lastAccess;
    }

    public Gender getGender() {
        return gender;
    }

    public List<Car> getCars() {
        return cars;
    }

    public Person getFather() {
        return father;
    }

    public void setFather(Person father) {
        this.father = father;
    }
}
