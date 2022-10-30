package mod.flatcoloredblocks.craftingitem;

import mod.flatcoloredblocks.FlatColoredBlocks;
import mod.flatcoloredblocks.ModUtil;
import mod.flatcoloredblocks.network.NetworkRouter;
import mod.flatcoloredblocks.network.packets.ScrolingGuiPacket;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

/**
 * Container for for crafting item's gui, manages scroll communication slots,
 * and shift clicking.
 */
public class ContainerColoredBlockCrafter extends AbstractContainerMenu implements MenuProvider
{

	final Player thePlayer;
	final InventoryColoredBlockCrafter craftinginv;

	public ContainerColoredBlockCrafter(
			final int id,
			final Player player,
			final Level world,
			final int x,
			final int y,
			final int z )
	{
		super(FlatColoredBlocks.instance.containerType, id);
		thePlayer = player;
		craftinginv = new InventoryColoredBlockCrafter( thePlayer, this );
		try {
			craftinginv.updateContents();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		final Inventory playerInventory = player.getInventory();
		final int i = ( 7 - 4 ) * 18;

		for ( int j = 0; j < 7; ++j )
		{
			for ( int k = 0; k < 9; ++k )
			{
				addSlot( new SlotColoredBlockCrafter( craftinginv, craftinginv, k + j * 9, 8 + k * 18, 18 + j * 18 ) );
			}
		}

		for ( int l = 0; l < 3; ++l )
		{
			for ( int j1 = 0; j1 < 9; ++j1 )
			{
				addSlot( new SlotChangeDetect( playerInventory, craftinginv, j1 + l * 9 + 9, 8 + j1 * 18, 104 + l * 18 + i ) );
			}
		}

		for ( int i1 = 0; i1 < 9; ++i1 )
		{
			addSlot( new SlotChangeDetect( playerInventory, craftinginv, i1, 8 + i1 * 18, 162 + i ) );
		}
	}

	@Override
	public boolean stillValid(
			final Player playerIn )
	{
		return true;
	}

	@Override
	public ItemStack quickMoveStack(
			final Player playerIn,
			final int index )
	{
		int emptySlots = 0;

		for ( final Slot s : slots )
		{
			if ( !( s instanceof SlotColoredBlockCrafter ) )
			{
				if ( !s.hasItem() )
				{
					emptySlots++;
				}
			}
		}

		if ( emptySlots > 0 )
		{
			final Slot s = slots.get( index );
			if ( s instanceof SlotColoredBlockCrafter )
			{
				final ItemStack which = s.getItem();
				final ItemStack out = craftinginv.craftItem( which, 64, false );

				moveItemStackTo( out, 7 * 9, slots.size(), true );
			}
		}

		return ModUtil.getEmptyStack();
	}

	float scrollPercent = 0;
	float originalScroll = 0;

	public void setScroll(
			final float currentScroll ) throws IOException {
		scrollPercent = currentScroll;

		final int rowsOfScrolling = Math.max( ( craftinginv.getContainerSize() + 8 ) / 9 - 7, 0 );
		craftinginv.offset = Math.round( rowsOfScrolling * currentScroll ) * 9;

		if ( Math.abs( originalScroll - currentScroll ) > 0.00001 )
		{
			if ( thePlayer.level.isClientSide )
			{
				final ScrolingGuiPacket sgp = new ScrolingGuiPacket(PacketByteBufs.empty());
				originalScroll = sgp.scroll = scrollPercent;

				// send...
				NetworkRouter.instance.sendToServer( sgp );
			}
		}
	}

	public int getItemCount()
	{
		return craftinginv.getContainerSize();
	}

	@Environment(EnvType.CLIENT)
	public static Object getGuiClass()
	{
		return GuiColoredBlockCrafter.class;
	}

	@Override
	public Component getDisplayName() {
		return null;
	}

	@Nullable
	@Override
	public AbstractContainerMenu createMenu(int i, Inventory inventory, Player player) {
		return this;
	}
}
