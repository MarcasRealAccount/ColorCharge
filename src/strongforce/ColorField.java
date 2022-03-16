package strongforce;

import java.util.ArrayList;

public class ColorField {
	private final ArrayList<ColorPoint> points = new ArrayList<>();

	public void addColorPoint(ColorPoint point) {
		this.points.add(point);
	}

	public void removeColorPoint(ColorPoint point) {
		this.points.remove(point);
	}

	public ColorPoint closestPoint(float x, float y) {
		ColorPoint closest = null;
		float closestDistSq = 0.2F;
		for (var point : points) {
			float dx = point.x - x;
			float dy = point.y - y;
			float distSq = dx * dx + dy * dy;
			if (distSq < closestDistSq) {
				closest = point;
				closestDistSq = distSq;
			}
		}
		return closest;
	}

	public void calculateColor(float x, float y, Color color) {
		float r = 0.0F;
		float g = 0.0F;
		float b = 0.0F;
		for (var point : points) {
			float dx = point.x - x;
			float dy = point.y - y;
			float distSq = dx * dx + dy * dy;
			float amp = point.mass / distSq;
			r += point.r * amp;
			g += point.g * amp;
			b += point.b * amp;
		}
		color.r = r;
		color.g = g;
		color.b = b;
	}

	public void calculateDirection(float x, float y, float d, Vector rg, Vector rb, Vector gb) {
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

		float rg1 = z1.r - z1.g;
		float rg2 = z2.r - z2.g;
		float rg3 = z3.r - z3.g;
		float rg4 = z4.r - z4.g;
		float rg5 = z5.r - z5.g;
		float rg6 = z6.r - z6.g;
		float rg7 = z7.r - z7.g;
		float rg8 = z8.r - z8.g;

		rg.x = (rg3 - rg1 + 2 * (rg8 - rg6) + rg5 - rg4) / (8 * d);
		rg.y = (rg1 - rg6 + 2 * (rg2 - rg7) + rg3 - rg8) / (8 * d);

		float rb1 = z1.r - z1.b;
		float rb2 = z2.r - z2.b;
		float rb3 = z3.r - z3.b;
		float rb4 = z4.r - z4.b;
		float rb5 = z5.r - z5.b;
		float rb6 = z6.r - z6.b;
		float rb7 = z7.r - z7.b;
		float rb8 = z8.r - z8.b;

		rb.x = (rb3 - rb1 + 2 * (rb8 - rb6) + rb5 - rb4) / (8 * d);
		rb.y = (rb1 - rb6 + 2 * (rb2 - rb7) + rb3 - rb8) / (8 * d);

		float gb1 = z1.g - z1.b;
		float gb2 = z2.g - z2.b;
		float gb3 = z3.g - z3.b;
		float gb4 = z4.g - z4.b;
		float gb5 = z5.g - z5.b;
		float gb6 = z6.g - z6.b;
		float gb7 = z7.g - z7.b;
		float gb8 = z8.g - z8.b;

		gb.x = (gb3 - gb1 + 2 * (gb8 - gb6) + gb5 - gb4) / (8 * d);
		gb.y = (gb1 - gb6 + 2 * (gb2 - gb7) + gb3 - gb8) / (8 * d);
	}
}
