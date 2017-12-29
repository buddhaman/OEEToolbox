package com.buddha.world;

import com.badlogic.gdx.math.Vector2;

public class Particle {
	
	public Vector2 pos;
	public Vector2 oldPos;
	public float invMass;
	public float friction = 0.97f;
	
	public Particle(float x, float y, float mass) {
		pos = new Vector2(x, y);
		oldPos = new Vector2(x, y);
		invMass = 1f/mass;
	}
	
	public void update() {
		float tempX = pos.x;
		float tempY = pos.y;
		pos.add((pos.x-oldPos.x)*friction, (pos.y-oldPos.y)*friction);
		oldPos.set(tempX, tempY);
	}
	
	public void addImpulse(float x, float y) {
		oldPos.sub(x, y);
	}
	
	public void resetVel() {
		oldPos.set(pos);
	}
	
	public void setVel(float x, float y) {
		oldPos.set(pos.x-x, pos.y-y);
	}
}

