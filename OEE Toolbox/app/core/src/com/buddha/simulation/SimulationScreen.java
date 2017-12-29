package com.buddha.simulation;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.Vector2;
import com.buddha.render.SimulationRenderer;
import com.buddha.world.Genome;

public class SimulationScreen implements Screen {

	public SpriteBatch batch;
	public TextureAtlas atlas;
	public Simulation simulation;
	public SimulationRenderer renderer;
	
	public SimulationScreen() {
		batch = new SpriteBatch();
		atlas = new TextureAtlas("spritesheet.txt");
		simulation = new Simulation();
		renderer = new SimulationRenderer(atlas, batch);
	}
	
	@Override
	public void show() {
		
	}

	@Override
	public void render(float delta) {
		Gdx.gl.glClearColor(.5f, .5f, .5f, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		simulation.update();
		renderer.render(simulation);
		Vector2 mousePos = renderer.cam.screenToWorld(Gdx.input.getX(), Gdx.input.getY());
		if(Gdx.input.isTouched()) {
			simulation.world.grab(mousePos);
		}
		if(Gdx.input.isKeyJustPressed(Keys.E)) {
			simulation.world.addOrganism(mousePos.x, mousePos.y, new Genome());
		}
	}

	@Override
	public void resize(int width, int height) {
		renderer.resize(width, height);
	}

	@Override
	public void pause() {
		
	}

	@Override
	public void resume() {
		
	}

	@Override
	public void hide() {
		
	}

	@Override
	public void dispose() {
		
	}

}
