/*******************************************************************************
 * @author Reika Kalseki
 * 
 * Copyright 2015
 * 
 * All rights reserved.
 * Distribution of the software in any form is only allowed with
 * explicit, prior permission from the owner.
 ******************************************************************************/
package Reika.MeteorCraft.Registry;

import Reika.DragonAPI.Interfaces.Configuration.BooleanConfig;
import Reika.DragonAPI.Interfaces.Configuration.DecimalConfig;
import Reika.DragonAPI.Interfaces.Configuration.IntegerConfig;
import Reika.DragonAPI.Interfaces.Configuration.UserSpecificConfig;
import Reika.MeteorCraft.MeteorCraft;

public enum MeteorOptions implements IntegerConfig, BooleanConfig, DecimalConfig, UserSpecificConfig {

	CHANCE("Overworld Meteor Rarity", 72000), //one meteor every hour
	ENDCHANCE("End Meteor Rarity", 72000), //one meteor every hour
	FORESTCHANCE("Twilight Forest Meteor Rarity", 54000), //one meteor every 45 min
	OTHERCHANCE("Meteor Rarity in Other Dimensions", 36000), //one meteor every 30 min
	ORE("Meteor Ore Density", 40), //default 40% ore
	MINY("Minimum Impactable Y", -1),
	DIM0BURST("Always Skyburst in Overworld", false),
	NETHERBURST("Always Skyburst in Nether", false),
	ENDBURST("Always Skyburst in End", false),
	DIM7BURST("Always Skyburst in Twilight Forest", false),
	OTHER("Always Skyburst in Other Dimensions", false),
	SHOWER("Allow Meteor Showers", true),
	EXPLODE("Global Airburst Override", false),
	OLDGEN("Generate Ancient Meteors", true),
	LOADCHANCE("Percent of Meteors With Ore", 100),
	NOGUNBURST("Defence Gun Deletes Meteors Instead of Skyburst", false),
	VOLUME("Meteor Sound Volume", 1F);

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

	private MeteorOptions(String l, float d) {
		label = l;
		defaultFloat = d;
		type = float.class;
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

	public Class getPropertyType() {
		return type;
	}

	public String getLabel() {
		return label;
	}

	public boolean getState() {
		return (Boolean)MeteorCraft.config.getControl(this.ordinal());
	}

	public int getValue() {
		return (Integer)MeteorCraft.config.getControl(this.ordinal());
	}

	public float getFloat() {
		return (Float)MeteorCraft.config.getControl(this.ordinal());
	}

	public boolean isDummiedOut() {
		return type == null;
	}

	@Override
	public boolean getDefaultState() {
		return defaultState;
	}

	@Override
	public int getDefaultValue() {
		return defaultValue;
	}

	@Override
	public float getDefaultFloat() {
		return defaultFloat;
	}

	@Override
	public boolean isEnforcingDefaults() {
		return false;
	}

	@Override
	public boolean shouldLoad() {
		return true;
	}

	@Override
	public boolean isUserSpecific() {
		switch(this) {
			case VOLUME:
				return true;
			default:
				return false;
		}
	}

}
