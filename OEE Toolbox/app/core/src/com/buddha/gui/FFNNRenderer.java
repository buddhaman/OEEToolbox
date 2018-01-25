package com.buddha.gui;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.utils.Array;
import com.buddha.agent.Agent;
import com.buddha.neural.FFNN;
import com.buddha.render.RenderUtils;
import com.buddha.world.AABB;
import com.buddha.world.Circle;
import com.buddha.world.Constraint;

public class FFNNRenderer extends Actor {
	public FFNN ffnn;
	public Agent agent;
	public AtlasRegion circle;
	public AtlasRegion square;
	
	public Array<Circle> circles = new Array<Circle>();
	public Array<Circle[]> layers = new Array<Circle[]>();
	public Array<Syn> synapses = new Array<Syn>();
	float neuronR = 4;
	float minStrength;
	
	public Circle selected = null;
	public boolean mouseDown;
	public float mx = 10000f;
	public float my = 10000f;
	
	public AABB bounds = new AABB(0,0,0,0);
	
	public FFNNRenderer(Agent agent, TextureAtlas atlas, float width, float height) {
		this.ffnn = agent.brain;
		this.agent = agent;
		circle = atlas.findRegion("circle");
		square = atlas.findRegion("blank");
		float hDist = 16;
		float vDist = 56;
		for(int j = 0; j < ffnn.layers.size(); j++) {
			float[] layer = ffnn.layers.get(j);
			Circle[] cLayer = new Circle[layer.length];
			float lw = layer.length*hDist;
			for(int i = 0; i < layer.length; i++) {
				Circle c = addCircle(width/2-lw/2+i*hDist, j*vDist);
				cLayer[i] = c;
			}
			layers.add(cLayer);
		}
		for(int layer = 1; layer < ffnn.synapses.size()+1; layer++) {
			float[][] synapses = ffnn.synapses.get(layer-1);
			for(int i = 0; i < synapses.length; i++) {
				Circle c1 = layers.get(layer)[i];
				for(int j = 1; j < synapses[i].length; j++) {
					float strength = synapses[i][j];
					if(Math.abs(strength) > minStrength) {
						Circle c2 = layers.get(layer-1)[j-1];
						connect(c1, c2, synapses[i][j]);
					}
				}
			}
		}
		synapses.shuffle();
	}
	
	public void connect(Circle c1, Circle c2, float strength) {
		synapses.add(new Syn(c1, c2, strength));
	}
	
	public Circle addCircle(float x, float y) {
		Circle c = new Circle(x, y, neuronR, 1);
		c.particle.friction = 0.97f;
		circles.add(c);
		return c;
	}
	
	public float getNeuronActivation(int idx) {
		for(int j = 0; j < ffnn.layers.size(); j++) {
			float[] layer = ffnn.layers.get(j);
			if(idx < layer.length) {
				return layer[idx];
			} else {
				idx-=layer.length;
			}
		}
		System.err.println("Index isn't a neuron.");
		return -1;
	}
	
	public float getTX(float x) {
		return x-bounds.x1;
	}
	
	public float getTY(float y) {
		return y-bounds.y1;
	}
	
	public void drawSynapse(Batch batch, Syn s) {
		if(s.strength < 0) {
			batch.setColor(1, 0.41f, 0.38f, 1);
		} else  {
			batch.setColor(0.47f, .78f, 0.47f, 1);
		}
		RenderUtils.drawLine((SpriteBatch)batch,  getTX(s.c.a.pos.x),  getTY(s.c.a.pos.y),  
				getTX(s.c.b.pos.x),  getTY(s.c.b.pos.y),  Math.abs(s.strength), square);
	}
	
	public void drawCircle(Batch batch, float x, float y, float r) {
		batch.draw(circle, getTX(x-r), getTY(y-r), r*2, r*2);
	}
	
	@Override
	public void act(float delta) {
		super.act(delta);

		bounds.setWidth(getParent().getWidth());
		bounds.setHeight(getParent().getHeight()-30);
		for(Circle c : circles) {
			bounds.checkCollisions(c);
			c.update();
		}
		for(Syn s : synapses) {
			s.c.solve();
		}
		if(selected!=null && mouseDown) {
			 selected.particle.pos.set(mx, my);
		}
	}
	
	@Override
	public void draw(Batch batch, float parentAlpha) {
		super.draw(batch, parentAlpha);

		bounds.x1 = getParent().getX();
		bounds.y1 = getParent().getY();
		for(Syn s : synapses) {
			if(Math.abs(s.strength) > minStrength) {
				drawSynapse(batch, s);
			}
		}
		for(int i = 0; i < circles.size; i++) {
			Circle c = circles.get(i);
			float activation = getNeuronActivation(i);
			batch.setColor(activation, activation, activation, 1);

			float dst = c.particle.pos.dst(mx, my);
			float fact = neuronR+2*neuronR-2*neuronR*Math.min(100, dst)/100f;
			c.radius = fact;
			drawCircle(batch, c.getX(), c.getY(), c.radius);
		}
	}
	
	private class Syn {
		public Constraint c;
		public float strength;
		public Syn(Circle c1, Circle c2, float strength) {
			c = new Constraint(c1.particle, c2.particle);
			c.softness=0.1f;
			this.strength = strength;
		}
	}
	
	public final InputListener inputListener = new InputListener(){
		 public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
			 selected = null;
			 mx = x+bounds.x1;
			 my = y+bounds.y1;
			 mouseDown = true;
			 for(Circle c : circles) {
				 if(c.contains(mx, my)) {
					 selected = c;
				 }
			 }
		 	 return true;
        }
		 
		 public void touchDragged(InputEvent event, float x, float y, int pointer) {
			 mx = x+bounds.x1;
			 my = y+bounds.y1;
		 }
		 
		 public void touchUp (InputEvent event, float x, float y, int pointer, int button) {
			 mouseDown = false;
		 }
		 
		@Override
		public boolean mouseMoved(InputEvent event, float x, float y) {
			mx = x+bounds.x1;
			my = y+bounds.y1;
			return super.mouseMoved(event, x, y);
		}
	};
}
