package com.buddha.world;

public class Circle {
	public Particle particle;
	public float radius;

	public Circle(float x, float y, float r, float mass) {
		this.particle = new Particle(x, y, mass);
		this.radius = r;
	}
	
	public void update() {
		particle.update();
	}
}
