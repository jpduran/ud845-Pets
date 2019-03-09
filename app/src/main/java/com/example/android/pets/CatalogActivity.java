/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.android.pets;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.android.pets.data.PetEntry;
import com.example.android.pets.data.PetsDatabase;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * Displays list of pets that were entered and stored in the app.
 */
public class CatalogActivity extends AppCompatActivity {

    /** Adapter for the ListView */
    PetAdapter mAdapter;

    PetsDatabase mDb;

    ListView petListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catalog);

        // Setup FAB to open EditorActivity
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CatalogActivity.this, EditorActivity.class);
                startActivity(intent);
            }
        });

        // Find the ListView which will be populated with the pet data
        petListView = (ListView) findViewById(R.id.list);

        // Find and set empty view on the ListView, so that it only shows when the list has 0 items.
        View emptyView = findViewById(R.id.empty_view);
        petListView.setEmptyView(emptyView);

        // Setup an Adapter to create a list item for each row of pet data.
        // There is no pet data yet, so pass in a empty array list.
        mAdapter = new PetAdapter(this, new ArrayList<PetEntry>());
        petListView.setAdapter(mAdapter);

        // Setup the item click listener
        petListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                // Create new intent to go to {@link EditorActivity}
                Intent intent = new Intent(CatalogActivity.this, EditorActivity.class);
                PetEntry petSelected = (PetEntry) adapterView.getItemAtPosition(position);
                intent.putExtra(EditorActivity.EXTRA_PET_ID, petSelected.getId());
                // Launch the {@link EditorActivity} to display the data for the current pet.
                startActivity(intent);
            }
        });

        mDb = PetsDatabase.getInstance(getApplicationContext());
        setupViewModel();
    }

    private void setupViewModel() {
        CatalogViewModel viewModel = ViewModelProviders.of(this).get(CatalogViewModel.class);
        viewModel.getPets().observe(this, new Observer<List<PetEntry>>() {
            @Override
            public void onChanged(@Nullable List<PetEntry> petEntries) {
                mAdapter =  new PetAdapter(getApplicationContext(), petEntries);
                petListView.setAdapter(mAdapter);
            }
        });
    }

    /**
     * Helper method to insert hardcoded pet data into the database. For debugging purposes only.
     */
    private void insertPet() {
        new InsertPetTask(getApplicationContext()).execute();
    }

    /**
     * Helper method to delete all pets in the database.
     */
    private void deleteAllPets() {
        new DeleteAllTask(getApplicationContext()).execute();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_catalog.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_catalog, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Insert dummy data" menu option
            case R.id.action_insert_dummy_data:
                insertPet();
                return true;
            // Respond to a click on the "Delete all entries" menu option
            case R.id.action_delete_all_entries:
                deleteAllPets();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private static class InsertPetTask extends AsyncTask<Void, Void, Void> {

        final PetEntry dummyEntry = new PetEntry("Toto", "Terrier", 1, 7);

        private final WeakReference<Context> weakAppContext;

        InsertPetTask(Context AppContext) {
            this.weakAppContext = new WeakReference<>(AppContext);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            PetsDatabase database = PetsDatabase.getInstance(weakAppContext.get());
            database.petDao().insertPet(dummyEntry);
            return null;
        }
    }

    private static class DeleteAllTask extends AsyncTask<Void, Void, Void> {

        private final WeakReference<Context> weakAppContext;

        DeleteAllTask(Context AppContext) {
            this.weakAppContext = new WeakReference<>(AppContext);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            PetsDatabase database = PetsDatabase.getInstance(weakAppContext.get());
            database.petDao().deleteAllPets();
            return null;
        }
    }
}
