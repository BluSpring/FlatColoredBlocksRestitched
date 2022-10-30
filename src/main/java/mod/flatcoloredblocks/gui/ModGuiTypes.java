package mod.flatcoloredblocks.gui;

import mod.flatcoloredblocks.FlatColoredBlocks;
import mod.flatcoloredblocks.craftingitem.ContainerColoredBlockCrafter;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;

import java.lang.reflect.Constructor;

/**
 * Registry of Guis
 */
@SuppressWarnings( "unused" )
public enum ModGuiTypes
{

	colored_crafter( ContainerColoredBlockCrafter.class );

	private final Class<? extends AbstractContainerMenu> container;
	private final Class<?> gui;

	public final Constructor<?> container_construtor;
	public final Constructor<?> gui_construtor;

	private ModGuiTypes(
			final Class<? extends AbstractContainerMenu> c )
	{
		try
		{
			container = c;
			container_construtor = container.getConstructor( Player.class, Level.class, int.class, int.class, int.class );
		}
		catch ( final Exception e )
		{
			throw new RuntimeException( e );
		}

		// by default...
		Class<?> g = null;
		Constructor<?> g_construtor = null;

		// attempt to get gui class/constructor...
		try
		{
			g = (Class<?>) container.getMethod( "getGuiClass" ).invoke( null );
			g_construtor = g.getConstructor( Player.class, Level.class, int.class, int.class, int.class );
		}
		catch ( final Exception e )
		{
			// if and only if we are on the client should this be considered an
			// error...
			if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT)
			{
				throw new RuntimeException( e );
			}

		}

		gui = g;
		gui_construtor = g_construtor;

	}

	public ResourceLocation getID()
	{
		return new ResourceLocation( FlatColoredBlocks.MODID, toString() );
	}

	public AbstractContainerMenu create(
			Player player,
			Level worldIn,
			int x,
			int y,
			int z )
	{
		final ModGuiTypes self = this;

		return ModGuiRouter.createContainer(ModGuiTypes.colored_crafter, player, worldIn, 0, 0, 0);
	}
}
