package com.buddha.agent;

import java.util.HashMap;

import com.badlogic.gdx.utils.Array;
import com.buddha.simulation.Simulation;

public class InputModel {
	public Array<InputElement> inputElements = new Array<InputElement>();
	public HashMap<Class<? extends InputElement>, Array<? extends InputElement>> inputMap = 
			new HashMap<Class<? extends InputElement>, Array<? extends InputElement>>();
	
	public int size;
	
	public InputModel() {
		size = 0;
	}
	
	public float[] update(Simulation sim, Agent agent) {
		float[] input = new float[getSize()];
		int idx = 0;
		for(int e = 0; e < inputElements.size; e++) {
			InputElement element = inputElements.get(e);
			element.update(sim, agent);
			for(int i = 0; i < element.size; i++) {
				input[idx++]=element.input[i];
			}
		}
		return input;
	}
	
	public <T extends InputElement> void addInputElement(Class<T> type, T element) {
		inputElements.add(element);
		element.startIdx = size;
		size+=element.size;
		@SuppressWarnings("unchecked")
		Array<T> elements = (Array<T>)inputMap.get(type);
		if(elements==null) {
			elements = new Array<T>();
			inputMap.put(type, elements);
		}
		elements.add(element);
	}
	
	@SuppressWarnings("unchecked")
	public <T extends InputElement> Array<T> getElementsOf(Class<T> type) {
		return (Array<T>)inputMap.get(type);
	}
	
	public int getSize() {
		int size = 0;
		for(InputElement element : inputElements) {
			size+=element.size;
		}
		return size;
	}
}
