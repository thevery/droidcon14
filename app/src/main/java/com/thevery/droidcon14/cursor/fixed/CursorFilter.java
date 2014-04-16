package com.thevery.droidcon14.cursor.fixed;

import android.database.Cursor;
import android.provider.BaseColumns;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class CursorFilter {
    private Set<Long> filteredIds = new HashSet<>();

    public void addFilteredId(long id) {
        filteredIds.add(id);
    }

    public Cursor performFiltering(Cursor cursor) {
        Set<Integer> positionsList = new HashSet<>();
        if (filteredIds.isEmpty()) {
            return cursor;
        }
        int idPosition = cursor.getColumnIndexOrThrow(BaseColumns._ID);
        Set<Long> ids = new HashSet<>(filteredIds);
        for (cursor.moveToFirst(); !ids.isEmpty() && !cursor.isAfterLast(); cursor.moveToNext()) {
            long id = cursor.getLong(idPosition);
            if (ids.contains(Long.valueOf(id))) {
                ids.remove(Long.valueOf(id));
                positionsList.add(cursor.getPosition());
            }
        }
        filteredIds.removeAll(ids);
        return new FilteredCursor(cursor, new ArrayList<>(positionsList));

    }
}
