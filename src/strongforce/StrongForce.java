package strongforce;

import processing.core.PApplet;
import processing.core.PImage;
import processing.core.PVector;
import processing.event.MouseEvent;

public class StrongForce extends PApplet {
	private ColorField colorField = new ColorField();

	private PImage colors;
	private PImage fieldVectors;

	private ColorPoint hoveredPoint = null;

	private float minX = -2.0F;
	private float maxX = 2.0F;
	private float minY = -2.0F;
	private float maxY = 2.0F;

	private float pX = 0.0F;
	private float pY = 0.0F;

	public float mapScreenX(float x) {
		return map(x, 0, height, minX, maxX);
	}

	public float mapScreenY(float y) {
		return map(y, 0, height, minY, maxY);
	}

	public float mapColorFieldX(float x) {
		return map(x, minX, maxX, 0, height);
	}

	public float mapColorFieldY(float y) {
		return map(y, minY, maxY, 0, height);
	}

	@Override
	public void settings() {
		size(1440, 720, P2D);
	}

	@Override
	public void setup() {
		// Proton
//		colorField.addColorPoint(new ColorPoint(0.0F, -0.5F, 0.0F, 1.0F, 0.0F, 4.8e-1F));
//		colorField.addColorPoint(new ColorPoint(-0.5F, 0.5F, 0.0F, 0.0F, 1.0F, 2.3e-1F));
//		colorField.addColorPoint(new ColorPoint(0.5F, 0.5F, 1.0F, 0.0F, 0.0F, 2.3e-1F));

		// Meson
		colorField.addColorPoint(new ColorPoint(0.0F, -0.5F, 1.0F, 0.0F, 0.0F, 2.3e-1F));
		colorField.addColorPoint(new ColorPoint(0.0F, 0.5F, 1.0F, 1.0F, 0.0F, 2.3e-1F));
		colorField.addColorPoint(new ColorPoint(0.0F, -0.5F, 0.0F, 1.0F, 0.0F, 2.3e-1F));
		colorField.addColorPoint(new ColorPoint(0.0F, 0.5F, 0.0F, 1.0F, 1.0F, 2.3e-1F));
		colorField.addColorPoint(new ColorPoint(0.0F, -0.5F, 1.0F, 0.0F, 0.0F, 2.3e-1F));
		colorField.addColorPoint(new ColorPoint(0.0F, 0.5F, 0.0F, 1.0F, 1.0F, 2.3e-1F));
		colorField.addColorPoint(new ColorPoint(0.0F, -0.5F, 1.0F, 0.0F, 0.0F, 2.3e-1F));
		colorField.addColorPoint(new ColorPoint(0.0F, 0.5F, 0.0F, 1.0F, 1.0F, 2.3e-1F));

		colors       = createImage(height, height, RGB);
		fieldVectors = createImage(height, height, RGB);
		colors.loadPixels();
		fieldVectors.loadPixels();
	}

	@Override
	public void draw() {
		Color   color    = new Color();
		PVector rg       = new PVector();
		PVector rb       = new PVector();
		PVector gb       = new PVector();
		PVector combined = new PVector();
		for (int y = 0; y < height; ++y) {
			float my = map(y, 0, height, minY, maxY);
			for (int x = 0; x < height; ++x) {
				int   index = x + y * height;
				float mx    = map(x, 0, height, minX, maxX);

				colorField.calculateColor(mx, my, color);
				colorField.calculateDirection(mx, my, (maxX - minX) / height * 0.02F, rg, rb, gb);

				combined.x = rg.x + rb.x/* + gb.x */;
				combined.y = rg.y + rb.y/* + gb.y */;
				combined.z = rg.z + rb.z/* + gb.z */;

				int r = (int) max(min(map(color.r, 0.0F, 1.20F, 0.0F, 255.0F), 255.0F), 0.0F);
				int g = (int) max(min(map(color.g, 0.0F, 1.20F, 0.0F, 255.0F), 255.0F), 0.0F);
				int b = (int) max(min(map(color.b, 0.0F, 1.20F, 0.0F, 255.0F), 255.0F), 0.0F);

				colors.pixels[index] = 255 << 24 | (r & 0xFF) << 16 | (g & 0xFF) << 8 | (b & 0xFF);

				r = (int) (combined.x * 128.0F + 128.0F);
				g = (int) (combined.y * 128.0F + 128.0F);
				b = (int) (combined.z * 128.0F + 128.0F);

				fieldVectors.pixels[index] = 255 << 24 | (r & 0xFF) << 16 | (g & 0xFF) << 8 | (b & 0xFF);
			}
		}

		colors.updatePixels();
		fieldVectors.updatePixels();

		image(colors, 0, 0, height, height);
		image(fieldVectors, height, 0, height, height);

		if (hoveredPoint != null) {
			noFill();
			stroke(255, 0, 0);
			circle(mapColorFieldX(hoveredPoint.x), mapColorFieldY(hoveredPoint.y), 10.0F);
		}
	}

	@Override
	public void mouseWheel(MouseEvent event) {
		float eX = mapScreenX(event.getX());
		float eY = mapScreenY(event.getY());

		float pow        = event.getCount();
		float zoomFactor = (float) Math.pow(1.05F, pow);

		minX = (minX - eX) * zoomFactor + eX;
		minY = (minY - eY) * zoomFactor + eY;
		maxX = (maxX - eX) * zoomFactor + eX;
		maxY = (maxY - eY) * zoomFactor + eY;
	}

	@Override
	public void mouseMoved(MouseEvent event) {
		hoveredPoint = colorField.closestPoint(mapScreenX(event.getX()), mapScreenY(event.getY()));
	}

	@Override
	public void mousePressed(MouseEvent event) {
		if (event.getButton() == CENTER) {
			pX = mapScreenX(event.getX());
			pY = mapScreenY(event.getY());
		} else {
			hoveredPoint = colorField.closestPoint(mapScreenX(event.getX()), mapScreenY(event.getY()));
		}
	}

	@Override
	public void mouseDragged(MouseEvent event) {
		if (event.getButton() == CENTER) {
			float eX = mapScreenX(event.getX());
			float eY = mapScreenY(event.getY());

			minX = (minX - eX) + pX;
			minY = (minY - eY) + pY;
			maxX = (maxX - eX) + pX;
			maxY = (maxY - eY) + pY;
		} else if (hoveredPoint != null) {
			hoveredPoint.x = mapScreenX(event.getX());
			hoveredPoint.y = mapScreenY(event.getY());
		}
	}

	public static void main(String[] args) {
		PApplet.main(StrongForce.class, args);
	}
}
