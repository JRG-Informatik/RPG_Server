package com.uhrenclan.RGP_Server;

public class Client {
	public int ID;
	
	public Server server;
	public TCPClient tcp;
	
	public Client(int _id, Server _server) {
		ID = _id;
		server = _server;
		tcp = new TCPClient(_id, this);
	}
	
	public void BroadcastTCPData(Packet _packet){
        _packet.WriteLength();
        server.SendTCPDataToAll(ID, _packet);
    }
}
