package com.buddha.agent;

import com.buddha.world.Circle;

public class Ball {
	public Circle circle;
	
	public Ball(float x, float y) {
		circle = new Circle(x, y, 0.7f,0.3f);
		circle.particle.friction = 0.992f;
	}
	
	public void update() {
		
	}
}
