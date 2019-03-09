package com.example.android.pets.data;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

@Dao
public interface PetDao {

    @Insert
    long insertPet(PetEntry petEntry);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    int updatePet(PetEntry petEntry);

    @Query("DELETE FROM pet WHERE id = :id")
    int deletePet(int id);

    @Query("SELECT * FROM pet")
    LiveData<List<PetEntry>> loadAllPets();

    @Query("SELECT * FROM pet WHERE id = :id")
    LiveData<PetEntry> loadPetById(int id);

    @Query("DELETE FROM pet")
    void deleteAllPets();
}
