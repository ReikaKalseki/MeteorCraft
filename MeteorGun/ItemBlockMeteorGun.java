/*******************************************************************************
 * @author Reika Kalseki
 * 
 * Copyright 2013
 * 
 * All rights reserved.
 * Distribution of the software in any form is only allowed with
 * explicit, prior permission from the owner.
 ******************************************************************************/
package Reika.MeteorCraft.MeteorGun;

import java.util.List;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;

public class ItemBlockMeteorGun extends ItemBlock {

	public ItemBlockMeteorGun(int par1) {
		super(par1);
		hasSubtypes = true;
	}

	@Override
	public void getSubItems(int id, CreativeTabs tab, List li) {
		for (int i = 0; i < 3; i++) {
			li.add(new ItemStack(id, 1, i));
		}
	}

	@Override
	public int getMetadata(int par1)
	{
		return par1;
	}

}
