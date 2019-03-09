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

import android.content.Context;
import android.content.res.Resources;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.android.pets.data.PetEntry;

import java.util.List;

/**
 * {@link PetAdapter} is an adapter for a list . This adapter knows
 * how to create list items for each row of pet data
 */
public class PetAdapter extends ArrayAdapter<PetEntry> {

    /**
     * Constructs a new {@link PetAdapter}.
     *
     * @param context The context
     */
    public PetAdapter(Context context, List<PetEntry> petEntries) {
        super(context, 0, petEntries);
    }

    /**
     * Get a View that displays the data at the specified position in the data set.
     *
     * @param position    Existing view, returned earlier by newView() method
     * @param convertView app context
     * @param parent  The cursor from which to get the data. The cursor is already moved to the
     *                correct row.
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View listItemView = convertView;
        if(listItemView == null){
            listItemView = LayoutInflater.from(getContext()).inflate(
                    R.layout.list_item, parent, false);
        }

        PetEntry pet = getItem(position);

        // Find individual views that we want to modify in the list item layout
        TextView nameTextView = (TextView) listItemView.findViewById(R.id.name);
        TextView summaryTextView = (TextView) listItemView.findViewById(R.id.summary);

        // Read the pet attributes from the current pet
        String petName = pet.getName();
        String petBreed = pet.getBreed();

        // If the pet breed is empty string or null, then use some default text
        // that says "Unknown breed", so the TextView isn't blank.
        if (TextUtils.isEmpty(petBreed)) {
            petBreed = Resources.getSystem().getString(R.string.unknown_breed);
        }

        // Update the TextViews with the attributes for the current pet
        nameTextView.setText(petName);
        summaryTextView.setText(petBreed);

        return listItemView;
    }
}
