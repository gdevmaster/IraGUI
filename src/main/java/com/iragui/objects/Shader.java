package com.iragui.objects;

import org.joml.*;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL30;

import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Shader {

    private int shaderProgramID;
    private boolean beingUsed = false;

    private String vertexSource;
    private String fragmentSource;
    private String filePath;

    // Constructor: Load shader from combined source string
    public Shader(String source, boolean isString) {
        try {
            parseShaderSource(source);
        } catch (IOException e) {
            e.printStackTrace();
            assert false : "Error parsing shader source string.";
        }
    }

    // Constructor: Load shader from file
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

    // Cross-platform shader source parsing
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

    // Compile shaders and link program
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

    public void use() {
        if (!beingUsed) {
            GL30.glUseProgram(shaderProgramID);
            beingUsed = true;
        }
    }

    public void detach() {
        GL30.glUseProgram(0);
        beingUsed = false;
    }

    // Uniform upload methods
    public void uploadMat3f(String varName, Matrix3f mat3) {
        int varLocation = GL30.glGetUniformLocation(shaderProgramID, varName);
        use();
        FloatBuffer matBuffer = BufferUtils.createFloatBuffer(9);
        mat3.get(matBuffer);
        GL30.glUniformMatrix3fv(varLocation, false, matBuffer);
    }

    public void uploadMat4f(String varName, Matrix4f mat4) {
        int varLocation = GL30.glGetUniformLocation(shaderProgramID, varName);
        use();
        FloatBuffer matBuffer = BufferUtils.createFloatBuffer(16);
        mat4.get(matBuffer);
        GL30.glUniformMatrix4fv(varLocation, false, matBuffer);
    }

    public void uploadVec2f(String varName, Vector2f vec) {
        int varLocation = GL30.glGetUniformLocation(shaderProgramID, varName);
        use();
        GL30.glUniform2f(varLocation, vec.x, vec.y);
    }

    public void uploadVec3f(String varName, Vector3f vec) {
        int varLocation = GL30.glGetUniformLocation(shaderProgramID, varName);
        use();
        GL30.glUniform3f(varLocation, vec.x, vec.y, vec.z);
    }

    public void uploadVec4f(String varName, Vector4f vec) {
        int varLocation = GL30.glGetUniformLocation(shaderProgramID, varName);
        use();
        GL30.glUniform4f(varLocation, vec.x, vec.y, vec.z, vec.w);
    }

    public void uploadFloat(String varName, float val) {
        int varLocation = GL30.glGetUniformLocation(shaderProgramID, varName);
        use();
        GL30.glUniform1f(varLocation, val);
    }

    public void uploadInt(String varName, int val) {
        int varLocation = GL30.glGetUniformLocation(shaderProgramID, varName);
        use();
        GL30.glUniform1i(varLocation, val);
    }

    public void uploadTexture(String varName, int slot) {
        int varLocation = GL30.glGetUniformLocation(shaderProgramID, varName);
        use();
        GL30.glUniform1i(varLocation, slot);
    }

    public void uploadIntArray(String varName, int[] array) {
        int varLocation = GL30.glGetUniformLocation(shaderProgramID, varName);
        use();
        GL30.glUniform1iv(varLocation, array);
    }

    public int getId() {
        return shaderProgramID;
    }
}
