/*******************************************************************************
 * @author Reika Kalseki
 *
 * Copyright 2017
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

	CHANCE("Overworld Meteor Rarity", 72000), //Roughly how many ticks should pass, on average, between meteors in the overworld
	ENDCHANCE("End Meteor Rarity", 72000), //72000 is approx one hour
	FORESTCHANCE("Twilight Forest Meteor Rarity", 54000),
	OTHERCHANCE("Meteor Rarity in Other Dimensions", 36000),
	ORE("Meteor Ore Density", 40), //What % of a meteor should be ore vs "stone-type" block
	MINY("Minimum Impactable Y", -1), //What Y level will meteors always explode at. This allows to make only the mountain peaks impactable
	DIM0BURST("Always Skyburst in Overworld", false),
	NETHERBURST("Always Skyburst in Nether", false),
	ENDBURST("Always Skyburst in End", false),
	DIM7BURST("Always Skyburst in Twilight Forest", false),
	OTHER("Always Skyburst in Other Dimensions", false),
	SHOWER("Allow Meteor Showers", true), //Meteor showers are rare and short periods of dense meteor activity
	EXPLODE("Global Airburst Override", false), //Set this true to force all meteors to always explode
	OLDGEN("Generate Ancient Meteors", true), //Whether to generate ancient buried meteors
	LOADCHANCE("Percent of Meteors With Ore", 100),
	NOGUNBURST("Defence Gun Deletes Meteors Instead of Skyburst", false),
	VOLUME("Meteor Sound Volume (0.5-2.0)", 1F),
	TYPEHISTORY("Enforce Meteor Type Weights For Small Numbers", false), //Whether to keep a record of meteor types so that the distribution is less pure RNG and more tightly follows the configured rates
	;

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
