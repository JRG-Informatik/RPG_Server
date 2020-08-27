package com.uhrenclan.RGP_Server;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;

public class TCPClient {
	public Client client;
	private int ID;
	private Socket socket;
	private Packet receivedData;
    private byte[] receiveBuffer;
    private InputStream stream;
    
    private boolean connected = false;
	
	public int receiveBufferSize = 128, sendBufferSize = 4096;
	
	public TCPClient(int _id, Client _client){
		ID = _id;
		client = _client;
	}
	
	public void Connect(Socket _socket) throws Exception{
		socket = _socket;
		
		socket.setReceiveBufferSize(receiveBufferSize);
		socket.setSendBufferSize(sendBufferSize);
		
		stream = socket.getInputStream();
		receivedData = new Packet();
		receiveBuffer = new byte[receiveBufferSize];
		
		System.out.println("Incomimg connection: "+socket.getInetAddress().getCanonicalHostName());
		
		connected = true;
		new Thread() {
			@Override
			public void run() {
				while(connected) {
					ReadData();
				}
			}
		}.start();
		
		//Send Welcome packet
		try(Packet _packet = new Packet(0)){
        	_packet.Write(ID);
        	_packet.WriteLength();
        	SendData(_packet);
        }catch (Exception e) {}
	}
	
	public void ReadData() {
		try {
			byte[] _longdata = new byte[receiveBufferSize];
			int _byteLength = stream.read(_longdata);
			byte[] _data = new byte[_byteLength];
			System.arraycopy(_longdata, 0, _data, 0, _byteLength);
			System.arraycopy(_data, 0, receiveBuffer, 0, _byteLength);
			receivedData.Reset(HandleData(_data));
		} catch (IOException e) {
			e.printStackTrace();
			Disconnect();
		}
		//ServerSend.Welcome(id, "Welcome to the server!");
	}
	
	private boolean HandleData(byte[] _data) {
		int _packetLength = 0;
		receivedData.SetBytes(_data);
		if(receivedData.UnreadLength()>=4) {
			_packetLength = receivedData.ReadInt();
			if(_packetLength <= 0) {
				return true;
			}
		}
        while (_packetLength > 0 && _packetLength <= receivedData.UnreadLength()) {
            // While packet contains data AND packet data length doesn't exceed the length of the packet we're reading
            byte[] _packetBytes = receivedData.ReadBytes(_packetLength);
            client.server.ExecuteOnMainThread(new Runnable(){
            	@Override
            	public void run() {
	                try (Packet _packet = new Packet(_packetBytes)){
	                    int _packetId = _packet.ReadInt();
	                    client.server.getPacketHandler(_packetId).callback(ID, _packet);
	                    //client.server.packetHandlers.get(_packetId).callback(ID, _packet);
	                    client.server.getServerEventCallback().OnReceiveTCPPacket(client.tcp, _packet);
	                    // Call appropriate method to handle the packet
	                } catch (Exception e) {e.printStackTrace();}
            	}
            });

            _packetLength = 0; // Reset packet length
            if (receivedData.UnreadLength() >= 4){
                // If client's received data contains another packet
                _packetLength = receivedData.ReadInt();
                if (_packetLength <= 0){
                    // If packet contains no data
                    return true; // Reset receivedData instance to allow it to be reused
                }
            }
        }

        if (_packetLength <= 1){
            return true; // Reset receivedData instance to allow it to be reused
        }

        return false;
	}
	
	public void SendData(Packet _packet) {
		try{
            if (socket != null){
            	socket.getOutputStream().write(_packet.ToArray(), 0, _packet.Length());
            	socket.getOutputStream().flush();
            	client.server.getServerEventCallback().OnDispatchTCPPacket(this, _packet);
            }
        } catch (Exception e) {
            System.out.println("Error sending data to player via TCP:");
            e.printStackTrace();
        }
	}
	
	public void Disconnect() {
		try {
			stream.close();
			socket.close();
			stream = null;
			socket = null;
			receivedData = null;
			receiveBuffer = null;
			connected = false;
			
			client.server.getServerEventCallback().OnClientTCPDisconnect(this);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
