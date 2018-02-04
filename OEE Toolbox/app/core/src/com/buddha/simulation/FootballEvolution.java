package com.buddha.simulation;

import java.util.Arrays;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.buddha.agent.BallCollisionHandler;
import com.buddha.agent.InputModel;
import com.buddha.agent.Team;
import com.buddha.neural.FFNN;

public class FootballEvolution {
	
	public int popSize;
	public int selectionSize;
	public int generation;
	public Array<Team> population;
	public Array<Selection> selections = new Array<Selection>();
	public float crossoverProb = 0.7f;
	public float mutationProb = 0.5f;
	public int selectionIdx;
	public int numTournaments;
	public Array<float[]> solutions = new Array<float[]>();
	public Array<float[]> adaptations = new Array<float[]>();
	public float learningRate;
	
	public BallCollisionHandler ballCollisionHandler;
	public InputModel inputModel;
	public int geneSize;
	public int numGenes;
	
	public FootballEvolution(InputModel inputModel, int selectionSize, int tournaments) {
		this.numTournaments = tournaments;
		this.popSize = selectionSize*tournaments;
		this.selectionSize = selectionSize;
		ballCollisionHandler = Properties.current.getBProperty("handle ball") ? new BallCollisionHandler(3) : null;
		this.inputModel = inputModel;
		numGenes = Properties.current.getIProperty("genes per team");
		int outputs = ballCollisionHandler==null ? 3 : 3+ballCollisionHandler.getSize();
		geneSize = FFNN.calcSize(inputModel, outputs);
		float adaptationScl = Properties.current.getFProperty("adaptation vector")*(1.0f/geneSize);
		for(int i = 0; i < numGenes; i++) {
			float[] solution = new float[geneSize];
			float[] adaptation = new float[geneSize];
			for(int j = 0; j < geneSize; j++) {
				solution[j] = MathUtils.random(0, 0);
				adaptation[j] = adaptationScl;
			}
			solutions.add(solution);
			adaptations.add(adaptation);
		}
		learningRate = 0.025f;
		nextGeneration();
	}
	
	public void makeTournaments() {
		selectionIdx = 0;
		selections.clear();
		//form pairs for simulationscreen to evaluate
		Array<Tournament> tournaments = new Array<Tournament>(popSize/2);
		for(int i = 0; i < popSize/2; i++) {
			tournaments.add(new Tournament(population.get(i*2), population.get(i*2+1)));
		}
		for(int i = 0; i < popSize/selectionSize; i++) {
			Selection s = new Selection();
			for(int j = 0; j < selectionSize/2; j++) {
				s.tournaments.add(tournaments.pop());
			}
			selections.add(s);
		}
	}
	
	public void nextGeneration() {
		boolean adaptationEnabled = Properties.current.getBProperty("adaptation");
		float adaptationVec = Properties.current.getFProperty("adaptation vector");
		if(population!=null) {
			Array<float[]> epsilonSums = new Array<float[]>();
			Array<float[]> epsilonSquares = new Array<float[]>();
			for(int i = 0; i < numGenes; i++) {
				epsilonSums.add(new float[geneSize]);
				epsilonSquares.add(new float[geneSize]);
			}
			for(Selection sel : selections) {
				Array<float[]> epsilons = sel.getWinner().epsilons;
				for(int i = 0; i < numGenes; i++) {
					float[] eps = epsilons.get(i);
					FFNN.add(epsilonSums.get(i), eps);
					float[] eps2sum = epsilonSquares.get(i);
					
					for(int j = 0; j < geneSize; j++) {
						eps2sum[j] = eps2sum[j]+eps[j]*eps[j];
					}
				}
			}
			for(int i = 0; i < numGenes; i++) {
				float[] eps = epsilonSums.get(i);
				float[] eps2 = epsilonSquares.get(i);
				FFNN.scale(eps, 1f/((float)selections.size));
				FFNN.scale(eps2, 1f/((float)selections.size));
			}
			for(int i = 0; i < numGenes; i++) {
				FFNN.add(solutions.get(i), epsilonSums.get(i));
				//update adaptation vector
				float[] adaptation = adaptations.get(i);
				float[] epsilons2 = epsilonSquares.get(i);
				System.out.println("avg squared epsilons : " + FFNN.getMagnitude(epsilons2));
				FFNN.scale(epsilons2, learningRate);
				FFNN.scale(adaptation, (1-learningRate));
				FFNN.add(adaptation, epsilons2);
				System.out.println("solution vector magnitude : " + FFNN.getMagnitude(solutions.get(i)));
				System.out.println("adaptation vector magnitude : " + FFNN.getMagnitude(adaptation));
			}
		}
		Array<Team> nextPop = new Array<Team>();
		for(int i = 0; i < popSize; i++) {
			Array<float[]> epsilons = new Array<float[]>();
			Array<float[]> genes = new Array<float[]>();
			for(int j = 0; j < numGenes; j++) {
				float[] solution = solutions.get(j);
				float[] adaptation = adaptations.get(j);
				float[] epsilon = new float[geneSize];
				for(int k = 0; k < geneSize; k++) {
					epsilon[k] = adaptationEnabled ? (float)(MathUtils.random.nextGaussian()*Math.sqrt(adaptation[k])) :
						(float)(MathUtils.random.nextGaussian()*Math.sqrt(adaptationVec/geneSize));
				}
				float[] gene = Arrays.copyOf(solution, geneSize);
				FFNN.add(gene, epsilon);
				epsilons.add(epsilon);
				genes.add(gene);
			}
			Team newTeam = new Team(inputModel, ballCollisionHandler, genes, epsilons);
			nextPop.add(newTeam);
		}
		this.population = nextPop;
		makeTournaments();
		generation++;
	}

	public Tournament next() {
		Tournament next = selections.get(selectionIdx).next();
		if(next == null) {
			if(selectionIdx==selections.size-1) {
				return null;
			}
			next = selections.get(++selectionIdx).next();
		}
		return next;
	}
	
	public class Selection {
		public Array<Tournament> tournaments = new Array<Tournament>();
		public int atIdx;
		
		public Selection() {
			atIdx = 0;
		}
		
		public Team getWinner() {
			return tournaments.get(0).getWinner();
		}
		
		public Tournament next() {
			if(atIdx>=tournaments.size) {
				if(tournaments.size==1) {
					return null;
				} 
				Array<Tournament> nTournaments = new Array<Tournament>(tournaments.size/2);
				for(int i = 0; i < tournaments.size/2; i++) {
					Team w1 = new Team(tournaments.get(i*2).getWinner());
					Team w2 = new Team(tournaments.get(i*2+1).getWinner());
					nTournaments.add(new Tournament(w1, w2));
				}
				atIdx = 0;
				tournaments = nTournaments;
			}
			return tournaments.get(atIdx++);
		}
	}
	
	public class Tournament {
		public Team a;
		public Team b;
		
		public Tournament(Team a, Team b) {
			this.a = a;
			this.b = b;
		}
		
		public Team getWinner() {
			if(a.score==b.score) {
				int numActiveA = a.numActivePlayers();
				int numActiveB = b.numActivePlayers();
				if(numActiveA==numActiveB) {
					if(a.hasBall > b.hasBall) {
						return a;
					} else {
						return b;
					}
				} else if(a.numActivePlayers() > b.numActivePlayers()) {
					return a;
				} else {
					return b;
				}
			} else {
				if(a.score > b.score) {
					return a;
				} else {
					return b;
				}
			}
		}
	}
}
