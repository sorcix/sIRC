/*
 * MessageListener.java
 * 
 * This file is part of the Sorcix Java IRC Library (sIRC).
 * 
 * Copyright (C) 2008-2010 Vic Demuzere http://sorcix.com
 * 
 * Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without
 * restriction, including without limitation the rights to use, copy,
 * modify, merge, publish, distribute, sublicense, and/or sell copies
 * of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS
 * BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN
 * ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.sorcix.sirc.event;

import com.sorcix.sirc.Channel;
import com.sorcix.sirc.IrcConnection;
import com.sorcix.sirc.IrcPacket;
import com.sorcix.sirc.User;

/**
 * Notified of new IRC messages.
 */
public interface MessageEventListener {

    public static class Action extends BaseEvent {
        public final User sender;
        public final Channel target;
        public final String action;

        public Action(IrcConnection c, IrcPacket p) {
            super(c, p);
            action = p.getMessage().substring(7);
            if (Channel.CHANNEL_PREFIX.indexOf(p.getArguments().charAt(0)) >= 0) {
                // to channel
                target = c.getState().getChannel(p.getArgumentsArray()[0]);
                sender = target.updateUser(p.getSender(), true);
            } else {
                // to user
                target = null;
                sender = p.getSender();
            }
        }
    }

    public static class CtcpReply extends BaseEvent {
        public final String command;
        public final User sender;
        public final String message;

        public CtcpReply(IrcConnection c, IrcPacket p) {
            super(c, p);
            final int cmdPos = p.getMessage().indexOf(' ');
            command = p.getMessage().substring(0, cmdPos);
            final String args = p.getMessage().substring(cmdPos + 1);
            sender = p.getSender();
            message = args;
        }
    }

    public static class Message extends BaseEvent {
        public final User sender;
        public final Channel target;
        public final String message;
        public Message(IrcConnection c, IrcPacket p) {
            super(c, p);
            message = p.getMessage();
            if (Channel.CHANNEL_PREFIX.indexOf(
                    p.getArguments().charAt(0)) >= 0) {
                target = c.getState().getChannel(p.getArgumentsArray()[0]);
                sender = target.updateUser(p.getSender(), true);
            } else {
                target = null;
                sender = p.getSender();
            }
        }
    }

    public static class Notice extends BaseEvent {
        public final User sender;
        public final Channel target;
        public final String message;
        public Notice(IrcConnection c, IrcPacket p) {
            super(c, p);
            message = p.getMessage();
            if (Channel.CHANNEL_PREFIX.indexOf(
                    p.getArguments().charAt(0)) >= 0) {
                target = c.getState().getChannel(p.getArgumentsArray()[0]);
                sender = target.updateUser(p.getSender(), true);
            } else {
                target = null;
                sender = p.getSender();
            }
        }
    }

	/**
	 * Received an action in a channel.
	 */
	void onAction(Action action);

	/**
	 * Received a CTCP reply. Note that this event is only fired when
	 * receiving CTCP replies supported by sIRC. If you can't send a
	 * CTCP request, you won't get the reply.
	 */
	void onCtcpReply(CtcpReply reply);

	/**
	 * Received a message in a channel.
	 */
	void onMessage(Message message);

	/**
	 * Received a private notice.
	 */
	void onNotice(Notice notice);

	/**
	 * Received a private message.
	 */
	void onPrivateMessage(Message message);
}
