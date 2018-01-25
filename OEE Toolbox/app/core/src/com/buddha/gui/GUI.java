package com.buddha.gui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.ButtonGroup;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.HorizontalGroup;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.buddha.agent.Agent;
import com.buddha.simulation.Properties;
import com.buddha.simulation.SimulationScreen;

import net.dermetfan.utils.math.MathUtils;

public class GUI {
	public Skin skin;
	public Stage stage;
	private SimulationScreen screen;
	private OrthographicCamera camera;
	
	//Main table
	public Table mainTable;
	public BitmapFont simpleFont;
	public static final GlyphLayout fontLayout = new GlyphLayout();
	public BitmapFont font;
	
	//tickspeed
	public Label tickSpeedLabel;
	//generation
	public Label generationLabel;
	
	//options tabel
	ButtonGroup<Button> optionsGroup;
	
	public GUI(SimulationScreen screen) {
		this.screen = screen;
		skin = new Skin(Gdx.files.internal("uiskin.json"));
		camera = new OrthographicCamera();
		stage = new Stage(new ScreenViewport(camera));
		mainTable = new Table();
		mainTable.setFillParent(true);
		mainTable.setSkin(skin);
		Gdx.input.setInputProcessor(stage);
		
		FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("square-deal.ttf"));
		FreeTypeFontParameter parameter = new FreeTypeFontParameter();
		parameter.size = 8;
		font = generator.generateFont(parameter); // font size 8 pixels
		generator.dispose();
		font.setUseIntegerPositions(false);
		font.setFixedWidthGlyphs("0123456789");
	}
	
	public void setup() {
		//construct interface
		mainTable.top().left().add(speedTable()).left();
		mainTable.row();
		generationLabel = new Label("generation 0", skin);
		mainTable.add(generationLabel).left().row();
		mainTable.add(optionsTable()).left();
		stage.addActor(mainTable);
	}
	
	public void showBrainTable(Agent agent) {
		//brain
		float bwWidth = 400;
		float bwHeight = 400;
		final Window brainWindow = new Window("brain", skin, "resizable");
		brainWindow.setResizable(true);
		brainWindow.getTitleTable().add();
		FFNNRenderer renderer = new FFNNRenderer(agent, screen.atlas, bwWidth, bwHeight);
		Button closeButton = new Button(skin);
		closeButton.addListener(new ChangeListener(){
			public void changed(ChangeEvent event, Actor actor) {
				brainWindow.remove();
			}
		});
		closeButton.add(new Image(skin.getDrawable("icon-close")));
		brainWindow.getTitleTable().add(closeButton);
		brainWindow.setSize(bwWidth, bwHeight);
		//brainWindow.setPosition(stage.getWidth()/2-bwWidth/2, stage.getHeight()/2-bwHeight/2);
		brainWindow.add(renderer);
		brainWindow.addListener(renderer.inputListener);
		stage.addActor(brainWindow);
	}
	
	public Table optionsTable() {
		Window table = new Window("settings", skin);
		table.setMovable(false);
		//button to hide table
		CheckBox showSettings = new CheckBox("settings", skin);
		showSettings.setChecked(true);
		showSettings.addListener(new ChangeListener(){
			public void changed(ChangeEvent event, Actor actor) {
				table.setVisible(showSettings.isChecked());
			}
		});
		mainTable.add(showSettings).left().row();
		HorizontalGroup group = new HorizontalGroup();
		final Button rulesButton = new TextButton("rules", skin.get("toggle", TextButtonStyle.class)); 
		final Button inputsButton = new TextButton("input", skin.get("toggle", TextButtonStyle.class)); 
		rulesButton.setChecked(true);
		optionsGroup = new ButtonGroup<Button>(rulesButton, inputsButton);
		group.addActor(rulesButton);
		group.addActor(inputsButton);
		table.add(group).width(260);
		
		//rule table
		final Table rules = new Table();
		rules.add(new Label("rules are updated per generation", skin)).row();
		rules.add(getCheckBox("hardcore")).row();
		rules.add(getSlider("knockout")).row();
		
		//inputs table
		final Table inputs = new Table();
		inputs.add(inputTable());
		
		//options scrollpane
		final Table optionsTable = new Table();
		final ScrollPane scrollPane = new ScrollPane(optionsTable);
		optionsTable.add(rules);
		
		table.row();
		table.add(scrollPane);
		
		//set button input listener for switching tabs
		ChangeListener tabListener = new ChangeListener() {
			public void changed(ChangeEvent event, Actor actor) {
				optionsTable.clear();
				if(rulesButton.isChecked()) optionsTable.add(rules);
				if(inputsButton.isChecked()) optionsTable.add(inputs);
			}
		};
		rulesButton.addListener(tabListener);
		inputsButton.addListener(tabListener);
		
		return table;
	}
	
	public Table inputTable() {
		Table table = new Table();
		String[] inputOptions = new String[]{"player angle", "player distance","ball angle",
				"ball distance", "own goal angle", "own goal distance", "opp goal angle", "opp goal distance",
				"field edge angle", "field edge distance"};
		for(String s : inputOptions) {
			table.add(getCheckBox(s)).pad(5).left().row();
		}
		final TextButton startExperiment = new TextButton("start experiment", skin);
		table.add(startExperiment);
		return table;
	}
	
	public Table speedTable() {
		Table table = new Table();
		tickSpeedLabel = new Label("x 1", skin);
		
		final TextButton normal = new TextButton("x1", skin);
		normal.addListener(new ChangeListener(){
			public void changed(ChangeEvent event, Actor actor) {
				screen.ticksPerStep=1;
			}
		});
		final TextButton speedUp = new TextButton("+", skin);
		speedUp.addListener(new ChangeListener() {
			public void changed(ChangeEvent event, Actor actor) {
				screen.ticksPerStep*=2;
			}
		});
		final TextButton speedDown = new TextButton("-", skin);
		speedDown.addListener(new ChangeListener(){
			public void changed(ChangeEvent event, Actor actor) {
				screen.ticksPerStep/=2;
			}
		});
		
		float sz = 20;
		table.add(normal).pad(2).size(sz);
		table.add(speedDown).pad(2).size(sz);
		table.add(speedUp).pad(2).size(sz);
		table.add(tickSpeedLabel).height(sz);
		screen.ticksPerStep = MathUtils.clamp(screen.ticksPerStep, 1, 1024);
		return table;
	}
	
	public CheckBox getCheckBox(String name) {
		final CheckBox box = new CheckBox(name, skin);
		box.setChecked(Properties.current.getBProperty(name));
		box.addListener(new ChangeListener() {
			public void changed(ChangeEvent event, Actor actor) {
				Properties.current.setBProperty(name, box.isChecked());
			}
		});
		return box;
	}
	
	public Table getSlider(String name) {
		final Table table = new Table();
		float min = Properties.current.getFMin(name);
		float max = Properties.current.getFMax(name);
		float val = Properties.current.getFProperty(name);
		Slider slider = new Slider(min,  max, (max-min)/30f, false, skin);
		slider.setValue(val);
		slider.addListener(new ChangeListener() {
			public void changed(ChangeEvent event, Actor actor) {
				Properties.current.setFProperty(name, slider.getValue());
			}
		});
		table.add(new Label(name+" ", skin));
		table.add(slider);
		return table;
	}
	
	public void updateSpeed(SimulationScreen screen) {
		if(tickSpeedLabel!=null) {
			screen.ticksPerStep = MathUtils.clamp(screen.ticksPerStep, 1, 1024);
			tickSpeedLabel.setText("x " + Integer.toString(screen.ticksPerStep));
		}
	}
	
	public void render() {
		updateSpeed(screen);
		//infolabel
		String info = String.format("generation %d, tournament %d of %d, selection %d",
				screen.evolution.generation, screen.evolution.selectionIdx+1, screen.evolution.numTournaments,
				screen.evolution.selections.get(screen.evolution.selectionIdx).tournaments.size);
		generationLabel.setText(info);
		stage.act();
		stage.draw();
	}
	
	public boolean contains(float x, float y) {
		Actor actor = stage.hit(x, stage.getHeight()-y, true);
		if(actor!=null)
			return true;
		return false;
	}
	
	public void resize(int width, int height) {
		stage.getViewport().update(width, height, true);
	}
	
	public void dispose() {
		stage.dispose();
		skin.dispose();
	}
}
