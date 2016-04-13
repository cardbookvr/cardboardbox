package com.cardbookvr.cardboardbox;

/**
 * Created by Schoen and Jonathan on 4/13/2016.
 */
public class Cube {
    public static final float[] CUBE_COORDS = new float[] {
            // Front face
            -1.0f, 1.0f, 1.0f,
            -1.0f, -1.0f, 1.0f,
            1.0f, 1.0f, 1.0f,
            -1.0f, -1.0f, 1.0f,
            1.0f, -1.0f, 1.0f,
            1.0f, 1.0f, 1.0f,

            // Right face
            1.0f, 1.0f, 1.0f,
            1.0f, -1.0f, 1.0f,
            1.0f, 1.0f, -1.0f,
            1.0f, -1.0f, 1.0f,
            1.0f, -1.0f, -1.0f,
            1.0f, 1.0f, -1.0f,

            // Back face
            1.0f, 1.0f, -1.0f,
            1.0f, -1.0f, -1.0f,
            -1.0f, 1.0f, -1.0f,
            1.0f, -1.0f, -1.0f,
            -1.0f, -1.0f, -1.0f,
            -1.0f, 1.0f, -1.0f,

            // Left face
            -1.0f, 1.0f, -1.0f,
            -1.0f, -1.0f, -1.0f,
            -1.0f, 1.0f, 1.0f,
            -1.0f, -1.0f, -1.0f,
            -1.0f, -1.0f, 1.0f,
            -1.0f, 1.0f, 1.0f,

            // Top face
            -1.0f, 1.0f, -1.0f,
            -1.0f, 1.0f, 1.0f,
            1.0f, 1.0f, -1.0f,
            -1.0f, 1.0f, 1.0f,
            1.0f, 1.0f, 1.0f,
            1.0f, 1.0f, -1.0f,

            // Bottom face
            1.0f, -1.0f, -1.0f,
            1.0f, -1.0f, 1.0f,
            -1.0f, -1.0f, -1.0f,
            1.0f, -1.0f, 1.0f,
            -1.0f, -1.0f, 1.0f,
            -1.0f, -1.0f, -1.0f,
    };

    public static final float[] CUBE_COLORS_FACES = new float[] {
            // Front, green
            0f, 0.53f, 0.27f, 1.0f,
            // Right, blue
            0.0f, 0.34f, 0.90f, 1.0f,
            // Back, also green
            0f, 0.53f, 0.27f, 1.0f,
            // Left, also blue
            0.0f, 0.34f, 0.90f, 1.0f,
            // Top, red
            0.84f,  0.18f,  0.13f, 1.0f,
            // Bottom, also red
            0.84f,  0.18f,  0.13f, 1.0f,
    };

    public static final float[] CUBE_NORMALS_FACES = new float[] {
            // Front face
            0.0f, 0.0f, 1.0f,
            // Right face
            1.0f, 0.0f, 0.0f,
            // Back face
            0.0f, 0.0f, -1.0f,
            // Left face
            -1.0f, 0.0f, 0.0f,
            // Top face
            0.0f, 1.0f, 0.0f,
            // Bottom face
            0.0f, -1.0f, 0.0f,
    };

    public static final float[] CUBE_FOUND_COLORS_FACES = new float[]{
            // Same yellow for front, right, back, left, top, bottom faces
            1.0f,  0.65f, 0.0f, 1.0f,
            1.0f,  0.65f, 0.0f, 1.0f,
            1.0f,  0.65f, 0.0f, 1.0f,
            1.0f,  0.65f, 0.0f, 1.0f,
            1.0f,  0.65f, 0.0f, 1.0f,
            1.0f,  0.65f, 0.0f, 1.0f,
    };

    /**
     * Utility method for generating float arrays for cube faces
     *
     * @param model - float[] array of values per face.
     * @param coords_per_vertex - int number of coordinates per
    vertex.
     * @return - Returns float array of coordinates for
    triangulated cube faces.
     *               6 faces X 6 points X coords_per_vertex
     */
    public static float[] cubeFacesToArray(float[] model, int coords_per_vertex) {
        float coords[] = new float[6 * 6 * coords_per_vertex];
        int index = 0;
        for (int iFace=0; iFace < 6; iFace++) {
            for (int iVertex=0; iVertex < 6; iVertex++) {
                for (int iCoord=0; iCoord < coords_per_vertex; iCoord++) {
                    coords[index] = model[iFace*coords_per_vertex + iCoord];
                    index++;
                }
            }
        }
        return coords;
    }

}
