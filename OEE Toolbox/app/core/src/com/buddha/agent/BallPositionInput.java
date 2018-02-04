package com.buddha.agent;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.buddha.simulation.Simulation;

public class BallPositionInput extends InputElement {
	
	public boolean distance;
	public boolean angle;
	public boolean direction;
	public float cutoff;
	
	public BallPositionInput(boolean distance, boolean angle, boolean direction, float cutoff) {
		this.distance = distance;
		this.angle = angle;
		this.cutoff = cutoff;
		if(distance) size++;
		if(angle) size++;
		if(direction) size++;
		input = new float[size];
	}
	
	@Override
	public void update(Simulation sim, Agent agent) {
		Ball ball = sim.balls.get(0);
		Vector2 ballPos = ball.circle.particle.pos;
		float ang = Agent.calcAngle(agent, ballPos);
		float dst = Agent.calcDist(agent, ballPos, cutoff);
		float ballDir = MathUtils.atan2(ball.circle.particle.getXVel(), ball.circle.particle.getYVel());
		float dir = Agent.angDiff(ballDir-agent.direction);
		int idx = 0;
		if(distance) {
			input[idx++] = dst;
		}
		if(angle) {
			input[idx++] = ang;
		}
		if(direction) {
			input[idx++] = dir;
		}
	}
}
