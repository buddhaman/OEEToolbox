package com.buddha.simulation;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.Vector2;
import com.buddha.agent.Agent;
import com.buddha.agent.BallPositionInput;
import com.buddha.agent.FieldEdgeInput;
import com.buddha.agent.GoalPositionInput;
import com.buddha.agent.InputModel;
import com.buddha.agent.PlayerPositionInput;
import com.buddha.gui.GUI;
import com.buddha.render.SimulationRenderer;
import com.buddha.simulation.FootballEvolution.Tournament;

public class SimulationScreen implements Screen {

	public SpriteBatch batch;
	public TextureAtlas atlas;
	public Simulation simulation;
	public SimulationRenderer renderer;
	public FootballEvolution evolution;
	public InputModel inputModel;
	public GUI gui;
	
	public int gameDuration;			//1.5 minutes
	public int ticksPerStep = 1;
	public boolean clicked = false;
	
	public SimulationScreen() {
		gui = new GUI(this);
		batch = new SpriteBatch();
		atlas = new TextureAtlas("spritesheet.txt");
		renderer = new SimulationRenderer(atlas, batch);
		gui.setup();
		setupExperiment();
	}
	
	public void setupExperiment() {
		buildInputModel();
		int tournaments = Properties.current.getIProperty("tournaments");
		int selectionSize = (int)Math.pow(2, Properties.current.getIProperty("rounds"));
		evolution = new FootballEvolution(inputModel, selectionSize, tournaments);
		startNewGame();
	}
	
	public void buildInputModel() {
		inputModel = new InputModel();
		float cutoff = Properties.current.getFProperty("cutoff");
		boolean ballAngle = Properties.current.getBProperty("ball angle");
		boolean ballDistance = Properties.current.getBProperty("ball distance");
		boolean ballDirection = Properties.current.getBProperty("ball direction");
		boolean ownGoalAngle = Properties.current.getBProperty("own goal angle");
		boolean ownGoalDistance = Properties.current.getBProperty("own goal distance");
		boolean oppGoalAngle = Properties.current.getBProperty("opp goal angle");
		boolean oppGoalDistance = Properties.current.getBProperty("opp goal distance");
		boolean fieldEdgeAngle = Properties.current.getBProperty("field edge angle");
		boolean fieldEdgeDistance = Properties.current.getBProperty("field edge distance");
		
		//player inputs
		boolean teamAngle = Properties.current.getBProperty("team angle");
		boolean teamDistance = Properties.current.getBProperty("team distance");
		boolean teamDirection = Properties.current.getBProperty("team direction");
		boolean oppAngle = Properties.current.getBProperty("opp angle");
		boolean oppDistance = Properties.current.getBProperty("opp distance");
		boolean oppDirection = Properties.current.getBProperty("opp direction");
		int nearestTeamInputs = Properties.current.getIProperty("nearest team inputs");
		int nearestOppInputs = Properties.current.getIProperty("nearest opp inputs");
		int fixedTeamInputs = Properties.current.getIProperty("fixed team inputs");
		int fixedOppInputs = Properties.current.getIProperty("fixed opp inputs");
		
		for(int i = 0; i < nearestTeamInputs; i++) {
			PlayerPositionInput ppi = new PlayerPositionInput(i, true, false, teamDistance, teamAngle, teamDirection, cutoff);
			inputModel.addInputElement(PlayerPositionInput.class, ppi);
		}
		for(int i = 0; i < nearestOppInputs; i++) {
			PlayerPositionInput ppi = new PlayerPositionInput(i, false, false, oppDistance, oppAngle, oppDirection, cutoff);
			inputModel.addInputElement(PlayerPositionInput.class, ppi);
		}
		for(int i = 0; i < fixedTeamInputs; i++) {
			PlayerPositionInput ppi = new PlayerPositionInput(i, true, true, teamDistance, teamAngle, teamDirection, cutoff);
			inputModel.addInputElement(PlayerPositionInput.class, ppi);
		}
		for(int i = 0; i < fixedOppInputs; i++) {
			PlayerPositionInput ppi = new PlayerPositionInput(i, false, true, oppDistance, oppAngle, oppDirection, cutoff);
			inputModel.addInputElement(PlayerPositionInput.class, ppi);
		}
		
		BallPositionInput ballPosition = new BallPositionInput(ballDistance, ballAngle, ballDirection, cutoff);
		GoalPositionInput ownGoal = new GoalPositionInput(true, ownGoalDistance, ownGoalAngle, cutoff);
		GoalPositionInput oppGoal = new GoalPositionInput(false, oppGoalDistance, oppGoalAngle, cutoff);
		FieldEdgeInput edge = new FieldEdgeInput(fieldEdgeDistance, fieldEdgeAngle, cutoff/2f);
		inputModel.addInputElement(BallPositionInput.class, ballPosition);
		inputModel.addInputElement(GoalPositionInput.class, ownGoal);
		inputModel.addInputElement(GoalPositionInput.class, oppGoal);
		inputModel.addInputElement(FieldEdgeInput.class, edge);
	}
	
	public void startNewGame() {
		gameDuration = Properties.current.getIProperty("game duration")*60;	//is in seconds
		renderer.setSelected(null);
		Tournament tournament = evolution.next();
		if(tournament==null) {
			evolution.nextGeneration();
			tournament = evolution.next();
		}
		simulation = new Simulation(tournament.a, tournament.b, gameDuration);
	}
	
	@Override
	public void show() {
		
	}

	@Override
	public void render(float delta) {
		Gdx.gl.glClearColor(.5f, .5f, .5f, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		handleInput();
		for(int i = 0; i < ticksPerStep; i++) {
			simulation.update();
			//check if tournament is done
			if(simulation.ticks-- < 0) {
				startNewGame();
			}
		}
		renderer.render(simulation, this);
		gui.render();
	}
	
	public void handleInput() {
		int mx = Gdx.input.getX();
		int my = Gdx.input.getY();
		if(!clicked && Gdx.input.isTouched() && !gui.contains(mx, my)) {
			clicked=true;
			Vector2 worldPos = renderer.cam.screenToWorld(mx, my);
			Agent sel = simulation.select(worldPos);
			renderer.setSelected(sel);
			if(sel!=null) {
				gui.showBrainTable(sel);
				sel.team.printAdaptationLengths();
			}
		}
		clicked = Gdx.input.isTouched();
		if(Gdx.input.isKeyJustPressed(Keys.R)) {
			renderer.fancy=!renderer.fancy;
		}
		
		if(Gdx.input.isKeyPressed(Keys.Z)) {
			renderer.cam.scale*=0.99f;
		}
		if(Gdx.input.isKeyPressed(Keys.X)) {
			renderer.cam.scale/=0.99f;
		}
		float camSpeed = 0.3f+3*renderer.cam.scale;
		if(Gdx.input.isKeyPressed(Keys.W)) {
			renderer.cam.y+=camSpeed;
		}
		if(Gdx.input.isKeyPressed(Keys.A)) {
			renderer.cam.x-=camSpeed;
		}
		if(Gdx.input.isKeyPressed(Keys.S)) {
			renderer.cam.y-=camSpeed;
		}
		if(Gdx.input.isKeyPressed(Keys.D)) {
			renderer.cam.x+=camSpeed;
		}
	}

	@Override
	public void resize(int width, int height) {
		renderer.resize(width, height);
		gui.resize(width, height);
	}

	@Override
	public void pause() {
		
	}

	@Override
	public void resume() {
		
	}

	@Override
	public void hide() {
		
	}

	@Override
	public void dispose() {
		renderer.dispose();
		gui.dispose();
	}

}
