package mod.flatcoloredblocks.craftingitem;

import mod.flatcoloredblocks.ModUtil;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class SlotColoredBlockCrafter extends Slot
{

	InventoryColoredBlockCrafter secondInv;

	public SlotColoredBlockCrafter(
			final Container inv,
			final InventoryColoredBlockCrafter secondInv,
			final int index,
			final int x,
			final int y )
	{
		super( inv, index, x, y );
		this.secondInv = secondInv;
	}

	@Override
	public boolean mayPlace(
			final ItemStack stack )
	{
		return false;
	}

	@Override
	public boolean mayPickup(
			final Player playerIn )
	{
		return !ModUtil.isEmpty( secondInv.craftItem( getItem(), 1, true ) );
	}

	@Override
	public void onTake(
			Player thePlayer,
			ItemStack stack )
	{
		secondInv.craftItem( stack, 1, false );
	}

}
