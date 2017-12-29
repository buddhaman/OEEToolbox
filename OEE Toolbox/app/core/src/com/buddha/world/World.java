package com.buddha.world;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

public class World {
	
	public Array<Cell> cells = new Array<Cell>();
	public Array<Constraint> constraints = new Array<Constraint>();
	
	public World() {
		addOrganism(0,0, new Genome());
	}
	
	public void update() {
		for(Cell c : cells) {
			c.update();
		}
		constraints.shuffle();
		for(Constraint c : constraints) {
			c.solve();
		}
		//collision detection
		for(int i = 0; i < cells.size; i++) {
			for(int j = i+1; j < cells.size; j++) {
				Cell c1 = cells.get(i);
				Cell c2 = cells.get(j);
				float dst = c1.getPosition().dst(c2.getPosition());
				float len = c1.getRadius()+c2.getRadius();
				if(dst < len) {
					Constraint.solveFluid(c1.circle.particle, c2.circle.particle, len, 0.14f);
				}
			}
		}
	}
	
	public void addOrganism(float x, float y, Genome genome) {
		cells.add(new Cell(x, y, this, genome, null));
	}
	
	public void addCell(Cell cell) {
		cells.add(cell);
	}
	
	public void addConstraint(Constraint constraint) {
		constraints.add(constraint);
	}

	public void grab(Vector2 screenToWorld) {
		cells.sort((c1, c2) -> c1.circle.particle.pos.dst(screenToWorld) < c2.circle.particle.pos.dst(screenToWorld) ? -1 : 1);
		Cell c = cells.get(0);
		c.circle.particle.pos.set(screenToWorld);
	}
}
