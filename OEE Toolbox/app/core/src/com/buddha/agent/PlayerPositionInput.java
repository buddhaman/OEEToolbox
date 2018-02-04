package com.buddha.agent;

import java.util.Arrays;

import com.badlogic.gdx.math.Vector2;
import com.buddha.simulation.Simulation;

public class PlayerPositionInput extends InputElement {
	
	public int playerIdx;
	public boolean ownTeam;
	public boolean fixedIdx;
	public boolean distance;
	public boolean angle;
	public boolean direction;
	public float cutoff;
	
	public PlayerPositionInput(int playerIdx, boolean ownTeam, boolean fixedIdx, boolean distance, boolean angle, boolean direction, float cutoff) {
		this.playerIdx = playerIdx;
		this.ownTeam = ownTeam;
		this.distance = distance;
		this.angle = angle;
		this.cutoff = cutoff;
		this.direction = direction;
		this.fixedIdx = fixedIdx;
		if(distance) size++;
		if(angle) size++;
		if(direction) size++;
		input = new float[size];
	}
	
	@Override
	public void update(Simulation sim, Agent agent) {
		Team team = ownTeam ? agent.team : sim.teams.get(1-sim.teams.indexOf(agent.team, true));
		Agent target;
		if(fixedIdx) {
			int ownIdx = agent.team.players.indexOf(agent, true);
			target = team.players.get((ownIdx+1+playerIdx)%agent.team.players.size);
		} else {
			target = team.getClosest(agent.circle.particle.pos, ownTeam, playerIdx);
		}
		Vector2 nth = target.circle.particle.pos;
		int idx = 0;
		float ang = Agent.calcAngle(agent, nth);
		boolean inFov = Math.abs(ang) < agent.fov/2f;
		float angDif = target.direction-agent.direction;
		if(distance) {
			input[idx++]= inFov ? Agent.calcDist(agent, nth, cutoff) : 0;
		} 
		if(angle) {
			input[idx++]= inFov ? Agent.calcAngle(agent, nth) : 0;
		}
		if(direction) {
			input[idx++] = inFov ? Agent.angDiff(angDif) : 0;
		}
	}
	
	@Override
	public String toString() {
		return "dit is PlayerPosition " + Arrays.toString(input);
	}
}
