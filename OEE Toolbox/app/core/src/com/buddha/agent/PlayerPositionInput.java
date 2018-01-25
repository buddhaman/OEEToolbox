package com.buddha.agent;

import java.util.Arrays;

import com.badlogic.gdx.math.Vector2;
import com.buddha.simulation.Simulation;

public class PlayerPositionInput extends InputElement {
	
	public int nthDistance;
	public boolean ownTeam;
	public boolean distance;
	public boolean angle;
	public float cutoff;
	
	public PlayerPositionInput(int nthDistance, boolean ownTeam, boolean distance, boolean angle, float cutoff) {
		this.nthDistance = nthDistance;
		this.ownTeam = ownTeam;
		this.distance = distance;
		this.angle = angle;
		this.cutoff = cutoff;
		if(distance) size++;
		if(angle) size++;
		input = new float[size];
	}
	
	@Override
	public void update(Simulation sim, Agent agent) {
		Team team = ownTeam ? agent.team : sim.teams.get(1-sim.teams.indexOf(agent.team, true));
		Vector2 nth = team.getClosest(agent.circle.particle.pos, ownTeam, nthDistance).circle.particle.pos;
		int idx = 0;
		float ang = Agent.calcAngle(agent, nth);
		boolean inFov = Math.abs(ang) < agent.fov/2f;
		if(distance) {
			input[idx++]= inFov ? Agent.calcDist(agent, nth, cutoff) : 0;
		} 
		if(angle) {
			input[idx++]= inFov ? Agent.calcAngle(agent, nth) : 0;
		}
	}
	
	@Override
	public String toString() {
		return "dit is PlayerPosition " + Arrays.toString(input);
	}
}
