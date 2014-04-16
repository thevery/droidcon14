package com.thevery.droidcon14.cursor.fixed;

import android.database.Cursor;
import android.database.CursorWrapper;

import java.util.Collections;
import java.util.List;

public class FilteredCursor extends CursorWrapper {

    private final List<Integer> filteredPositions;
    private int position;

    /**
     * Creates a cursor wrapper.
     *
     * @param cursor The underlying cursor to wrap.
     */
    public FilteredCursor(Cursor cursor, List<Integer> filteredPositions) {
        super(cursor);
        this.filteredPositions = filteredPositions;
        Collections.sort(filteredPositions);
    }

    @Override
    public boolean moveToPosition(int position) {
        //TODO:Think of smarter approach
        int inner = -1;
        int outer = -1;
        while (outer != position) {
            inner++;
            if (!filteredPositions.contains(inner)) {
                outer++;
            }
        }
        this.position = position;
        return super.moveToPosition(inner);
    }


    @Override
    public String toString() {
        return super.toString() + ": filtered_pos=" + filteredPositions;
    }

    @Override
    public int getPosition() {
        return position;
    }

    @Override
    public int getCount() {
        return super.getCount() - filteredPositions.size();
    }
}