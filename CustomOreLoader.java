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

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.codec.Charsets;

import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

import Reika.DragonAPI.Exception.InstallationException;
import Reika.DragonAPI.Exception.RegistrationException;
import Reika.DragonAPI.Exception.UserErrorException;
import Reika.DragonAPI.IO.ReikaFileReader;
import Reika.DragonAPI.Instantiable.Data.Immutable.BlockKey;
import Reika.DragonAPI.Instantiable.IO.LuaBlock;
import Reika.DragonAPI.Instantiable.IO.LuaBlock.LuaBlockDatabase;
import Reika.DragonAPI.Libraries.Java.ReikaJavaLibrary;
import Reika.DragonAPI.Libraries.Registry.ReikaItemHelper;
import Reika.MeteorCraft.MeteorGenerator.MeteorType;

public class CustomOreLoader {

	public static final CustomOreLoader instance = new CustomOreLoader();

	private final ArrayList<CustomOreEntry> data = new ArrayList();

	private final LuaBlockDatabase oreData = new LuaBlockDatabase();

	private CustomOreLoader() {

	}

	public static class CustomOreEntry {

		public final String displayName;
		public final double baseWeight;

		private final EnumSet<MeteorType> types = EnumSet.noneOf(MeteorType.class);

		private final HashMap<BlockKey, Double> oreItems = new HashMap();

		private CustomOreEntry(String name, double weight) {
			displayName = name;
			baseWeight = weight;
		}

		public Map<BlockKey, Double> getItems() {
			return Collections.unmodifiableMap(oreItems);
		}

		public Collection<MeteorType> getMeteorTypes() {
			return Collections.unmodifiableCollection(types);
		}

		@Override
		public String toString() {
			return displayName+" with weight "+baseWeight+"; "+oreItems.size()+" ores found";
		}
	}

	public final String getSaveFileName() {
		return "MeteorCraft_CustomOres.lua";
	}

	public final File getFullSavePath() {
		return new File(MeteorCraft.config.getConfigFolder(), this.getSaveFileName());
	}

	public void loadFile() {
		oreData.clear();
		MeteorCraft.logger.log("Loading custom ore config.");
		File f = this.getFullSavePath();
		if (f.exists()) {
			try {
				oreData.loadFromFile(f);
				LuaBlock root = oreData.getRootBlock();
				for (LuaBlock b : root.getChildren()) {
					try {
						String type = b.getString("type");
						if (type.equalsIgnoreCase("example"))
							continue;
						MeteorCraft.logger.log("Parsing meteor ore entry '"+type+"'");
						oreData.addBlock(type, b);
						data.add(this.parseOreEntry(type, b));
					}
					catch (Exception e) {
						MeteorCraft.logger.logError("Could not parse config section "+b.getString("type")+": ");
						ReikaJavaLibrary.pConsole(b);
						ReikaJavaLibrary.pConsole("----------------------Cause------------------------");
						e.printStackTrace();
					}
				}
			}
			catch (Exception e) {
				if (e instanceof UserErrorException)
					throw new InstallationException(MeteorCraft.instance, "Configs could not be loaded! Correct them and try again.", e);
				else
					throw new RegistrationException(MeteorCraft.instance, "Configs could not be loaded! Correct them and try again.", e);
			}

			MeteorCraft.logger.log("Configs loaded.");
		}
		else {
			oreData.defaultBlockType = MeteorOreLuaBlock.class;
			String id = "example";
			MeteorOreLuaBlock bk = new MeteorOreLuaBlock(id, null, oreData);
			bk.putData("type", id);
			bk.putData("baseWeight", 40);

			MeteorOreLuaBlock types = new MeteorOreLuaBlock("meteorTypes", bk, oreData);
			bk.setComment(types.name, "Which meteor types this ore can spawn in. This example lists all four options.");
			for (MeteorType type : MeteorType.list) {
				types.addListData(type.name());
			}

			MeteorOreLuaBlock items = new MeteorOreLuaBlock("oreBlocks", bk, oreData);
			Object[] defaults = new Object[] {"minecraft:wood_planks", 1F, "ore:oreCopper", 2.5F, "ore:blockSteel", 0.125F};
			for (int i = 0; i < defaults.length; i += 2) {
				MeteorOreLuaBlock item = new MeteorOreLuaBlock("{", items, oreData);
				item.putData("item", (String)defaults[i]);
				float wt = (float)defaults[i+1];
				if (wt != 1)
					item.putData("relativeWeight", wt);
				if (i == 0)
					item.setComment("item", "The item type, either a namespaced item registry name, or an OreDictionary tag, prefaced with 'ore:'.");
			}
			items.setOrdering(null);
			bk.setOrdering(null);
			oreData.addBlock(id, bk);
			this.createOreFile(f, bk);
		}
	}

