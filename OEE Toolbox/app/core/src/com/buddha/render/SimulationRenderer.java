package com.buddha.render;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.utils.Array;
import com.buddha.cellSystems.OrganicConstraint;
import com.buddha.simulation.Simulation;
import com.buddha.world.Cell;
import com.buddha.world.World;

public class SimulationRenderer {
	
	public RenderUtils utils;
	public SpriteBatch batch;
	public Camera cam;
	public AtlasRegion circle;
	public AtlasRegion square;
	
	public SimulationRenderer(TextureAtlas atlas, SpriteBatch batch) {
		this.batch = batch;
		circle = atlas.findRegion("circle");
		square = atlas.findRegion("blank");
		utils = new RenderUtils(batch, square);
		cam = new Camera();
	}
	
	public void render(Simulation sim) {
		World world = sim.world;
		cam.update();
		batch.setProjectionMatrix(cam.cam.combined);
		batch.begin();

		for(Cell c : world.cells) {
			batch.setColor(Color.RED);
			batch.draw(circle, c.getX()-c.getRadius(), c.getY()-c.getRadius(), c.getRadius()*2, c.getRadius()*2);
		}
		for(Cell c : world.cells) {
			Array<OrganicConstraint> organicConstraints = c.getSystemsOfType(OrganicConstraint.class);
			if(organicConstraints!=null)
			for(OrganicConstraint oc : organicConstraints) {
				batch.setColor(Color.BLACK);
				utils.drawLine(oc.p1.getX(), oc.p1.getY(), oc.p2.getX(), oc.p2.getY(), 0.1f);
			}
		}

		batch.end();
	}
	
	public void resize(int width, int height) {
		cam.resize(width, height);
	}
	
}
