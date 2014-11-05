package com.sorcix.sirc;

import static org.mockito.Matchers.matches;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import static org.junit.Assert.*;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class IrcPacketTest {
    @Mock
    IrcConnection connection;

    @Test
    public void capLs() {
        IrcPacket p = new IrcPacket(":leguin.freenode.net CAP * LS :" +
                "account-notify extended-join identify-msg multi-prefix sasl",
                connection);
        assertEquals("CAP", p.getCommand());
        assertEquals(
                "account-notify extended-join identify-msg multi-prefix sasl",
                p.getMessage());
        assertEquals("* LS", p.getArguments());
        assertEquals("LS", p.getArgumentsArray()[1]);
    }

    @Test
    public void capReqMultiPrefix() {
        IrcPacket p = new IrcPacket(
                ":irc.colosolutions.net CAP * ACK :multi-prefix", connection);
        assertEquals("CAP", p.getCommand());
        assertEquals("multi-prefix", p.getMessage());
        assertEquals("* ACK", p.getArguments());
        assertEquals("ACK", p.getArgumentsArray()[1]);
    }

    @Test
    public void capNak() {
        IrcPacket p = new IrcPacket(
                ":irc.znc.in CAP unknown-nick NAK :server-time-iso", connection);
        assertEquals("CAP", p.getCommand());
        assertEquals("server-time-iso", p.getMessage());
        assertEquals("unknown-nick NAK", p.getArguments());
        assertEquals("NAK", p.getArgumentsArray()[1]);
    }

    @Test
    public void createCapLs() {
        IrcPacket p = IrcPacketFactory.createCAPLS();
        assertEquals("CAP LS", p.getRaw());
    }

    @Test
    public void welcomeNumeric() {
        IrcPacket p = new IrcPacket("@time=2014-09-15T15:33:11.232Z :" +
                "irc.choopa.net 001 pfn :" +
                "Welcome to the EFNet Internet Relay Chat Network pfn",
                connection);
        assertTrue(p.isNumeric());
        assertEquals(1, p.getNumericCommand());
    }

}
