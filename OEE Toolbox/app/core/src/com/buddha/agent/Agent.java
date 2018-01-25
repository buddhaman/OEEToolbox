package com.buddha.agent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.buddha.neural.FFNN;
import com.buddha.phys3d.Particle3;
import com.buddha.phys3d.Skeleton;
import com.buddha.simulation.Properties;
import com.buddha.simulation.Simulation;
import com.buddha.simulation.Timer;
import com.buddha.world.Circle;

public class Agent {
	
	public static float minWeight = -5f;
	public static float maxWeight = 5f;
	
	public Circle circle;
	public float direction;
	public float turnSpeed = 0.6f;
	public float speed = 0.1f;
	public float fov = MathUtils.PI2;
	public FFNN brain;
	public float[] input;
	public Color color;
	public int geneIdx;
	public Skeleton skeleton;
	public Team team;
	public Timer inactive;	//wheter hit or dead
	
	public Agent(float x, float y, float[] gene, Team team) {
		this.team = team;
		this.circle = new Circle(x, y, 1f, 1f);
		circle.particle.friction = 0.8f;
		int inputSize = team.inputModel.size;
		int layerSize = Properties.current.getIProperty("layer size");
		int layerNum = Properties.current.getIProperty("hidden layers");
		int outputSize = 3;
		brain = new FFNN(gene, inputSize, layerSize, outputSize, layerNum);
		this.input = new float[inputSize];
		float knockout = Properties.current.getFProperty("knockout");
		inactive = new Timer((int)(knockout*60));
	}
	
	public void update() {
		float[] output = brain.update(input);
		this.inactive.update();
		if(!inactive()) {
			move(output[0]);
			turn(output[1]);
			turn(-output[2]);
		}
		if(skeleton!=null) {
			updateSkeleton();
		}
	}
	
	public void setInput(float[] input) {
		this.input = input;
	}
	
	public Skeleton getSkeleton() {
		if(skeleton==null) {
			skeleton=new Skeleton(circle.getX(), circle.getY());
		}
		return skeleton;
	}
	
	public void updateSkeleton() {
		if(!inactive()) {
			skeleton.move();
		}
		skeleton.setPosition(this);
		skeleton.update();
	}
	
	public void setColor(Color color) {
		this.color = color;
	}
	
	public void turn(float alpha) {
		direction+=alpha*turnSpeed;
		if(direction < -MathUtils.PI) {
			direction+=MathUtils.PI2;
		} 
		if(direction > MathUtils.PI) {
			direction-=MathUtils.PI2;
		}
	}
	
	public void move(float fac) {
		circle.particle.addImpulse(MathUtils.cos(direction)*speed*fac, MathUtils.sin(direction)*speed*fac);
	}

	public void setState(float x, float y, float angle) {
		this.circle.particle.pos.set(x, y);
		direction = angle;
		this.circle.particle.resetVel();
	}

	public static float[] crossover(float[] gene1, float[] gene2) {
		float crossProb = 1f/((float)gene1.length);
		boolean in1 = true;
		float[] newGene = new float[gene1.length];
		for(int i = 0; i < gene1.length; i++) {
			in1 = MathUtils.randomBoolean(crossProb) ? !in1 : in1;
			newGene[i] = in1 ? gene1[i] : gene2[i];
		}
		return newGene;
	}
	
	public static void mutate(float[] gene) {
		float mutateProb = 12f/((float)gene.length);
		float mutationRate = 0.8f;
		for(int i = 0; i < gene.length; i++) {
			if(MathUtils.randomBoolean(mutateProb)) {
				gene[i] = gene[i]+MathUtils.random(-1f, 1f)*mutationRate;
				gene[i] = MathUtils.clamp(gene[i], minWeight, maxWeight);
			}
		}
	}
	
	public static void mutate(float[] gene, float[] adaptation) {
		float learningRate = (float)(1.0/Math.sqrt(gene.length));
		for(int i = 0; i < gene.length; i++) {
			adaptation[i]=(float)(adaptation[i]*Math.exp(learningRate*MathUtils.random.nextGaussian()));
			gene[i] = gene[i]+(float)(MathUtils.random.nextGaussian())*adaptation[i];
			gene[i] = MathUtils.clamp(gene[i], minWeight, maxWeight);
		}
	}

	public void setGeneIdx(int geneIdx) {
		this.geneIdx = geneIdx;
	}

	public void hitBall(Ball ball) {
		if(skeleton!=null) {
			skeleton.hitBall(ball);
		}
	}
	
	public void hit() {
		this.inactive.start();
	}
	
	public boolean inactive() {
		return inactive.enabled;
	}
	
	public float getDepth() {
		return circle.getY();
	}
	
	public static float calcAngle(Agent a, Vector2 v) {
		float dx = v.x-a.circle.particle.pos.x;
		float dy = v.y-a.circle.particle.pos.y;
		float diff = MathUtils.atan2(dy, dx)-a.direction;
		if(diff > MathUtils.PI) {
			diff-=MathUtils.PI2;
		}
		if(diff < -MathUtils.PI) {
			diff+=MathUtils.PI2;
		}
		return diff;
	}
	
	public static float calcDist(Agent a, Vector2 v, float cutoff) {
		float dst = v.dst(a.circle.particle.pos);
		return 1f-Math.min(cutoff, dst)/cutoff;
	}
	
	public static void collision(Agent a1, Agent a2, boolean hardcore) {
		//switch
		if(MathUtils.randomBoolean()) {
			Agent temp = a1;
			a1 = a2;
			a2 = temp;
		}
		if(!a1.inactive() && !a2.inactive()) {
			if(MathUtils.randomBoolean(0.03f)) {
				if(hardcore) {
					a2.hit();
				}
				if(a1.skeleton!=null && a2.skeleton!=null) {
					float force = 4f+(hardcore ? 10f : 0f);
					float dx = a2.circle.getX()-a1.circle.getX();
					float dy = a2.circle.getY()-a1.circle.getY();
					float radsum = (a1.circle.radius+a2.circle.radius);
					dx/=radsum;
					dy/=radsum;
					Particle3 hand = MathUtils.randomBoolean() ? a1.skeleton.get(3) : a1.skeleton.get(5);
					hand.pos.set(a2.skeleton.get(1).pos);
					a2.skeleton.get(1).addImpulse(dx*force, dy*force, 0);
				}
			}
		}
	}

	
}
