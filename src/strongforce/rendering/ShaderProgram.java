package strongforce.rendering;

import org.lwjgl.opengl.GL20;

public class ShaderProgram {
	private String vertexShader;
	private String fragmentShader;

	private int program;

	public void bind() {
		GL20.glUseProgram(this.program);
	}

	public void setUniform1i(String name, int value) {
		GL20.glUniform1i(GL20.glGetUniformLocation(this.program, name), value);
	}

	public void setUniform1f(String name, float value) {
		GL20.glUniform1f(GL20.glGetUniformLocation(this.program, name), value);
	}

	public void updateGL() {
		if (this.vertexShader == null || this.fragmentShader == null)
			return;

		if (this.program == 0)
			this.program = GL20.glCreateProgram();

		Shader vs = new Shader();
		vs.setSource(vertexShader, GL20.GL_VERTEX_SHADER);
		Shader fs = new Shader();
		fs.setSource(fragmentShader, GL20.GL_FRAGMENT_SHADER);

		vs.updateGL();
		fs.updateGL();

		GL20.glAttachShader(this.program, vs.getShaderID());
		GL20.glAttachShader(this.program, fs.getShaderID());

		GL20.glLinkProgram(this.program);

		if (GL20.glGetProgrami(this.program, GL20.GL_LINK_STATUS) != GL20.GL_TRUE)
			throw new RuntimeException(GL20.glGetProgramInfoLog(this.program));

		GL20.glDetachShader(this.program, vs.getShaderID());
		GL20.glDetachShader(this.program, fs.getShaderID());

		vs.cleanUp();
		fs.cleanUp();
	}

	public void cleanUp() {
		GL20.glUseProgram(0);
		if (this.program != 0)
			GL20.glDeleteProgram(this.program);
		this.program = 0;
	}

	public int getProgramID() {
		return program;
	}

	public void setShaderSource(String vertexShader, String fragmentShader) {
		this.vertexShader = vertexShader;
		this.fragmentShader = fragmentShader;
	}
}
