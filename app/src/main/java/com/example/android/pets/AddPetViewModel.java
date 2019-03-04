package com.example.android.pets;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;

import com.example.android.pets.data.PetEntry;
import com.example.android.pets.data.PetsDatabase;

public class AddPetViewModel extends ViewModel {

    private LiveData<PetEntry> pet;

    public AddPetViewModel(PetsDatabase database, int petId) {
        pet = database.petDao().loadPetById(petId);
    }

    public LiveData<PetEntry> getPet(){
        return pet;
    }
}
