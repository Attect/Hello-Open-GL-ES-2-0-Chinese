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

import android.opengl.GLES20;

/**
 * A two-dimensional triangle for use as a drawn object in OpenGL ES 2.0.
 * 一个在OpenGL ES 2.0中绘制用的二维三角形对象
 */
public class Triangle {

    private final String vertexShaderCode =
            // This matrix member variable provides a hook to manipulate
            // the coordinates of the objects that use this vertex shader
            // 这个模型成员变量提供一个钩子来操作在这个顶点着色器中使用的对象的坐标
            "uniform mat4 uMVPMatrix;" +
            "attribute vec4 vPosition;" +
            "void main() {" +
            // the matrix must be included as a modifier of gl_Position
            // 模型必须被包含为一个gl_Position的修改器
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
    private final int mProgram;
    private int mPositionHandle;
    private int mColorHandle;
    private int mMVPMatrixHandle;

    // number of coordinates per vertex in this array
    // 在这个顶点数组中坐标的数量
    static final int COORDS_PER_VERTEX = 3;
    static float triangleCoords[] = {
            // in counterclockwise order:
            // 逆时针的顺序:
            0.0f,  0.622008459f, 0.0f,   // top 上
           -0.5f, -0.311004243f, 0.0f,   // bottom left 左下
            0.5f, -0.311004243f, 0.0f    // bottom right 右下
    };
    private final int vertexCount = triangleCoords.length / COORDS_PER_VERTEX;
    private final int vertexStride = COORDS_PER_VERTEX * 4; // 4 bytes per vertex 每个顶点4字节

    float color[] = { 0.63671875f, 0.76953125f, 0.22265625f, 0.0f };

    /**
     * Sets up the drawing object data for use in an OpenGL ES context.
     * 初始化一个在OpenGL ES中绘制的对象数据
     */
    public Triangle() {
        // initialize vertex byte buffer for shape coordinates
        // 初始化形状坐标的顶点ByteBuffer
        ByteBuffer bb = ByteBuffer.allocateDirect(
                // (number of coordinate values * 4 bytes per float)
                // (分配坐标的数量*4字节的内存空间)(每个float类型占4字节)
                triangleCoords.length * 4);
        // use the device hardware's native byte order
        // 使用设备的硬件的字节顺序(ByteOrder)[提升效率]
        bb.order(ByteOrder.nativeOrder());

        // create a floating point buffer from the ByteBuffer
        // 从ByteBuffer中创建一个FloatBuffer
        // [将ByteBuffer转换成FloatBuffer]
        vertexBuffer = bb.asFloatBuffer();
        // add the coordinates to the FloatBuffer
        // 添加坐标到FloatBuffer中
        vertexBuffer.put(triangleCoords);
        // set the buffer to read the first coordinate
        // 设置这个buffer读取第一个顶点
        vertexBuffer.position(0);

        // prepare shaders and OpenGL program
        // 准备Shaders(着色器)和OpenGL程序
        int vertexShader = MyGLRenderer.loadShader(
                GLES20.GL_VERTEX_SHADER, vertexShaderCode);
        int fragmentShader = MyGLRenderer.loadShader(
                GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode);

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

        // Enable a handle to the triangle vertices
        // 为这些三角形顶点启用一个句柄 [即将mPositionHandle作为三角形顶点的操作句柄]
        GLES20.glEnableVertexAttribArray(mPositionHandle);

        // Prepare the triangle coordinate data
        // 准备三角形坐标数据
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

        // Draw the triangle
        // 绘制三角形
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, vertexCount);

        // Disable vertex array
        // 禁用顶点数组
        GLES20.glDisableVertexAttribArray(mPositionHandle);
    }

}
