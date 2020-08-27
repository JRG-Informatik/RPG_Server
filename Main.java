package com.uhrenclan.RGP_Server;

public class Main {

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
			}

			@Override
			public void OnClientDisconnect(Client client) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void OnClientTCPDisconnect(TCPClient tcpClient) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void OnReceiveTCPPacket(TCPClient tcpClient, Packet packet) {
				// TODO Auto-generated method stub
				System.out.println("Client "+tcpClient.client.ID+" sent packet: "+packet.ToArray());
			}

			@Override
			public void OnDispatchTCPPacket(TCPClient tcpClient, Packet packet) {
				// TODO Auto-generated method stub
				System.out.println("Client "+tcpClient.client.ID+" dispatched packet: "+packet.ToArray());
			}});
		try {
			server.Listen(80);
			System.out.println("Server listening on port "+80);
		} catch (Exception e) {
			System.out.println("server couldn't launch... "+e.toString());
		};
	}

}
