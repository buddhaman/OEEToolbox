package com.buddha.world;

import com.badlogic.gdx.utils.Array;

public class World {
	public Array<Constraint> constraints = new Array<Constraint>();
	public Array<Circle> circles = new Array<Circle>();
	public Array<AABB> triggers = new Array<AABB>();
	
	public AABB bounds;
	
	public World() {
		
	}
	
	public void update() {
		for(int i = 0; i < circles.size; i++) {
			Circle c = circles.get(i);
			c.update();
		}
	}
	
	public void addCircle(Circle circle) {
		circles.add(circle);
	}
	
	public void addConstraint(Constraint constraint) {
		constraints.add(constraint);
	}
	
	public void setBounds(float x1, float y1, float x2, float y2) {
		bounds = new AABB(x1, y1, x2, y2);
	}
	
	public void checkBounds() {
		if(bounds!=null) {
			for(Circle c : circles) {
				if(c.getX() > bounds.x2) {
					c.bounce(1f, 0, bounds.x2, c.getY());
				}
				if(c.getY() > bounds.y2) {
					c.bounce(0, 1f, c.getX(), bounds.y2);
				}
				if(c.getX() < bounds.x1) {
					c.bounce(1f, 0, bounds.x1, c.getY());
				}
				if(c.getY() < bounds.y1) {
					c.bounce(0, 1f, c.getX(), bounds.y1);
				}
			}
		}
	}
	
	public static void solveCollision(Circle c1, Circle c2) {
		Constraint.solveFluid(c1.particle, c2.particle, c1.radius+c2.radius, 1);
	}
}
