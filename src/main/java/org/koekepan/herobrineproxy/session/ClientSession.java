package org.koekepan.herobrineproxy.session;

import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.koekepan.herobrineproxy.ConsoleIO;
import org.koekepan.herobrineproxy.behaviour.BehaviourHandler;
import org.koekepan.herobrineproxy.packet.IPacketSession;
import org.koekepan.herobrineproxy.packet.PacketAdapter;
import org.koekepan.herobrineproxy.packet.PacketHandler;
import org.koekepan.herobrineproxy.packet.PacketSession;
import org.koekepan.herobrineproxy.packet.behaviours.DefaultPacketBehaviours;


import com.github.steveice10.packetlib.Session;
import com.github.steveice10.packetlib.packet.Packet;

public class ClientSession implements IClientSession {

	private String username = null;
	private Session client;
	private IPacketSession packetSession;
	private PacketHandler packetHandler;
	private PacketAdapter packetAdapter;
	private ScheduledExecutorService packetExecutor;
	private Future<?> packetFuture;
	
	public ClientSession(Session client) { 
		this.client = client;
		packetExecutor = Executors.newSingleThreadScheduledExecutor();
		
		this.initialiseSession();
	}
	
	
	private void initialiseSession() {		
		this.packetSession = new PacketSession(client);
		this.packetHandler = new PacketHandler(new DefaultPacketBehaviours(), packetSession);
		this.packetAdapter = new PacketAdapter(packetHandler);
		this.client.addListener(packetAdapter);
		packetFuture = packetExecutor.scheduleAtFixedRate(this.packetHandler, 0, 1, TimeUnit.MILLISECONDS);
	}
	

	@Override
	public void setUsername(String username) {
		this.username = username;
	}
	
	
	@Override
	public String getUsername() {
		return this.username;
	}

	
	@Override
	public String getHost() {
		return client.getHost();
	}
	

	@Override
	public int getPort() {
		return client.getPort();
	}

	
	@Override
	public boolean isConnected() {
		return client.isConnected();
	}

	
	@Override
	public void disconnect() {
		if (packetFuture != null) {
			packetFuture.cancel(false);
		}
		if (isConnected()) {
			client.disconnect("Disconnecting client from proxy.");
		}
	}
	
	
	@Override
	public void setPacketBehaviours(BehaviourHandler<Packet> behaviours) {
	//	ConsoleIO.println("ClientSession::setPacketBehaviours");		
		this.packetHandler.setBehaviours(behaviours);
		packetFuture = packetExecutor.scheduleAtFixedRate(this.packetHandler, 0, 1, TimeUnit.MILLISECONDS);
	}


	@Override
	public void sendPacket(Packet packet) {
		packetHandler.sendPacket(packet);		
	}


	@Override
	public void packetReceived(Packet packet) {
		//ConsoleIO.println("ClientSession::packet received <"+packet.getClass().getSimpleName()+">");
		packetHandler.packetReceived(packet);		
	}
}
