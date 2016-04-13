/*
 * Copyright (C) 2011 The Android Open Source Project
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
package com.example.android.opengl;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.view.MotionEvent;

/**
 * A view container where OpenGL ES graphics can be drawn on screen.
 * This view can also be used to capture touch events, such as a user
 * interacting with drawn objects.
 * 一个使OpenGL ES将画面绘制在屏幕上的视图容器
 * 这个View依然可以用来捕获触摸事件,例如用户和画面上的内容进行交互
 */
public class MyGLSurfaceView extends GLSurfaceView {

    private final MyGLRenderer mRenderer;

    public MyGLSurfaceView(Context context) {
        super(context);

        // Create an OpenGL ES 2.0 context.
        // 创建一个OpenGL ES 2.0的上下文
        setEGLContextClientVersion(2);

        // Set the Renderer for drawing on the GLSurfaceView
        // 设置在这个视图容器中绘图的渲染器
        mRenderer = new MyGLRenderer();
        setRenderer(mRenderer);


        // Render the view only when there is a change in the drawing data
        // 仅在数据有变更和调用requestRender()后才更新屏幕上的内容
        // [另一种模式可以通过IDE提示到,效果就是默认每秒60fps]
        // [另外请注意MyRenderer中的onDrawFrame()方法]
        setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
    }

    private final float TOUCH_SCALE_FACTOR = 180.0f / 320;
    private float mPreviousX;
    private float mPreviousY;

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        // MotionEvent reports input details from the touch screen
        // and other input controls. In this case, you are only
        // interested in events where the touch position changed.
        // MotionEvent会报告详细的触摸屏或其它点触设备的输入数据.在这个代码段中,
        // 你仅可以在当触摸坐标发生改变时触发本段代码

        float x = e.getX();
        float y = e.getY();

        switch (e.getAction()) {
            case MotionEvent.ACTION_MOVE:

                float dx = x - mPreviousX;
                float dy = y - mPreviousY;

                // reverse direction of rotation above the mid-line
                // 屏幕水平中心线以上反方向旋转
                if (y > getHeight() / 2) {
                    dx = dx * -1 ;
                }

                // reverse direction of rotation to left of the mid-line
                // 屏幕垂直中心线以左反方向旋转
                if (x < getWidth() / 2) {
                    dy = dy * -1 ;
                }

                mRenderer.setAngle(
                        mRenderer.getAngle() +
                        ((dx + dy) * TOUCH_SCALE_FACTOR));  // = 180.0f / 320
                requestRender();
        }

        mPreviousX = x;
        mPreviousY = y;
        return true;
    }



}
