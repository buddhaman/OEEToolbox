package com.buddha.agent;

import com.badlogic.gdx.math.MathUtils;

public class BallCollisionHandler {
	
	int outputIdx;
	
	public BallCollisionHandler(int outputIdx) {
		this.outputIdx = outputIdx;
	}
	
	public void handleCollision(Agent agent, Ball ball) {
		//check if in fov
		
		float angDiff = Agent.calcAngle(agent, ball.circle.particle.pos);
		if(Math.abs(angDiff) < MathUtils.PI/2f && agent.output[outputIdx+1]>0.25f) {
			float angle = getAngle(agent);
			float magnitude = getMagnitude(agent);
			float r = agent.circle.radius+ball.circle.radius+0.1f;
			float cos = MathUtils.cos(angle);
			float sin = MathUtils.sin(angle);
			float kickX = agent.circle.getX()+MathUtils.cos(agent.direction)*r;
			float kickY = agent.circle.getY()+MathUtils.sin(agent.direction)*r;
			ball.circle.particle.pos.set(kickX, kickY);
			ball.circle.particle.setVel(cos*magnitude+agent.circle.particle.getXVel(), 
					sin*magnitude+agent.circle.particle.getYVel());
			agent.kick(kickX, kickY, angle, magnitude);
		}
	}
	
	public int getSize() {
		return 2;
	}

	public float getAngle(Agent agent) {
		return agent.direction+(agent.output[outputIdx]-0.5f)*MathUtils.PI;
	}
	
	public float getMagnitude(Agent agent) {
		return agent.output[outputIdx+1]*0.9f;
	}
}
