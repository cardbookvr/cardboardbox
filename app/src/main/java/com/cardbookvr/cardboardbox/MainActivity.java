package com.cardbookvr.cardboardbox;

import android.opengl.GLES20;
import android.opengl.Matrix;
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
    // light positioned just above the user
    private static final float[] LIGHT_POS_IN_WORLD_SPACE = new float[]{0.0f, 2.0f, 0.0f, 1.0f };
    private final float[] lightPosInEyeSpace = new float[4];

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

    private float[] triTransform;

    private static float cubeCoords[] = Cube.CUBE_COORDS;
    private static float cubeColors[] = Cube.cubeFacesToArray(Cube.CUBE_COLORS_FACES, 4);
    private static float cubeFoundColors[] = Cube.cubeFacesToArray(Cube.CUBE_FOUND_COLORS_FACES, 4);
    private static float cubeNormals[] = Cube.cubeFacesToArray(Cube.CUBE_NORMALS_FACES, 3);

    private final int cubeVertexCount = cubeCoords.length / COORDS_PER_VERTEX;
    private float cubeColor[] = { 0.8f, 0.6f, 0.2f, 0.0f }; // yellow-ish
    private float[] cubeTransform;
    private float cubeDistance = 5f;

    private static float floorCoords[] = Floor.FLOOR_COORDS;
    private static float floorColors[] = Floor.FLOOR_COLORS;
    private static float floorNormals[] = Floor.FLOOR_NORMALS;
    private final int floorVertexCount = floorCoords.length / COORDS_PER_VERTEX;
    private float[] floorTransform;
    private float floorDepth = 20f;


    // Viewing variables
    private static final float Z_NEAR = 0.1f;
    private static final float Z_FAR = 100.0f;
    private static final float CAMERA_Z = 0.01f;

    private float[] camera;
    private float[] view;
    private float[] modelViewProjection;

    private float[] triView;

    private float[] cubeView;

    private static final float TIME_DELTA = 0.3f;

    private float[] floorView;

    private static final float YAW_LIMIT = 0.12f;
    private static final float PITCH_LIMIT = 0.12f;

    private float[] headView;


    // Rendering variables
    private int simpleVertexShader;
    private int simpleFragmentShader;
    private int triProgram;
    private int triPositionParam;
    private int triColorParam;
    private int triMVPMatrixParam;

    private FloatBuffer cubeVerticesBuffer;
    private FloatBuffer cubeColorsBuffer;
    private FloatBuffer cubeFoundColorsBuffer;
    private FloatBuffer cubeNormalsBuffer;

    private int cubeProgram;
    private int cubePositionParam;
    private int cubeColorParam;
    private int cubeMVPMatrixParam;

    private int lightVertexShader;
    private int passthroughFragmentShader;

    private int cubeNormalParam;
    private int cubeModelViewParam;
    private int cubeLightPosParam;

    private int gridFragmentShader;

    private FloatBuffer floorVerticesBuffer;
    private FloatBuffer floorColorsBuffer;
    private FloatBuffer floorNormalsBuffer;
    private int floorProgram;
    private int floorPositionParam;
    private int floorColorParam;
    private int floorMVPMatrixParam;
    private int floorNormalParam;
    private int floorModelParam;
    private int floorModelViewParam;
    private int floorLightPosParam;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        CardboardView cardboardView = (CardboardView) findViewById(R.id.cardboard_view);
        cardboardView.setRenderer(this);
        setCardboardView(cardboardView);

        camera = new float[16];
        view = new float[16];
        modelViewProjection = new float[16];

        triTransform = new float[16];
        triView = new float[16];

        cubeTransform = new float[16];
        cubeView = new float[16];

        floorTransform = new float[16];
        floorView = new float[16];

        headView = new float[16];
    }

    @Override
    public void onNewFrame(HeadTransform headTransform) {
        // Build the camera matrix and apply it to the ModelView.
        Matrix.setLookAtM(camera, 0, 0.0f, 0.0f, CAMERA_Z, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f);

        Matrix.rotateM(cubeTransform, 0, TIME_DELTA, 0.5f, 0.5f, 1.0f);

        headTransform.getHeadView(headView, 0);
    }

    @Override
    public void onDrawEye(Eye eye) {
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        // Apply the eye transformation to the camera
        Matrix.multiplyMM(view, 0, eye.getEyeView(), 0, camera, 0);

        // Calculate position of the light
        Matrix.multiplyMV(lightPosInEyeSpace, 0, view, 0, LIGHT_POS_IN_WORLD_SPACE, 0);

        // Get the perspective transformation
        float[] perspective = eye.getPerspective(Z_NEAR, Z_FAR);

        // Apply perspective transformation to the view, and draw
        Matrix.multiplyMM(triView, 0, view, 0, triTransform, 0);
        Matrix.multiplyMM(modelViewProjection, 0, perspective, 0, triView, 0);

        drawTriangle();

        Matrix.multiplyMM(cubeView, 0, view, 0, cubeTransform, 0);
        Matrix.multiplyMM(modelViewProjection, 0, perspective, 0,
                cubeView, 0);
        drawCube();

        Matrix.multiplyMM(floorView, 0, view, 0, floorTransform, 0);
        Matrix.multiplyMM(modelViewProjection, 0, perspective, 0, floorView, 0);
        drawFloor();

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
        prepareRenderingCube();
        prepareRenderingFloor();
    }

    @Override
    public void onRendererShutdown() {

    }

    private void drawTriangle() {
        // Add program to OpenGL ES environment
        GLES20.glUseProgram(triProgram);

        // Pass the MVP transformation to the shader
        GLES20.glUniformMatrix4fv(triMVPMatrixParam, 1, false, modelViewProjection, 0);

        // Prepare the coordinate data
        GLES20.glVertexAttribPointer(triPositionParam, COORDS_PER_VERTEX,
                GLES20.GL_FLOAT, false, 0, triVerticesBuffer);

        // Set color for drawing
        GLES20.glUniform4fv(triColorParam, 1, triColor, 0);

        // Draw the model
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, triVertexCount);
    }

    private void drawCube() {
        GLES20.glUseProgram(cubeProgram);

        // Set the light position in the shader
        GLES20.glUniform3fv(cubeLightPosParam, 1, lightPosInEyeSpace, 0);

        // Set the ModelView in the shader, used to calculate lighting
        GLES20.glUniformMatrix4fv(cubeModelViewParam, 1, false, cubeView, 0);

        GLES20.glUniformMatrix4fv(cubeMVPMatrixParam, 1, false, modelViewProjection, 0);

        GLES20.glVertexAttribPointer(cubePositionParam, COORDS_PER_VERTEX,
                GLES20.GL_FLOAT, false, 0, cubeVerticesBuffer);
        GLES20.glVertexAttribPointer(cubeNormalParam, 3,
                GLES20.GL_FLOAT, false, 0, cubeNormalsBuffer);

        if (isLookingAtObject(cubeView, cubeTransform)) {
            GLES20.glVertexAttribPointer(cubeColorParam, 4,
                    GLES20.GL_FLOAT, false, 0, cubeFoundColorsBuffer);
        } else {
            GLES20.glVertexAttribPointer(cubeColorParam, 4,
                    GLES20.GL_FLOAT, false, 0, cubeColorsBuffer);
        }

        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, cubeVertexCount);
    }

    private void drawFloor() {
        GLES20.glUseProgram(floorProgram);
        GLES20.glUniform3fv(floorLightPosParam, 1,
                lightPosInEyeSpace, 0);
        GLES20.glUniformMatrix4fv(floorModelParam, 1, false,
                floorTransform, 0);
        GLES20.glUniformMatrix4fv(floorModelViewParam, 1, false,
                floorView, 0);
        GLES20.glUniformMatrix4fv(floorMVPMatrixParam, 1, false,
                modelViewProjection, 0);
        GLES20.glVertexAttribPointer(floorPositionParam,
                COORDS_PER_VERTEX,
                GLES20.GL_FLOAT, false, 0, floorVerticesBuffer);
        GLES20.glVertexAttribPointer(floorNormalParam, 3,
                GLES20.GL_FLOAT, false, 0,
                floorNormalsBuffer);
        GLES20.glVertexAttribPointer(floorColorParam, 4,
                GLES20.GL_FLOAT, false, 0,
                floorColorsBuffer);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0,
                floorVertexCount);
    }


    private void initializeScene() {
        // Position the triangle
        Matrix.setIdentityM(triTransform, 0);
        Matrix.translateM(triTransform, 0, 5, 0, -5);

        // Rotate and position the cube
        Matrix.setIdentityM(cubeTransform, 0);
        Matrix.translateM(cubeTransform, 0, 0, 0, -cubeDistance);
        Matrix.rotateM(cubeTransform, 0, 30, 1, 1, 0);

        // Position the floor
        Matrix.setIdentityM(floorTransform, 0);
        Matrix.translateM(floorTransform, 0, 0, -floorDepth, 0);
    }

    private void compileShaders() {
        simpleVertexShader = loadShader(GLES20.GL_VERTEX_SHADER, R.raw.mvp_vertex);
        simpleFragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, R.raw.simple_fragment);

        lightVertexShader = loadShader(GLES20.GL_VERTEX_SHADER, R.raw.light_vertex);
        passthroughFragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, R.raw.passthrough_fragment);

        gridFragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, R.raw.grid_fragment);
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
        // get handle to shape's transformation matrix
        triMVPMatrixParam = GLES20.glGetUniformLocation(triProgram, "u_MVP");
    }

    private void prepareRenderingCube() {
        // Allocate buffers
        ByteBuffer bb = ByteBuffer.allocateDirect(cubeCoords.length * 4);
        bb.order(ByteOrder.nativeOrder());
        cubeVerticesBuffer = bb.asFloatBuffer();
        cubeVerticesBuffer.put(cubeCoords);
        cubeVerticesBuffer.position(0);

        ByteBuffer bbColors = ByteBuffer.allocateDirect(cubeColors.length * 4);
        bbColors.order(ByteOrder.nativeOrder());
        cubeColorsBuffer = bbColors.asFloatBuffer();
        cubeColorsBuffer.put(cubeColors);
        cubeColorsBuffer.position(0);

        ByteBuffer bbFoundColors = ByteBuffer.allocateDirect(cubeFoundColors.length * 4);
        bbFoundColors.order(ByteOrder.nativeOrder());
        cubeFoundColorsBuffer = bbFoundColors.asFloatBuffer();
        cubeFoundColorsBuffer.put(cubeFoundColors);
        cubeFoundColorsBuffer.position(0);

        ByteBuffer bbNormals = ByteBuffer.allocateDirect(cubeNormals.length * 4);
        bbNormals.order(ByteOrder.nativeOrder());
        cubeNormalsBuffer = bbNormals.asFloatBuffer();
        cubeNormalsBuffer.put(cubeNormalParam);
        cubeNormalsBuffer.position(0);

        // Create GL program
        cubeProgram = GLES20.glCreateProgram();
        GLES20.glAttachShader(cubeProgram, lightVertexShader);
        GLES20.glAttachShader(cubeProgram, passthroughFragmentShader);
        GLES20.glLinkProgram(cubeProgram);
        GLES20.glUseProgram(cubeProgram);

        // Get shader params
        cubeModelViewParam = GLES20.glGetUniformLocation(cubeProgram, "u_MVMatrix");
        cubeMVPMatrixParam = GLES20.glGetUniformLocation(cubeProgram, "u_MVP");
        cubeLightPosParam = GLES20.glGetUniformLocation(cubeProgram, "u_LightPos");

        cubePositionParam = GLES20.glGetAttribLocation(cubeProgram, "a_Position");
        cubeNormalParam = GLES20.glGetAttribLocation(cubeProgram, "a_Normal");
        cubeColorParam = GLES20.glGetAttribLocation(cubeProgram, "a_Color");

        // Enable arrays
        GLES20.glEnableVertexAttribArray(cubePositionParam);
        GLES20.glEnableVertexAttribArray(cubeNormalParam);
        GLES20.glEnableVertexAttribArray(cubeColorParam);
    }

    private void prepareRenderingFloor() {
        // Allocate buffers
        ByteBuffer bb = ByteBuffer.allocateDirect(floorCoords.length * 4);
        bb.order(ByteOrder.nativeOrder());
        floorVerticesBuffer = bb.asFloatBuffer();
        floorVerticesBuffer.put(floorCoords);
        floorVerticesBuffer.position(0);

        ByteBuffer bbColors = ByteBuffer.allocateDirect(floorColors.length * 4);
        bbColors.order(ByteOrder.nativeOrder());
        floorColorsBuffer = bbColors.asFloatBuffer();
        floorColorsBuffer.put(floorColors);
        floorColorsBuffer.position(0);

        ByteBuffer bbNormals = ByteBuffer.allocateDirect(floorNormals.length * 4);
        bbNormals.order(ByteOrder.nativeOrder());
        floorNormalsBuffer = bbNormals.asFloatBuffer();
        floorNormalsBuffer.put(floorNormals);
        floorNormalsBuffer.position(0);

        // Create GL program
        floorProgram = GLES20.glCreateProgram();
        GLES20.glAttachShader(floorProgram, lightVertexShader);
        GLES20.glAttachShader(floorProgram, gridFragmentShader);
        GLES20.glLinkProgram(floorProgram);
        GLES20.glUseProgram(floorProgram);

        // Get shader params
        floorPositionParam = GLES20.glGetAttribLocation(floorProgram, "a_Position");
        floorNormalParam = GLES20.glGetAttribLocation(floorProgram, "a_Normal");
        floorColorParam = GLES20.glGetAttribLocation(floorProgram, "a_Color");

        floorModelParam = GLES20.glGetUniformLocation(floorProgram, "u_Model");
        floorModelViewParam = GLES20.glGetUniformLocation(floorProgram, "u_MVMatrix");
        floorMVPMatrixParam = GLES20.glGetUniformLocation(floorProgram, "u_MVP");
        floorLightPosParam = GLES20.glGetUniformLocation(floorProgram, "u_LightPos");

        // Enable arrays
        GLES20.glEnableVertexAttribArray(floorPositionParam);
        GLES20.glEnableVertexAttribArray(floorNormalParam);
        GLES20.glEnableVertexAttribArray(floorColorParam);
    }

    /**
     * Check if user is looking at object by calculating where the
     object is in eye-space.
     *
     * @return true if the user is looking at the object.
     */
    private boolean isLookingAtObject(float[] modelView, float[] modelTransform) {
        float[] initVec = { 0, 0, 0, 1.0f };
        float[] objPositionVec = new float[4];

        // Convert object space to camera space. Use the headView from onNewFrame.
        Matrix.multiplyMM(modelView, 0, headView, 0, modelTransform, 0);
        Matrix.multiplyMV(objPositionVec, 0, modelView, 0, initVec, 0);

        float pitch = (float) Math.atan2(objPositionVec[1], -objPositionVec[2]);
        float yaw = (float) Math.atan2(objPositionVec[0], -objPositionVec[2]);

        return Math.abs(pitch) < PITCH_LIMIT && Math.abs(yaw) < YAW_LIMIT;
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
