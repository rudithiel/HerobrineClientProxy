package org.koekepan.herobrineproxy.session;

import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.koekepan.herobrineproxy.ConsoleIO;
import org.koekepan.herobrineproxy.behaviour.BehaviourHandler;
import org.koekepan.herobrineproxy.packet.EstablishConnectionPacket;
import org.koekepan.herobrineproxy.packet.PacketHandler;
import org.koekepan.herobrineproxy.packet.SPSPacketSession;
import org.koekepan.herobrineproxy.packet.behaviours.DefaultPacketBehaviours;
import org.koekepan.herobrineproxy.sps.ISPSConnection;

import com.github.steveice10.mc.protocol.packet.ingame.client.ClientPluginMessagePacket;
import com.github.steveice10.mc.protocol.packet.login.client.LoginStartPacket;
import com.github.steveice10.packetlib.packet.Packet;
import com.google.common.base.Charsets;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public class SPSSession implements IServerSession {

	private ISPSConnection spsClient;
	private String username = null;
	private SPSPacketSession packetSession;
	private PacketHandler packetHandler;
	private ScheduledExecutorService packetExecutor;
	private Future<?> packetFuture;
	
	
	private volatile CountDownLatch joinedLatch;
	private volatile boolean joined;	

	Set<String> channels = new TreeSet<String>();
	
	public SPSSession(ISPSConnection spsClient) { 
		
		joinedLatch = new CountDownLatch(1);
		
		this.setJoined(false);
		addChannelRegistration("Koekepan|migrate");
		addChannelRegistration("Koekepan|kick");
		addChannelRegistration("Koekepan|latency");
		
		this.spsClient = spsClient;
		packetExecutor = Executors.newSingleThreadScheduledExecutor();
		
		this.initialiseSession();
	}
	
	
	private void initialiseSession() {		
		ConsoleIO.println("Initialize SPSSession lmao");
		this.packetSession = new SPSPacketSession(spsClient);
		this.packetSession.setChannel("login");
		this.packetHandler = new PacketHandler(new DefaultPacketBehaviours(), packetSession);
		packetFuture = packetExecutor.scheduleAtFixedRate(this.packetHandler, 0, 1, TimeUnit.MILLISECONDS);
	}
	

	@Override
	public void setUsername(String username) {
		ConsoleIO.println("SPSSession::setUsername => Settting session username to <"+username+">");
		this.username = username;
		this.packetSession.setUsername(username);
		this.spsClient.addListener(this);
	}
	
	
	@Override
	public String getUsername() {
		return this.username;
	}

	
	@Override
	public String getHost() {
		return spsClient.getHost();
	}
	

	@Override
	public int getPort() {
		return spsClient.getPort();
	}

	
	@Override
	public void connect() {
		ConsoleIO.println("SPSSession::connect => Player <"+getUsername()+"> is connecting to <"+getHost()+":"+getPort()+">");
		EstablishConnectionPacket loginPacket = new EstablishConnectionPacket(getUsername(), true);		
		//LoginStartPacket loginPacket = new LoginStartPacket(getUsername());
		this.sendPacket(loginPacket);
	}
	
	
	
	@Override
	public boolean isConnected() {
		return true;
	}

	
	@Override
	public void disconnect() {
		EstablishConnectionPacket loginPacket = new EstablishConnectionPacket(getUsername(), false);	
		this.sendPacket(loginPacket);
		this.spsClient.removeListener(this);
		//if (packetFuture != null) {
		//	packetFuture.cancel(false);
		//}
	}
	
	
	
	@Override
	public void setPacketBehaviours(BehaviourHandler<Packet> behaviours) {
		if (!packetFuture.isDone()) {
			packetFuture.cancel(false);
		}
		this.packetHandler.setBehaviours(behaviours);
		packetFuture = packetExecutor.scheduleAtFixedRate(this.packetHandler, 0, 1, TimeUnit.MILLISECONDS);
	}
	
	
	private void addChannelRegistration(String channel) {
		channels.add(channel);
	}
	

	@Override
	public void sendPacket(Packet packet) {
		packetHandler.sendPacket(packet);		
	}


	@Override
	public void packetReceived(Packet packet) {
		ConsoleIO.println("SPSSession::packetReceived => received packet <"+packet.getClass().getSimpleName()+">");
		packetHandler.packetReceived(packet);		
	}
	
	
	@Override
	public void setJoined(boolean joined) {
		this.joined = joined;
		if (joined) {
			this.getJoinedCountDownLatch().countDown();
		} else {
			joinedLatch = new CountDownLatch(1);
		}
	}
	
	
	public CountDownLatch getJoinedCountDownLatch() {
		return this.joinedLatch;
	}
	
	
	@Override
	public void registerClientForChannels() {
		for (String channel : channels) {
			registerClientForChannel(channel);
		}
	}
	
	
	private void registerClientForChannel(String channel) {
		byte[] payload = writeStringToPluginMessageData(channel);
		String registerMessage = "REGISTER";
		ClientPluginMessagePacket registerPacket = new ClientPluginMessagePacket(registerMessage, payload);
		this.sendPacket(registerPacket);
	}


	private byte[] writeStringToPluginMessageData(String message) {
		byte[] data = message.getBytes(Charsets.UTF_8);
		ByteBuf buff = Unpooled.buffer();        
		buff.writeBytes(data);
		return buff.array();
	}
	
	
}
