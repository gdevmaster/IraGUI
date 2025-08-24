package com.iragui.objects;

import org.joml.*;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL30;

import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Represents an OpenGL shader program composed of a vertex shader and a fragment shader.
 * <p>
 * This class is responsible for loading shader source code (from a file or raw string),
 * compiling shaders, linking them into a shader program, and providing utility methods
 * to bind the shader and upload uniform variables.
 * </p>
 */
public class Shader {

    private int shaderProgramID;
    private boolean beingUsed = false;

    private String vertexSource;
    private String fragmentSource;
    private String filePath;

    /**
     * Represents an OpenGL shader program composed of a vertex shader and a fragment shader.
     * <p>
     * This class is responsible for loading shader source code (from a file or raw string),
     * compiling shaders, linking them into a shader program, and providing utility methods
     * to bind the shader and upload uniform variables.
     * </p>
     */
    public Shader(String source, boolean isString) {
        try {
            parseShaderSource(source);
        } catch (IOException e) {
            e.printStackTrace();
            assert false : "Error parsing shader source string.";
        }
    }

    /**
     * Creates a shader program by loading shader source code from a file.
     * <p>
     * The file must contain both vertex and fragment shader code separated
     * by lines starting with {@code #type vertex} or {@code #type fragment}.
     * </p>
     *
     * @param filePath The path to the shader file.
     */
    public Shader(String filePath) {
        this.filePath = filePath;

        try {
            String source = new String(Files.readAllBytes(Paths.get(filePath)));
            parseShaderSource(source);
        } catch (IOException e) {
            e.printStackTrace();
            assert false : "Error loading shader from file: '" + filePath + "'";
        }
    }

   
    private void parseShaderSource(String source) throws IOException {
        String[] lines = source.split("\\R"); // Split on any line separator
        StringBuilder vertexBuilder = new StringBuilder();
        StringBuilder fragmentBuilder = new StringBuilder();

        String currentType = null;

        for (String line : lines) {
            if (line.trim().startsWith("#type")) {
                String[] tokens = line.trim().split("\\s+");
                if (tokens.length < 2) {
                    throw new IOException("Invalid shader type declaration: " + line);
                }
                currentType = tokens[1].trim().toLowerCase();
            } else {
                if ("vertex".equals(currentType)) {
                    vertexBuilder.append(line).append(System.lineSeparator());
                } else if ("fragment".equals(currentType)) {
                    fragmentBuilder.append(line).append(System.lineSeparator());
                } else if (currentType != null) {
                    throw new IOException("Unknown shader type: " + currentType);
                }
            }
        }

        vertexSource = vertexBuilder.toString();
        fragmentSource = fragmentBuilder.toString();
    }

    /**
     * Compiles the vertex and fragment shaders and links them into a shader program.
     * <p>
     * This must be called before using the shader.
     * </p>
     *
     * @throws AssertionError if shader compilation or linking fails.
     */
    public void compile() {
        int vertexID = GL30.glCreateShader(GL30.GL_VERTEX_SHADER);
        GL30.glShaderSource(vertexID, vertexSource);
        GL30.glCompileShader(vertexID);

        if (GL30.glGetShaderi(vertexID, GL30.GL_COMPILE_STATUS) == GL30.GL_FALSE) {
            int len = GL30.glGetShaderi(vertexID, GL30.GL_INFO_LOG_LENGTH);
            System.out.println("ERROR: '" + filePath + "'\n\tVertex shader compilation failed");
            System.out.println(GL30.glGetShaderInfoLog(vertexID, len));
            assert false : "";
        }

        int fragmentID = GL30.glCreateShader(GL30.GL_FRAGMENT_SHADER);
        GL30.glShaderSource(fragmentID, fragmentSource);
        GL30.glCompileShader(fragmentID);

        if (GL30.glGetShaderi(fragmentID, GL30.GL_COMPILE_STATUS) == GL30.GL_FALSE) {
            int len = GL30.glGetShaderi(fragmentID, GL30.GL_INFO_LOG_LENGTH);
            System.out.println("ERROR: '" + filePath + "'\n\tFragment shader compilation failed");
            System.out.println(GL30.glGetShaderInfoLog(fragmentID, len));
            assert false : "";
        }

        shaderProgramID = GL30.glCreateProgram();
        GL30.glAttachShader(shaderProgramID, vertexID);
        GL30.glAttachShader(shaderProgramID, fragmentID);
        GL30.glLinkProgram(shaderProgramID);

        if (GL30.glGetProgrami(shaderProgramID, GL30.GL_LINK_STATUS) == GL30.GL_FALSE) {
            int len = GL30.glGetProgrami(shaderProgramID, GL30.GL_INFO_LOG_LENGTH);
            System.out.println("ERROR: '" + filePath + "'\n\tLinking of shaders failed");
            System.out.println(GL30.glGetProgramInfoLog(shaderProgramID, len));
            assert false : "";
        }

        // Clean up shaders after linking
        GL30.glDetachShader(shaderProgramID, vertexID);
        GL30.glDetachShader(shaderProgramID, fragmentID);
        GL30.glDeleteShader(vertexID);
        GL30.glDeleteShader(fragmentID);
    }

