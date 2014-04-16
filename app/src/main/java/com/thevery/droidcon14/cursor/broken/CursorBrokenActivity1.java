package com.thevery.droidcon14.cursor.broken;

import android.app.ListActivity;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewTreeObserver;
import android.widget.ListView;

import com.thevery.droidcon14.BackgroundContainer;
import com.thevery.droidcon14.R;
import com.thevery.droidcon14.cursor.SQLiteHelper;

import java.util.HashMap;

import static android.provider.BaseColumns._ID;
import static com.thevery.droidcon14.cursor.ApplesContentProvider.CONTENT_URI;
import static com.thevery.droidcon14.cursor.SQLiteHelper.ApplesTable.CULTIVAR;
import static java.lang.String.valueOf;

public class CursorBrokenActivity1 extends ListActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    public static final String TAG = "CursorBrokenActivity1";

    private static final int SWIPE_DURATION = 250;
    private static final int MOVE_DURATION = 150;
    private ListView listView;
    private ApplesCursorAdapter adapter;
    private BackgroundContainer backgroundContainer;
    private boolean isSwiping = false;
    private boolean isItemPressed = false;
    private HashMap<Long, Integer> itemIdTopMap = new HashMap<>();

    private int swipeSlop;
    /**
     * Handle touch events to fade/move dragged items as they are swiped out
     */
    private View.OnTouchListener touchListener = new View.OnTouchListener() {

        float downX;

        @Override
        public boolean onTouch(final View v, MotionEvent event) {
            //non-masked since we don't care about multi-touch events
            int action = event.getAction();
            switch (action) {
                //first touch
                case MotionEvent.ACTION_DOWN:
                    if (isItemPressed) {
                        // Multi-item swipes not handled
                        return false;
                    }
                    isItemPressed = true;
                    downX = event.getX();
                    break;
                //touch moved outside of screen - return to initial position
                case MotionEvent.ACTION_CANCEL:
                    restoreView(v);
                    isItemPressed = false;
                    break;
                //actual movement
                case MotionEvent.ACTION_MOVE: {
                    float x = event.getX() + v.getTranslationX();
                    float deltaX = x - downX;
                    float deltaXAbs = Math.abs(deltaX);
                    if (!isSwiping) {
                        if (deltaXAbs > swipeSlop) {
                            isSwiping = true;
                            listView.requestDisallowInterceptTouchEvent(true);
                            backgroundContainer.showBackground(v.getTop(), v.getHeight());
                        }
                    }
                    if (isSwiping) {
                        v.setTranslationX((x - downX));
                        v.setAlpha(1 - (float) 1 * deltaXAbs / v.getWidth());
                    }
                }
                break;
                case MotionEvent.ACTION_UP: {
                    //animate remove or reset to initial position
                    if (isSwiping) {
                        float x = event.getX() + v.getTranslationX();
                        float deltaX = x - downX;
                        float deltaXAbs = Math.abs(deltaX);
                        //used to determine the speed
                        float fractionCovered;
                        float endX;
                        float endAlpha;
                        //more than a quarter of the width
                        final boolean remove = deltaXAbs > v.getWidth() / 4;
                        if (remove) {
                            fractionCovered = deltaXAbs / v.getWidth();
                            endX = deltaX < 0 ? -v.getWidth() : v.getWidth();
                            endAlpha = 0;
                        } else {
                            fractionCovered = 1 - (deltaXAbs / v.getWidth());
                            endX = 0;
                            endAlpha = 1;
                        }
                        // Animate position and alpha of swiped item
                        // NOTE: This is a simplified version of swipe behavior, for the
                        // purposes of this demo about animation. A real version should use
                        // velocity (via the VelocityTracker class) to send the item off or
                        // back at an appropriate speed.
                        long duration = (int) ((1 - fractionCovered) * SWIPE_DURATION);
                        listView.setEnabled(false);
                        v.animate().setDuration(duration)
                                .alpha(endAlpha)
                                .translationX(endX)
                                .withEndAction(new Runnable() {
                                    @Override
                                    public void run() {
                                        restoreView(v);
                                        if (remove) {
                                            animateRemoval(listView, v);
                                        } else {
                                            backgroundContainer.hideBackground();
                                            isSwiping = false;
                                            listView.setEnabled(true);
                                        }
                                    }
                                });
                    }
                }
                isItemPressed = false;
                break;
                default:
                    return false;
            }
            return true;
        }

        private void restoreView(View v) {
            v.setAlpha(1);
            v.setTranslationX(0);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_view_deletion);
        listView = getListView();
        backgroundContainer = (BackgroundContainer) findViewById(R.id.listViewBackground);
        swipeSlop = ViewConfiguration.get(this).getScaledTouchSlop();
        adapter = new ApplesCursorAdapter(this, R.layout.text_with_background, null, touchListener);
        listView.setAdapter(adapter);
        getLoaderManager().restartLoader(0, null, this);
    }

    /**
     * This method animates all other views in the ListView container (not including ignoreView)
     * into their final positions. It is called after ignoreView has been removed from the
     * adapter, but before layout has been run. The approach here is to figure out where
     * everything is now, then allow layout to run, then figure out where everything is after
     * layout, and then to run animations between all of those start/end positions.
     */
    private void animateRemoval(final ListView listview, final View viewToRemove) {
        int firstVisiblePosition = listview.getFirstVisiblePosition();
        for (int i = 0; i < listview.getChildCount(); ++i) {
            View child = listview.getChildAt(i);
            if (child != viewToRemove) {
                int position = firstVisiblePosition + i;
                long itemId = adapter.getItemId(position);
                itemIdTopMap.put(itemId, child.getTop());
            }
        }

        final long itemToRemove = adapter.getItemId(listView.getPositionForView(viewToRemove));
        removeItem(itemToRemove);

        final ViewTreeObserver observer = listview.getViewTreeObserver();
        observer.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            public boolean onPreDraw() {
                observer.removeOnPreDrawListener(this);
                boolean firstAnimation = true;
                int firstVisiblePosition = listview.getFirstVisiblePosition();
                for (int i = 0; i < listview.getChildCount(); ++i) {
                    final View child = listview.getChildAt(i);
                    int position = firstVisiblePosition + i;
                    long itemId = adapter.getItemId(position);
                    Integer startTop = itemIdTopMap.get(itemId);
                    int top = child.getTop();
                    if (startTop != null) {
                        if (startTop != top) {
                            int delta = startTop - top;
                            child.setTranslationY(delta);
                            child.animate().setDuration(MOVE_DURATION).translationY(0);
                            if (firstAnimation) {
                                child.animate().withEndAction(new Runnable() {
                                    public void run() {
                                        backgroundContainer.hideBackground();
                                        isSwiping = false;
                                        listView.setEnabled(true);
                                    }
                                });
                                firstAnimation = false;
                            }
                        }
                    } else {
                        // Animate new views along with the others. The catch is that they did not
                        // exist in the start state, so we must calculate their starting position
                        // based on neighboring views.
                        int childHeight = child.getHeight() + listview.getDividerHeight();
                        startTop = top + (i > 0 ? childHeight : -childHeight);
                        int delta = startTop - top;
                        child.setTranslationY(delta);
                        child.animate().setDuration(MOVE_DURATION).translationY(0);
                        if (firstAnimation) {
                            child.animate().withEndAction(new Runnable() {
                                public void run() {
                                    backgroundContainer.hideBackground();
                                    isSwiping = false;
                                    listView.setEnabled(true);
                                }
                            });
                            firstAnimation = false;
                        }
                    }
                }
                itemIdTopMap.clear();
                return true;
            }
        });
    }

    private void removeItem(long itemId) {
        getContentResolver().delete(CONTENT_URI, SQLiteHelper.ApplesTable._ID + "=?", new String[]{String.valueOf(itemId)});
    }

    private void removeItem(int position) {
        Log.d(TAG, "removeItem");
        final long id = adapter.getItemId(position);
        new Thread(new Runnable() {
            @Override
            public void run() {
                getContentResolver().delete(CONTENT_URI, _ID + "=?", new String[]{valueOf(id)});

            }
        }).start();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(this, CONTENT_URI, new String[]{_ID, CULTIVAR}, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        //invoked on every delete
        Log.d(TAG, "onLoadFinished");
        adapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        adapter.swapCursor(null);
    }
}
