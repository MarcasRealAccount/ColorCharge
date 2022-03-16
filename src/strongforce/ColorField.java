package strongforce;

import java.util.ArrayList;

import strongforce.rendering.HDRImage;

public class ColorField {
	private final ArrayList<ColorPoint> points = new ArrayList<>();

	public double minX = -1.0;
	public double maxX = 1.0;
	public double y = 0.0;

	public HDRImage image = new HDRImage();

	public double mapScreenX(double x) {
		return mapScreenX(x, false);
	}

	public double mapScreenX(double x, boolean repeat) {
		if (repeat)
			x %= image.getWidth();
		return StrongForce.map(x, 0, image.getWidth(), minX, maxX);
	}

	public double mapScreenY(double y) {
		double aspect = (double) this.image.getHeight() / this.image.getWidth();
		double height = (this.maxX - this.minX) * aspect;
		double minY = this.y - (height * 0.5);
		double maxY = this.y + (height * 0.5);
		return StrongForce.map(y, 0, image.getHeight(), minY, maxY);
	}

	public void updateColors(int newWidth, int newHeight) {
		this.image.allocate(newWidth + 2, newHeight + 2);

		double aspect = (double) this.image.getHeight() / this.image.getWidth();
		double height = (this.maxX - this.minX) * aspect;
		double minY = this.y - (height * 0.5);
		double maxY = this.y + (height * 0.5);

		int index = 0;
		Color color = new Color();
		for (int y = 0; y < this.image.getHeight(); ++y) {
			double my = StrongForce.map(y, 1, this.image.getHeight() - 1, minY, maxY);
			for (int x = 0; x < this.image.getWidth(); ++x) {
				double mx = StrongForce.map(x, 1, this.image.getWidth() - 1, this.minX, this.maxX);
				calculateColor(mx, my, color);

				this.image.data[index++] = (float) color.r;
				this.image.data[index++] = (float) color.g;
				this.image.data[index++] = (float) color.b;
			}
		}

		this.image.updateGL();
	}

	public void addColorPoint(ColorPoint point) {
		this.points.add(point);
	}

	public void removeColorPoint(ColorPoint point) {
		this.points.remove(point);
	}

	public ColorPoint closestPoint(double x, double y) {
		ColorPoint closest = null;
		double closestDistSq = (this.maxX - this.minX) * (30.0 / this.image.getWidth());
		closestDistSq *= closestDistSq;
		for (var point : points) {
			double dx = point.x - x;
			double dy = point.y - y;
			double distSq = dx * dx + dy * dy;
			if (distSq < closestDistSq) {
				closest = point;
				closestDistSq = distSq;
			}
		}
		return closest;
	}

	public void calculateColor(double x, double y, Color color) {
		double r = 0.0F;
		double g = 0.0F;
		double b = 0.0F;
		for (var point : this.points) {
			double dx = point.x - x;
			double dy = point.y - y;
			double amp = point.mass / (dx * dx + dy * dy);
			r += point.r * amp;
			g += point.g * amp;
			b += point.b * amp;
		}
		color.r = r;
		color.g = g;
		color.b = b;
	}

	public void calculateDirection(double x, double y, double d, Vector rg, Vector rb, Vector gb) {
		Color z1 = new Color();
		Color z2 = new Color();
		Color z3 = new Color();
		Color z4 = new Color();
		Color z5 = new Color();
		Color z6 = new Color();
		Color z7 = new Color();
		Color z8 = new Color();
		calculateColor(x - d, y - d, z1);
		calculateColor(x, y - d, z2);
		calculateColor(x + d, y - d, z3);
		calculateColor(x - d, y, z4);
		calculateColor(x + d, y, z5);
		calculateColor(x - d, y + d, z6);
		calculateColor(x, y + d, z7);
		calculateColor(x + d, y + d, z8);

		double den = 1.0 / (8.0 * d);

		double rg1 = z1.r + z1.g;
		double rg2 = z2.r + z2.g;
		double rg3 = z3.r + z3.g;
		double rg4 = z4.r + z4.g;
		double rg5 = z5.r + z5.g;
		double rg6 = z6.r + z6.g;
		double rg7 = z7.r + z7.g;
		double rg8 = z8.r + z8.g;

		rg.x = (rg3 - rg1 + 2 * (rg8 - rg6) + rg5 - rg4) * den;
		rg.y = (rg1 - rg6 + 2 * (rg2 - rg7) + rg3 - rg8) * den;

		double rb1 = z1.r + z1.b;
		double rb2 = z2.r + z2.b;
		double rb3 = z3.r + z3.b;
		double rb4 = z4.r + z4.b;
		double rb5 = z5.r + z5.b;
		double rb6 = z6.r + z6.b;
		double rb7 = z7.r + z7.b;
		double rb8 = z8.r + z8.b;

		rb.x = (rb3 - rb1 + 2 * (rb8 - rb6) + rb5 - rb4) * den;
		rb.y = (rb1 - rb6 + 2 * (rb2 - rb7) + rb3 - rb8) * den;

		double gb1 = z1.g + z1.b;
		double gb2 = z2.g + z2.b;
		double gb3 = z3.g + z3.b;
		double gb4 = z4.g + z4.b;
		double gb5 = z5.g + z5.b;
		double gb6 = z6.g + z6.b;
		double gb7 = z7.g + z7.b;
		double gb8 = z8.g + z8.b;

		gb.x = (gb3 - gb1 + 2 * (gb8 - gb6) + gb5 - gb4) * den;
		gb.y = (gb1 - gb6 + 2 * (gb2 - gb7) + gb3 - gb8) * den;
	}
}
