package org.koekepan.herobrineproxy.packet.behaviours.server;

import com.github.steveice10.mc.protocol.packet.ingame.client.ClientPluginMessagePacket;
import com.github.steveice10.mc.protocol.packet.ingame.server.ServerPluginMessagePacket;
import com.github.steveice10.packetlib.packet.Packet;
import com.google.common.base.Charsets;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import org.koekepan.herobrineproxy.ConsoleIO;
import org.koekepan.herobrineproxy.behaviour.Behaviour;
import org.koekepan.herobrineproxy.session.IProxySessionNew;
import org.koekepan.performance.LatencyData;;

public class ServerPluginMessagePacketBehaviour implements Behaviour<Packet> {
	private IProxySessionNew proxySession;
	
	@SuppressWarnings("unused")
	private ServerPluginMessagePacketBehaviour() {}
	
	
	public ServerPluginMessagePacketBehaviour(IProxySessionNew proxySession) {
		this.proxySession = proxySession;
	}

	
	@Override
	public void process(Packet packet) {
		ServerPluginMessagePacket pluginMessagePacket = (ServerPluginMessagePacket)packet;
		//ConsoleIO.println("ServerPluginMessagePacketBehaviour::process => PluginMessagePacket for channel <"+pluginMessagePacket.getChannel()+">");
		if (pluginMessagePacket.getChannel().equals("Koekepan|migrate")) {
			byte[] payload = pluginMessagePacket.getData();
			String hostname = this.readStringFromData(payload);
			ConsoleIO.println("ServerPluginMessagePacketBehaviour::process => Received a migration message for client <"+proxySession.getUsername()+"> to migrate to server <"+hostname+">");
			proxySession.migrate(hostname, proxySession.getServerPort()); 
		} else if (pluginMessagePacket.getChannel().equals("Koekepan|kick")) {
				byte[] payload = pluginMessagePacket.getData();
				String reason = this.readStringFromData(payload);
				ConsoleIO.println("ServerPluginMessagePacketBehaviour::process => Received a kick message for client <"+proxySession.getUsername()+"> with reason <"+reason+">");
				if (proxySession.isConnected()) {
					proxySession.disconnect();
				}
		} else if (pluginMessagePacket.getChannel().equals("Koekepan|latency")) {
			byte[] payload = pluginMessagePacket.getData();
			this.echoLatencyPacket(proxySession, "Koekepan|latency", payload);
			//System.out.println("ServerPluginMessagePacketBehaviour::process => Received latency measurement packet for client <"+client.getName()+">");	
		}
	}
	
	/**
     * Reads a compressed int from the buffer. To do so it maximally reads 5 byte-sized chunks whose most significant
     * bit dictates whether another byte should be read.
     */
    private int readVarIntFromBuffer(ByteBuf buffer)
    {
        int var1 = 0;
        int var2 = 0;
        byte var3;

        do
        {
            var3 = buffer.readByte();
            var1 |= (var3 & 127) << var2++ * 7;

            if (var2 > 5)
            {
                throw new RuntimeException("VarInt too big");
            }
        }
        while ((var3 & 128) == 128);

        return var1;
    }
    	
	
	private String readStringFromData(byte[] data) {
		ByteBuf buffer = Unpooled.wrappedBuffer(data);

        int length = this.readVarIntFromBuffer(buffer);
		byte[] readBytes = new byte[length];
    	
    	buffer.readBytes(readBytes);
		String message = new String(readBytes, Charsets.UTF_8);
        return message;
	}
	
	
	private void echoLatencyPacket(IProxySessionNew proxySession, String channel, byte[] payload) {
		long currentTime = System.currentTimeMillis();
		LatencyData data = new LatencyData();
		data.reconstructFromBytes(payload);
		if (data.clientSentTime == -1) {
			data.clientSentTime = currentTime;					
			ClientPluginMessagePacket echoPacket = new ClientPluginMessagePacket(channel, data.convertToBytes());
			proxySession.sendPacketToServer(echoPacket);
		} else {
			data.clientReceiveTime = currentTime;
			//log.info("HerobrineMigrationListener::echoLatencyPacket => CurrentTime <"+currentTime+"> Server <"+data.serverSentTime+","+data.serverReceiveTime+"> Serverside RTT <"+(data.serverReceiveTime - data.serverSentTime)+"> Client <"+data.clientSentTime+","+data.clientReceiveTime+"> Clientside Latency <"+(data.clientReceiveTime - data.clientSentTime)+">");	
		}		
	}
}
