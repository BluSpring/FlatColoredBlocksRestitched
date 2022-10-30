package mod.flatcoloredblocks.craftingitem;

import mod.flatcoloredblocks.ModUtil;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

class ItemCraftingSource
{
	private final Container inv;
	private final int slot;

	private int used = 0;
	boolean simulate = false;

	public ItemCraftingSource(
			final Inventory ip,
			final int x )
	{
		inv = ip;
		slot = x;
	}

	@Override
	public int hashCode()
	{
		return inv.hashCode() ^ slot;
	}

	@Override
	public boolean equals(
			final Object obj )
	{
		final ItemCraftingSource s = (ItemCraftingSource) obj;
		return inv == s.inv && slot == s.slot;
	}

	public void consume(
			final int i )
	{
		if ( simulate )
		{
			++used;
		}
		else
		{
			inv.removeItem( slot, i );
		}
	}

	public ItemStack getStack()
	{
		if ( simulate )
		{
			ItemStack is = inv.getItem( slot );
			if ( is != null )
			{
				is = is.copy();
				ModUtil.alterStack( is, -used );
			}
			return is;
		}

		return inv.getItem( slot );
	}

}