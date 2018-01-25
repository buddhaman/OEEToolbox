package com.buddha.simulation;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.buddha.agent.InputModel;
import com.buddha.agent.Team;

public class FootballEvolution {
	
	public int popSize;
	public int selectionSize;
	public int generation;
	public Array<Team> population = new Array<Team>();
	public Array<Selection> selections = new Array<Selection>();
	public float crossoverProb = 0.7f;
	public float mutationProb = 0.5f;
	public int selectionIdx;
	public int numTournaments;
	
	public FootballEvolution(InputModel inputModel, int selectionSize, int tournaments) {
		this.numTournaments = tournaments;
		this.popSize = selectionSize*tournaments;
		this.selectionSize = selectionSize;
		for(int i = 0; i < popSize; i++) {
			population.add(new Team(inputModel));
		}
		makeTournaments();
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
		//recombine and mutate
		Array<Team> nextPop = new Array<Team>();
		for(int i = 0; i < popSize; i++) {
			Team m1 = selections.random().getWinner();
			Team newTeam = null;
			boolean mutate = MathUtils.randomBoolean(mutationProb);
			if(MathUtils.randomBoolean(crossoverProb)) {
				Team m2 = selections.random().getWinner();
				newTeam = new Team(m1, m2, mutate);
			} else {
				newTeam = new Team(m1, mutate);
			}
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
					Team w1 = new Team(tournaments.get(i*2).getWinner(), false);
					Team w2 = new Team(tournaments.get(i*2+1).getWinner(), false);
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
				if(a.hasBall > b.hasBall) {
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
