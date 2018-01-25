package com.buddha.neural;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.badlogic.gdx.math.MathUtils;
import com.buddha.agent.Agent;
import com.buddha.agent.InputModel;
import com.buddha.simulation.Properties;

public class FFNN {

	public List<float[]> layers = new ArrayList<float[]>();
	public List<float[][]> synapses = new ArrayList<float[][]>();
	
	public FFNN(float[] genome, int inputNum, int hiddenNum, int outputNum, int hiddenLayers) {
		int genomeIdx = 0;
		layers.add(new float[inputNum]);
		for(int hl = 0; hl < hiddenLayers; hl++) {
			layers.add(new float[hiddenNum]);
		}
		layers.add(new float[outputNum]);
		
		for(int layer = 1; layer < layers.size(); layer++) {
			int num = layers.get(layer).length;
			int prevNum = layers.get(layer-1).length;
			float[][] syn = new float[num][prevNum+1];		//+1 for bias
			for(int i = 0; i < num; i++) {
				for(int j = 0; j < prevNum+1; j++) {
					syn[i][j] = genome[genomeIdx++];
				}
			}
			synapses.add(syn);
		}
	}
	
	public float[] update(float[] input) {
		layers.set(0, input);
		for(int atLayer = 1; atLayer < layers.size(); atLayer++) {
			float[] layer = layers.get(atLayer);
			float[] prevLayer = layers.get(atLayer-1);
			float[][] syn = synapses.get(atLayer-1);
			for(int neur = 0; neur < layer.length; neur++) {
				//update neuron
				float sum = syn[neur][0];
				for(int i = 0; i < prevLayer.length; i++) {
					sum+=prevLayer[i]*syn[neur][i+1];
				}
				layer[neur] = squash(sum); 
			}
		}
		return layers.get(layers.size()-1);
	}
	
	public static int calcSize(InputModel model) {
		int hiddenNum = Properties.current.getIProperty("layer size");
		int inputNum = model.size;
		int hiddenLayers = Properties.current.getIProperty("hidden layers");
		int outputNum = 3;
		return hiddenNum*(inputNum+1)+(hiddenLayers-1)*hiddenNum*(hiddenNum+1)+outputNum*(hiddenNum+1);
	}
	
	public static float[] getRandomGene(int length, float range) {
		float[] array = new float[length];
		for(int i = 0; i < length; i++) {
			array[i] = MathUtils.random(-range, range);
		}
		return array;
	}
	
	public float squash(float x) {
		return (float)(1.0/(1+Math.exp(-x)));
	}
	
	@Override
	public String toString() {
		String s = "synapses:\n";
		for(int i = 0; i < synapses.size(); i++) {
			float[][] syn = synapses.get(i);
			for(int j = 0; j < syn.length; j++) {
				s+=Arrays.toString(syn[j])+" ";
			}
			s+='\n';
		}
		s+="state:\n";
		for(int i = 0; i < layers.size(); i++) {
			s+=Arrays.toString(layers.get(i))+"\n";
		}
		return s;
	}
	
}
