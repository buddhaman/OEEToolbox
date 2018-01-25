package com.buddha.simulation;

import java.util.HashMap;

public class Properties {
	
	public HashMap<String, Integer> integerMap = new HashMap<String, Integer>();
	public HashMap<String, Boolean> booleanMap = new HashMap<String, Boolean>();
	public HashMap<String, Float> floatMap = new HashMap<String, Float>();
	
	public static final String MAX_STRING = "maxv_";
	public static final String MIN_STRING = "minv_";
	public static Properties current = new Properties();
	
	public Properties() {
		initDefault();
	}
	
	public void setIProperty(String name, int value) {
		integerMap.put(name, value);
	}
	
	public int getIProperty(String name) {
		return integerMap.get(name);
	}
	
	public void setFProperty(String name, float value) {
		floatMap.put(name, value);
	}
	
	public float getFProperty(String name) {
		return floatMap.get(name);
	}
	
	public void setBProperty(String name, boolean value) {
		booleanMap.put(name, value);
	}
	
	public boolean getBProperty(String name) {
		return booleanMap.get(name);
	}
	
	public void setFProperty(String name, float value, float min, float max) {
		floatMap.put(name, value);
		floatMap.put(MAX_STRING+name, max);
		floatMap.put(MIN_STRING+name, min);
	}
	
	public void setIProperty(String name, int value, int min, int max) {
		integerMap.put(name, value);
		integerMap.put(MIN_STRING+name, min);
		integerMap.put(MAX_STRING+name, max);
	}
	
	public float getFMin(String name) {
		return floatMap.get(MIN_STRING+name);
	}
	
	public float getFMax(String name) {
		return floatMap.get(MAX_STRING+name);
	}
	
	public void initDefault() {
		setBProperty("hardcore", false);
		
		//inputs
		setBProperty("player angle", true);
		setBProperty("player distance", true);
		setBProperty("ball angle", true);
		setBProperty("ball distance", true);
		setBProperty("own goal angle", true);
		setBProperty("own goal distance", true);
		setBProperty("opp goal angle", true);
		setBProperty("opp goal distance", true);
		setBProperty("field edge distance", true);
		setBProperty("field edge angle", true);
		setFProperty("cutoff", 50, 10f, 110f);
		
		setFProperty("knockout", 3f, 1f, 90f);

		setIProperty("game duration", 90);
		
		setIProperty("hidden layers", 2);
		setIProperty("layer size", 23);
	}

	
}
