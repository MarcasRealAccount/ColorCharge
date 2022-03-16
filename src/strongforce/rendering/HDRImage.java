package strongforce.rendering;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL30;

public class HDRImage {
	public float[] data;

	private int width, height;

	private int pWidth, pHeight;
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

		if (this.data.length > this.pDataLength || this.width != this.pWidth || this.height != this.pHeight) {
			GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL30.GL_RGB32F, this.width, this.height, 0, GL11.GL_RGB,
					GL11.GL_FLOAT, this.data);
			this.pDataLength = this.data.length;
			this.pWidth = this.width;
			this.pHeight = this.height;
		} else {
			GL11.glTexSubImage2D(GL11.GL_TEXTURE_2D, 0, 0, 0, this.width, this.height, GL11.GL_RGB, GL11.GL_FLOAT,
					this.data);
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
		if (this.width != width || this.height != height) {
			this.data = new float[width * height * 3];
			this.width = width;
			this.height = height;
		}
	}

	public void setData(float[] data, int width, int height) {
		this.data = data;
		this.width = width;
		this.height = height;
	}

	public float[] getData() {
		return this.data;
	}

	public int getWidth() {
		return this.width;
	}

	public int getHeight() {
		return this.height;
	}
}
