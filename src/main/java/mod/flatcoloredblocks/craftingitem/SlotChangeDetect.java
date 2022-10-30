package mod.flatcoloredblocks.craftingitem;

import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;

import java.io.IOException;

public class SlotChangeDetect extends Slot
{

	InventoryColoredBlockCrafter secondInv;

	public SlotChangeDetect(
			final Container inv,
			final InventoryColoredBlockCrafter secondInv,
			final int index,
			final int xPosition,
			final int yPosition )
	{
		super( inv, index, xPosition, yPosition );
		this.secondInv = secondInv;
	}

	@Override
	public void setChanged()
	{
		super.setChanged();
		try {
			secondInv.updateContents();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

}
