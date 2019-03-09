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

import android.app.AlertDialog;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.android.pets.data.PetEntry;
import com.example.android.pets.data.PetsDatabase;

import java.lang.ref.WeakReference;

/**
 * Allows user to create a new pet or edit an existing one.
 */
public class EditorActivity extends AppCompatActivity {

    // Extra for the pet ID to be received in the intent
    public static final String EXTRA_PET_ID = "extraPetId";

    // Extra for the pet ID to be received after rotation
    public static final String INSTANCE_PET_ID = "instancePetId";

    // Constant for default pet id to be used when not in update mode
    private static final int DEFAULT_PET_ID = -1;

    private int mPetId = DEFAULT_PET_ID;

    // Member variable for the Database
    private PetsDatabase mDb;

    /** EditText field to enter the pet's name */
    private EditText mNameEditText;

    /** EditText field to enter the pet's breed */
    private EditText mBreedEditText;

    /** EditText field to enter the pet's weight */
    private EditText mWeightEditText;

    /** EditText field to enter the pet's gender */
    private Spinner mGenderSpinner;

    /**
     * Gender of the pet. The possible valid values are in the PetEntry.java file:
     * {@link PetEntry#GENDER_UNKNOWN}, {@link PetEntry#GENDER_MALE}, or
     * {@link PetEntry#GENDER_FEMALE}.
     */
    private int mGender = PetEntry.GENDER_UNKNOWN;

    /** Boolean flag that keeps track of whether the pet has been edited (true) or not (false) */
    private boolean mPetHasChanged = false;

    /**
     * OnTouchListener that listens for any user touches on a View, implying that they are modifying
     * the view, and we change the mPetHasChanged boolean to true.
     */
    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mPetHasChanged = true;
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        mDb = PetsDatabase.getInstance(getApplicationContext());

        initViews();
        setupSpinner();

        if (savedInstanceState != null && savedInstanceState.containsKey(INSTANCE_PET_ID)) {
            mPetId = savedInstanceState.getInt(INSTANCE_PET_ID, DEFAULT_PET_ID);
        }

        // Examine the intent that was used to launch this activity,
        // in order to figure out if we're creating a new pet or editing an existing one.
        Intent intent = getIntent();

        // If the intent DOES contain an extra with the pet id, then we know that we are
        // editing an existing new pet.
        if (intent != null && intent.hasExtra(EXTRA_PET_ID)) {
            // Otherwise this is an existing pet, so change app bar to say "Edit Pet"
            setTitle(getString(R.string.editor_activity_title_edit_pet));

            if(mPetId == DEFAULT_PET_ID) {
                //populate the UI
                mPetId = intent.getIntExtra(EXTRA_PET_ID, DEFAULT_PET_ID);
                AddPetViewModelFactory factory = new AddPetViewModelFactory(mDb, mPetId);
                final AddPetViewModel viewModel =
                        ViewModelProviders.of(EditorActivity.this,
                                factory).get(AddPetViewModel.class);

                viewModel.getPet().observe(EditorActivity.this, new Observer<PetEntry>() {
                    @Override
                    public void onChanged(@Nullable PetEntry petEntry) {
                        viewModel.getPet().removeObserver(this);
                        populateUI(petEntry);
                    }
                });
            }
        } else {
            // This is a new pet, so change the app bar to say "Add a Pet"
            setTitle(getString(R.string.editor_activity_title_new_pet));

            // Invalidate the options menu, so the "Delete" menu option can be hidden.
            // (It doesn't make sense to delete a pet that hasn't been created yet.)
            invalidateOptionsMenu();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt(INSTANCE_PET_ID, mPetId);
        super.onSaveInstanceState(outState);
    }

    private void initViews() {
        // Find all relevant views that we will need to read user input from
        mNameEditText = (EditText) findViewById(R.id.edit_pet_name);
        mBreedEditText = (EditText) findViewById(R.id.edit_pet_breed);
        mWeightEditText = (EditText) findViewById(R.id.edit_pet_weight);
        mGenderSpinner = (Spinner) findViewById(R.id.spinner_gender);

        // Setup OnTouchListeners on all the input fields, so we can determine if the user
        // has touched or modified them. This will let us know if there are unsaved changes
        // or not, if the user tries to leave the editor without saving.
        mNameEditText.setOnTouchListener(mTouchListener);
        mBreedEditText.setOnTouchListener(mTouchListener);
        mWeightEditText.setOnTouchListener(mTouchListener);
        mGenderSpinner.setOnTouchListener(mTouchListener);
    }

