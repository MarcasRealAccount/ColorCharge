package strongforce;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;

import strongforce.rendering.Image;
import strongforce.rendering.Mesh;
import strongforce.rendering.ShaderProgram;
import strongforce.rendering.StaticVertex2D;
import strongforce.windowing.Window;

public class StrongForce {
	private Window window = new Window(1440, 720, "ColorField");

	private Mesh<StaticVertex2D> rect = new Mesh<StaticVertex2D>();
	private ShaderProgram rectShaders = new ShaderProgram();

	private Image colors = new Image();
	private Image fieldVectors = new Image();

	private ColorField colorField = new ColorField();
	private ColorPoint hoveredPoint = null;

	private float minX = -2.0F;
	private float maxX = 2.0F;
	private float minY = -2.0F;
	private float maxY = 2.0F;

	private float pX = 0.0F;
	private float pY = 0.0F;

	private float mouseX = 0.0F;
	private float mouseY = 0.0F;
	private int buttonsDown = 0;
	private boolean middle = false;

	public static float clamp(float x, float min, float max) {
		return Math.max(Math.min(x, max), min);
	}

	public static float map(float x, float xmin, float xmax, float ymin, float ymax) {
		return (ymax - ymin) / (xmax - xmin) * (x - xmin) + ymin;
	}

	public static float mapc(float x, float xmin, float xmax, float ymin, float ymax) {
		return clamp(map(x, xmin, xmax, ymin, ymax), ymin, ymax);
	}

	public float mapScreenX(float x) {
		return map(x, 0, colors.getHeight(), minX, maxX);
	}

	public float mapScreenY(float y) {
		return map(y, 0, colors.getHeight(), minY, maxY);
	}

	public float mapColorFieldX(float x) {
		return map(x, minX, maxX, 0, colors.getHeight());
	}

	public float mapColorFieldY(float y) {
		return map(y, minY, maxY, 0, colors.getHeight());
	}

	public void run() {
		window.create();

		GLFW.glfwSetScrollCallback(window.getWindowPtr(), this::mouseWheel);
		GLFW.glfwSetCursorPosCallback(window.getWindowPtr(), this::mouseMove);
		GLFW.glfwSetMouseButtonCallback(window.getWindowPtr(), this::mouseButton);

		rect.setVertices(new StaticVertex2D[] { new StaticVertex2D(-1.0F, 1.0F, 0.0F, 0.0F),
				new StaticVertex2D(1.0F, 1.0F, 1.0F, 0.0F), new StaticVertex2D(1.0F, -1.0F, 1.0F, 1.0F),
				new StaticVertex2D(-1.0F, -1.0F, 0.0F, 1.0F) });
		rect.setIndices(new int[] { 0, 1, 2, 2, 3, 0 });
		rect.updateGL();

		String vertexShader = """
				#version 410 core

				layout(location = 0) in vec2 position;
				layout(location = 1) in vec2 uv;

				layout(location = 0) out vec2 passUV;

				void main() {
					gl_Position = vec4(position, 0.0, 1.0);
					passUV = uv;
				}
								""";
		String fragmentShader = """
				#version 410 core

				layout(location = 0) in vec2 passUV;

				layout(location = 0) out vec4 color;

				uniform sampler2D colors;
				uniform sampler2D fieldVectors;

				void main() {
					if (passUV.x >= 0.5)
						color = texture(fieldVectors, vec2(passUV.x * 2.0 - 1.0, passUV.y));
					else
						color = texture(colors, vec2(passUV.x * 2.0, passUV.y));
				}
				""";
		rectShaders.setShaderSource(vertexShader, fragmentShader);
		rectShaders.updateGL();
		rectShaders.bind();
		rectShaders.setUniform1i("colors", 0);
		rectShaders.setUniform1i("fieldVectors", 1);

		colors.allocate(720, 720);
		fieldVectors.allocate(720, 720);

		// Proton
//		colorField.addColorPoint(new ColorPoint(0.0F, -0.5F, 0.0F, 1.0F, 0.0F, 4.8e-1F));
//		colorField.addColorPoint(new ColorPoint(-0.5F, 0.5F, 0.0F, 0.0F, 1.0F, 2.3e-1F));
//		colorField.addColorPoint(new ColorPoint(0.5F, 0.5F, 1.0F, 0.0F, 0.0F, 2.3e-1F));

		// Meson
		colorField.addColorPoint(new ColorPoint(0.0F, -0.5F, 1.0F, 0.0F, 0.0F, 2.3e-1F));
		colorField.addColorPoint(new ColorPoint(0.0F, 0.5F, 0.0F, 1.0F, 1.0F, 2.3e-1F));

		while (!window.isCloseRequested()) {
			Color color = new Color();
			Vector rg = new Vector();
			Vector rb = new Vector();
			Vector gb = new Vector();
			Vector combined = new Vector();
			for (int y = 0; y < colors.getHeight(); ++y) {
				float my = map(y, 0, colors.getHeight(), minY, maxY);
				for (int x = 0; x < colors.getWidth(); ++x) {
					int index = x + y * colors.getWidth();
					float mx = map(x, 0, colors.getWidth(), minX, maxX);

					colorField.calculateColor(mx, my, color);
					colorField.calculateDirection(mx, my, (maxX - minX) / colors.getWidth() * 0.02F, rg, rb, gb);

					combined.x = rg.x + rb.x/* + gb.x */;
					combined.y = rg.y + rb.y/* + gb.y */;
					combined.z = rg.z + rb.z/* + gb.z */;

					int r = (int) mapc(color.r, 0.0F, 1.20F, 0.0F, 255.0F);
					int g = (int) mapc(color.g, 0.0F, 1.20F, 0.0F, 255.0F);
					int b = (int) mapc(color.b, 0.0F, 1.20F, 0.0F, 255.0F);

					colors.data[index] = 255 << 24 | (b & 0xFF) << 16 | (g & 0xFF) << 8 | (r & 0xFF);

					r = (int) (combined.x * 128.0F + 128.0F);
					g = (int) (combined.y * 128.0F + 128.0F);
					b = (int) (combined.z * 128.0F + 128.0F);

					fieldVectors.data[index] = 255 << 24 | (b & 0xFF) << 16 | (g & 0xFF) << 8 | (r & 0xFF);
				}
			}

			colors.updateGL();
			fieldVectors.updateGL();

			GL11.glClearColor(0.1F, 0.1F, 0.1F, 1.0F);
			GL11.glClearDepth(0.0);
			GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);

			rectShaders.bind();
			colors.bind(GL13.GL_TEXTURE0);
			fieldVectors.bind(GL13.GL_TEXTURE1);
			rect.draw();

			window.update();
		}

