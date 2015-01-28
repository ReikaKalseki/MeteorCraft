package Reika.MeteorCraft.Blocks;

import java.util.Collection;

import net.minecraft.entity.item.EntityFallingBlock;
import net.minecraft.world.World;
import Reika.DragonAPI.Libraries.Java.ReikaJavaLibrary;
import Reika.DragonAPI.Libraries.MathSci.ReikaMathLibrary;
import Reika.MeteorCraft.Event.MeteorCraftEvent;
import Reika.MeteorCraft.Event.MeteorCraftEvent.AirburstEvent;

public class TileEntityMeteorMagnet extends TileEntityMeteorBase {

	@Override
	protected void onEvent(MeteorCraftEvent ev) {
		if (this.canPerformActions() && ev instanceof AirburstEvent) {
			Collection<EntityFallingBlock> c = ((AirburstEvent)ev).getBlocks();
			ReikaJavaLibrary.pConsole(c.size()+": "+c);
			for (EntityFallingBlock e : c) {
				double dx = e.posX-xCoord-0.5;
				double dy = e.posY-yCoord-0.5;
				double dz = e.posZ-zCoord-0.5;
				if (ReikaMathLibrary.py3d(dx, 0, dz) <= this.getRange()) {
					double dd = ReikaMathLibrary.py3d(dx, dy, dz);
					double v = 2;
					e.motionX *= 0.5;
					e.motionZ *= 0.5;
					e.motionX += -dx*v/dd;
					e.motionY += 0.25-dy*v/dd;
					e.motionZ += -dz*v/dd;
					if (!ev.world.isRemote)
						e.velocityChanged = true;
				}
			}
		}
	}

	private double getRange() {
		return 128;
	}

	@Override
	public long getMinPower() {
		return 1048576; //1MW
	}

	@Override
	public void updateEntity(World world, int x, int y, int z, int meta) {

	}

	@Override
	protected String getTEName() {
		return "Meteor Fragment Magnet";
	}

}
