package com.uhrenclan;

import com.uhrenclan.RGP_Server.Client;
import com.uhrenclan.RGP_Server.Packet;
import com.uhrenclan.RGP_Server.Server;
import com.uhrenclan.RGP_Server.TCPClient;
import com.uhrenclan.RGP_Server.Server.ServerEventCallback;

public class TestServer {

	public static void main(String[] args) {
		Server server = new Server();
		server.setServerEventCallback(new Server.ServerEventCallback() {

			@Override
			public void OnClientConnection(Client client) {
				// TODO Auto-generated method stub
				System.out.println("Bonded with client "+client.ID);
			}

			@Override
			public void OnClientConnectionAuthenticated(Client client) {
				// TODO Auto-generated method stub
				System.out.println("Authenticated client "+client.ID);
				try(Packet packet = new Packet(1)){
					packet.Write("Test String hahaha");
					server.SendTCPData(client.ID, packet);
				}catch(Exception e) {}
			}

			@Override
			public void OnClientDisconnect(Client client) {
				// TODO Auto-generated method stub
				System.out.println("Client "+client.ID+" disconnected");
			}

			@Override
			public void OnClientTCPDisconnect(TCPClient tcpClient) {
				// TODO Auto-generated method stub
				System.out.println("TCPClient "+tcpClient.client.ID+" disconnected");
			}

			@Override
			public void OnReceiveTCPPacket(TCPClient tcpClient, Packet packet) {
				// TODO Auto-generated method stub
				System.out.println("Client "+tcpClient.client.ID+" sent packet: "+packet.toString());
			}

			@Override
			public void OnDispatchTCPPacket(TCPClient tcpClient, Packet packet) {
				// TODO Auto-generated method stub
				System.out.println("Client "+tcpClient.client.ID+" dispatched packet: "+packet.toString());
			}});
		try {
			server.Listen("127.0.0.1",3000);
			System.out.println("Server listening on port "+3000);
		} catch (Exception e) {
			System.out.println("server couldn't launch... "+e.toString());
		};
	}

}
