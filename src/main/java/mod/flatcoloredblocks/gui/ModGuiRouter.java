package mod.flatcoloredblocks.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;

/**
 * Client / Server Gui + Container Handler
 */
public class ModGuiRouter
{
	public static AbstractContainerMenu createContainer(
			ModGuiTypes type,
			final Player player,
			final Level world,
			final int x,
			final int y,
			final int z )
	{
		try
		{
			return (AbstractContainerMenu) type.container_construtor.newInstance( player, world, x, y, z );
		}
		catch ( final Exception e )
		{
			throw new RuntimeException( e );
		}
	}

	public Screen apply(ResourceLocation id)
	{
		try
		{
			final ModGuiTypes guiType = ModGuiTypes.valueOf( id.getPath() );
			return (Screen) guiType.gui_construtor.newInstance( Minecraft.getInstance().player, Minecraft.getInstance().player.level, 0, 0, 0 );
		}
		catch ( final Exception e )
		{
			throw new RuntimeException( e );
		}
	}
}
