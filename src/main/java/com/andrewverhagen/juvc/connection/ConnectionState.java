package com.andrewverhagen.juvc.connection;

public enum ConnectionState {

    UNOPENED(0),
    CONNECTING(1),
    CONNECTED(2),
    CLOSED(3);

    private final int connectionLevel;

    ConnectionState(int connectionLevel) {
        this.connectionLevel = connectionLevel;
    }

    public boolean canMoveToConnectionLevel(ConnectionState desiredState) {
        return this.connectionLevel < desiredState.connectionLevel;
    }
}
