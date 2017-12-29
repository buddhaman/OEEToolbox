package com.buddha.simulation;

import com.buddha.world.World;

public class Simulation {
	
	public World world;
	
	public Simulation() {
		world = new World();
	}
	
	public void update() {
		world.update();
	}
}
