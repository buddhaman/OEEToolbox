package com.buddha.render;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.buddha.agent.Agent;
import com.buddha.agent.Ball;
import com.buddha.agent.BallCollisionHandler;
import com.buddha.agent.BallPositionInput;
import com.buddha.agent.FieldEdgeInput;
import com.buddha.agent.GoalPositionInput;
import com.buddha.agent.InputModel;
import com.buddha.agent.PlayerPositionInput;
import com.buddha.gui.GUI;
import com.buddha.phys3d.Constraint3;
import com.buddha.phys3d.Particle3;
import com.buddha.phys3d.Skeleton;
import com.buddha.simulation.Simulation;
import com.buddha.simulation.SimulationScreen;
import com.buddha.world.AABB;
import com.buddha.world.World;

public class SimulationRenderer {
	
	public RenderUtils utils;
	public SpriteBatch batch;
	public Camera cam;
	public AtlasRegion circle;
	public AtlasRegion square;
	private Simulation sim;
	public Agent selected;
	public boolean fancy = true;
	public float depth = 0.8f;
	public FrameBuffer fbo;
	public float skeletonWidth = 0.25f;
	boolean started = false;
	
	//colors
	public static Color shoeColor = Color.DARK_GRAY;
	public static Color skinColor = new Color(234f/255f,192f/255f,134f/255f, 1);
	public static Color ballColor = new Color(0.4f, 0.4f, 0.4f, 1);
	public static Color ballColorShade = new Color(0.35f, 0.35f, 0.35f, 1);
	public static Color leftScoreColor = new Color(1f, 0.41f, 0.38f, 1);
	public static Color rightScoreColor = new Color(0.68f, 0.78f, 1, 1);
	public static Color grassColor1 = new Color(119f/255f*0.7f, 221f/255f*0.7f, 119f/255f*0.7f,1f);
	public static Color grassColor2 = new Color(119f/255f*0.63f, 221f/255f*0.63f, 119f/255f*0.63f,1f);
	public static Color randomColors;
	
	public SimulationRenderer(TextureAtlas atlas, SpriteBatch batch) {
		this.batch = batch;
		circle = atlas.findRegion("circle");
		square = atlas.findRegion("blank");
		utils = new RenderUtils(batch, square);
		cam = new Camera();
		
		//try framebuffer
		try {
			fbo = new FrameBuffer(Format.RGBA8888, 1024, 512, false);
		} catch (Exception e) {
			System.err.print("Can't create framebuffer, no shadows supported.");
		}
	}
	
	public void render(Simulation sim, SimulationScreen screen) {
		this.sim = sim;
		World world = sim.world;
		if(!started) {
			started=true;
			this.centerCamera();
		}
		cam.update();
		batch.setProjectionMatrix(cam.cam.combined);
		batch.begin();
		//render field
		AABB bounds = world.bounds;
		if(bounds!=null) {
			drawField(bounds);
		}
		if(selected!=null)
			drawInfo(selected, screen.gui.font);
		renderText(screen.gui.font);
		if(fancy) {
			drawShadows();
		}
		sim.agents.sort((a, b) -> Float.compare(b.getDepth(), a.getDepth()));
		for(Agent agent : sim.agents) {
			if(agent.inactive()) {
				drawAgent(agent);
			}
		}
		for(Ball ball : sim.balls) {
			drawBall(ball);
		}
		for(Agent agent : sim.agents) {
			if(!agent.inactive())
				drawAgent(agent);
		}
		
		//render goals
		for(AABB goal : sim.goals) {
			batch.setColor(1,1,1,0.5f);
			batch.draw(square, goal.x1, goal.y1, goal.getWidth(), goal.getHeight());
		}

		batch.end();
	}
	
