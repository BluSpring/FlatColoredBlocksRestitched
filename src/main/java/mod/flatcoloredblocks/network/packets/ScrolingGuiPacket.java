package mod.flatcoloredblocks.network.packets;

import mod.flatcoloredblocks.craftingitem.ContainerColoredBlockCrafter;
import mod.flatcoloredblocks.network.ModPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;

import java.io.IOException;

/**
 * Used to synchronize scroll bar on crafting item between server and client.
 * Sent from client to server.
 */
public class ScrolingGuiPacket extends ModPacket
{
	public float scroll = 0;

	public ScrolingGuiPacket(FriendlyByteBuf buf) throws IOException {
		super(buf);
	}

	@Override
	public void server(
			final ServerPlayer player ) throws IOException {
		final AbstractContainerMenu c = player.containerMenu;
		if ( c instanceof ContainerColoredBlockCrafter )
		{
			final ContainerColoredBlockCrafter ccc = (ContainerColoredBlockCrafter) c;
			ccc.setScroll( scroll );
		}
	}

	@Override
	public void getPayload(
			final FriendlyByteBuf buffer )
	{
		buffer.writeFloat( scroll );
	}

	@Override
	public void readPayload(
			final FriendlyByteBuf buffer )
	{
		// no data..
		scroll = buffer.readFloat();
	}

}
