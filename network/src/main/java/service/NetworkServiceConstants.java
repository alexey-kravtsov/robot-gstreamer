package service;

class NetworkServiceConstants {
    static final int CRC32_SIZE_BYTES = 4;
    static final int MTU_SAFE_MESSAGE_SIZE_BYTES = 1300;
    static final int SO_RECEIVE_TIMEOUT_MS = 5000;
    static final int SO_SEND_TIMEOUT_MS = 500;

    static final byte PROTOCOL_VERSION = 1;

    static final byte SESSION_TIMEOUT_SEC = 15;
    static final byte SESSION_PING_PERIOND_SEC = 5;
}