	private void drawField(AABB bounds) {
		Color lineColor = Color.WHITE;
		float lineWidth = 0.2f;
		int divisions = 8;
		float dw = bounds.getWidth()/divisions;
		batch.setColor(grassColor1);
		batch.draw(square, bounds.x1, bounds.y1, bounds.getWidth(), bounds.getHeight());
		for(int i = 0; i < divisions; i++) {
			batch.setColor(i%2==0 ? grassColor1 : grassColor2);
			batch.draw(square, bounds.x1+dw*i, bounds.y1, dw, bounds.getHeight());
		}		
		batch.setColor(lineColor);
		utils.drawCircle(circle, bounds.getCX(), bounds.getCY(), bounds.getHeight()/6f);
		batch.setColor(grassColor1);
		utils.drawCircle(circle, bounds.getCX(), bounds.getCY(), bounds.getHeight()/6f-lineWidth*2);
		batch.setColor(lineColor);
		utils.drawLine(bounds.getCX(), bounds.y2, bounds.getCX(), bounds.y1, lineWidth);
		utils.drawCircle(circle, bounds.getCX(), bounds.getCY(), bounds.getHeight()/100f);
		RenderUtils.drawLineRect(batch, bounds.x1, bounds.y1, 
				bounds.x2-bounds.x1, bounds.y2-bounds.y1, lineWidth, square);
		RenderUtils.drawLineRect(batch, bounds.x1, bounds.y1+bounds.getHeight()/4f, 
				bounds.getWidth()/8f, bounds.getHeight()*2f/4f, lineWidth, square);
		RenderUtils.drawLineRect(batch, bounds.x2-bounds.getWidth()/8f, bounds.y1+bounds.getHeight()/4f, 
				bounds.getWidth()/8f, bounds.getHeight()*2f/4f, lineWidth, square);
	}
	
	private void drawBall(Ball ball) {
		float r = ball.circle.radius;
		float grid = r*1.2f;
		float dotSize = 0.3f;
		int minX = ((int)((ball.circle.getX()-r)/grid));
		int minY = ((int)((ball.circle.getY()-r)/grid));
		int rn = (int)(2*r/grid)+1;
		batch.setColor(ballColorShade);
		utils.drawCircle(circle, ball.circle.getX(), ball.circle.getY(), r*1.3f);
		batch.setColor(ballColor);
		utils.drawCircle(circle, ball.circle.getX(), ball.circle.getY(), ball.circle.radius);
		batch.setColor(ballColorShade);
		for(int i = -1; i < rn+1; i++) {
			for(int j = -1; j < rn+1; j++) {
				float xx = (minX+i)*grid+(ball.circle.getX()*2)%grid;
				float yy = (minY+j)*grid+(ball.circle.getY()*2)%grid;
				float dx = xx-ball.circle.getX();
				float dy = yy-ball.circle.getY();
				if(dx*dx + dy*dy < r*r) {
					float l = (float)Math.sqrt(dx*dx+dy*dy);
					float fac = (float)Math.sqrt(1-(l/r)*(l/r));
					dx=fac*dotSize*dx/(l);
					dy=fac*dotSize*dy/(l);
					RenderUtils.drawLine(batch, xx-dx, yy-dy, xx+dx, yy+dy, dotSize, circle);
				}
			}
		}
	}
	
	public void drawShadows() {
		batch.end();
		fbo.begin();
		Gdx.gl.glClearColor(0,0,0,0);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		batch.begin();
		
		for(Agent agent : sim.agents) {
			drawShadow(agent);
		}
		
		batch.end();
		fbo.end();
		batch.begin();
		batch.setColor(1,1,1,0.3f);
		batch.draw(fbo.getColorBufferTexture(), cam.x-cam.width*cam.scale/2f, cam.y+cam.height*cam.scale/2f,
				cam.width*cam.scale, -cam.height*cam.scale);
	}

