package com.buddha.agent;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.buddha.simulation.Properties;
import com.buddha.world.AABB;

public class Team {
	
	public static final Color[] colors = new Color[]{new Color(0x55ec5dff), new Color(0xe4ec55ff), 
			new Color(0xf49aefff), new Color(0x9f9af4ff), Color.BLACK, Color.WHITE, Color.BROWN, Color.ORANGE};
	private static DstComparer DIST_COMPARER = new DstComparer();
	private static int id_counter = 0;
	public int id = id_counter++;
	
	private Array<float[]> genes;
	public Array<float[]> epsilons;
	public  Array<Agent> players = new Array<Agent>();
	private Array<Agent> playersTemp = new Array<Agent>();
	
	public int size = 11;
	public int hasBall;
	public int score;
	public Color color;
	public int numGenes;
	public InputModel inputModel;
	public BallCollisionHandler ballHandler;
	public int layerSize;
	public int layerNum;
	
	public Team(InputModel model, BallCollisionHandler ballHandler, Array<float[]> genes, Array<float[]> epsilons) {
		this.inputModel = model;
		this.ballHandler = ballHandler;
		this.genes = genes;
		this.epsilons = epsilons;
		this.layerSize = Properties.current.getIProperty("layer size");
		this.layerNum = Properties.current.getIProperty("hidden layers");
		this.numGenes = Properties.current.getIProperty("genes per team");
		init();
	}
	
	public void copyProperties(Team parent) {
		this.inputModel = parent.inputModel;
		this.ballHandler = parent.ballHandler;
		this.numGenes = parent.numGenes;
		this.layerSize = parent.layerSize;
		this.layerNum = parent.layerNum;
		this.genes = parent.genes;
		this.epsilons = parent.epsilons;
	}
	
	public Team(Team team) {
		copyProperties(team);
		init();
	}
	
	public void init() {
		for(int i = 0; i < size; i++) {
			int geneIdx = (int)(((float)(i*numGenes))/size);
			Agent agent = new Agent(0,0,genes.get(geneIdx), this, layerSize, layerNum);
			agent.setGeneIdx(geneIdx);
			players.add(agent);
			playersTemp.add(agent);
		}
	}
	
	public Agent getClosest(Vector2 v, boolean excludeV, int idx) {
		DIST_COMPARER.compareTo = v;
		Agent closest =  playersTemp.selectRanked(DIST_COMPARER, excludeV ? idx+2 : idx+1);
		return closest;
	}
	
	/**
	 * @param side -1 (left) or 1 (right)
	 */
	public void position(int side, AABB bounds, boolean heal) {
		float x1 = side==-1 ? bounds.x1 : bounds.getCX();
		for(int i = 0; i < size; i++) {
			players.get(i).setState(x1+MathUtils.random(bounds.getWidth()/2f), 
					bounds.y1+MathUtils.random(bounds.getHeight()), (side+1)/2*MathUtils.PI);
			players.get(i).inactive.reset();
		}
	}
	
	public int numActivePlayers() {
		int num = 0;
		for(Agent a: players) {
			if(a.hasHitBall) num++;
		}
		return num;
	}
	
	public void score() {
		score++;
	}
	
	public void printAdaptationLengths() {
		
	}

	public void knockout() {
		for(Agent a : players) {
			a.knockout();
		}
	}
	
}
