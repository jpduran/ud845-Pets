package com.example.android.pets;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.support.annotation.NonNull;

import com.example.android.pets.data.PetEntry;
import com.example.android.pets.data.PetsDatabase;

import java.util.List;

public class CatalogViewModel extends AndroidViewModel {

    private LiveData<List<PetEntry>> pets;

    public CatalogViewModel(@NonNull Application application) {
        super(application);
        PetsDatabase database =
                PetsDatabase.getInstance(this.getApplication());
        pets = database.petDao().loadAllPets();
    }

    public LiveData<List<PetEntry>> getPets(){
        return pets;
    }
}
