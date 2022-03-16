package strongforce.rendering;

import java.nio.ByteBuffer;

public abstract class Vertex {
	public abstract int getAlignedSize();

	public abstract void pushData(ByteBuffer buffer);

	public abstract void vertexAttribs(Mesh<?> mesh);
}
