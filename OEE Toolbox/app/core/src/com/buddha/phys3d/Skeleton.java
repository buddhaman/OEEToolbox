package com.buddha.phys3d;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.buddha.agent.Agent;
import com.buddha.agent.Ball;
import com.buddha.world.Circle;

public class Skeleton {
	
	public Array<Particle3> particles = new Array<Particle3>();
	public Array<Constraint3> constraints = new Array<Constraint3>();
	public float gravity = 0.02f;
	
	public Particle3 lfoot;
	public Vector3 lfootPos = new Vector3();
	public Particle3 rfoot;
	public Vector3 rfootPos = new Vector3();
	public Particle3 head;
	float unit = 1f;
	float feetLerp = 1f;
	
	public Skeleton(float x, float y) {
		
		//head
		addParticle(x, y, 2.5f*unit);
		addParticle(x, y, 2f*unit);
		//left arm
		addParticle(x-unit, y, 2f*unit);
		addParticle(x-unit*2, y, 2f*unit);
		//right arm
		addParticle(x+unit, y, 2f*unit);
		addParticle(x+unit*2, y, 2f*unit);
		//middle
		addParticle(x, y, 1*unit);
		//bottom
		addParticle(x, y, 0);
		//left leg
		addParticle(x-unit, y, 0);
		addParticle(x-2f*unit, y, 0);
		//right leg
		addParticle(x+unit, y, 0);
		addParticle(x+2f*unit, y, 0);
		
		//connect
		connect(0, 1);
		connect(1, 2);
		connect(2, 3);
		connect(1, 4);
		connect(4, 5);
		connect(1, 6);
		connect(6, 7);
		connect(7, 8);
		connect(8, 9);
		connect(7, 10);
		connect(10, 11);
		head = particles.get(0);
		lfoot = particles.get(9);
		rfoot = particles.get(11);
	}
	
	public void update() {
		for(Particle3 p : particles) {
			p.update();
			p.addImpulse(0, 0, -gravity);
			if(p.pos.z < 0)
				p.pos.z = 0;
		}
		moveFoot(lfoot.pos, lfootPos);
		moveFoot(rfoot.pos, rfootPos);
		//selfCollision();
		for(Constraint3 c : constraints) {
			c.solve();
		}
		for(Constraint3 c : constraints) {
			c.solve();
		}
	}
	
	public void move() {
		selfCollision();
		head.addImpulse(0, 0, gravity*24);
	}
	
	public void selfCollision() {
		for(int i = 0; i < particles.size-1; i++) {
			Particle3 p1 = particles.get(i);
			for(int j = i+1; j < particles.size; j++) {
				Particle3 p2 = particles.get(j);
				if(p1.pos.dst2(p2.pos) < unit*unit) {
					Constraint3.solveFluid(p1, p2, unit*2, 0.3f);
				}
			}
		}
	}
	
	public void setPosition(Agent agent) {
		Circle c = agent.circle;
		float r = c.radius*0.8f+agent.circle.particle.getSpeed()/(4f*agent.speed);
		float frontX = c.getX()+MathUtils.cos(agent.direction)*r;
		float frontY = c.getY()+MathUtils.sin(agent.direction)*r;
		if(!(Vector2.dst2(c.getX(), c.getY(), lfootPos.x, lfootPos.y) < r*r)) {
			lfootPos.set(frontX, frontY, 0);
			rfootPos.set(c.getX(), c.getY(), 0);
		}
		if(!(Vector2.dst2(c.getX(), c.getY(), rfootPos.x, rfootPos.y) < r*r)) {
			rfootPos.set(frontX, frontY, 0);
			lfootPos.set(c.getX(), c.getY(), 0);
		}
	}
	
	public Vector3 getAvgPos() {
		Vector3 avgPos = new Vector3();
		for(Particle3 p : particles) {
			avgPos.add(p.pos);
		}
		avgPos.scl(1f/particles.size);
		return avgPos;
	}
	
	public void moveFoot(Vector3 from, Vector3 to) {
		float nx = from.x*(1-feetLerp)+to.x*feetLerp;
		float ny = from.y*(1-feetLerp)+to.y*feetLerp;
		float nz = from.z*(1-feetLerp)+to.z*feetLerp;
		from.set(nx, ny, nz);
	}
	
	public void addParticle(float x, float y, float z) {
		addParticle(new Particle3(x, y, z, 1f));
	}
	
	public void connect(int idx1, int idx2) {
		Particle3 p1 = particles.get(idx1);
		Particle3 p2 = particles.get(idx2);
		Constraint3 c = new Constraint3(p1, p2);
		addConstraint(c);
	}
	
	public void addParticle(Particle3 p) {
		particles.add(p);
	}
	
	public void addConstraint(Constraint3 c) {
		constraints.add(c);
	}

	public Particle3 get(int idx) {
		return particles.get(idx);
	}
	
	public Constraint3 getC(int idx) {
		return constraints.get(idx);
	}

	public void hitBall(Ball ball) {
		Vector3 foot = MathUtils.randomBoolean() ? lfootPos : rfootPos;
		foot.set(ball.circle.getX(), ball.circle.getY(), 0);
	}
}
