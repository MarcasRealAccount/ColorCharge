package strongforce;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;

import strongforce.rendering.Mesh;
import strongforce.rendering.ShaderProgram;
import strongforce.rendering.StaticVertex2D;
import strongforce.windowing.Window;

public class StrongForce {
	private Window window = new Window(1440, 720, "ColorField");

	private Mesh<StaticVertex2D> rect = new Mesh<StaticVertex2D>();
	private ShaderProgram rectShaders = new ShaderProgram();

	private ColorField colorField = new ColorField();
	private ColorPoint hoveredPoint = null;

	private double pX = 0.0F;
	private double pY = 0.0F;

	private int fw = 0, fh = 0;
	private double mouseX = 0.0F;
	private double mouseY = 0.0F;
	private int buttonsDown = 0;
	private boolean camDrag = false;

	public static double clamp(double x, double min, double max) {
		return Math.max(Math.min(x, max), min);
	}

	public static double map(double x, double xmin, double xmax, double ymin, double ymax) {
		return (ymax - ymin) / (xmax - xmin) * (x - xmin) + ymin;
	}

	public static double mapc(double x, double xmin, double xmax, double ymin, double ymax) {
		return clamp(map(x, xmin, xmax, ymin, ymax), ymin, ymax);
	}

	public static double lerp(double a, double b, double t) {
		return a * (1.0 - t) + b * t;
	}

