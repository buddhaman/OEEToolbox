package com.buddha.cellSystems;

import java.util.Random;

import com.buddha.simulation.OEEToolbox;
import com.buddha.world.Cell;
import com.buddha.world.Circle;
import com.buddha.world.Constraint;
import com.buddha.world.Genome;
import com.buddha.world.Genome.Gene;
import com.buddha.world.World;

public class AppendCell extends CellSystem {

	public Cell newCell;
	public float targetR;
	
	public AppendCell(Cell cell) {
		super(cell);
	}

	@Override
	public void update() {
		Random rand = OEEToolbox.rand;
		if(newCell==null) {
			float r1 = 1;
			float r2 = 1;
			Cell p1 = this.cell;
			Cell p2 = cell.getPrevious(1);
			if(cell.jumpBack!=0) {
				p1 = cell.getPrevious(cell.jumpBack);
				p2 = cell.getPrevious(cell.jumpBack+2);
				int c1num = p1 ==null ? 0 : p1.getNumOfType(OrganicConstraint.class);
				int c2num = p2 == null ? 0 : p2.getNumOfType(OrganicConstraint.class);
				if(p1==null || p2==null || c1num>=5 || c2num>=5) {
					p1 = this.cell;
					p2 = cell.getPrevious(1);
				}
			}
			if(p2==null) {
				constructCell(rand, cell.world, p1.getX()+rand.nextFloat()-0.5f, p1.getY()+rand.nextFloat()-0.5f);
				addConstraint(p1, newCell, r1+1);
			} else {
				float dst = p1.circle.particle.pos.dst(p2.circle.particle.pos);
				constructCell(rand, cell.world, (p1.getX()+p2.getX())/2f, (p1.getY()+p2.getY())/2f);
				addConstraint(p1, newCell, r1+dst/2f);
				newCell.parent=p1;
				addConstraint(p2, newCell, r2+dst/2f);
			}
		}
		if(newCell.circle.radius < targetR) {
			newCell.circle.radius+=0.01f;
		} else {
			newCell.genome.next();
			newCell.readGenome();
			remove();
		}
	}
	
	public void addConstraint(Cell p1, Cell p2, float targetR) {
		Constraint constraint = new Constraint(p1.circle.particle, p2.circle.particle);
		OrganicConstraint c1 = new OrganicConstraint(p1, p2, constraint, targetR);
		OrganicConstraint c2 = new OrganicConstraint(p2, p1, constraint, targetR);
		p1.addSystem(OrganicConstraint.class, c1);
		p2.addSystem(OrganicConstraint.class, c2);
		p1.world.addConstraint(constraint);
	}

	public void constructCell(Random rand, World world, float x, float y) {
		Gene gene = cell.genome.getGene();
		if(cell.circle!=null) {
			newCell = new Cell(x, y, world, cell.genome, cell);
			world.addCell(newCell);
			newCell.parent = cell;
		} else {
			newCell = cell;
		}
		targetR = (gene.information[1]%500)/500f+1;
		Circle body = new Circle(x, y, 0.01f, 1);
		newCell.circle = body;
	}
}
