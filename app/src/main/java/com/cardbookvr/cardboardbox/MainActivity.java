package com.cardbookvr.cardboardbox;

import android.opengl.GLES20;
import android.os.Bundle;

import com.google.vrtoolkit.cardboard.CardboardActivity;
import com.google.vrtoolkit.cardboard.CardboardView;
import com.google.vrtoolkit.cardboard.Eye;
import com.google.vrtoolkit.cardboard.HeadTransform;
import com.google.vrtoolkit.cardboard.Viewport;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;

public class MainActivity extends CardboardActivity implements CardboardView.StereoRenderer {
    private static final String TAG = "MainActivity";

    // Scene variables
    // Model variables
    private static final int COORDS_PER_VERTEX = 3;
    private static float triCoords[] = {
            // in counter-clockwise order
            0.0f,  0.6f, -1.0f, // top
            -0.5f, -0.3f, -1.0f, // bottom left
            0.5f, -0.3f, -1.0f  // bottom right
    };

    private final int triVertexCount = triCoords.length / COORDS_PER_VERTEX;
    // yellow-ish color
    private float triColor[] = { 0.8f, 0.6f, 0.2f, 0.0f };
    private FloatBuffer triVerticesBuffer;

    // Viewing variables
    // Rendering variables
    private int simpleVertexShader;
    private int simpleFragmentShader;
    private int triProgram;
    private int triPositionParam;
    private int triColorParam;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        CardboardView cardboardView = (CardboardView) findViewById(R.id.cardboard_view);
        cardboardView.setRenderer(this);
        setCardboardView(cardboardView);
    }

    @Override
    public void onNewFrame(HeadTransform headTransform) {

    }

    @Override
    public void onDrawEye(Eye eye) {
        drawTriangle();
    }

    @Override
    public void onFinishFrame(Viewport viewport) {

    }

    @Override
    public void onSurfaceChanged(int i, int i1) {

    }

    @Override
    public void onSurfaceCreated(EGLConfig eglConfig) {
        initializeScene();
        compileShaders();
        prepareRenderingTriangle();
    }

    @Override
    public void onRendererShutdown() {

    }

    private void drawTriangle() {
        // Add program to OpenGL ES environment
        GLES20.glUseProgram(triProgram);

        // Prepare the coordinate data
        GLES20.glVertexAttribPointer(triPositionParam, COORDS_PER_VERTEX,
                GLES20.GL_FLOAT, false, 0, triVerticesBuffer);

        // Set color for drawing
        GLES20.glUniform4fv(triColorParam, 1, triColor, 0);

        // Draw the model
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, triVertexCount);
    }

    private void initializeScene() {
    }

    private void compileShaders() {
        simpleVertexShader = loadShader(GLES20.GL_VERTEX_SHADER, R.raw.simple_vertex);
        simpleFragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, R.raw.simple_fragment);
    }

    private void prepareRenderingTriangle() {
        // Allocate buffers
        // initialize vertex byte buffer for shape coordinates (4 */ bytes per float)
        ByteBuffer bb = ByteBuffer.allocateDirect(triCoords.length * 4);
        // use the device hardware's native byte order
        bb.order(ByteOrder.nativeOrder());

        // create a floating point buffer from the ByteBuffer
        triVerticesBuffer = bb.asFloatBuffer();
        // add the coordinates to the FloatBuffer
        triVerticesBuffer.put(triCoords);
        // set the buffer to read the first coordinate
        triVerticesBuffer.position(0);

        // Create GL program
        // create empty OpenGL ES Program
        triProgram = GLES20.glCreateProgram();
        // add the vertex shader to program
        GLES20.glAttachShader(triProgram, simpleVertexShader);
        // add the fragment shader to program
        GLES20.glAttachShader(triProgram, simpleFragmentShader);
        // build OpenGL ES program executable
        GLES20.glLinkProgram(triProgram);
        // set program as current
        GLES20.glUseProgram(triProgram);

        // Get shader params
        // get handle to vertex shader's a_Position member
        triPositionParam = GLES20.glGetAttribLocation(triProgram, "a_Position");
        // enable a handle to the triangle vertices
        GLES20.glEnableVertexAttribArray(triPositionParam);
        // get handle to fragment shader's u_Color member
        triColorParam = GLES20.glGetUniformLocation(triProgram, "u_Color");
    }


    /**
     * Utility method for compiling a OpenGL shader.
     *
     * @param type - Vertex or fragment shader type.
     * @param resId - int containing the resource ID of the shader
    code file.
     * @return - Returns an id for the shader.
     */
    private int loadShader(int type, int resId){
        String code = readRawTextFile(resId);
        int shader = GLES20.glCreateShader(type);

        // add the source code to the shader and compile it
        GLES20.glShaderSource(shader, code);
        GLES20.glCompileShader(shader);

        return shader;
    }

    /**
     * Converts a raw text file into a string.
     *
     * @param resId The resource ID of the raw text file about to
    be turned into a shader.
     * @return The content of the text file, or null in case of
    error.
     */
    private String readRawTextFile(int resId) {
        InputStream inputStream =
                getResources().openRawResource(resId);
        try {
            BufferedReader reader = new BufferedReader(new
                    InputStreamReader(inputStream));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
            reader.close();
            return sb.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


}