	private void drawShadow(Agent agent) {
		if(agent.skeleton==null || fbo==null)
			return;
		
		batch.setColor(Color.BLACK);
		
		//draw actual stuff
		for(Particle3 p : agent.skeleton.particles) {
			drawCircle(p.pos.x, p.pos.y, 0, skeletonWidth*1.5f);
		}
		for(Constraint3 c : agent.skeleton.constraints) {
			utils.drawLine(c.a.pos.x, c.a.pos.y, c.b.pos.x, c.b.pos.y, skeletonWidth*2f);
		}
		Vector3 headPos = agent.skeleton.get(0).pos;
		drawCircle(headPos.x, headPos.y, 0, skeletonWidth*3f);
	}

	public void setSelected(Agent agent) {
		this.selected = agent;
	}
	
	public void drawAgent(Agent agent) {
		if(fancy) {
			drawSkeleton(agent);
		} else {
			batch.setColor(agent.team.color);
			utils.drawCircle(circle, agent.circle.getX(), agent.circle.getY(), agent.circle.radius);
			batch.setColor(Color.BLACK);
			float x2 = agent.circle.getX()+MathUtils.cos(agent.direction);
			float y2 = agent.circle.getY()+MathUtils.sin(agent.direction);
			utils.drawLine(agent.circle.getX(), agent.circle.getY(), x2, y2, 0.3f);
		}
	}
	
	public void drawSkeleton(Agent agent) {
		Skeleton skeleton = agent.getSkeleton();
		float width = skeletonWidth;
		Color teamColor = agent.team.color;
		//lower legs
		batch.setColor(skinColor);
		drawLimb(skeleton.getC(10), width);
		drawLimb(skeleton.getC(8), width);
		batch.setColor(shoeColor);
		drawParticle(skeleton.get(9), width*1.5f);
		drawParticle(skeleton.get(11), width*1.5f);
		batch.setColor(skinColor);
		drawParticle(skeleton.get(8), width);
		drawParticle(skeleton.get(10), width);
		
		//pants
		batch.setColor(teamColor);
		drawShirt(skeleton.getC(7), width*2f, width);
		drawShirt(skeleton.getC(9), width*2f, width);
		drawParticle(skeleton.get(7), width*2f);
		drawParticle(skeleton.get(6), width*1.5f);
		drawShirt(skeleton.getC(6), width*1.5f, width*2f);
		drawShirt(skeleton.getC(5), width, width*1.5f);
		//neck
		drawParticle(skeleton.get(1), width);
		//arms
		batch.setColor(skinColor);
		drawParticle(skeleton.get(2), width);
		drawParticle(skeleton.get(4), width);
		batch.setColor(teamColor);
		drawShirt(skeleton.getC(1), width, width*1.5f);
		drawShirt(skeleton.getC(3), width, width*1.5f);
		batch.setColor(skinColor);
		drawLimb(skeleton.getC(2), width);
		drawLimb(skeleton.getC(4), width);
		drawParticle(skeleton.get(3), width*1.5f);
		drawParticle(skeleton.get(5), width*1.5f);
		drawLimb(skeleton.getC(0), width);
		
		float eyeAngle = 0.7f;
		if(agent.direction+eyeAngle >= 0) {
			drawEye(agent, skeleton, width, eyeAngle);
		}
		if(agent.direction-eyeAngle < -MathUtils.PI || 
				agent.direction-eyeAngle >= 0) {
			drawEye(agent, skeleton, width, -eyeAngle);
		}
		//draw head
		batch.setColor(skinColor);
		drawParticle(skeleton.get(0), width*3f);
		if(agent.direction+eyeAngle > MathUtils.PI || 
				agent.direction+eyeAngle < 0) {
			drawEye(agent, skeleton, width, eyeAngle);
		}
		if(agent.direction-eyeAngle < 0) {
			drawEye(agent, skeleton, width, -eyeAngle);
		}
	}
	
	public void drawEye(Agent agent, Skeleton skeleton, float width, float angle) {
		Vector3 headPos = skeleton.get(0).pos;
		float r1 = width*2;
		float sin = MathUtils.sin(agent.direction+angle);
		float cos = MathUtils.cos(agent.direction+angle);
		float sincr = MathUtils.sin(agent.direction+angle+0.1f);
		float coscr = MathUtils.cos(agent.direction+angle+0.1f);
		batch.setColor(Color.WHITE);
		drawCircle(headPos.x+cos*r1, headPos.y+sin*r1, headPos.z, width*1.7f);
		batch.setColor(Color.BLACK);
		drawCircle(headPos.x+coscr*r1, headPos.y+sincr*r1, headPos.z, width);
	}
	
