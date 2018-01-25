package com.buddha.agent;

import com.badlogic.gdx.math.Vector2;
import com.buddha.simulation.Simulation;

public class FieldEdgeInput extends InputElement {
	
	public boolean distance;
	public boolean angle;
	public float cutoff;
	
	public FieldEdgeInput(boolean distance, boolean angle, float cutoff) {
		this.distance = distance;
		this.angle = angle;
		this.cutoff = cutoff;
		if(distance) size++;
		if(angle) size++;
		input = new float[size];
	}
	
	@Override
	public void update(Simulation sim, Agent agent) {
		Vector2 toEdge = sim.world.bounds.getClosestEdgePoint(agent.circle.particle.pos);
		float ang = Agent.calcAngle(agent, toEdge);
		float dst = Agent.calcDist(agent, toEdge, cutoff);
		int idx = 0;
		if(distance) input[idx++] = dst;
		if(angle) input[idx++] = ang;
	}
}
