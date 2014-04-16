package com.thevery.droidcon14.cursor.fixed;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import android.view.View;
import android.widget.ResourceCursorAdapter;
import android.widget.TextView;

public class ApplesFilterAdapter extends ResourceCursorAdapter {
    public static final String TAG = "ApplesFilterAdapter";

    private View.OnTouchListener touchListener;
    private CursorFilter cursorFilter = new CursorFilter();


    public ApplesFilterAdapter(Context context, int layout, Cursor c) {
        super(context, layout, c, false);
    }

    public ApplesFilterAdapter(Context stdCursorListView, int layout, Cursor cursor, View.OnTouchListener touchListener) {
        this(stdCursorListView, layout, cursor);
        this.touchListener = touchListener;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        TextView text = (TextView) view;
        text.setText(cursor.getString(1));
        if (view.getTag() == null) {
            view.setTag(touchListener);
            view.setOnTouchListener(touchListener);
        }
    }

    @Override
    public Cursor swapCursor(Cursor newCursor) {
        Log.d(TAG, "swapCursor, cursor=" + newCursor);
        Cursor filtered = newCursor != null ? cursorFilter.performFiltering(newCursor) : null;
        return super.swapCursor(filtered);
    }

    public void addFilteredId(long id) {
        Log.d(TAG, "addFilteredId");
        cursorFilter.addFilteredId(id);
        swapCursor(getCursor());
    }
}
