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

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import android.opengl.GLES20;

/**
 * A two-dimensional square for use as a drawn object in OpenGL ES 2.0.
 * 一个在OpenGL ES 2.0中绘制用的二维正方形对象
 */
public class Square {

    private final String vertexShaderCode =
            // This matrix member variable provides a hook to manipulate
            // the coordinates of the objects that use this vertex shader
            // 这个模型成员变量提供一个钩子来操作在这个顶点着色器中使用的对象的坐标
            "uniform mat4 uMVPMatrix;" +
            "attribute vec4 vPosition;" +
            "void main() {" +
            // The matrix must be included as a modifier of gl_Position.
            //模型必须被包含为一个gl_Position的修改器
            // [必须给gl_Position赋值]
            // Note that the uMVPMatrix factor *must be first* in order
            // for the matrix multiplication product to be correct.
            // 注意,为了使矩阵乘积计算正确,uMVPMatrix因数必须在计算式的第一项
            "  gl_Position = uMVPMatrix * vPosition;" +
            "}";

    private final String fragmentShaderCode =
            "precision mediump float;" +
            "uniform vec4 vColor;" +
            "void main() {" +
            "  gl_FragColor = vColor;" +
            "}";

    private final FloatBuffer vertexBuffer;
    private final ShortBuffer drawListBuffer;
    private final int mProgram;
    private int mPositionHandle;
    private int mColorHandle;
    private int mMVPMatrixHandle;

    // number of coordinates per vertex in this array
    // 在这个顶点数组中坐标的数量
    static final int COORDS_PER_VERTEX = 3;
    static float squareCoords[] = {
            -0.5f,  0.5f, 0.0f,   // top left 左上
            -0.5f, -0.5f, 0.0f,   // bottom left 左下
             0.5f, -0.5f, 0.0f,   // bottom right 右下
             0.5f,  0.5f, 0.0f }; // top right 右上

    private final short drawOrder[] = { 0, 1, 2, 0, 2, 3 }; // order to draw vertices 顶点绘制的顺序

    private final int vertexStride = COORDS_PER_VERTEX * 4; // 4 bytes per vertex 每个顶点4字节

    float color[] = { 0.2f, 0.709803922f, 0.898039216f, 1.0f };

    /**
     * Sets up the drawing object data for use in an OpenGL ES context.
     * 初始化一个在OpenGL ES中绘制的对象数据
     */
    public Square() {
        // initialize vertex byte buffer for shape coordinates
        // 初始化形状坐标的顶点ByteBuffer
        ByteBuffer bb = ByteBuffer.allocateDirect(
        // (# of coordinate values * 4 bytes per float)
        // (分配坐标的数量*4字节的内存空间)(每个float类型占4字节)
                squareCoords.length * 4);
        bb.order(ByteOrder.nativeOrder());
        vertexBuffer = bb.asFloatBuffer();
        vertexBuffer.put(squareCoords);
        vertexBuffer.position(0);

        // initialize byte buffer for the draw list
        // 从顶点绘制顺序队列中初始化ByteBuffer
        ByteBuffer dlb = ByteBuffer.allocateDirect(
                // (# of coordinate values * 2 bytes per short)
                // (分配坐标值 * 2 byte)(short类型占2字节)
                drawOrder.length * 2);
        dlb.order(ByteOrder.nativeOrder());
        drawListBuffer = dlb.asShortBuffer();
        drawListBuffer.put(drawOrder);
        drawListBuffer.position(0);

        // prepare shaders and OpenGL program
        // 准备Shaders(着色器)和OpenGL程序
        int vertexShader = MyGLRenderer.loadShader(
                GLES20.GL_VERTEX_SHADER,
                vertexShaderCode);
        int fragmentShader = MyGLRenderer.loadShader(
                GLES20.GL_FRAGMENT_SHADER,
                fragmentShaderCode);

        mProgram = GLES20.glCreateProgram();             // create empty OpenGL Program         创建一个空的OpenGL程序
        GLES20.glAttachShader(mProgram, vertexShader);   // add the vertex shader to program    添加顶点着色器到程序中
        GLES20.glAttachShader(mProgram, fragmentShader); // add the fragment shader to program  添加片段着色器到程序中
        GLES20.glLinkProgram(mProgram);                  // create OpenGL program executables   创建OpenGL可执行程序
    }

    /**
     * Encapsulates the OpenGL ES instructions for drawing this shape.
     * 封装OpenGL ES相关指令来绘制这个形状
     *
     * @param mvpMatrix - The Model View Project matrix in which to draw
     * this shape.
     *                  - 用于绘制这个形状用的模型视图项目 [父层]
     */
    public void draw(float[] mvpMatrix) {
        // Add program to OpenGL environment
        // 添加程序到OpenGL环境
        GLES20.glUseProgram(mProgram);

        // get handle to vertex shader's vPosition member
        // 获得顶点着色器的vPosition成员的操作句柄
        mPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");

        //注意:两个三角形拼成一个正方形,因此下方依然翻译为三角

        // Enable a handle to the triangle vertices
        // 为这些三角形顶点启用一个句柄 [即将mPositionHandle作为三角形顶点的操作句柄]
        GLES20.glEnableVertexAttribArray(mPositionHandle);

        // Prepare the triangle coordinate data
        // 准备三角形的坐标数据
        GLES20.glVertexAttribPointer(
                mPositionHandle, COORDS_PER_VERTEX,
                GLES20.GL_FLOAT, false,
                vertexStride, vertexBuffer);

        // get handle to fragment shader's vColor member
        // 获得片段着色器的vColor成员
        mColorHandle = GLES20.glGetUniformLocation(mProgram, "vColor");

        // Set color for drawing the triangle
        // 设置要绘制的三角形的颜色
        GLES20.glUniform4fv(mColorHandle, 1, color, 0);

        // get handle to shape's transformation matrix
        // 获得图形的变换矩阵的操作句柄
        mMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");
        MyGLRenderer.checkGlError("glGetUniformLocation");

        // Apply the projection and view transformation
        // 应用投影和视图变换
        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mvpMatrix, 0);
        MyGLRenderer.checkGlError("glUniformMatrix4fv");

        // Draw the square
        // 绘制正方形
        GLES20.glDrawElements(
                GLES20.GL_TRIANGLES, drawOrder.length,
                GLES20.GL_UNSIGNED_SHORT, drawListBuffer);

        // Disable vertex array
        // 禁用顶点数组
        GLES20.glDisableVertexAttribArray(mPositionHandle);
    }

}