	public void drawLimb(Constraint3 c, float r) {
		utils.drawLine(c.a.pos.x, c.a.pos.y+c.a.pos.z*depth, 
				c.b.pos.x, c.b.pos.y+c.b.pos.z*depth, r);
	}
	
	public void drawShirt(Constraint3 c, float r1, float r2) {
		utils.drawLine(c.a.pos.x, c.a.pos.y+c.a.pos.z*depth, 
				c.b.pos.x, c.b.pos.y+c.b.pos.z*depth, r1, r2);
	}
	
	public void drawCircle(float x, float y, float z, float r) {
		utils.drawCircle(circle, x, y+z*depth, r);
	}
	
	public void drawParticle(Particle3 p, float r) {
		utils.drawCircle(circle, p.pos.x, p.pos.y+p.pos.z*depth, r);
	}
	
	public void drawInfo(Agent agent, BitmapFont font) {
		//TODO: make
		Vector2 pos = agent.circle.particle.pos;
		InputModel inputModel = agent.team.inputModel;
		batch.setColor(1,1,1,0.3f);
		float cutoff = 0;
		for(PlayerPositionInput ppi : inputModel.getElementsOf(PlayerPositionInput.class)) {
			drawInfo(agent, ppi.startIdx, ppi.distance, ppi.angle, ppi.cutoff);
			cutoff = ppi.cutoff;
		}
		for(BallPositionInput bpi : inputModel.getElementsOf(BallPositionInput.class)) {
			drawInfo(agent, bpi.startIdx, bpi.distance, bpi.angle, bpi.cutoff);
		}
		for(GoalPositionInput gpi : inputModel.getElementsOf(GoalPositionInput.class)) {
			drawInfo(agent, gpi.startIdx, gpi.distance, gpi.angle, gpi.cutoff);
		}
		for(FieldEdgeInput fei : inputModel.getElementsOf(FieldEdgeInput.class)) {
			drawInfo(agent, fei.startIdx, fei.distance, fei.angle, fei.cutoff);
		}
		drawCone(pos.x, pos.y, cutoff, agent.direction-agent.fov/2f, agent.fov);
		BallCollisionHandler bh = agent.team.ballHandler;
		if(agent.team.ballHandler!=null) {
			float angle = bh.getAngle(agent);
			float magnitude = bh.getMagnitude(agent)*5f;
			batch.setColor(1f,.41f,.38f, 1f);
			drawArrow(pos.x, pos.y, magnitude, angle);
		}
		if(agent.team.numGenes!=1) {
			font.getData().setScale(0.4f);
			String text = "gene "+agent.geneIdx;
			GUI.fontLayout.setText(font, text);
			this.drawShadedText(font, text, agent.team.color,
					0.8f, agent.circle.getX()-GUI.fontLayout.width/2, agent.circle.getY()-3);
		}
	}
	
	public void drawInfo(Agent a, int idx, boolean distance, boolean angle, float cutoff) {
		if(!distance && !angle)
			return;
		float r = distance ? (1-a.input[idx])*cutoff : 5;
		float theta = angle ? (distance ? a.input[idx+1] : a.input[idx]) : 0;
		drawRadialLine(a.circle.getX(), a.circle.getY(), r, theta+a.direction);
	}
	
	public void drawRadialLine(float x0, float y0, float r, float theta) {
		float lw = 0.1f;
		utils.drawLine(x0, y0, x0+MathUtils.cos(theta)*r, y0+MathUtils.sin(theta)*r, lw);
		utils.drawCircle(circle, x0+MathUtils.cos(theta)*r,  y0+MathUtils.sin(theta)*r, lw*4);
		utils.drawCircle(circle, x0,  y0, lw*4);
	}
	
