package strongforce.rendering;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;

public class Image {
	public int[] data;

	private int width, height;

	private int pDataLength;
	private int texture;

	public void bind(int bank) {
		GL13.glActiveTexture(bank);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, this.texture);
	}

	public void updateGL() {
		if (this.data == null)
			return;

		if (this.texture == 0)
			this.texture = GL11.glGenTextures();

		GL11.glBindTexture(GL11.GL_TEXTURE_2D, this.texture);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_CLAMP);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_CLAMP);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);

		if (this.data.length > this.pDataLength) {
			GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, this.width, this.height, 0, GL11.GL_RGBA,
					GL11.GL_UNSIGNED_BYTE, this.data);
			this.pDataLength = this.data.length;
		} else {
			GL11.glTexSubImage2D(GL11.GL_TEXTURE_2D, 0, 0, 0, this.width, this.height, GL11.GL_RGBA,
					GL11.GL_UNSIGNED_BYTE, this.data);
		}

		GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
	}

	public void cleanUp() {
		if (this.texture != 0)
			GL11.glDeleteTextures(this.texture);
		this.texture = 0;
	}

	public int getTextureID() {
		return this.texture;
	}

	public void allocate(int width, int height) {
		this.data = new int[width * height];
		this.width = width;
		this.height = height;
	}

	public void setData(int[] data, int width, int height) {
		this.data = data;
		this.width = width;
		this.height = height;
	}

	public int[] getData() {
		return this.data;
	}

	public int getWidth() {
		return this.width;
	}

	public int getHeight() {
		return this.height;
	}
}