    /**
     * Setup the dropdown spinner that allows the user to select the gender of the pet.
     */
    private void setupSpinner() {
        // Create adapter for spinner. The list options are from the String array it will use
        // the spinner will use the default layout
        ArrayAdapter genderSpinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.array_gender_options, android.R.layout.simple_spinner_item);

        // Specify dropdown layout style - simple list view with 1 item per line
        genderSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);

        // Apply the adapter to the spinner
        mGenderSpinner.setAdapter(genderSpinnerAdapter);

        // Set the integer mSelected to the constant values
        mGenderSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selection = (String) parent.getItemAtPosition(position);
                if (!TextUtils.isEmpty(selection)) {
                    if (selection.equals(getString(R.string.gender_male))) {
                        mGender = PetEntry.GENDER_MALE;
                    } else if (selection.equals(getString(R.string.gender_female))) {
                        mGender = PetEntry.GENDER_FEMALE;
                    } else {
                        mGender = PetEntry.GENDER_UNKNOWN;
                    }
                }
            }

            // Because AdapterView is an abstract class, onNothingSelected must be defined
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                mGender = PetEntry.GENDER_UNKNOWN;
            }
        });
    }

    private void populateUI(PetEntry pet) {
        if (pet == null){
            return;
        }

        // Extract out the value from the Pet received
        String name = pet.getName();
        String breed = pet.getBreed();
        int gender = pet.getGender();
        int weight = pet.getWeight();

        // Update the views on the screen with the values from the database
        mNameEditText.setText(name);
        mBreedEditText.setText(breed);
        mWeightEditText.setText(Integer.toString(weight));

        // Gender is a dropdown spinner, so map the constant value from the database
        // into one of the dropdown options (0 is Unknown, 1 is Male, 2 is Female).
        // Then call setSelection() so that option is displayed on screen as the current selection.
        switch (gender) {
            case PetEntry.GENDER_MALE:
                mGenderSpinner.setSelection(1);
                break;
            case PetEntry.GENDER_FEMALE:
                mGenderSpinner.setSelection(2);
                break;
            default:
                mGenderSpinner.setSelection(0);
                break;
        }
    }

    /**
     * Get user input from editor and save pet into database.
     */
    private void savePet() {
        // Read from input fields
        // Use trim to eliminate leading or trailing white space
        String nameString = mNameEditText.getText().toString().trim();
        String breedString = mBreedEditText.getText().toString().trim();
        String weightString = mWeightEditText.getText().toString().trim();

        // Check if this is supposed to be a new pet
        // and check if all the fields in the editor are blank
        if (mPetId == DEFAULT_PET_ID &&
                TextUtils.isEmpty(nameString) && TextUtils.isEmpty(breedString) &&
                TextUtils.isEmpty(weightString) && mGender == PetEntry.GENDER_UNKNOWN) {
            // Since no fields were modified, we can return early without creating a new pet.
            return;
        }

        // Create a new Pet Entry with the input values.
        // If the weight is not provided by the user, don't try to parse the string into an
        // integer value. Use 0 by default.
        int weight = 0;
        if (!TextUtils.isEmpty(weightString)) {
            weight = Integer.parseInt(weightString);
        }

        PetEntry petEntry = new PetEntry(nameString, breedString, mGender, weight);

        // Determine if this is a new or existing pet by checking if mCurrentPetUri is null or not
        if (mPetId == DEFAULT_PET_ID) {
            // This is a NEW pet
            new InsertPetTask(getApplicationContext()).execute(petEntry);
        } else {
            // Otherwise this is an EXISTING pet
            petEntry.setId(mPetId);
            new UpdatePetTask(getApplicationContext()).execute(petEntry);
        }
        // Close the activity
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_editor.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    /**
     * This method is called after invalidateOptionsMenu(), so that the
     * menu can be updated (some menu items can be hidden or made visible).
     */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        // If this is a new pet, hide the "Delete" menu item.
        if (mPetId == DEFAULT_PET_ID) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.action_save:
                // Save pet to database
                savePet();
                // Exit activity
                finish();
                return true;
            // Respond to a click on the "Delete" menu option
            case R.id.action_delete:
                // Pop up confirmation dialog for deletion
                showDeleteConfirmationDialog();
                return true;
            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                // If the pet hasn't changed, continue with navigating up to parent activity
                // which is the {@link CatalogActivity}.
                if (!mPetHasChanged) {
                    NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    return true;
                }

                // Otherwise if there are unsaved changes, setup a dialog to warn the user.
                // Create a click listener to handle the user confirming that
                // changes should be discarded.
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // User clicked "Discard" button, navigate to parent activity.
                                NavUtils.navigateUpFromSameTask(EditorActivity.this);
                            }
                        };

                // Show a dialog that notifies the user they have unsaved changes
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * This method is called when the back button is pressed.
     */
    @Override
    public void onBackPressed() {
        // If the pet hasn't changed, continue with handling back button press
        if (!mPetHasChanged) {
            super.onBackPressed();
            return;
        }

        // Otherwise if there are unsaved changes, setup a dialog to warn the user.
        // Create a click listener to handle the user confirming that changes should be discarded.
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // User clicked "Discard" button, close the current activity.
                        finish();
                    }
                };

        // Show dialog that there are unsaved changes
        showUnsavedChangesDialog(discardButtonClickListener);
    }

    /**
     * Show a dialog that warns the user there are unsaved changes that will be lost
     * if they continue leaving the editor.
     *
     * @param discardButtonClickListener is the click listener for what to do when
     *                                   the user confirms they want to discard their changes
     */
    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the postivie and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Keep editing" button, so dismiss the dialog
                // and continue editing the pet.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * Prompt the user to confirm that they want to delete this pet.
     */
    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the postivie and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the pet.
                deletePet();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the pet.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * Perform the deletion of the pet in the database.
     */
    private void deletePet() {
        // Only perform the delete if this is an existing pet.
        if (mPetId != DEFAULT_PET_ID) {
            new DeletePetTask(getApplicationContext()).execute(mPetId);
        }
        // Close the activity
        finish();
    }

    private static class InsertPetTask extends AsyncTask<PetEntry, Void, Long> {

        private final WeakReference<Context> weakAppContext;

        InsertPetTask(Context AppContext) {
            this.weakAppContext = new WeakReference<>(AppContext);
        }

        @Override
        protected Long doInBackground(PetEntry... petEntries) {
            PetsDatabase database = PetsDatabase.getInstance(weakAppContext.get());
            return database.petDao().insertPet(petEntries[0]);
        }

        @Override
        protected void onPostExecute(Long result) {
            // Show a toast message depending on whether or not the insertion was successful.
            if (result != (long) -1) {
                // If the insertion was successful, it returned a positive, bigger or equal to 0, id.
                Toast.makeText(weakAppContext.get(), weakAppContext.get().getString(R.string.editor_insert_pet_successful),
                        Toast.LENGTH_SHORT).show();

            } else {
                // Otherwise, there was an error with insertion.
                Toast.makeText(weakAppContext.get(), weakAppContext.get().getString(R.string.editor_insert_pet_failed),
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    private static class UpdatePetTask extends AsyncTask<PetEntry, Void, Integer> {

        private final WeakReference<Context> weakAppContext;

        UpdatePetTask(Context AppContext) {
            this.weakAppContext = new WeakReference<>(AppContext);
        }

        @Override
        protected Integer doInBackground(PetEntry... petEntries) {
            PetsDatabase database = PetsDatabase.getInstance(weakAppContext.get());
            return database.petDao().updatePet(petEntries[0]);
        }

        @Override
        protected void onPostExecute(Integer result) {
            // Show a toast message depending on whether or not the insertion was successful.
            if (result == 0) {
                // If no rows were affected, then there was an error with the update.
                Toast.makeText(weakAppContext.get(), weakAppContext.get().getString(R.string.editor_update_pet_failed),
                        Toast.LENGTH_SHORT).show();

            } else {
                // Otherwise, the update was successful and we can display a toast.
                Toast.makeText(weakAppContext.get(), weakAppContext.get().getString(R.string.editor_update_pet_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    private static class DeletePetTask extends AsyncTask<Integer, Void, Integer> {

        private final WeakReference<Context> weakAppContext;

        DeletePetTask(Context AppContext) {
            this.weakAppContext = new WeakReference<>(AppContext);
        }

        @Override
        protected Integer doInBackground(Integer... ids) {
            PetsDatabase database = PetsDatabase.getInstance(weakAppContext.get());
            return database.petDao().deletePet(ids[0]);
        }

        @Override
        protected void onPostExecute(Integer result) {
            // Show a toast message depending on whether or not the insertion was successful.
            if (result == 0) {
                // If no rows were affected, then there was an error with the delete.
                Toast.makeText(weakAppContext.get(), weakAppContext.get().getString(R.string.editor_delete_pet_failed),
                        Toast.LENGTH_SHORT).show();

            } else {
                // Otherwise, the delete was successful and we can display a toast.
                Toast.makeText(weakAppContext.get(), weakAppContext.get().getString(R.string.editor_delete_pet_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }
    }
}