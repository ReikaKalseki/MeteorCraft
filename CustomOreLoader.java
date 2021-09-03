/*******************************************************************************
 * @author Reika Kalseki
 *
 * Copyright 2017
 *
 * All rights reserved.
 * Distribution of the software in any form is only allowed with
 * explicit, prior permission from the owner.
 ******************************************************************************/
package Reika.MeteorCraft;

import java.io.BufferedReader;
import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.codec.Charsets;

import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

import Reika.DragonAPI.IO.ReikaFileReader;
import Reika.DragonAPI.Instantiable.Data.Immutable.BlockKey;
import Reika.DragonAPI.Libraries.Java.ReikaStringParser;

public class CustomOreLoader {

	public static final CustomOreLoader instance = new CustomOreLoader();

	private final ArrayList<CustomOreEntry> data = new ArrayList();

	private CustomOreLoader() {

	}

	public static class CustomOreEntry {

		public final String displayName;
		public final int spawnWeight;
		public final int size;
		private final ArrayList<BlockKey> oreItems = new ArrayList();
		private final ArrayList<String> oreNames = new ArrayList();
		public final int meteorType;

		private CustomOreEntry(String name, int weight, int type, String... ores) {
			displayName = name;
			spawnWeight = weight;
			for (int i = 0; i < ores.length; i++) {
				String s = ores[i];
				oreNames.add(s);
				for (ItemStack is : OreDictionary.getOres(s)) {
					oreItems.add(BlockKey.fromItem(is));
				}
				if (oreItems.isEmpty())
					throw new IllegalStateException("Cannot have entries with no corresponding ores!");
			}
			size = oreItems.size();
			meteorType = type;
		}

		public List<BlockKey> getItems() {
			return Collections.unmodifiableList(oreItems);
		}

		@Override
		public String toString() {
			return displayName+" with weight "+spawnWeight+" ("+size+" ores found from "+oreNames+")";
		}
	}

	public final String getSaveFileName() {
		return "MeteorCraft_CustomOres.cfg";
	}

	public final File getFullSavePath() {
		return new File(MeteorCraft.config.getConfigFolder(), this.getSaveFileName());
	}

	public void loadFile() {
		MeteorCraft.logger.log("Loading custom ore config.");
		File f = this.getFullSavePath();
		if (!f.exists())
			if (!this.createOreFile(f))
				return;
		try(BufferedReader p = ReikaFileReader.getReader(f, Charsets.UTF_8)) {
			String line = "";
			while (line != null) {
				line = p.readLine();
				if (line != null && !line.isEmpty() && !line.startsWith("//")) {
					try {
						CustomOreEntry entry = this.parseString(line);
						if (entry != null) {
							data.add(entry);
							MeteorCraft.logger.log("Added ore entry "+entry);
						}
						else {
							MeteorCraft.logger.logError("Malformed custom ore entry: "+line);
						}
					}
					catch (Exception e) {
						MeteorCraft.logger.logError("Malformed custom ore entry ["+e.getLocalizedMessage()+"]: '"+line+"'");
						e.printStackTrace();
					}
				}
			}
		}
		catch (Exception e) {
			MeteorCraft.logger.log(e.getMessage()+", and it caused the read to fail!");
			e.printStackTrace();
		}
	}

	private boolean createOreFile(File f) {
		try {
			f.createNewFile();
			PrintWriter p = new PrintWriter(f);
			this.writeCommentLine(p, "-------------------------------");
			this.writeCommentLine(p, " MeteorCraft Custom Ore Loader ");
			this.writeCommentLine(p, "-------------------------------");
			this.writeCommentLine(p, "");
			this.writeCommentLine(p, "Use this file to add custom ores to meteor generation.");
			this.writeCommentLine(p, "Specify one per line, and format them as 'Name, Spawn Weight, Meteor Type, OreDictionary Name(s)'");
			this.writeCommentLine(p, "Meteor Types:");
			this.writeCommentLine(p, "\t0 - Stone (Overworld Ores)");
			this.writeCommentLine(p, "\t1 - Netherrack (Nether Ores)");
			this.writeCommentLine(p, "\t2 - End Stone (End Ores)");
			this.writeCommentLine(p, "");
			this.writeCommentLine(p, "Sample Lines:");
			this.writeCommentLine(p, "\tSample Ore 1, 2, 0, oreSample");
			this.writeCommentLine(p, "\tSample Ore 2, 2, 2, oreNotSample, oreSecondName, oreHasLotsOfVariants");
			this.writeCommentLine(p, "");
			this.writeCommentLine(p, "Entries missing names or spawn weights, or having less than one Ore Dictionary name, are incorrect.");
			this.writeCommentLine(p, "Incorrectly formatted lines will be ignored and will log an error in the console.");
			this.writeCommentLine(p, "Lines beginning with '//' are comments and will be ignored, as will empty lines. Spaces are stripped.");
			this.writeCommentLine(p, "");
			this.writeCommentLine(p, "NOTE WELL: It is your responsibility to choose the spawning blocks appropriately.");
			this.writeCommentLine(p, "\tWhile you can theoretically spawn anything from the Ore Dictionary in meteors,");
			this.writeCommentLine(p, "\tnull or missing blocks, non-blocks and blocks with TileEntities are very likely");
			this.writeCommentLine(p, "\tto crash and corrupt the world. No support will be provided in this case.");
			this.writeCommentLine(p, "====================================================================================");
			p.append("\n");
			p.close();
			return true;
		}
		catch (Exception e) {
			MeteorCraft.logger.logError("Could not generate CustomOre Config.");
			e.printStackTrace();
			return false;
		}
	}

	private static void writeCommentLine(PrintWriter p, String line) {
		p.append("// "+line+"\n");
	}

	private CustomOreEntry parseString(String s) throws Exception {
		String[] parts = s.split(",");
		for (int i = 1; i < parts.length; i++)
			parts[i] = ReikaStringParser.stripSpaces(parts[i]);
		if (parts.length < 4)
			throw new IllegalArgumentException("Invalid parameter count.");
		String name = parts[0];
		if (name.isEmpty())
			throw new IllegalArgumentException("Empty name is invalid.");
		int weight = Integer.parseInt(parts[1]);
		if (weight < 0)
			throw new IllegalArgumentException("Negative weights are not permitted.");
		int type = Integer.parseInt(parts[2]);
		if (type < 0 || type > 2)
			throw new IllegalArgumentException("Invalid meteor type.");
		String[] ores = new String[parts.length-3];
		System.arraycopy(parts, 3, ores, 0, ores.length);
		return new CustomOreEntry(name, weight, type, ores);
	}

	public List<CustomOreEntry> getEntries() {
		return Collections.unmodifiableList(data);
	}

}