    /**
     * Activates (binds) this shader program for use.
     * <p>
     * If the shader is already in use, this call does nothing.
     * </p>
     */
    public void use() {
        if (!beingUsed) {
            GL30.glUseProgram(shaderProgramID);
            beingUsed = true;
        }
    }

    /**
     * Deactivates (unbinds) any currently active shader program.
     */
    public void detach() {
        GL30.glUseProgram(0);
        beingUsed = false;
    }

    /**
     * Uploads a 3x3 matrix to a shader uniform.
     *
     * @param varName the name of the uniform variable in the shader.
     * @param mat3    the {@link Matrix3f} to upload.
     */
    public void uploadMat3f(String varName, Matrix3f mat3) {
        int varLocation = GL30.glGetUniformLocation(shaderProgramID, varName);
        use();
        FloatBuffer matBuffer = BufferUtils.createFloatBuffer(9);
        mat3.get(matBuffer);
        GL30.glUniformMatrix3fv(varLocation, false, matBuffer);
    }

    /**
     * Uploads a 4x4 matrix to a shader uniform.
     *
     * @param varName the name of the uniform variable in the shader.
     * @param mat4    the {@link Matrix4f} to upload.
     */
    public void uploadMat4f(String varName, Matrix4f mat4) {
        int varLocation = GL30.glGetUniformLocation(shaderProgramID, varName);
        use();
        FloatBuffer matBuffer = BufferUtils.createFloatBuffer(16);
        mat4.get(matBuffer);
        GL30.glUniformMatrix4fv(varLocation, false, matBuffer);
    }

    /**
     * Uploads a 2D vector to a shader uniform.
     *
     * @param varName the name of the uniform variable in the shader.
     * @param vec     the {@link Vector2f} to upload.
     */
    public void uploadVec2f(String varName, Vector2f vec) {
        int varLocation = GL30.glGetUniformLocation(shaderProgramID, varName);
        use();
        GL30.glUniform2f(varLocation, vec.x, vec.y);
    }

    /**
     * Uploads a 3D vector to a shader uniform.
     *
     * @param varName the name of the uniform variable in the shader.
     * @param vec     the {@link Vector3f} to upload.
     */
    public void uploadVec3f(String varName, Vector3f vec) {
        int varLocation = GL30.glGetUniformLocation(shaderProgramID, varName);
        use();
        GL30.glUniform3f(varLocation, vec.x, vec.y, vec.z);
    }

    /**
     * Uploads a 4D vector to a shader uniform.
     *
     * @param varName the name of the uniform variable in the shader.
     * @param vec     the {@link Vector4f} to upload.
     */
    public void uploadVec4f(String varName, Vector4f vec) {
        int varLocation = GL30.glGetUniformLocation(shaderProgramID, varName);
        use();
        GL30.glUniform4f(varLocation, vec.x, vec.y, vec.z, vec.w);
    }

    /**
     * Uploads a float value to a shader uniform.
     *
     * @param varName the name of the uniform variable in the shader.
     * @param val     the float value to upload.
     */
    public void uploadFloat(String varName, float val) {
        int varLocation = GL30.glGetUniformLocation(shaderProgramID, varName);
        use();
        GL30.glUniform1f(varLocation, val);
    }

    /**
     * Uploads an integer value to a shader uniform.
     *
     * @param varName the name of the uniform variable in the shader.
     * @param val     the integer value to upload.
     */
    public void uploadInt(String varName, int val) {
        int varLocation = GL30.glGetUniformLocation(shaderProgramID, varName);
        use();
        GL30.glUniform1i(varLocation, val);
    }

    /**
     * Uploads a texture slot index to a shader uniform.
     *
     * @param varName the name of the uniform variable in the shader.
     * @param slot    the texture slot (e.g., 0 for GL_TEXTURE0).
     */
    public void uploadTexture(String varName, int slot) {
        int varLocation = GL30.glGetUniformLocation(shaderProgramID, varName);
        use();
        GL30.glUniform1i(varLocation, slot);
    }

    /**
     * Uploads an array of integers to a shader uniform.
     *
     * @param varName the name of the uniform variable in the shader.
     * @param array   the array of integers to upload.
     */
    public void uploadIntArray(String varName, int[] array) {
        int varLocation = GL30.glGetUniformLocation(shaderProgramID, varName);
        use();
        GL30.glUniform1iv(varLocation, array);
    }

    /**
     * Gets the OpenGL ID of this shader program.
     *
     * @return the shader program ID.
     */
    public int getId() {
        return shaderProgramID;
    }
}