	private CustomOreEntry parseOreEntry(String type, LuaBlock b) throws NumberFormatException, IllegalArgumentException, IllegalStateException {
		CustomOreEntry e = new CustomOreEntry(type, b.getDouble("baseWeight"));
		LuaBlock types = b.getChild("meteorTypes");
		if (types == null)
			throw new IllegalArgumentException("No meteor type definitions!");
		if (types.isEmpty())
			throw new IllegalArgumentException("No meteor types specified!");
		for (String s : types.getDataValues()) {
			e.types.add(MeteorType.valueOf(s.toUpperCase(Locale.ENGLISH)));
		}
		LuaBlock ores = b.getChild("oreBlocks");
		if (ores == null)
			throw new IllegalArgumentException("No ore block definitions!");
		if (ores.isEmpty())
			throw new IllegalArgumentException("No ore blocks specified!");
		for (LuaBlock item : ores.getChildren()) {
			double wt = 1;
			if (item.containsKey("relativeWeight"))
				wt = item.getDouble("relativeWeight");
			String val = item.getString("item");
			if (val.startsWith("ore:")) {
				for (ItemStack is : OreDictionary.getOres(val.substring(4))) {
					e.oreItems.put(BlockKey.fromItem(is), wt*e.baseWeight);
				}
			}
			else {
				ItemStack is = ReikaItemHelper.lookupItem(val);
				if (is == null || is.getItem() == null)
					throw new IllegalArgumentException("Item does not exist!");
				if (Block.getBlockFromItem(is.getItem()) == null)
					throw new IllegalArgumentException("Item is not a block!");
				e.oreItems.put(BlockKey.fromItem(is), wt*e.baseWeight);
			}
		}

		if (e.oreItems.isEmpty())
			throw new IllegalArgumentException("No ore blocks found!");

		return e;
	}

	private static class MeteorOreLuaBlock extends LuaBlock {

		protected MeteorOreLuaBlock(String n, LuaBlock parent, LuaBlockDatabase db) {
			super(n, parent, db);
			//requiredElements.add("baseWeight"); //allow inherit
			//requiredElements.add("meteorTypes");
			//requiredElements.add("oreBlocks");
		}

	}

	private boolean createOreFile(File f, LuaBlock example) {
		ArrayList<String> p = new ArrayList();
		this.writeCommentLine(p, "-------------------------------");
		this.writeCommentLine(p, " MeteorCraft Custom Ore Loader ");
		this.writeCommentLine(p, "-------------------------------");
		this.writeCommentLine(p, "");
		this.writeCommentLine(p, "Use this file to add custom ores to meteor generation.");
		this.writeCommentLine(p, "Consult the example entry below, or the MeteorCraft page on the site for detailed documentation of the format.");
		this.writeCommentLine(p, "");
		this.writeCommentLine(p, "Incorrectly formatted entries will be ignored and will log an error in the console.");
		this.writeCommentLine(p, "Lines beginning with '--' are comments and will be ignored, as will empty lines.");
		this.writeCommentLine(p, "");
		this.writeCommentLine(p, "NOTE WELL: It is your responsibility to choose the spawning blocks appropriately.");
		this.writeCommentLine(p, "\tWhile you can theoretically spawn anything from the Ore Dictionary in meteors,");
		this.writeCommentLine(p, "\tnull or missing blocks, non-blocks and blocks with TileEntities are very likely");
		this.writeCommentLine(p, "\tto crash and corrupt the world. No support will be provided in this case.");
		this.writeCommentLine(p, "====================================================================================");
		p.addAll(example.writeToStrings());
		return ReikaFileReader.writeLinesToFile(f, p, true, Charsets.UTF_8);
	}

	private static void writeCommentLine(ArrayList<String> li, String line) {
		li.add("-- "+line);
	}

	public List<CustomOreEntry> getEntries() {
		return Collections.unmodifiableList(data);
	}

}
