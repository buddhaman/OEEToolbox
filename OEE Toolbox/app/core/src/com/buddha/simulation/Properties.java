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
	
	public int getIMin(String name) {
		return integerMap.get(MIN_STRING+name);
	}
	
	public int getIMax(String name) {
		return integerMap.get(MAX_STRING+name);
	}
	
	public void initDefault() {
		setBProperty("hardcore", false);
		
		//inputs
		setBProperty("team angle", true);
		setBProperty("team distance", true);
		setBProperty("team direction", false);
		setBProperty("opp angle", true);
		setBProperty("opp distance", true);
		setBProperty("opp direction", false);
		setIProperty("nearest team inputs", 1, 0, 10);
		setIProperty("nearest opp inputs", 1, 0, 10);
		setIProperty("fixed team inputs", 0, 0, 10);
		setIProperty("fixed opp inputs", 0, 0, 10);
		setBProperty("ball angle", true);
		setBProperty("ball distance", true);
		setBProperty("ball direction", false);
		setBProperty("own goal angle", false);
		setBProperty("own goal distance", false);
		setBProperty("opp goal angle", true);
		setBProperty("opp goal distance", false);
		setBProperty("field edge distance", false);
		setBProperty("field edge angle", false);
		setFProperty("cutoff", 30, 10f, 110f);
		setBProperty("handle ball", true);
		setIProperty("hidden layers", 2, 1, 9);
		setIProperty("layer size", 15, 1, 40);
		setIProperty("genes per team", 1, 1, 11);
		setFProperty("adaptation vector", 30f, 1f, 100f);
		setBProperty("adaptation", false);
		setIProperty("tournaments", 4, 1, 12);
		setIProperty("rounds", 4, 2, 8);
		
		setFProperty("knockout", 3f, 1f, 90f);
		setIProperty("game duration", 20, 5, 180);
	}

	
}
