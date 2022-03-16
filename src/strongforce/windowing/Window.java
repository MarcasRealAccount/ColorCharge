package strongforce.windowing;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL;

public class Window {
	private long windowPtr;

	private int width, height;
	private String title;

	public Window(int width, int height, String title) {
		this.width = width;
		this.height = height;
		this.title = title;
		this.windowPtr = 0L;
	}

	public long getWindowPtr() {
		return this.windowPtr;
	}

	public void create() {
		if (!GLFW.glfwInit())
			throw new RuntimeException("Failed to initialize GLFW");

		GLFW.glfwDefaultWindowHints();
		GLFW.glfwWindowHint(GLFW.GLFW_CLIENT_API, GLFW.GLFW_OPENGL_API);
		GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MAJOR, 4);
		GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MINOR, 1);

		this.windowPtr = GLFW.glfwCreateWindow(this.width, this.height, this.title, 0, 0);
		if (this.windowPtr == 0L)
			throw new RuntimeException("Failed to create GLFW window");

		GLFW.glfwMakeContextCurrent(this.windowPtr);

		GL.createCapabilities();
	}

	public void destroy() {
		GLFW.glfwDestroyWindow(this.windowPtr);
		GLFW.glfwTerminate();
	}

	public void update() {
		GLFW.glfwSwapBuffers(this.windowPtr);
		GLFW.glfwPollEvents();
	}

	public boolean isCloseRequested() {
		return GLFW.glfwWindowShouldClose(this.windowPtr);
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}
}
