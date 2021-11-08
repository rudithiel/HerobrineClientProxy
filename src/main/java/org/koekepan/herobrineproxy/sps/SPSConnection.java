package org.koekepan.herobrineproxy.sps;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.koekepan.herobrineproxy.ConsoleIO;
import org.koekepan.herobrineproxy.HerobrineProxyProtocol;
//import org.koekepan.herobrineproxy.SPSServerProxy;
import org.koekepan.herobrineproxy.packet.EstablishConnectionPacket;
import org.koekepan.herobrineproxy.packet.PacketListener;
import org.koekepan.herobrineproxy.session.IProxySessionConstructor;
import org.koekepan.herobrineproxy.session.IProxySessionNew;
import org.koekepan.herobrineproxy.session.ISession;

import com.github.steveice10.mc.protocol.MinecraftProtocol;
import com.github.steveice10.mc.protocol.data.SubProtocol;
import com.github.steveice10.mc.protocol.packet.ingame.client.player.ClientPlayerMovementPacket;
import com.github.steveice10.mc.protocol.packet.ingame.client.player.ClientPlayerPositionPacket;
import com.github.steveice10.mc.protocol.packet.ingame.client.player.ClientPlayerPositionRotationPacket;
import com.github.steveice10.mc.protocol.packet.login.client.LoginStartPacket;
import com.github.steveice10.packetlib.Session;
import com.github.steveice10.packetlib.io.NetInput;
import com.github.steveice10.packetlib.io.NetOutput;
import com.github.steveice10.packetlib.io.buffer.ByteBufferNetInput;
import com.github.steveice10.packetlib.io.buffer.ByteBufferNetOutput;
import com.github.steveice10.packetlib.packet.Packet;
import com.github.steveice10.packetlib.packet.PacketProtocol;
import com.google.gson.Gson;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class SPSConnection implements ISPSConnection {
	
	int x;
	int y;
	int radius = 1000;
	
	int SPSPort;
	String SPSHost;
	private Socket socket;
	int connectionID;
	private SPSProxyProtocol protocol;
	//private PacketProtocol protocol;
	private Map<String, ISession> listeners = new HashMap<String, ISession>();
	private IProxySessionConstructor sessionConstructor;
	
	public SPSConnection(String SPSHost, int SPSPort) {
		this.SPSHost = SPSHost;
		this.SPSPort = SPSPort;
		this.protocol = new SPSProxyProtocol();
	}
	
	
	public SPSConnection(String SPSHost, int SPSPort, IProxySessionConstructor sessionConstructor) {
		this(SPSHost, SPSPort);
		this.sessionConstructor = sessionConstructor;
	}


	private boolean initializeConnection() {
		ConsoleIO.println("Initialize SPS connection");
		String URL = "http://"+this.SPSHost+":"+this.SPSPort;
		boolean result = false;
		try {
			this.socket = IO.socket(URL);
			result = true;
		} catch (URISyntaxException e) {
				e.printStackTrace();
		}
		return result;
	}

	public void initialiseListeners() {
		ConsoleIO.println("Initialize SPS listeners");
		socket.on("ID", new Emitter.Listener() {
			@Override
			public void call(Object... data) {
				receiveConnectionID((int) data[0]);
				subscribe(0,0,0);
			}
		});
		
		socket.on("getType", new Emitter.Listener() {
			
			@Override
			public void call(Object... args) {
				socket.emit("type", "client");
			}
		});
		
		socket.on("publication", new Emitter.Listener() {
			@Override
			public void call(Object... data) {
				SPSPacket packet = receivePublication(data);
				String username = packet.username;
				int x = packet.x;
				int y = packet.y;
				int radius = packet.radius;
				
				if (packet.packet instanceof EstablishConnectionPacket) {
					EstablishConnectionPacket loginPacket = (EstablishConnectionPacket)packet.packet;
					username = loginPacket.getUsername();
					if (loginPacket.establishConnection()) {
						ConsoleIO.println("SPSConnection::publication Must establish new connection for session <"+username+">");
						IProxySessionNew proxySession = sessionConstructor.createProxySession(username);
						String host = proxySession.getServerHost();
						int port = proxySession.getServerPort();
						proxySession.connect(host, port);					
					} else {
						ConsoleIO.println("SPSConnection::publication Must disconnect ession of user <"+username+">");
						IProxySessionNew proxySession = sessionConstructor.getProxySession(username);
						if (proxySession != null) {
							proxySession.disconnect();
						} else {
							ConsoleIO.println("SPSConnection::publication => Received a packet for an unknown session <"+username+">");
						}
					}
				} else if (listeners.containsKey(username)) {					
					//listeners.get(username).packetReceived(packet.packet);
					ConsoleIO.println("SPSConnection::publication => Sending packet <"+packet.packet.getClass().getSimpleName()+"> for player <"+username+"> at <"+x+":"+y+":"+radius+">");
					listeners.get(username).sendPacket(packet.packet);
				} else {
					ConsoleIO.println("SPSConnection::publication => Received a packet for an unknown session <"+username+">");
				}
			}
		});
	}


	@Override
	public void connect() {
		if (initializeConnection()) {
			initialiseListeners();
			socket.connect();
		}
	}

	
	@Override
	public void disconnect() {
		ConsoleIO.println("SPS disconnected");
		socket.disconnect();
	}

	
	private Packet retrievePacket(String publication) {
		Gson gson = new Gson();
		Packet packet = null;
		try {
			//ConsoleIO.println(data[0].toString());
			byte[] payload = gson.fromJson(publication, byte[].class);
			packet = this.bytesToPacket(payload);
		} catch (Throwable e) {
			ConsoleIO.println(e.toString());
		}
		//ConsoleIO.println("SPSConnection::retrievePacket => Retrieved packet <"+packet.getClass().getSimpleName()+">");
		return packet;
	}
	
	
	@Override
	public SPSPacket receivePublication(Object... data) {
		int connectionID = (int)data[0];
		String username = (String)data[1];
		int x = (int)data[2];
		int y = (int)data[3];
		int radius = (int)data[4];
		String publication = data[5].toString();
		String channel = (String)data[6];
		Packet packet = retrievePacket(publication);
		SPSPacket spsPacket = new SPSPacket(packet, username, x, y, radius, channel);
		return spsPacket;
	}

	
	@Override
	public void receiveConnectionID(int connectionID) {
		this.connectionID = connectionID;
		ConsoleIO.println("Received connectionID: <"+connectionID+">");
	}
	
	
	@Override
	public void publish(SPSPacket packet) { // , String username, String channel) {
		ConsoleIO.println("Publish packet " + packet.toString());
		
		if ((packet.packet instanceof ClientPlayerPositionPacket)) {
			ConsoleIO.println("POSITION PACKET");
			double x = ((ClientPlayerMovementPacket) packet.packet).getX();
			double z = ((ClientPlayerMovementPacket) packet.packet).getZ();
			x = (int) x;
			z = (int) z;
			socket.emit("subscribe", x, z, 50);
		} else if ((packet.packet instanceof ClientPlayerPositionRotationPacket)) {
			ConsoleIO.println("POSITION PACKET");
			double x = ((ClientPlayerMovementPacket) packet.packet).getX();
			double z = ((ClientPlayerMovementPacket) packet.packet).getZ();
			x = (int) x;
			z = (int) z;
			socket.emit("subscribe", x, z, 50);
		}
		//convert to JSON
		Gson gson = new Gson();
		byte[] payload = this.packetToBytes(packet.packet);
		String json = gson.toJson(payload);
		//ConsoleIO.println("Connection <"+connectionID+"> sent packet <"+packet.packet.getClass().getSimpleName()+"> on channel <"+packet.channel+">");
		socket.emit("publish", connectionID, packet.username, 0, 0, 0, json, packet.channel);
	}


	@Override
	public void subscribe(int x, int z, int aoi) {
		socket.emit("subscribe", x, z, 50);
		// TODO Auto-generated method stub

	}

	@Override
	public void unsubscribed(String channel) {
		// TODO Auto-generated method stub
	}

		
	private byte[] packetToBytes(Packet packet) {
		ByteBuffer buffer = ByteBuffer.allocate(75000);
		ByteBufferNetOutput output = new ByteBufferNetOutput(buffer);
		
		int packetId = protocol.getOutgoingId(packet.getClass());
		try {
			protocol.getPacketHeader().writePacketId(output, packetId);
			packet.write(output);
		} catch (Exception e) {
			ConsoleIO.println("Exception: "+e.toString());
		}
		byte[] payload = new byte[buffer.position()];
		buffer.flip();
		buffer.get(payload);
		return payload;
	}
	
	
	private Packet bytesToPacket(byte[] payload) {
		ByteBuffer buffer = ByteBuffer.wrap(payload);
		ByteBufferNetInput input = new ByteBufferNetInput(buffer);
		Packet packet = null;
		try {
			int packetId = protocol.getPacketHeader().readPacketId(input);
		//	ConsoleIO.println("SPSConnection::byteToPacket => Protocol status <"+protocol.getSubProtocol().toString()+">");
			packet = protocol.createIncomingPacket(packetId);
			packet.read(input);
			
		} catch (Exception e) {
			ConsoleIO.println("Exception: "+e.toString());
		}
		return packet;
	}


	@Override
	public void addListener(ISession listener) {
		String username = listener.getUsername();
		listeners.put(username, listener);
	}
	
	
	@Override
	public void removeListener(ISession listener) {
		String username = listener.getUsername();
		listeners.remove(username);
	}


	@Override
	public String getHost() {
		return this.SPSHost;
	}


	@Override
	public int getPort() {
		return this.SPSPort;
	}
}
