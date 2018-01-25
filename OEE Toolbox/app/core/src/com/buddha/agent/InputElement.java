package com.buddha.agent;

import com.buddha.simulation.Simulation;

public class InputElement {
	
	public int size;
	protected float[] input;
	public int startIdx;
	
	public InputElement() {
		
	}
	
	public void update(Simulation sim, Agent agent) {
		
	}
	
	public int teamIdx(Agent a, Simulation sim) {
		return sim.teams.indexOf(a.team, true);
	}
}
