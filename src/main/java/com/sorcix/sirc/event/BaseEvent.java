package com.sorcix.sirc.event;

import com.sorcix.sirc.IrcConnection;
import com.sorcix.sirc.IrcPacket;

import java.util.Date;

/**
 * @author pfnguyen
 */
public class BaseEvent {
    private final Date timestamp;
    private final IrcConnection connection;

    public BaseEvent(IrcConnection c, IrcPacket p) {
        this.timestamp = p.getTimestamp();
        connection = c;
    }
}
