package strongforce.rendering;

import java.nio.ByteBuffer;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL30;
import org.lwjgl.system.MemoryUtil;

public class Mesh<V extends Vertex> {
	private V[] vertices;
	private int[] indices = null;

	private int pDataLength = 0;
	private int pIndicesLength = 0;
	private int vertexCount = 0;
	private int vao = 0;
	private int vbo = 0;
	private int ibo = 0;

	public void bind() {
		GL30.glBindVertexArray(this.vao);
	}

	public void draw() {
		bind();
//		GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_FILL);
		GL15.glDrawElements(GL11.GL_TRIANGLES, this.vertexCount, GL11.GL_UNSIGNED_INT, 0);
	}

	public void updateGL() {
		if (this.vertices == null || this.indices == null)
			return;

		if (this.vao == 0)
			this.vao = GL30.glGenVertexArrays();
		if (this.vbo == 0)
			this.vbo = GL15.glGenBuffers();
		if (this.ibo == 0)
			this.ibo = GL15.glGenBuffers();

		GL30.glBindVertexArray(this.vao);

		ByteBuffer data = MemoryUtil.memAlloc(this.vertices.length * this.vertices[0].getAlignedSize());
		for (int i = 0; i < this.vertices.length; ++i)
			this.vertices[i].pushData(data);
		data.flip();
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, this.vbo);

		if (data.capacity() > this.pDataLength) {
			GL15.glBufferData(GL15.GL_ARRAY_BUFFER, data, GL15.GL_STATIC_DRAW);
			this.pDataLength = data.capacity();
		} else {
			GL15.glBufferSubData(GL15.GL_ARRAY_BUFFER, 0, data);
		}

		GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, this.ibo);
		if (this.indices.length > this.pIndicesLength) {
			GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, this.indices, GL15.GL_STATIC_DRAW);
			this.pIndicesLength = this.indices.length;
		} else {
			GL15.glBufferSubData(GL15.GL_ELEMENT_ARRAY_BUFFER, 0, this.indices);
		}

		this.vertices[0].vertexAttribs(this);

		this.vertexCount = this.indices.length;
		GL30.glBindVertexArray(0);

		MemoryUtil.memFree(data);
	}

	public void cleanUp() {
		GL30.glBindVertexArray(0);

		if (this.vao != 0)
			GL30.glDeleteVertexArrays(this.vao);
		if (this.vbo != 0)
			GL15.glDeleteBuffers(this.vbo);
		if (this.ibo != 0)
			GL15.glDeleteBuffers(this.ibo);
		this.vao = 0;
		this.vbo = 0;
		this.ibo = 0;
	}

	public int getVao() {
		return vao;
	}

	public int getVbo() {
		return vbo;
	}

	public int getIbo() {
		return ibo;
	}

	public void setVertices(V[] vertices) {
		this.vertices = vertices;
	}

	public V[] getVertices() {
		return this.vertices;
	}

	public void setIndices(int[] indices) {
		this.indices = indices;
	}

	public int[] getIndices() {
		return this.indices;
	}
}
