package com.buddha.world;

import java.util.HashMap;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.buddha.cellSystems.AppendCell;
import com.buddha.cellSystems.CellSystem;
import com.buddha.cellSystems.OrganicConstraint;
import com.buddha.simulation.OEEToolbox;
import com.buddha.world.Genome.Gene;

public class Cell {
	public World world;
	public Circle circle;
	public Genome genome;
	public int energy;
	public Cell parent;
	
	//info on building next cell
	public int jumpBack = 0;
	
	public HashMap<Class<? extends CellSystem>, Array<CellSystem>> systemMap = 
			new HashMap<Class<? extends CellSystem>, Array<CellSystem>>();
	public Array<CellSystem> allSystems = new Array<CellSystem>();
	
	public Cell(float x, float y, World world, Genome genome, Cell parent) {
		this.world = world;
		this.genome = genome;
		this.parent = parent;
		if(parent==null) {
			((AppendCell)readGenome()).constructCell(OEEToolbox.rand, world, x, y);
		}
	}
	
	public void addSystem(Class<? extends CellSystem> type, CellSystem s) {
		Array<CellSystem> systemsOfType = systemMap.get(type);
		allSystems.add(s);
		if(systemsOfType==null) {
			Array<CellSystem> sysArray = new Array<CellSystem>();
			sysArray.add(s);
			systemMap.put(type, sysArray);
		} else {
			systemsOfType.add(s);
		}
	}
	
	public void setPos(float x, float y) {
		circle.particle.pos.set(x, y);
	}
	
	public void update() {
		for(int i = allSystems.size-1; i >= 0; i--) {
			CellSystem s = allSystems.get(i);
			if(s.remove) {
				removeSystem(s);
				continue;
			}
			s.update();
		}
		circle.update();
	}
	
	public void removeSystem(CellSystem s) {
		allSystems.removeValue(s, true);
		Array<CellSystem> array = systemMap.get(s.getClass());
		array.removeValue(s, true);
	}
	
	@SuppressWarnings("unchecked")
	public <T extends CellSystem> Array<T> getSystemsOfType(Class<T> type) {
		return (Array<T>) systemMap.get(type);
	}
	
	public Cell getPrevious(int previous) {
		if(previous==0) {
			return this;
		} else {
			if(parent==null) {
				return null;
			}
			return parent.getPrevious(previous-1);
		}
	}
	
	public CellSystem readGenome() {
		Gene gene = genome.getGene();
		if(gene==null)
			return null;
		int type = gene.information[0];
		CellSystem system = Genome.getConstructor(type).apply(this);
		addSystem(Genome.getType(type), system);
		return system;
	}
	
	public float getX() {
		return circle.particle.pos.x;
	}
	
	public float getY() {
		return circle.particle.pos.y;
	}
	
	public float getRadius() {
		return circle.radius;
	}

	public Vector2 getPosition() {
		return circle.particle.pos;
	}

	public int getNumOfType(Class<? extends CellSystem> type) {
		Array<? extends CellSystem> typeArray = getSystemsOfType(type);
		if(typeArray==null)
			return 0;
		else
			return typeArray.size;
	}
}
