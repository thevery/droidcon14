package com.thevery.droidcon14.array;

import android.app.ListActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewTreeObserver;
import android.widget.ListView;

import com.thevery.droidcon14.Apples;
import com.thevery.droidcon14.BackgroundContainer;
import com.thevery.droidcon14.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class ArrayActivity extends ListActivity {
    public static final int FULL_ALPHA = 0;
    public static final int NO_ALPHA = 1;
    private static final int SWIPE_DURATION = 250;
    private static final int MOVE_DURATION = 150;

    private ListView listView;
    private StableArrayAdapter adapter;
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
                        v.setAlpha(1 - deltaXAbs / v.getWidth());
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
                            endAlpha = FULL_ALPHA;
                        } else {
                            fractionCovered = 1 - (deltaXAbs / v.getWidth());
                            endX = 0;
                            endAlpha = NO_ALPHA;
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
        adapter = new StableArrayAdapter(this, R.layout.text_with_background, new ArrayList<>(Arrays.asList(Apples.APPLES_NAMES)), touchListener);
        setListAdapter(adapter);

        swipeSlop = ViewConfiguration.get(this).getScaledTouchSlop();

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

        int position = listView.getPositionForView(viewToRemove);
        adapter.remove(adapter.getItem(position));

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
}
