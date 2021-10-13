package org.koekepan.herobrineproxy.sps;

import java.util.HashMap;
import java.util.Map;

import org.koekepan.herobrineproxy.packet.PacketListener;
import org.koekepan.herobrineproxy.session.IProxySessionNew;
import org.koekepan.herobrineproxy.session.ISession;

import com.github.steveice10.packetlib.Session;
import com.github.steveice10.packetlib.packet.Packet;


/* SPS javascript server

API	to receive message from SPS
	: "type"
	: "publication"

API to send message to SPS
	: "server connected"
	: "client connected"
	: "disconnect"
	: "publish" // spatial publish
	: "publish packet" // send packet to specific channel
	: "function" : "sub", "usub" // spatial publish subscribe
	: "move"
	*/

public interface ISPSConnection {
	
	public String getHost();
	public int getPort();
	
	public void connect();
	public void disconnect();
	// Receiving functions
	public SPSPacket receivePublication(Object... data);  // "publication"
	public void receiveConnectionID(int connection); // "type"

	
	// Sending functions
	public void subscribe(String channel); // "function" : "sub"
	public void unsubscribed(String channel); // "function : usub"
	public void publish(SPSPacket packet);
	public void addListener(ISession listener);
	public void removeListener(ISession listener);
}
