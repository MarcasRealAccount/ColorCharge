package strongforce.rendering;

import org.lwjgl.opengl.GL20;

public class Shader {
	private String source;
	private int type;

	private int shader;

	public void updateGL() {
		cleanUp();

		this.shader = GL20.glCreateShader(this.type);
		GL20.glShaderSource(this.shader, this.source);
		GL20.glCompileShader(this.shader);

		if (GL20.glGetShaderi(this.shader, GL20.GL_COMPILE_STATUS) != GL20.GL_TRUE)
			throw new RuntimeException(GL20.glGetShaderInfoLog(this.shader));
	}

	public void cleanUp() {
		if (this.shader != 0)
			GL20.glDeleteShader(this.shader);
		this.shader = 0;
	}

	public int getShaderID() {
		return shader;
	}

	public void setSource(String source, int type) {
		this.source = source;
		this.type = type;
	}

	public String getSource() {
		return this.source;
	}
}
