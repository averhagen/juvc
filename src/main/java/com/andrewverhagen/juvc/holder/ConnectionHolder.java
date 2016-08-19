package com.andrewverhagen.juvc.holder;

import com.andrewverhagen.juvc.connection.VirtualConnection;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.List;

public class ConnectionHolder {

    private final ArrayList<VirtualConnection> virtualConnections;
    private final ArrayList<DatagramPacket> outputPackets;
    private final int maxAmountOfConnections;
    private final ClosedConnectionRemover closedConnectionRemover;

    public ConnectionHolder(int maxAmountOfConnections) {
        if (maxAmountOfConnections < 1)
            throw new IllegalArgumentException("Holder must have a max size of at least one.");
        this.maxAmountOfConnections = maxAmountOfConnections;
        this.virtualConnections = new ArrayList<>();
        this.outputPackets = new ArrayList<>();
        this.closedConnectionRemover = new ClosedConnectionRemover();
    }

    public void addConnection(VirtualConnection connectionToAdd) throws HolderIsFullException, AlreadyHoldingConnectionException {
        if (this.holdingConnection(connectionToAdd))
            throw new AlreadyHoldingConnectionException();
        if (this.atCapacity())
            throw new HolderIsFullException();
        synchronized (this.virtualConnections) {
            if (this.virtualConnections.add(connectionToAdd)) {
                connectionToAdd.openConnection();
                connectionToAdd.addObserver(closedConnectionRemover);
            }
        }
    }

    public boolean atCapacity() {
        synchronized (this.virtualConnections) {
            return this.virtualConnections.size() >= this.maxAmountOfConnections;
        }
    }

    public boolean holdingConnection(VirtualConnection connectionToCheck) {
        this.removeClosedConnections();
        synchronized (virtualConnections) {
            for (VirtualConnection virtualConnection : virtualConnections)
                if (virtualConnection.containsAddress(connectionToCheck))
                    return true;
        }
        return false;
    }

    public void distributePacketToConnections(DatagramPacket inputPacket) {
        synchronized (virtualConnections) {
            for (VirtualConnection virtualConnection : virtualConnections)
                virtualConnection.handleInput(inputPacket);
        }
    }

    public List<DatagramPacket> getOutputPackets() {
        this.removeClosedConnections();
        outputPackets.clear();
        synchronized (virtualConnections) {
            for (VirtualConnection virtualConnection : virtualConnections) {
                DatagramPacket outputPacket = virtualConnection.getOutputPacket();
                if (outputPacket != null)
                    outputPackets.add(outputPacket);
            }
        }
        return outputPackets;
    }

    public void closeConnections() {
        synchronized (virtualConnections) {
            for (VirtualConnection virtualConnection : virtualConnections)
                virtualConnection.closeConnection();
            this.removeClosedConnections();
        }
    }

    private void removeClosedConnections() {
        this.closedConnectionRemover.removeClosedConnectionsInList(virtualConnections);
    }

    public class AlreadyHoldingConnectionException extends Exception {
    }

    public class HolderIsFullException extends Exception {
    }
}
