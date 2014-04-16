package com.thevery.droidcon14.cursor.broken;

import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.widget.ResourceCursorAdapter;
import android.widget.TextView;

public class ApplesCursorAdapter extends ResourceCursorAdapter {

    private View.OnTouchListener touchListener;

    public ApplesCursorAdapter(Context context, int layout, Cursor c) {
        super(context, layout, c, false);
    }

    public ApplesCursorAdapter(Context stdCursorListView, int layout, Cursor cursor, View.OnTouchListener touchListener) {
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
}
