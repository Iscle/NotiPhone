package me.iscle.notiphone.service;

public enum ConnectionState {
    DISCONNECTED, // We're doing nothing
    CONNECTING, // Initiating an outgoing connection
    CONNECTED // Connected to a remote device
}
