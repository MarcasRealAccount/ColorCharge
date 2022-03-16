package strongforce.rendering;

import java.nio.ByteBuffer;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;

public class StaticVertex2D extends Vertex {
	public float x, y;
	public float u, v;

	public StaticVertex2D(float x, float y, float u, float v) {
		this.x = x;
		this.y = y;
		this.u = u;
		this.v = v;
	}

	@Override
	public int getAlignedSize() {
		return 16;
	}

	@Override
	public void pushData(ByteBuffer buffer) {
		buffer.putFloat(x);
		buffer.putFloat(y);
		buffer.putFloat(u);
		buffer.putFloat(v);
	}

	@Override
	public void vertexAttribs(Mesh<?> mesh) {
		GL30.glVertexAttribPointer(0, 2, GL11.GL_FLOAT, false, 16, 0);
		GL30.glVertexAttribPointer(1, 2, GL11.GL_FLOAT, false, 16, 8);
		GL30.glEnableVertexAttribArray(0);
		GL30.glEnableVertexAttribArray(1);
	}
}
