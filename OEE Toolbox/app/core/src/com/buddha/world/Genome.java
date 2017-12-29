package com.buddha.world;

import java.util.Random;
import java.util.function.Function;

import com.badlogic.gdx.utils.Array;
import com.buddha.cellSystems.AppendCell;
import com.buddha.cellSystems.CellSystem;
import com.buddha.cellSystems.JumpBack;
import com.buddha.simulation.OEEToolbox;

public class Genome {
	
	public Array<Gene> genes = new Array<Gene>();
	public int idx;
	public int maxId = 0;
	public static final int[] typeSize = new int[]{3, 1};
	
	public Genome() {
		idx = 0;
		genes.add(new Gene(maxId++, 0));
		for(int i = 0; i < 100; i++) {
			genes.add(new Gene(maxId++));
		}
	}
	
	public class Gene {
		public static final int MAX_INT_SIZE = 5000;
		public int id;
		public int[] information;
		
		public Gene(int id) {
			this(id, OEEToolbox.rand.nextInt(typeSize.length));
		}
		
		public Gene(int id, int type) {
			Random rand = OEEToolbox.rand;
			this.id = id;
			information = new int[typeSize[type]+1];
			information[0] = type;
			for(int i = 1; i < information.length; i++) {
				information[i] = rand.nextInt(MAX_INT_SIZE);
			}
		}
	}
	
	public Gene getGene() {
		if(idx < genes.size) {
			return genes.get(idx);
		} else {
			return null;
		}
	}
	
	public void next() {
		idx++;
	}
	
	/**
	 * static stuff
	 */
	
	public static Array<Function<Cell, CellSystem>> constructors = new Array<Function<Cell, CellSystem>>();
	public static Array<Class<? extends CellSystem>> types = new Array<Class<? extends CellSystem>>();
	
	public static void addConstructor(Function<Cell, CellSystem> constructor) {
		constructors.add(constructor);
	}
	
	public static void initConstructors() {
		constructors.add(c -> new AppendCell(c));
		types.add(AppendCell.class);
		constructors.add(c -> new JumpBack(c));
		types.add(JumpBack.class);
	}
	
	public static Function<Cell, CellSystem> getConstructor(int idx) {
		return constructors.get(idx);
	}
	
	public static Class<? extends CellSystem> getType(int idx) {
		return types.get(idx);
	}
}
