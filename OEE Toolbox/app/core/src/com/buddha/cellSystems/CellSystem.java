package com.buddha.cellSystems;

import java.util.Random;

import com.buddha.simulation.OEEToolbox;
import com.buddha.world.Cell;
import com.buddha.world.Genome;
import com.buddha.world.Genome.Gene;

public abstract class CellSystem {
	public Cell cell;
	
	public boolean remove = false;
	
	public CellSystem(Cell cell) {
		this.cell = cell;
	}
	
	public void remove() {
		remove = true;
	}
	
	public abstract void update();
}