	public void drawArrow(float x0, float y0, float r, float theta) {
		float x1 = x0+MathUtils.cos(theta)*r;
		float y1 = y0+MathUtils.sin(theta)*r;
		float arrowRad = MathUtils.PI/4f;
		float al = 1f;
		float p1x = x1+MathUtils.cos(theta+MathUtils.PI-arrowRad)*al;
		float p1y = y1+MathUtils.sin(theta+MathUtils.PI-arrowRad)*al;
		float p2x = x1+MathUtils.cos(theta+MathUtils.PI+arrowRad)*al;
		float p2y = y1+MathUtils.sin(theta+MathUtils.PI+arrowRad)*al;
		float aw = 0.2f;
		utils.drawLine(x0, y0, x1, y1, aw);
		utils.drawLine(x1, y1, p1x, p1y, aw);
		utils.drawLine(x1, y1, p2x, p2y, aw);
		utils.drawCircle(circle, x1, y1, aw);
		utils.drawCircle(circle, p1x, p1y, aw);
		utils.drawCircle(circle, p2x, p2y, aw);
	}
	
	public void drawCone(float x0, float y0, float r, float theta0, float tw) {
		int segments = (int)Math.max(1, tw*100f/MathUtils.PI2);
		float dtheta = tw/segments;
		for(int i = 0; i < segments; i++) {
			float theta = theta0+dtheta*i;
			float x1 = x0+r*MathUtils.cos(theta);
			float y1 = y0+r*MathUtils.sin(theta);
			float x2 = x0+r*MathUtils.cos(theta+dtheta);
			float y2 = y0+r*MathUtils.sin(theta+dtheta);
			utils.drawTriangle(x0, y0, x1, y1, x2, y2);
		}
	}
	
	public void renderText(BitmapFont font) {
		GlyphLayout layout = GUI.fontLayout;
		AABB bounds = sim.world.bounds;
		layout.setText(font, Integer.toString(sim.teams.get(1).score));
		Color c0 = sim.teams.get(0).color;
		Color c1 = sim.teams.get(1).color;
		float shade = 0.8f;
		font.getData().setScale(1);
		drawShadedText(font, Integer.toString(sim.teams.get(0).score), 
				c0, shade, bounds.x1+0.8f, bounds.y1+layout.height+1);
		drawShadedText(font, Integer.toString(sim.teams.get(1).score),
				c1, shade, bounds.x2-layout.width, bounds.y1+layout.height+1);
		int minutes = sim.ticks/3600;
		int seconds = (sim.ticks/60)%60;
		String time = String.format("%02d.%02d", minutes, seconds);
		
		layout.setText(font, time);
		float xoff = 0.5f;
		drawShadedText(font, time, Color.GOLD, 0.8f, 
				bounds.getCX()-layout.width/2+xoff, bounds.y1+layout.height+1);
		layout.setText(font, ".");
		drawShadedText(font, ".", Color.GOLD, 0.8f, 
				bounds.getCX()-layout.width/2+xoff, bounds.y1+layout.height+1+3);
	}
	
	public void drawShadedText(BitmapFont font, String text, Color c, float shade, float x, float y) {
		font.setColor(c.r*shade*shade, c.g*shade*shade, c.b*shade*shade, 1);
		font.draw(batch, text, x, y-0.7f*font.getScaleY()); //?????? fonts behave fukc
		font.setColor(c.r*shade, c.g*shade, c.b*shade, 1);
		font.draw(batch, text, x, y); 
	}
	
	public void centerCamera() {
		AABB bounds = sim.world.bounds;
		cam.x = (bounds.x1+bounds.x2)/2f;
		cam.y = (bounds.y1+bounds.y2)/2f;
	}
	
	public void resize(int width, int height) {
		cam.resize(width, height);
	}
	
	public void dispose() {
		fbo.dispose();
	}
}
