package mod.flatcoloredblocks.craftingitem;

import com.google.common.base.Stopwatch;
import mod.flatcoloredblocks.FlatColoredBlocks;
import mod.flatcoloredblocks.RegistryHelper;
import mod.flatcoloredblocks.RegistryItem;
import mod.flatcoloredblocks.gui.ModGuiTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class ItemColoredBlockCrafter extends Item implements RegistryItem
{

	public int scrollIndex = -1;
	List<Item> options = new ArrayList<Item>();
	Stopwatch stopWatch;

	public ItemColoredBlockCrafter()
	{
		super( ( new Item.Properties() ).tab( FlatColoredBlocks.instance.creativeTab ) );
		setRegistryName( FlatColoredBlocks.MODID, "coloredcraftingitem" );
	}

	@Override
	public InteractionResultHolder<ItemStack> use(
			final Level worldIn,
			final Player playerIn,
			final InteractionHand hand )
	{
		final ItemStack itemStackIn = playerIn.getItemInHand( hand );

		if ( worldIn.isClientSide )
		{
			return InteractionResultHolder.success(itemStackIn);
		}

		if ( playerIn instanceof ServerPlayer)
		{
			var container = ModGuiTypes.colored_crafter.create(playerIn, worldIn, 0, 0, 0);
			playerIn.openMenu((ContainerColoredBlockCrafter) container);
			//NetworkHooks.openGui( (ServerPlayer) playerIn, ModGuiTypes.colored_crafter.create( playerIn, worldIn, 0, 0, 0 ), null );
		}

		return InteractionResultHolder.success(itemStackIn);
	}

	@Override
	public void appendHoverText(
			ItemStack stack,
			Level worldIn,
			List<Component> tooltip,
			TooltipFlag flagIn )
	{
		if ( scrollIndex == -1 && worldIn != null )
		{
			scrollIndex = 0;
			stopWatch = Stopwatch.createStarted();

			options.clear();
			options.addAll( RegistryHelper.getItemsFromTag(new ResourceLocation( FlatColoredBlocks.instance.config.solidCraftingBlock ) ) );
			options.addAll( RegistryHelper.getItemsFromTag(new ResourceLocation( FlatColoredBlocks.instance.config.glowingCraftingBlock ) ) );
			options.addAll( RegistryHelper.getItemsFromTag(new ResourceLocation( FlatColoredBlocks.instance.config.transparentCraftingBlock ) ) );
		}

		if ( !options.isEmpty() && scrollIndex >= 0 )
		{
			if ( stopWatch.elapsed( TimeUnit.SECONDS ) >= 1.2 )
			{
				scrollIndex = ++scrollIndex % options.size();
				stopWatch = Stopwatch.createStarted();
			}

			Item it = options.get( scrollIndex );
			tooltip.add( Component.translatable( "item.flatcoloredblocks.coloredcraftingitem.tip1", it.getName( it.getDefaultInstance() ) ) );
		}

		tooltip.add( Component.translatable( "item.flatcoloredblocks.coloredcraftingitem.tip2" ) );

		super.appendHoverText( stack, worldIn, tooltip, flagIn );
	}

}
