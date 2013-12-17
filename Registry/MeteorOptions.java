/*******************************************************************************
 * @author Reika Kalseki
 * 
 * Copyright 2013
 * 
 * All rights reserved.
 * Distribution of the software in any form is only allowed with
 * explicit, prior permission from the owner.
 ******************************************************************************/
package Reika.MeteorCraft.Registry;

import net.minecraftforge.common.Configuration;
import Reika.DragonAPI.Exception.RegistrationException;
import Reika.DragonAPI.Interfaces.ConfigList;
import Reika.MeteorCraft.MeteorCraft;

public enum MeteorOptions implements ConfigList {

	LOGLOADING("Console Loading Info", true),
	DEBUGMODE("Debug Mode", false),
	CHANCE("Meteor Rarity", 72000), //one meteor every hour
	ORE("Meteor Ore Density", 40), //default 40% ore
	MINY("Minimum Impactable Y", -1),
	DIM0BURST("Always Skyburst in Overworld", false),
	NETHERBURST("Always Skyburst in Nether", false),
	ENDBURST("Always Skyburst in End", false),
	DIM7BURST("Always Skyburst in Twilight Forest", false),
	OTHER("Always Skyburst in Other Dimensions", false),
	SHOWER("Allow Meteor Showers", true),
	EXPLODE("Global Airburst Override", false);

	private String label;
	private boolean defaultState;
	private int defaultValue;
	private float defaultFloat;
	private Class type;

	public static final MeteorOptions[] optionList = MeteorOptions.values();

	private MeteorOptions(String l, boolean d) {
		label = l;
		defaultState = d;
		type = boolean.class;
	}

	private MeteorOptions(String l, int d) {
		label = l;
		defaultValue = d;
		type = int.class;
	}

	public boolean isBoolean() {
		return type == boolean.class;
	}

	public boolean isNumeric() {
		return type == int.class;
	}

	public boolean isDecimal() {
		return type == float.class;
	}

	public float setDecimal(Configuration config) {
		if (!this.isDecimal())
			throw new RegistrationException(MeteorCraft.instance, "Config Property \""+this.getLabel()+"\" is not decimal!");
		return (float)config.get("Control Setup", this.getLabel(), defaultFloat).getDouble(defaultFloat);
	}

	public float getFloat() {
		return (Float)MeteorCraft.config.getControl(this.ordinal());
	}

	public Class getPropertyType() {
		return type;
	}

	public int setValue(Configuration config) {
		if (!this.isNumeric())
			throw new RegistrationException(MeteorCraft.instance, "Config Property \""+this.getLabel()+"\" is not numerical!");
		return config.get("Control Setup", this.getLabel(), defaultValue).getInt();
	}

	public String getLabel() {
		return label;
	}

	public boolean setState(Configuration config) {
		if (!this.isBoolean())
			throw new RegistrationException(MeteorCraft.instance, "Config Property \""+this.getLabel()+"\" is not boolean!");
		return config.get("Control Setup", this.getLabel(), defaultState).getBoolean(defaultState);
	}

	public boolean getState() {
		return (Boolean)MeteorCraft.config.getControl(this.ordinal());
	}

	public int getValue() {
		return (Integer)MeteorCraft.config.getControl(this.ordinal());
	}

	public boolean isDummiedOut() {
		return type == null;
	}

}
