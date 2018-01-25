package com.buddha.agent;

import java.util.Arrays;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.buddha.neural.FFNN;
import com.buddha.world.AABB;

public class Team {
	
	private static DstComparer DIST_COMPARER = new DstComparer();
	private static int id_counter = 0;
	public int id = id_counter++;
	
	private Array<float[]> genes = new Array<float[]>();
	private Array<float[]> adaptation = new Array<float[]>();
	public Array<Agent> players = new Array<Agent>();
	
	public int size = 11;
	public int hasBall;
	public int score;
	public Color color;
	public int numGenes = 1;
	public InputModel inputModel;
	
	public Team(InputModel model) {
		this.inputModel = model;
		int geneSize = FFNN.calcSize(model);
		for(int i = 0; i < numGenes; i++) {
			genes.add(FFNN.getRandomGene(geneSize, 1f));
			adaptation.add(FFNN.getRandomGene(geneSize, 15f/((float)Math.sqrt(geneSize))));
		}
		init();
	}
	
	public Team(Team parent1, Team parent2, boolean mutate) {
		this.inputModel = parent1.inputModel;
		for(int i = 0; i < parent1.numGenes; i++) {
			float[] result = Agent.crossover(parent1.genes.get(i), parent2.genes.get(i));
			float[] aResult = Agent.crossover(parent1.adaptation.get(i), parent2.adaptation.get(i));
			genes.add(result);
			adaptation.add(aResult);
		}
		if(mutate) {
			mutate();
		}
		init();
	}
	
	public Team(Team parent, boolean mutate) {
		this.inputModel = parent.inputModel;
		players.clear();
		for(int i = 0; i < parent.numGenes; i++) {
			float[] copyX = Arrays.copyOf(parent.genes.get(i), parent.genes.get(i).length);
			float[] copyA = Arrays.copyOf(parent.adaptation.get(i), parent.adaptation.get(i).length);
			genes.add(copyX);
			adaptation.add(copyA);
		}
		if(mutate) {
			mutate();
		}
		init();
	}
	
	public void init() {
		color = new Color(MathUtils.random(), MathUtils.random(), MathUtils.random(), 1f);
		for(int i = 0; i < size; i++) {
			int geneIdx = MathUtils.random(genes.size-1);
			Agent agent = new Agent(0,0,genes.get(geneIdx), this);
			agent.setColor(color);
			agent.setGeneIdx(geneIdx);
			players.add(agent);
		}
	}
	
	public Agent getClosest(Vector2 v, boolean excludeV, int idx) {
		DIST_COMPARER.compareTo = v;
		Agent closest =  players.selectRanked(DIST_COMPARER, excludeV ? idx+2 : idx+1);
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
	
	public void mutate() {
		for(int i = 0; i < numGenes; i++) {
			Agent.mutate(genes.get(i), adaptation.get(i));
		}
	}
	
	public void score() {
		score++;
	}
	
	public void printAdaptationLengths() {
		for(float[] adaptation : this.adaptation) {
			float length = 0;
			for(int i = 0; i < adaptation.length; i++) {
				length+=adaptation[i]*adaptation[i];
			}
			System.out.println(Math.sqrt(length));
		}
	}
	
}
