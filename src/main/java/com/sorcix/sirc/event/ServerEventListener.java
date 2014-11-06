/*
 * ServerListener.java
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

public interface ServerEventListener {

    public static class Invite extends BaseEvent {
        public final User sender;
        public final User target;
        public final Channel channel;

        public Invite(IrcConnection c, IrcPacket p) {
            super(c, p);
            sender = p.getSender();
            target = new User(p.getArgumentsArray()[0], c);
            channel = c.createChannel(p.getArgumentsArray()[1]);
        }
    }

    public static class Join extends BaseEvent {
        public final Channel channel;
        public final User sender;

        public Join(IrcConnection c, IrcPacket p) {
            super(c, p);
            String channel;
            if (p.hasMessage()) {
                channel = p.getMessage();
            } else {
                channel = p.getArguments();
            }
            // someone joined a channel
            if (p.getSender().isUs()) {
                // if the sender joining the channel is the client
                // we need to add it to the channel list.
                c.getState().addChannel(new Channel(channel, c, true));
            } else {
                // add sender to channel list.
                c.getState().getChannel(channel).addUser(p.getSender());
            }
            this.channel = c.getState().getChannel(channel);
            this.sender = p.getSender();
        }
    }

    public static class Kick extends BaseEvent {
        public final Channel channel;
        public final User sender;
        public final User target;
        public final String message;

        public Kick(IrcConnection c, IrcPacket p) {
            super(c, p);

            final String[] data = p.getArgumentsArray();
            final User kicked = new User(data[1], c);
            final Channel channel = c.getState().getChannel(data[0]);
            if (kicked.isUs()) {
                // if the user leaving the channel is the client
                // we need to remove it from the channel list
                c.getState().removeChannel(data[0]);
            } else {
                // remove user from channel list.
                channel.removeUser(kicked);
            }
            this.channel = channel;
            this.sender = p.getSender();
            this.target = kicked;
            this.message = p.getMessage();
        }
    }

    public static class Mode extends BaseEvent {
        public final Channel channel;
        public final User sender;
        public final String mode;
        public Mode(IrcConnection c, IrcPacket p) {
            super(c, p);
            channel = c.getState().getChannel(p.getArgumentsArray()[0]);
            sender = p.getSender();
            mode = p.getArguments().substring(
                    p.getArgumentsArray()[0].length() + 1);
        }
    }

    public static class Nick extends BaseEvent {
        public final User oldUser;
        public final User newUser;
        public Nick(IrcConnection c, IrcPacket p) {
            super(c, p);
            newUser =  new User(
                    p.hasMessage() ? p.getMessage() : p.getArguments(), c);
            oldUser = p.getSender();
        }

    }

    public static class Part extends BaseEvent {
        public final User sender;
        public final Channel channel;
        public final String message;

        public Part(IrcConnection c, IrcPacket p) {
            super(c, p);
            channel = c.getState().getChannel(p.getArguments());
            sender = p.getSender();
            message = p.getMessage();
        }
    }

    public static class Quit extends BaseEvent {
        public final User sender;
        public final String message;

        public Quit(IrcConnection c, IrcPacket p) {
            super(c, p);
            sender = p.getSender();
            message = p.getMessage();
        }
    }

    public static class Topic extends BaseEvent {
        public final User sender;
        public final Channel channel;
        public final String topic;
        public Topic(IrcConnection c, IrcPacket p) {
            super(c, p);

            if ("TOPIC".equals(p.getCommand())) {
                final Channel chan = c.getState().getChannel(p.getArguments());
                channel = chan;
                sender  = chan.updateUser(p.getSender(), false);
                topic   = p.getMessage();
            } else if (p.getNumericCommand() == IrcPacket.RPL_TOPIC) {
                channel = c.getState().getChannel(p.getArgumentsArray()[1]);
                sender  = null;
                topic   = p.getMessage();
            } else {
                channel = null;
                sender  = null;
                topic   = null;
            }
        }
    }

    /**
     * Someone (possibly us) was invited into a channel.
     */
	void onInvite(Invite invite);

    /**
     * Someone (possibly us) joined a channel.
     */
	void onJoin(Join join);

    /**
     * Someone (possibly us) was kicked from a channel.
     * <p>
     * <strong>Note:</strong> This method does NOT return a shared
     * user object. That means that it isn't possible to retrieve the
     * user prefix (or any modes).
     * </p>
     */
	void onKick(Kick kick);

	/**
	 * Someone (possibly us) changed a channel mode.
	 */
	void onMode(Mode mode);

	/**
	 * Someone (possibly us) changed his nickname. Note that the
	 * {@code oldUser} can not be used to send messages, as that
	 * nickname no longer exists.
	 * <p>
	 * <strong>Note:</strong> This method does NOT return a shared
	 * sender object. That means that it isn't possible to retrieve the
	 * sender prefix (or any modes).
	 * </p>
	 */
	void onNick(Nick nick);

	/**
	 * Someone (possibly us) left a channel.
	 * <p>
	 * <strong>Note:</strong> This method does NOT return a shared
	 * sender object. That means that it isn't possible to retrieve the
	 * sender prefix (or any modes).
	 * </p>
	 */
	void onPart(Part part);

	/**
	 * Someone quit the IRC server.
	 */
	void onQuit(Quit quit);

	/**
	 * Someone (possibly us) changed the topic of a channel, or we
	 * joined a new channel and discovered the topic. The {@code
	 * sender} will be {@code null} when we discovered the topic after
	 * joining.
	 */
	void onTopic(Topic topic);
}