	public void run() {
		this.window.create();

		GLFW.glfwSetKeyCallback(this.window.getWindowPtr(), this::key);
		GLFW.glfwSetScrollCallback(this.window.getWindowPtr(), this::mouseWheel);
		GLFW.glfwSetCursorPosCallback(this.window.getWindowPtr(), this::mouseMove);
		GLFW.glfwSetMouseButtonCallback(this.window.getWindowPtr(), this::mouseButton);
		GLFW.glfwSetFramebufferSizeCallback(this.window.getWindowPtr(), this::framebufferSize);

		int[] pfw = new int[1], pfh = new int[1];
		GLFW.glfwGetFramebufferSize(window.getWindowPtr(), pfw, pfh);
		this.fw = pfw[0];
		this.fh = pfh[0];

		this.rect.setVertices(new StaticVertex2D[] { new StaticVertex2D(-1.0F, 1.0F, 0.0F, 0.0F),
				new StaticVertex2D(1.0F, 1.0F, 1.0F, 0.0F), new StaticVertex2D(1.0F, -1.0F, 1.0F, 1.0F),
				new StaticVertex2D(-1.0F, -1.0F, 0.0F, 1.0F) });
		this.rect.setIndices(new int[] { 0, 1, 2, 2, 3, 0 });
		this.rect.updateGL();

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
				uniform float width;
				uniform float height;

				vec3 getColorCharge(ivec2 pos, ivec2 size) {
					return texture(colors, vec2(float(pos.x) / float(size.x), float(pos.y) / float(size.y))).rgb;
				}

				vec3 calculateFieldDirection(ivec2 pos, ivec2 size) {
					vec3 z1 = getColorCharge(ivec2(pos.x - 1, pos.y - 1), size);
					vec3 z2 = getColorCharge(ivec2(pos.x, pos.y - 1), size);
					vec3 z3 = getColorCharge(ivec2(pos.x + 1, pos.y - 1), size);
					vec3 z4 = getColorCharge(ivec2(pos.x - 1, pos.y), size);
					vec3 z5 = getColorCharge(ivec2(pos.x + 1, pos.y), size);
					vec3 z6 = getColorCharge(ivec2(pos.x - 1, pos.y + 1), size);
					vec3 z7 = getColorCharge(ivec2(pos.x, pos.y + 1), size);
					vec3 z8 = getColorCharge(ivec2(pos.x + 1, pos.y + 1), size);

					float xden = float(size.x) / (8.0 * width);
					float yden = float(size.y) / (8.0 * height);

					vec3 direction = vec3(0.0, 0.0, 0.0);

					float rg1 = z1.r + z1.g;
					float rg2 = z2.r + z2.g;
					float rg3 = z3.r + z3.g;
					float rg4 = z4.r + z4.g;
					float rg5 = z5.r + z5.g;
					float rg6 = z6.r + z6.g;
					float rg7 = z7.r + z7.g;
					float rg8 = z8.r + z8.g;

					direction.x = (rg3 - rg1 + 2 * (rg8 - rg6) + rg5 - rg4) * xden;
					direction.y = (rg1 - rg6 + 2 * (rg2 - rg7) + rg3 - rg8) * yden;

					float rb1 = z1.r + z1.b;
					float rb2 = z2.r + z2.b;
					float rb3 = z3.r + z3.b;
					float rb4 = z4.r + z4.b;
					float rb5 = z5.r + z5.b;
					float rb6 = z6.r + z6.b;
					float rb7 = z7.r + z7.b;
					float rb8 = z8.r + z8.b;

					direction.x += (rb3 - rb1 + 2 * (rb8 - rb6) + rb5 - rb4) * xden;
					direction.y += (rb1 - rb6 + 2 * (rb2 - rb7) + rb3 - rb8) * yden;

					return direction;
				}

				void main() {
					ivec2 size = textureSize(colors, 0);
					vec3 col;
					ivec2 pos = ivec2(int((passUV.x * 2.0 - 1.0) * (size.x - 2)) + 1, int(passUV.y * (size.y - 2)) + 1);
					if (passUV.x >= 0.5) {
						col = abs(calculateFieldDirection(pos, size));
						col.x = mod(col.x, 1.0);
						col.y = mod(col.y, 1.0);
						col.z = mod(col.z, 1.0);
					} else {
						col = getColorCharge(pos, size);
					}
					color = vec4(col, 1.0);
				}
				""";
		this.rectShaders.setShaderSource(vertexShader, fragmentShader);
		this.rectShaders.updateGL();
		this.rectShaders.bind();
		this.rectShaders.setUniform1i("colors", 0);

		// Proton
//		this.colorField.addColorPoint(new ColorPoint(0.0F, -0.5F, 0.0F, 1.0F, 0.0F, 4.8e-1F));
//		this.colorField.addColorPoint(new ColorPoint(-0.5F, 0.5F, 0.0F, 0.0F, 1.0F, 2.3e-1F));
//		this.colorField.addColorPoint(new ColorPoint(0.5F, 0.5F, 1.0F, 0.0F, 0.0F, 2.3e-1F));

		// Meson
//		this.colorField.addColorPoint(new ColorPoint(0.0, -0.5, 1.0, 0.0, 0.0, 2.3e-1));
//		this.colorField.addColorPoint(new ColorPoint(0.0, 0.5, 0.0, 1.0, 1.0, 2.3e-1));
		
		// Repulsive?
		this.colorField.addColorPoint(new ColorPoint(0.0, -0.5, -1.0, 0.0, 0.0, 2.3e-1));
		this.colorField.addColorPoint(new ColorPoint(0.0, 0.5, 1.0, 0.0, 0.0, 2.3e-1));

		/*
		 * Color color = new Color(); Vector rg = new Vector(); Vector rb = new
		 * Vector(); Vector gb = new Vector(); Vector combined = new Vector();
		 */
		while (!this.window.isCloseRequested()) {
			this.colorField.updateColors(this.fw / 2, this.fh);

			GL11.glViewport(0, 0, this.fw, this.fh);
			GL11.glClearColor(0.1F, 0.1F, 0.1F, 1.0F);
			GL11.glClearDepth(0.0);
			GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);

			this.rectShaders.bind();
			double aspect = (double) this.colorField.image.getHeight() / this.colorField.image.getWidth();
			double height = (this.colorField.maxX - this.colorField.minX) * aspect;
			this.rectShaders.setUniform1f("width", (float) (this.colorField.maxX - this.colorField.minX));
			this.rectShaders.setUniform1f("height", (float) height);
			this.colorField.image.bind(GL13.GL_TEXTURE0);
			this.rect.draw();

			this.window.update();
		}

		this.rect.cleanUp();
		this.rectShaders.cleanUp();
		this.colorField.image.cleanUp();
		this.window.destroy();
	}

	public void key(long windowPtr, int keycode, int scancode, int action, int mods) {
		if (action == GLFW.GLFW_PRESS)
			keyPress(windowPtr, keycode);
		else if (action == GLFW.GLFW_RELEASE)
			keyRelease(windowPtr, keycode);
	}

	public void keyPress(long windowPtr, int keycode) {

	}

	public void keyRelease(long windowPtr, int keycode) {
		if (keycode == GLFW.GLFW_KEY_ESCAPE)
			GLFW.glfwSetWindowShouldClose(windowPtr, true);
	}

	public void mouseWheel(long windowPtr, double dx, double dy) {
		double eX = this.colorField.mapScreenX(this.mouseX, true);
		double eY = this.colorField.mapScreenY(this.mouseY);

		double zoomFactor = Math.pow(1.05F, dy);

		this.colorField.minX = (this.colorField.minX - eX) * zoomFactor + eX;
		this.colorField.maxX = (this.colorField.maxX - eX) * zoomFactor + eX;
		this.colorField.y = (this.colorField.y - eY) * zoomFactor + eY;
	}

	public void mouseMove(long windowPtr, double x, double y) {
		this.mouseX = x;
		this.mouseY = y;

		if (this.buttonsDown > 0)
			mouseDrag(windowPtr, x, y);
		else
			this.hoveredPoint = this.colorField.closestPoint(this.colorField.mapScreenX(this.mouseX, true),
					this.colorField.mapScreenY(this.mouseY));
	}

	public void mouseButton(long windowPtr, int button, int action, int mods) {
		if (action == GLFW.GLFW_PRESS)
			mousePress(windowPtr, button);
		else if (action == GLFW.GLFW_RELEASE)
			mouseRelease(windowPtr, button);
	}

	public void mousePress(long windowPtr, int button) {
		++buttonsDown;
		if (this.hoveredPoint == null) {
			this.pX = this.colorField.mapScreenX(this.mouseX);
			this.pY = this.colorField.mapScreenY(this.mouseY);
			this.camDrag = true;
		} else if (!this.camDrag) {
			this.hoveredPoint = this.colorField.closestPoint(this.colorField.mapScreenX(this.mouseX, true),
					this.colorField.mapScreenY(this.mouseY));
		}
	}

	public void mouseRelease(long windowPtr, int button) {
		--this.buttonsDown;
		this.camDrag = false;
	}

	public void mouseDrag(long windowPtr, double x, double y) {
		if (this.camDrag) {
			double eX = this.colorField.mapScreenX(this.mouseX);
			double eY = this.colorField.mapScreenY(this.mouseY);

			this.colorField.minX = (this.colorField.minX - eX) + this.pX;
			this.colorField.maxX = (this.colorField.maxX - eX) + this.pX;
			this.colorField.y = (this.colorField.y - eY) + this.pY;
			// this.pX = eX;
			// this.pY = eY;
		} else if (hoveredPoint != null) {
			this.hoveredPoint.x = this.colorField.mapScreenX(this.mouseX, true);
			this.hoveredPoint.y = this.colorField.mapScreenY(this.mouseY);
		}
	}

	public void framebufferSize(long windowPtr, int width, int height) {
		this.fw = width;
		this.fh = height;
	}

	public static void main(String[] args) {
		new StrongForce().run();
	}
}
