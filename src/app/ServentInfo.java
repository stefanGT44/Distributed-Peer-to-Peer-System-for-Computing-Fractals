package app;

import java.io.Serializable;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Objects;

/**
 * This is an immutable class that holds all the information for a servent.
 *
 * @author stefanGT44
 */
public class ServentInfo implements Serializable {

	private static final long serialVersionUID = 5304170042791281555L;
	private final String ipAddress;
	private final short listenerPort;
	private final int id;
	
	public ServentInfo(String ipAddress, short listenerPort, int id) {
		this.ipAddress = ipAddress;
		this.listenerPort = listenerPort;
		this.id = id;
	}
	
	public ServentInfo(short listenerPort, int id) throws UnknownHostException {
		this.ipAddress = InetAddress.getLocalHost().getHostAddress();
		this.listenerPort = listenerPort;
		this.id = id;
	}

	public String getIpAddress() {
		return ipAddress;
	}

	public short getListenerPort() {
		return listenerPort;
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(id);
	}
	
	@Override
	public boolean equals(Object obj) {
		ServentInfo other = (ServentInfo)obj;
		return id == other.id;
	}

	public int getId() {
		return id;
	}
	
	@Override
	public String toString() {
		return "[" + id + "|" + ipAddress + "|" + listenerPort + "]";
	}

}
