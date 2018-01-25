package com.buddha.agent;

import com.badlogic.gdx.math.Vector2;
import com.buddha.simulation.Simulation;

public class BallPositionInput extends InputElement {
	
	public boolean distance;
	public boolean angle;
	public float cutoff;
	
	public BallPositionInput(boolean distance, boolean angle, float cutoff) {
		this.distance = distance;
		this.angle = angle;
		this.cutoff = cutoff;
		if(distance) size++;
		if(angle) size++;
		input = new float[size];
	}
	
	@Override
	public void update(Simulation sim, Agent agent) {
		Vector2 ballPos = sim.balls.get(0).circle.particle.pos;
		float ang = Agent.calcAngle(agent, ballPos);
		float dst = Agent.calcDist(agent, ballPos, cutoff);
		int idx = 0;
		if(distance) {
			input[idx++] = dst;
		}
		if(angle && ang < agent.fov/2f) {
			input[idx++] = ang;
		}
	}
}
