/*******************************************************************************
 * @author Reika Kalseki
 * 
 * Copyright 2015
 * 
 * All rights reserved.
 * Distribution of the software in any form is only allowed with
 * explicit, prior permission from the owner.
 ******************************************************************************/
package Reika.MeteorCraft.API;

import java.lang.reflect.Method;

public class MeteorSpawnAPI {

	private static Method blacklist;
	private static Method chance;

	/** Blacklist meteor spawns in a given dimension. */
	public static void blacklistDimension(int dim) {
		try {
			blacklist.invoke(null, dim);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	/** Set meteor spawn chance in a given dimension. Note that chances are inverted; you are effectively supplying n for rand.nextInt(n) == 0. */
	public static void setDimensionChance(int dim, int ch) {
		if (dim <= 0)
			throw new IllegalArgumentException("This chance would cause a crash!");
		try {
			chance.invoke(null, dim, ch);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	static {
		try {
			Class c = Class.forName("Reika.MeteorCraft.MeteorSpawnController");
			blacklist = c.getDeclaredMethod("blacklistDimension", int.class);
			blacklist.setAccessible(true);

			chance = c.getDeclaredMethod("setDimensionChance", int.class, int.class);
			chance.setAccessible(true);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

}