		rect.cleanUp();
		rectShaders.cleanUp();
		colors.cleanUp();
		fieldVectors.cleanUp();
		window.destroy();
	}

	public void mouseWheel(long windowPtr, double dx, double dy) {
		float eX = mapScreenX(mouseX);
		float eY = mapScreenY(mouseY);

		float zoomFactor = (float) Math.pow(1.05F, dy);

		this.minX = (this.minX - eX) * zoomFactor + eX;
		this.minY = (this.minY - eY) * zoomFactor + eY;
		this.maxX = (this.maxX - eX) * zoomFactor + eX;
		this.maxY = (this.maxY - eY) * zoomFactor + eY;
	}

	public void mouseMove(long windowPtr, double x, double y) {
		this.mouseX = (float) x;
		this.mouseY = (float) y;

		if (buttonsDown > 0)
			mouseDrag(windowPtr, x, y);
		else
			this.hoveredPoint = this.colorField.closestPoint(mapScreenX(this.mouseX), mapScreenY(this.mouseY));
	}

	public void mouseButton(long windowPtr, int button, int action, int mods) {
		if (action == GLFW.GLFW_PRESS)
			mousePress(windowPtr, button);
		else if (action == GLFW.GLFW_RELEASE)
			mouseRelease(windowPtr, button);
	}

	public void mousePress(long windowPtr, int button) {
		++buttonsDown;
		if (button == GLFW.GLFW_MOUSE_BUTTON_MIDDLE) {
			this.pX = mapScreenX(this.mouseX);
			this.pY = mapScreenY(this.mouseY);
			this.middle = true;
		} else {
			this.hoveredPoint = this.colorField.closestPoint(mapScreenX(this.mouseX), mapScreenY(this.mouseY));
		}
	}

	public void mouseRelease(long windowPtr, int button) {
		--buttonsDown;
		if (button == GLFW.GLFW_MOUSE_BUTTON_MIDDLE)
			this.middle = false;
	}

	public void mouseDrag(long windowPtr, double x, double y) {
		if (middle) {
			float eX = mapScreenX(this.mouseX);
			float eY = mapScreenY(this.mouseY);

			this.minX = (this.minX - eX) + this.pX;
			this.minY = (this.minY - eY) + this.pY;
			this.maxX = (this.maxX - eX) + this.pX;
			this.maxY = (this.maxY - eY) + this.pY;
			this.pX = eX;
			this.pY = eY;
		} else if (hoveredPoint != null) {
			this.hoveredPoint.x = mapScreenX(this.mouseX);
			this.hoveredPoint.y = mapScreenY(this.mouseY);
		}
	}

	public static void main(String[] args) {
		new StrongForce().run();
	}
}
