package mod.flatcoloredblocks;

import mod.flatcoloredblocks.block.BlockFlatColored;
import net.minecraft.locale.Language;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

@SuppressWarnings( "deprecation" )
public class ModUtil
{

	public static void alterStack(
			final ItemStack stack,
			final int deltaStackSize )
	{
		setStackSize( stack, getStackSize( stack ) + deltaStackSize );
	}

	public static void setStackSize(
			final ItemStack stack,
			final int stackSize )
	{
		stack.setCount( stackSize );
	}

	public static int getStackSize(
			final ItemStack stack )
	{
		return stack.getCount();
	}

	public static ItemStack getEmptyStack()
	{
		return ItemStack.EMPTY;
	}

	public static String translateToLocal(
			final String string )
	{
		return Language.getInstance().getOrDefault(string);
	}

	public static boolean isEmpty(
			final ItemStack i )
	{
		return i.isEmpty();
	}

	public static BlockState getFlatColoredBlockState(
			BlockFlatColored blk,
			ItemStack stack )
	{
		return blk.getstateForStack( stack );
	}

}
