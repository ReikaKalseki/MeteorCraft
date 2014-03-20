package Reika.MeteorCraft;

import java.util.Random;

import net.minecraft.world.World;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.chunk.IChunkProvider;
import Reika.DragonAPI.ModInteract.ReikaTwilightHelper;
import Reika.MeteorCraft.MeteorGenerator.MeteorType;
import Reika.MeteorCraft.Registry.MeteorOptions;
import cpw.mods.fml.common.IWorldGenerator;

public class OldMeteorGenerator implements IWorldGenerator {

	@Override
	public void generate(Random random, int chunkX, int chunkZ, World world, IChunkProvider chunkGenerator, IChunkProvider chunkProvider) {
		if (this.canGen(world)) {
			if (random.nextInt(200) == 0)
				this.generate(random, world, chunkX*16, chunkZ*16);
		}
	}

	private boolean canGen(World world) {
		if (!MeteorOptions.OLDGEN.getState())
			return false;
		int id = world.provider.dimensionId;
		if (id == -1 || id == 1 || world.provider.hasNoSky)
			return false;
		if (world.getWorldInfo().getTerrainType() == WorldType.FLAT)
			return false;
		return true;
	}

	private void generate(Random rand, World world, int chunkX, int chunkZ) {
		int x = chunkX+rand.nextInt(16);
		int z = chunkZ+rand.nextInt(16);
		int y = this.getGenY(world, x, z, rand);
		MeteorGenerator.instance.generate(world, x, y, z, this.getRandomType(rand));
	}

	private MeteorType getRandomType(Random rand) {
		if (rand.nextInt(10) == 0)
			return MeteorType.END;
		else if (rand.nextInt(5) == 0)
			return MeteorType.NETHERRACK;
		else
			return MeteorType.STONE;
	}

	private int getGenY(World world, int x, int z, Random rand) {
		if (world.provider.dimensionId == ReikaTwilightHelper.getDimensionID())
			return 5+rand.nextInt(30);
		BiomeGenBase biome = world.getBiomeGenForCoords(x, z);
		int max = (int)(biome.maxHeight*world.provider.getAverageGroundLevel());
		int y = 5+rand.nextInt(max);
		while (world.canBlockSeeTheSky(x, y+1, z)) {
			y = 5+rand.nextInt(max);
		}
		return y;
	}

}
