/*******************************************************************************
 * @author Reika Kalseki
 * 
 * Copyright 2014
 * 
 * All rights reserved.
 * Distribution of the software in any form is only allowed with
 * explicit, prior permission from the owner.
 ******************************************************************************/
package Reika.MeteorCraft.Blocks;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;

public class ItemBlockMeteorMachine extends ItemBlock {

	public ItemBlockMeteorMachine(Block b) {
		super(b);
		hasSubtypes = true;
	}

	@Override
	public void getSubItems(Item id, CreativeTabs tab, List li) {
		for (int i = 0; i < 4; i++) {
			li.add(new ItemStack(id, 1, i));
		}
	}

	@Override
	public int getMetadata(int par1) {
		return par1;
	}

	@Override
	public String getItemStackDisplayName(ItemStack is) {
		if (is.getItemDamage() < 3)
			return "Meteor Defence Gun (Tier "+is.getItemDamage()+")";
		if (is.getItemDamage() == 3)
			return "Meteor Radar";
		return "Unnamed Meteor Machine";
	}

}
