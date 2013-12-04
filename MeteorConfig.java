/*******************************************************************************
 * @author Reika Kalseki
 * 
 * Copyright 2013
 * 
 * All rights reserved.
 * Distribution of the software in any form is only allowed with
 * explicit, prior permission from the owner.
 ******************************************************************************/
package Reika.MeteorCraft;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

import Reika.DragonAPI.Base.DragonAPIMod;
import Reika.DragonAPI.Instantiable.IO.ControlledConfig;
import Reika.DragonAPI.Interfaces.ConfigList;
import Reika.DragonAPI.Interfaces.IDRegistry;
import Reika.DragonAPI.Libraries.Java.ReikaJavaLibrary;
import Reika.DragonAPI.ModRegistry.ModOreList;
import Reika.MeteorCraft.Registry.MeteorOptions;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;

public class MeteorConfig extends ControlledConfig {

	public MeteorConfig(DragonAPIMod mod, ConfigList[] option, IDRegistry[] blocks, IDRegistry[] items, IDRegistry[] id, int cfg) {
		super(mod, option, blocks, items, id, cfg);
	}

	private static final ArrayList<String> modOres = ReikaJavaLibrary.getEnumEntriesWithoutInitializing(ModOreList.class);
	private static final int oreLength = modOres.size();
	private boolean[] ores = new boolean[oreLength];

	//Initialization of the config
	@Override
	public void initProps(FMLPreInitializationEvent event) {

		super.initProps(event);

		config.load();
		for (int i = 0; i < oreLength; i++) {
			String name = modOres.get(i);
			ores[i] = config.get("Generate Ores", name, true).getBoolean(true);
		}

		/*******************************/
		//save the data
		config.save();
	}

	@Override
	protected void resetConfigFile() {
		String path = this.getConfigPath()+"_Old_Config_Backup.txt";
		File backup = new File(path);
		if (backup.exists())
			backup.delete();
		try {
			ReikaJavaLibrary.pConsole(configMod.getDisplayName().toUpperCase()+": Writing Backup File to "+path);
			ReikaJavaLibrary.pConsole(configMod.getDisplayName().toUpperCase()+": Use this to restore custom IDs if necessary.");
			backup.createNewFile();
			if (!backup.exists())
				ReikaJavaLibrary.pConsole(configMod.getDisplayName().toUpperCase()+": Could not create backup file at "+path+"!");
			else {
				PrintWriter p = new PrintWriter(backup);
				p.println("#####----------THESE ARE ALL THE OLD CONFIG SETTINGS YOU WERE USING----------#####");
				p.println("#####---IF THEY DIFFER FROM THE DEFAULTS, YOU MUST RE-EDIT THE CONFIG FILE---#####");
				for (int i = 0; i < MeteorOptions.optionList.length; i++) {
					String label = MeteorOptions.optionList[i].getLabel();
					if (MeteorOptions.optionList[i].isBoolean())
						controls[i] = MeteorOptions.optionList[i].setState(config);
					if (MeteorOptions.optionList[i].isNumeric())
						controls[i] = MeteorOptions.optionList[i].setValue(config);
					if (MeteorOptions.optionList[i].isDecimal())
						controls[i] = MeteorOptions.optionList[i].setDecimal(config);
					p.println(label+": "+String.valueOf(controls[i]));
				}

				for (int i = 0; i < oreLength; i++) {
					String name = modOres.get(i);
					ores[i] = config.get("Generate Ores", name, true).getBoolean(true);
				}

				p.close();
			}
		}
		catch (IOException e) {
			ReikaJavaLibrary.pConsole(configMod.getDisplayName().toUpperCase()+": Could not create backup file due to IOException!");
			e.printStackTrace();
		}
		configFile.delete();
	}

	public boolean shouldGenerateOre(ModOreList ore) {
		return ores[ore.ordinal()];
	}
}
