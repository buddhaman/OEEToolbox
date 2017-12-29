package com.buddha.cellSystems;

import com.buddha.world.Cell;

public class JumpBack extends CellSystem {

	public JumpBack(Cell cell) {
		super(cell);
		cell.jumpBack++;
		cell.genome.next();
		cell.readGenome();
		remove();
	}

	@Override
	public void update() {
		
	}

}
