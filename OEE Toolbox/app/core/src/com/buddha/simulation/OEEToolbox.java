package com.buddha.simulation;

import java.util.Random;

import com.badlogic.gdx.Game;
import com.buddha.world.Genome;

public class OEEToolbox extends Game {
	
	public static final Random rand = new Random();
	
	@Override
	public void create () {
		Genome.initConstructors();
		setScreen(new SimulationScreen());
	}

	@Override
	public void render () {
		super.render();
	}
	
	@Override
	public void dispose () {
		super.dispose();
	}
}
