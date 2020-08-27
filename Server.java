package com.uhrenclan.RGP_Server;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;

import java.util.HashMap;


public class Server {
	private ServerSocket server;
	private Map<Integer, Client> clients = new HashMap<Integer, Client>();
	private boolean listen = false;
	private ThreadManager threadManager = new ThreadManager();
	
	private ServerEventCallback serverEventCallback;
	public static interface ServerEventCallback{
		void OnClientConnection(Client client);
		void OnClientConnectionAuthenticated(Client client);
		void OnClientDisconnect(Client client);
		void OnClientTCPDisconnect(TCPClient tcpClient);
		void OnReceiveTCPPacket(TCPClient tcpClient, Packet packet);
		void OnDispatchTCPPacket(TCPClient tcpClient, Packet packet);
	}
	public void setServerEventCallback(ServerEventCallback _serverEventCallback) {
		serverEventCallback = _serverEventCallback;
	}
	public ServerEventCallback getServerEventCallback() {
		return serverEventCallback;
	}
	
	public static interface PacketHandler{
		void callback(int _fromClient, Packet _packet);
	}
	private Map<Integer, PacketHandler> packetHandlers = new HashMap<Integer, PacketHandler>(){{
		//WelcomeReceived callback from Client
		put(0, new PacketHandler() {
			@Override
			public void callback(int _fromClient, Packet _packet) {
				//Read ID from Server packet
		        int checkID = _packet.ReadInt();
		        if(checkID == _fromClient) {
		        	getServerEventCallback().OnClientConnectionAuthenticated(clients.get(_fromClient));
		        }
		    }});
	}};
	public PacketHandler getPacketHandler(int id) {
		return packetHandlers.get(id);
	}
	public void addPacketHandler(int id, PacketHandler packetHandler) {
		if(id<=0) return;
		packetHandlers.put(id, packetHandler);
	}
	public void addPacketHandlers(Map<Integer, PacketHandler> _packetHandlers) {
		for(Map.Entry<Integer, PacketHandler> pair: _packetHandlers.entrySet()) {
			addPacketHandler(pair.getKey(), pair.getValue());
		}
	}
	public void removePacketHandler(int id) {
		if(id<=0)return;
		packetHandlers.remove(id);
	}
	
	public Server() {}
	public void Listen(int port) throws Exception {Listen(null, port);}
	public void Listen(String serverIp, int port) throws Exception {
		if(serverIp == null || serverIp.isEmpty()) server = new ServerSocket(0,1,InetAddress.getByName(serverIp));
		else server = new ServerSocket(port,1,InetAddress.getLocalHost()); 
		
		//listens to new connection on second thread;
		listen=true;
		new Thread() {
			@Override
			public void run() {
				while(listen) {
					TCPListen();
					threadManager.UpdateMain();
				}
			}
		}.start();
	}
	
	private void TCPListen(){
		try {
			final Socket clientSocket = server.accept();
			int id = 0;
			while(true) {
				if(clients.containsKey(id)) id++;
				else break;
			}
			Client client = new Client(id, this);
			client.tcp.Connect(clientSocket);
			clients.put(id, client);
			serverEventCallback.OnClientConnection(client);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void SendTCPData(int _toClient, Packet _packet){
        _packet.WriteLength();
        clients.get(_toClient).tcp.SendData(_packet);
    }
	
	public void SendTCPDataToAll(Packet _packet){
        _packet.WriteLength();
        for (Client client: clients.values()){
            client.tcp.SendData(_packet);
        }
    }
	
	public void SendTCPDataToAll(int _exceptClient, Packet _packet){
        _packet.WriteLength();
        for (Client client: clients.values()){
            if (client.ID != _exceptClient){
                client.tcp.SendData(_packet);
            }
        }
    }
	
	public void ExecuteOnMainThread(Runnable runnable) {
		threadManager.ExecuteOnMainThread(runnable);
	}
}
