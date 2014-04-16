/*
 * Copyright (C) 2013 The Android Open Source Project
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

package com.thevery.droidcon14;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.FrameLayout;

public class BackgroundContainer extends FrameLayout {

    boolean isShowing = false;
    Drawable background;
    int openAreaTop;
    int openAreaHeight;
    boolean shouldUpdateBounds = false;

    public BackgroundContainer(Context context) {
        super(context);
        init();
    }

    public BackgroundContainer(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public BackgroundContainer(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        background = getResources().getDrawable(R.drawable.tiled_background);
    }

    public void showBackground(int top, int bottom) {
        //If this view doesn't do any drawing on its own, set this flag to allow further optimizations.
        setWillNotDraw(false);
        openAreaTop = top;
        openAreaHeight = bottom;
        isShowing = true;
        shouldUpdateBounds = true;
    }

    public void hideBackground() {
        setWillNotDraw(true);
        isShowing = false;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (isShowing) {
            if (shouldUpdateBounds) {
                background.setBounds(0, 0, getWidth(), openAreaHeight);
            }
            canvas.save();
            canvas.translate(0, openAreaTop);
            background.draw(canvas);
            canvas.restore();
        }
    }

}
