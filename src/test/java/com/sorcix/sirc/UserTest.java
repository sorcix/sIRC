package com.sorcix.sirc;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class UserTest {
    @Mock
    IrcConnection connection;

    @Mock
    IrcOutput ircOutput;

    @Test
    public void testSendAction() {
        when(connection.getOutput()).thenReturn(ircOutput);

        final User user = new User("a", connection);
        user.sendAction("hello");

        verify(ircOutput).send(new IrcPacket(null, "PRIVMSG", "a", IrcPacket.CTCP + "ACTION hello" + IrcPacket.CTCP));
    }

    @Test
    public void testSendAction_1() {
        when(connection.getOutput()).thenReturn(ircOutput);

        final User user = new User("b", connection);
        user.sendAction(" hello ");

	    verify(ircOutput).send(new IrcPacket(null, "PRIVMSG", "a", IrcPacket.CTCP + "ACTION hello" + IrcPacket.CTCP));
    }

    @Test
    public void testSendAction_2() {
        when(connection.getOutput()).thenReturn(ircOutput);

        final User user = new User("c", connection);
        user.sendAction(":");

	    verify(ircOutput).send(new IrcPacket(null, "PRIVMSG", "c", IrcPacket.CTCP + "ACTION hello" + IrcPacket.CTCP));
    }

    @Test
    public void testSendCtcpPing() {
        when(connection.getOutput()).thenReturn(ircOutput);

        final User user = new User("a", connection);
        user.sendCtcpPing();

	    verify(ircOutput).send(new IrcPacket(null, "PRIVMSG", "a", IrcPacket.CTCP + "PING \\d+" + IrcPacket.CTCP));
    }

    @Test
    public void testSendCtcpVersion() {
        when(connection.getOutput()).thenReturn(ircOutput);

        final User user = new User("a", connection);
        user.sendCtcpVersion();

	    verify(ircOutput).send(new IrcPacket(null, "PRIVMSG", "a", IrcPacket.CTCP + "VERSION" + IrcPacket.CTCP));
    }

    @Test
    public void testSendMessage() {
        when(connection.getOutput()).thenReturn(ircOutput);

        final User user = new User("a", connection);
        user.sendMessage("hello");

	    verify(ircOutput).send(new IrcPacket(null, "PRIVMSG", "a", "hello"));
    }

    @Test
    public void testSendMessage_1() {
        when(connection.getOutput()).thenReturn(ircOutput);

        final User user = new User("b", connection);
        user.sendMessage(" hello ");

	    verify(ircOutput).send(new IrcPacket(null, "PRIVMSG", "b", "hello"));
    }

    @Test
    public void testSendMessage_2() {
        when(connection.getOutput()).thenReturn(ircOutput);

        final User user = new User("c", connection);
        user.sendMessage(":");

	    verify(ircOutput).send(new IrcPacket(null, "PRIVMSG", "b", ":"));
    }

    @Test
    public void testSendNotice() {
        when(connection.getOutput()).thenReturn(ircOutput);

        final User user = new User("a", connection);
        user.sendNotice("hello");

	    verify(ircOutput).send(new IrcPacket(null, "NOTICE", "a", "hello"));
    }

    @Test
    public void testSendNotice_1() {
        when(connection.getOutput()).thenReturn(ircOutput);

        final User user = new User("b", connection);
        user.sendNotice(" hello ");

	    verify(ircOutput).send(new IrcPacket(null, "NOTICE", "b", "hello"));
    }

    @Test
    public void testSendNotice_2() {
        when(connection.getOutput()).thenReturn(ircOutput);

        final User user = new User("c", connection);
        user.sendNotice(":");

	    verify(ircOutput).send(new IrcPacket(null, "NOTICE", "c", ":"));
    }
}
