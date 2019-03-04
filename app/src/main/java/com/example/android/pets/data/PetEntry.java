package com.example.android.pets.data;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;

@Entity(tableName = "pet")
public class PetEntry {

    @PrimaryKey(autoGenerate = true)
    private int id;
    private String name;
    private String breed;
    private int gender;
    private int weight;

    public PetEntry(int id, String name, String breed, int gender, int weight) {
        this.id = id;
        this.name = name;
        this.breed = breed;
        this.gender = gender;
        this.weight = weight;
    }

    @Ignore
    public PetEntry(String name, String breed, int gender, int weight) {
        this.name = name;
        this.breed = breed;
        this.gender = gender;
        this.weight = weight;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBreed() {
        return breed;
    }

    public void setBreed(String breed) {
        this.breed = breed;
    }

    public int getGender() {
        return gender;
    }

    public void setGender(int gender) {
        this.gender = gender;
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }
}
