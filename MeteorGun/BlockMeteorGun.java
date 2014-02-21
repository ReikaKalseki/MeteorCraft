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

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Icon;
import net.minecraft.world.World;
import Reika.DragonAPI.ModList;

public class BlockMeteorGun extends Block {

	private final Icon[] topIcon = new Icon[3];
	private final Icon[] sideIcon = new Icon[3];
	private final Icon[] bottomIcon = new Icon[3];

	public BlockMeteorGun(int par1) {
		super(par1, Material.iron);
		this.setCreativeTab(CreativeTabs.tabMisc);
	}

	@Override
	public boolean hasTileEntity(int meta) {
		return meta < 3;
	}

	@Override
	public TileEntity createTileEntity(World world, int meta) {
		return new TileEntityMeteorGun();
	}

	@Override
	public Icon getIcon(int s, int meta) {
		return s == 0 ? bottomIcon[meta] : s == 1 ? topIcon[meta] : sideIcon[meta];
	}

	@Override
	public void registerIcons(IconRegister ico) {
		String s = ModList.ROTARYCRAFT.isLoaded() ? "_rc" : "";
		for (int i = 0; i < 3; i++) {
			topIcon[i] = ico.registerIcon("meteorcraft:gun_"+i+"_top"+s);
			sideIcon[i] = ico.registerIcon("meteorcraft:gun_"+i+"_side"+s);
			bottomIcon[i] = ico.registerIcon("meteorcraft:gun_"+i+"_bottom"+s);
		}
	}

}
