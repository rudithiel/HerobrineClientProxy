package org.koekepan.herobrineproxy.session;

import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.CountDownLatch;
//import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.koekepan.herobrineproxy.ConsoleIO;
import org.koekepan.herobrineproxy.HerobrineProxyProtocol;
import org.koekepan.herobrineproxy.behaviour.BehaviourHandler;
import org.koekepan.herobrineproxy.packet.IPacketSession;
import org.koekepan.herobrineproxy.packet.PacketAdapter;
import org.koekepan.herobrineproxy.packet.PacketHandler;
import org.koekepan.herobrineproxy.packet.PacketSession;
import org.koekepan.herobrineproxy.packet.behaviours.DefaultPacketBehaviours;

import com.github.steveice10.mc.protocol.MinecraftProtocol;
import com.github.steveice10.mc.protocol.data.SubProtocol;
import com.github.steveice10.mc.protocol.packet.ingame.client.ClientPluginMessagePacket;
import com.github.steveice10.packetlib.Client;
import com.github.steveice10.packetlib.Session;
import com.github.steveice10.packetlib.packet.Packet;
import com.github.steveice10.packetlib.tcp.TcpSessionFactory;
import com.google.common.base.Charsets;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public class ServerSession implements IServerSession {

	private Session serverSession;
	private IPacketSession serverPacketSession;
	
	private MinecraftProtocol protocol; 
	
	private String username;
	private String host;
	private int port;
		
	private int connectRetries = 5;
	private int connectTimeout = 5;

	private Future<?> connectTask;	
	private volatile CountDownLatch connectLatch;
	private ScheduledExecutorService connectExecutor;

	
	private volatile CountDownLatch joinedLatch;
	private volatile boolean joined;	
	private volatile boolean migrate;
	
	private PacketHandler packetHandler;
	private PacketAdapter packetAdapter;
	private ScheduledExecutorService packetExecutor;
	private Future<?> packetFuture;
	
	Set<String> channels = new TreeSet<String>();
	
	
	public ServerSession() {
		joinedLatch = new CountDownLatch(1);
		connectLatch = new CountDownLatch(connectRetries);
		
		this.setJoined(false);
		this.setMigrating(false);
		addChannelRegistration("Koekepan|migrate");
		addChannelRegistration("Koekepan|kick");
		addChannelRegistration("Koekepan|latency");
		packetExecutor = Executors.newSingleThreadScheduledExecutor();
		connectExecutor = Executors.newSingleThreadScheduledExecutor();
	}
	

	public ServerSession(String host, int port) {
		this();
		this.host = host;
		this.port = port;
	}

	
	public ServerSession(String username, String host, int port) {
		this(host, port);
		setUsername(username);
	}
	
	
	@Override
	public void setUsername(String username) {
		this.username = username;
		//protocol = new MinecraftProtocol(SubProtocol.LOGIN);
		protocol = new MinecraftProtocol(username);
		
		serverSession = new Client(host, port, protocol, new TcpSessionFactory(null)).getSession();
		
		this.serverPacketSession = new PacketSession(serverSession);
		this.packetHandler = new PacketHandler(new DefaultPacketBehaviours(), serverPacketSession);
		this.packetAdapter = new PacketAdapter(this.packetHandler);
		packetFuture = packetExecutor.scheduleAtFixedRate(this.packetHandler, 0, 1, TimeUnit.MILLISECONDS);
	}
	
	
	@Override
	public String getUsername() {
		return this.username;
	}
	

	@Override
	public String getHost() {
		return this.host;
	}

	
	@Override
	public int getPort() {
		return this.port;
	}

	
	@Override
	public void connect() {
				
		//ConsoleIO.println("ServerSession::connect => Protocol status <"+protocol.getSubProtocol().name()+">");	

		//clientPacketHandler.setPacketSession(new PacketSession(server));
		serverSession.addListener(packetAdapter);
		
		//clientPacketBehaviours.clearBehaviours();
//		clientPacketBehaviours.registerForwardingBehaviour();
//		serverPacketBehaviours.clearBehaviours();
//		serverPacketBehaviours.registerForwardingBehaviour();

		/*
		clientSession.removeListener(packetAdapter);
		clientForwarder = new PacketForwarder(server);
		clientSession.addListener(clientForwarder);
		serverForwarder = new PacketForwarder(clientSession);
		server.addListener(serverForwarder);
		*/
			
		ConsoleIO.println("ServerSession::connect => Connecting to server <"+host+":"+port+">");
		//serverSession.connect(true);
		this.robustConnect();
	//	server.connect(true);	
		ConsoleIO.println("Finished connecting to server <"+host+":"+port+">");		
	}
	

	@Override
	public boolean isConnected() {
		return serverSession.isConnected();
	}
	

	@Override
	public void disconnect() {
		if (isConnected()) {
			serverSession.disconnect("Finished.");
		}
		if (connectTask != null) {
			connectTask.cancel(true);
		}
	}
	

	/*@Override
	public boolean isMigrating() {
		// TODO Auto-generated method stub
		return false;
	}

	
	@Override
	public void setMigrating(boolean migrating) {
		// TODO Auto-generated method stub

	}
	

	@Override
	public boolean hasJoined() {
		// TODO Auto-generated method stub
		return false;
	}

	
	@Override
	public void setJoined(boolean joined) {
		// TODO Auto-generated method stub

	}*/


	/*@Override
	public PacketSession getPacketSession() {
		return this.serverPacketSession;
	}*/
	
	
	@Override
	public void sendPacket(Packet packet) {
		packetHandler.sendPacket(packet);		
	}
	
	
	@Override
	public void packetReceived(Packet packet) {
		ConsoleIO.println("ServerSession::packet received <"+packet.getClass().getSimpleName()+">");
		packetHandler.packetReceived(packet);		
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
	public void registerClientForChannels() {
		for (String channel : channels) {
			registerClientForChannel(channel);
		}
	}
	
	
	private void registerClientForChannel(String channel) {
		byte[] payload = writeStringToPluginMessageData(channel);
		String registerMessage = "REGISTER";
		ClientPluginMessagePacket registerPacket = new ClientPluginMessagePacket(registerMessage, payload);
		serverSession.send(registerPacket);
	}


	private byte[] writeStringToPluginMessageData(String message) {
		byte[] data = message.getBytes(Charsets.UTF_8);
		ByteBuf buff = Unpooled.buffer();        
		buff.writeBytes(data);
		return buff.array();
	}
	
	
	
	private void robustConnect() {	
		ConsoleIO.println("ServerSession::robustConnect => Starting thread to connect ");
		
		connectTask = connectExecutor.submit(new Runnable() {
			
			@Override
			public void run()  {
				try {
					ConsoleIO.println("["+Thread.currentThread().getId()+"] ServerSession::robustConnect => Player <"+getUsername()+">  attempt <"+(connectRetries-connectLatch.getCount()+1)+"> at connecting to "+getHost());
					//ConsoleIO.println("ServerSession::connect => Protocol status <"+protocol.getSubProtocol().name()+"> and CompressionThreshold <"+serverSession.getCompressionThreshold()+">");	

					serverSession.connect(true);	
				//	ConsoleIO.println("["+Thread.currentThread().getId()+"] ServerSession::robustConnect  => Waiting for player <"+getUsername()+"> to establish connection to "+getHost());
					//ConsoleIO.println("ServerSession::connect => Protocol status <"+protocol.getSubProtocol().name()+"> and CompressionThreshold <"+serverSession.getCompressionThreshold()+">");	

					boolean connected = getJoinedCountDownLatch().await(connectTimeout, TimeUnit.SECONDS);
				//	ConsoleIO.println("["+Thread.currentThread().getId()+"] ServerSession::robustConnect  => Finished waiting for player <"+getUsername()+"> to establish connection to "+getHost()+": "+getJoinedCountDownLatch().getCount());
					if (connected) {
						ConsoleIO.println("["+Thread.currentThread().getId()+"] ServerSession::robustConnect => Player <"+getUsername()+">: connection established to <"+getHost()+">: "+isConnected());
						registerClientForChannels();
						connectLatch = new CountDownLatch(connectRetries);
						connectTask.cancel(false);	
					} else {
						ConsoleIO.println("["+Thread.currentThread().getId()+"] ServerSession::robustConnect => Player <"+getUsername()+": Failed to establish connection to <"+getHost()+">");			
						connectLatch.countDown();
						if (connectLatch.getCount() > 0) {
							ConsoleIO.println("["+Thread.currentThread().getId()+"] ServerSession::robustConnect => Player <"+getUsername()+": Attempting to reconnect to <"+getHost()+">");										
							reconnect();
						} else {
							ConsoleIO.println("["+Thread.currentThread().getId()+"] ServerSession::robustConnect => Player <"+getUsername()+": Maximum reconnection attempts reached. Disconnecting...");			
							disconnect();
							setMigrating(false);
							setJoined(false);
							connectTask.cancel(false);
						}
					}
				
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				}
			}
		}); 
		ConsoleIO.println("Finished submitting connect task");
	}
	
	
	public CountDownLatch getJoinedCountDownLatch() {
		return this.joinedLatch;
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
	

	
	private boolean hasJoined() {
		return joined;
	}
	
	
	private void reconnect() {
		this.setJoined(false);
		this.robustConnect();
	}

	
	private boolean isMigrating() {
		return this.migrate;
	}

	
	private void setMigrating(boolean migrating) {
		this.migrate = migrating;
	}
}
