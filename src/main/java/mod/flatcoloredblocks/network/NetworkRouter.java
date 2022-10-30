package mod.flatcoloredblocks.network;

import io.netty.buffer.Unpooled;
import mod.flatcoloredblocks.FlatColoredBlocks;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientLoginNetworking;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.*;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;

import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

/**
 * Sends packets and handles networking.
 */
public class NetworkRouter
{

	/**
	 * Receives packets on the client and dispatches them to the processor.
	 */
	private class ClientPacketHandler
	{

		public void onPacketData(
				final FriendlyByteBuf buffer )
		{
			final ModPacket innerPacket = parsePacket( buffer );

			Minecraft.getInstance().execute(() -> innerPacket.client());
		}

	};

	/**
	 * Receives packets on the server and dispatches them to the processor.
	 */
	private class ServerPacketHandler
	{

		public void onPacketData(
				final FriendlyByteBuf buffer,
				final ServerPlayer playerEntity )
		{
			if ( playerEntity == null )
			{
				return;
			}

			final ModPacket innerPacket = parsePacket( buffer );
			innerPacket.serverEntity = playerEntity;

			playerEntity.getServer().execute(() -> {
				try {
					innerPacket.server( playerEntity );
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			});
		}
	};

	public static NetworkRouter instance;

	private final ResourceLocation channel;

	final ServerPacketHandler serverPacketHandler;
	final ClientPacketHandler clientPacketHandler;

	public NetworkRouter()
	{
		ModPacketTypes.init();
		channel = new ResourceLocation( FlatColoredBlocks.MODID, "basic" );
		var initializeChannel = new ResourceLocation(FlatColoredBlocks.MODID, "initialize");

		var ver = "1.0.0";

		ServerLoginConnectionEvents.QUERY_START.register((listener, server, sender, synchronizer) -> {
			sender.sendPacket(initializeChannel, PacketByteBufs.create().writeUtf(ver));
		});

		var modMetadata = FabricLoader.getInstance().getModContainer("flatcoloredblocksfabric").get().getMetadata();

		ServerLoginNetworking.registerGlobalReceiver(initializeChannel, (server, handler, understood, buf, synchronizer, responseSender) -> {
			if (!understood || !Objects.equals(buf.readUtf(), ver)) {
				handler.disconnect(Component.literal(String.format("Missing {} version {}!", modMetadata.getName(), ver)));
			}
		});

		ClientLoginNetworking.registerGlobalReceiver(initializeChannel, (client, handler, buf, listenerAdder) ->
				CompletableFuture.completedFuture(PacketByteBufs.create().writeUtf(ver))
		);

		clientPacketHandler = new ClientPacketHandler();
		ClientPlayNetworking.registerGlobalReceiver(channel, this::clientPacket);

		serverPacketHandler = new ServerPacketHandler();
		ServerPlayNetworking.registerGlobalReceiver(channel, this::serverPacket);
	}

	public void clientPacket(Minecraft client, ClientPacketListener handler, FriendlyByteBuf buf, PacketSender responseSender)
	{
		try
		{
			if ( clientPacketHandler != null )
			{
				clientPacketHandler.onPacketData( buf );
			}
		}
		catch ( Exception ignored )
		{
		}
	}

	public void serverPacket(
			MinecraftServer server,
			ServerPlayer player, ServerGamePacketListenerImpl handler, FriendlyByteBuf buf, PacketSender responseSender
	)
	{
		// find player
		try
		{
			if ( serverPacketHandler != null )
			{
				serverPacketHandler.onPacketData( buf, player );
			}
		}
		catch ( Exception ignored )
		{
		}
	}

	private ModPacket parsePacket(
			final FriendlyByteBuf buffer )
	{
		final int id = buffer.readByte();

		try
		{
			final ModPacket packet = ModPacketTypes.constructByID( id );
			packet.readPayload( buffer );
			return packet;
		}
		catch ( final InstantiationException e )
		{
			throw new RuntimeException( e );
		}
		catch ( final IllegalAccessException e )
		{
			throw new RuntimeException( e );
		}
	}

	@Environment( EnvType.CLIENT )
	public void sendToServer(
			ModPacket packet )
	{
		Minecraft minecraft = Minecraft.getInstance();
		if ( minecraft != null )
		{
			ClientPacketListener netHandler = minecraft.getConnection();
			if ( netHandler != null )
			{
				int id = ModPacketTypes.getID( packet.getClass() );
				FriendlyByteBuf buffer = PacketByteBufs.copy( Unpooled.buffer() );
				buffer.writeVarInt( id );
				packet.getPayload( buffer );

				ClientPlayNetworking.send(channel, buffer);
			}
		}
	}

	public void sendPacketToClient(
			ModPacket packet,
			ServerPlayer player )
	{
		if ( player != null && player.connection != null )
		{
			int id = ModPacketTypes.getID( packet.getClass() );
			FriendlyByteBuf buffer = PacketByteBufs.copy( Unpooled.buffer() );
			buffer.writeVarInt( id );
			packet.getPayload( buffer );

			ServerPlayNetworking.send(player, channel, buffer);
		}
	}
}