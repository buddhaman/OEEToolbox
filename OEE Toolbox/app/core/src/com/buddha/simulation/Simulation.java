package com.buddha.simulation;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.buddha.agent.Agent;
import com.buddha.agent.Ball;
import com.buddha.agent.InputModel;
import com.buddha.agent.Team;
import com.buddha.world.AABB;
import com.buddha.world.Circle;
import com.buddha.world.World;

public class Simulation {
	
	public World world;
	public Array<Agent> agents = new Array<Agent>();
	public Array<Ball> balls = new Array<Ball>();
	public Array<AABB> goals = new Array<AABB>();
	public Array<Team> teams = new Array<Team>();
	public Team hasBall = null;
	public boolean hardcore = Properties.current.getBProperty("hardcore");
	public int ticks;
	
	public Simulation(Team team1, Team team2, int duration) {
		this.ticks = duration;
		world = new World();
		world.setBounds(0, 0, 110, 70);
		addBall(new Ball(world.bounds.getCX(),world.bounds.getCY()));
		
		//place goals
		AABB bounds = world.bounds;
		float goalSize = 20;
		goals.add(new AABB(bounds.x1, bounds.getCY()-goalSize/2f, bounds.x1+5, bounds.getCY()+goalSize/2f));
		goals.add(new AABB(bounds.x2-5, bounds.getCY()-goalSize/2f, bounds.x2, bounds.getCY()+goalSize/2f));
		addTeam(team1);
		addTeam(team2);
		startPosition();
	}
	
	public void startPosition() {
		for(int i = 0; i < 2; i++) {
			teams.get(i).position(i*2-1, world.bounds, !hardcore);
		}
		balls.get(0).circle.particle.pos.set(world.bounds.getCX(), world.bounds.getCY());
		balls.get(0).circle.particle.resetVel();
	}
	
	public void update() {
		Ball ball = balls.get(0);
		for(int i = 0; i < teams.size; i++) {
			for(Agent agent : teams.get(i).players) {
				//check overlap with ball
				if(hasBall!=teams.get(i) && Circle.overlaps(agent.circle, ball.circle)) {
					hasBall = teams.get(i);
				}
			}
		}
		for(int i = agents.size-1; i >= 0; i--) {
			Agent agent = agents.get(i);
			agent.update();
			float[] input = agent.team.inputModel.update(this, agent);
			agent.setInput(input);
		}
		//update ball
		if(hasBall!=null)
			hasBall.hasBall++;
		world.update();
		checkGoal();
		checkCollisions();
	}
	
	public void checkCollisions() {
		//check ball-player
		for(Ball ball : balls) {
			for(Agent agent : agents) {
				if(Circle.overlaps(ball.circle, agent.circle)) {
					World.solveCollision(ball.circle, agent.circle);
					agent.hitBall(ball);
				}
			}
		}
		for(int i = 0; i < agents.size-1; i++) {
			Agent a1 = agents.get(i);
			for(int j = i+1; j < agents.size; j++) {
				Agent a2 = agents.get(j);
				if(Circle.overlaps(a1.circle, a2.circle)) {
					World.solveCollision(a1.circle, a2.circle);
					Agent.collision(a1, a2, hardcore);
				}
			}
		}
	}
	
	public void checkGoal() {
		for(Ball b : balls) {
			for(int i = 0; i < 2; i++) {
				if(goals.get(i).contains(b.circle.getX(), b.circle.getY())) {
					teamScores(1-i);
				}
			}
		}
	}
	
	public void addTeam(Team team) {
		teams.add(team);
		for(Agent a : team.players) {
			addAgent(a);
		}
	}
	
	public void removeTeams() {
		teams.clear();
	}
	
	public void teamScores(int team) {
		teams.get(team).score();
		startPosition();
	}
	
	public void addAgent(Agent agent) {
		world.addCircle(agent.circle);
		agents.add(agent);
	}
	
	public void addBall(Ball ball) {
		world.addCircle(ball.circle);
		balls.add(ball);
	}

	public Agent select(Vector2 pos) {
		Agent agent = null;
		for(Agent a : agents) {
			if(a.circle.contains(pos)) {
				agent = a;
			}
		}
		return agent;
	}
}
