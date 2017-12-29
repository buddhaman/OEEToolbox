package com.buddha.cellSystems;

import com.buddha.world.Cell;
import com.buddha.world.Constraint;
import com.buddha.world.Genome;
import com.buddha.world.Genome.Gene;

public class OrganicConstraint extends CellSystem {

	public Constraint constraint;
	public float targetR;
	public Cell p1;
	public Cell p2;
	
	public OrganicConstraint(Cell p1, Cell p2, Constraint constraint, float targetR) {
		super(p1);
		this.p1 = p1;
		this.p2 = p2;
		this.constraint = constraint;
		this.targetR = targetR;
	}
	
	@Override
	public void update() {
		if(constraint.r < targetR) {
			float grow = 0.01f;
			constraint.r+=grow;
		}
	}
}
