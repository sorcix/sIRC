/*
 * ClientState.java
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
package com.sorcix.sirc;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Contains information about an {@link IrcConnection}.
 * 
 * @author Sorcix
 * @since 1.1.0
 */
public final class ClientState {

	// TODO: Allow changing the username (sIRC@..)
	/** The list of channels. */
	private final Map<String, Channel> channels;
	/** Contains a singleton for all known users. */
	private final Map<String, User> users;
	/** The local user. */
	private User client;

	/**
	 * Creates a new ClientState.
	 */
	protected ClientState() {
		channels = new HashMap<String, Channel>();
		users = new HashMap<String, User>();
	}

	/**
	 * Adds a channel to the channel map.
	 * 
	 * @param channel
	 *            The channel to add.
	 */
	protected void addChannel(final Channel channel) {
		if (!channels.containsKey(channel.getName().toLowerCase())) {
			channels.put(channel.getName().toLowerCase(), channel);
		}
	}

	/**
	 * Adds a user to the user map.
	 * 
	 * @param user
	 *            The user to add.
	 */
	protected void addUser(final User user) {
		if (!users.containsKey(user.getNickLower())) {
			users.put(user.getNickLower(), user);
		}
	}

	/**
	 * Retrieves a shared channel object from the channel map.
	 * 
	 * @param channel
	 *            A channel object representing this channel.
	 * @return The channel, or null if this channel doesn't exist. (The local
	 *         user is not in that channel)
	 * @see #getChannel(String)
	 */
	protected Channel getChannel(final Channel channel) {
		return getChannel(channel.getName());
	}

	/**
	 * Retrieves a shared channel object from the channel map.
	 * 
	 * @param channel
	 *            The channel name.
	 * @return The channel, or null if this channel doesn't exist. (The local
	 *         user is not in that channel)
	 */
	protected Channel getChannel(final String channel) {
 		if (channel != null && channels.containsKey(channel.toLowerCase())) {
			return channels.get(channel.toLowerCase());
		}
		return null;
	}

	/**
	 * Creates an iterator through all Channels.
	 * 
	 * @return an iterator through all Channels.
	 */
	public Iterator<Channel> getChannels() {
		return channels.values().iterator();
	}

	/**
	 * Retrieves the local {@link User}.
	 * 
	 * @return The local {@code User}.
	 */
	public User getClient() {
		return client;
	}

	/**
	 * Retrieves a shared user object from the users map.
	 * 
	 * @param nick
	 *            The nickname of this user.
	 * @return The shared user object, or null if there is no singleton User
	 *         object for this user.
	 */
	protected User getUser(final String nick) {
		//TODO: implement singleton users in User, Channel and IrcConnection
		if (users.containsKey(nick)) {
			return users.get(nick);
		}
		return null;
	}

	/**
	 * Checks if given channel is in the channel map.
	 * 
	 * @param name
	 *            The name of this channel.
	 * @return True if the channel is in the list, false otherwise.
	 */
	protected boolean hasChannel(final String name) {
		return name != null && channels.containsKey(name.toLowerCase());
	}

	/**
	 * Remove all channels from the channel map.
	 */
	protected void removeAll() {
		channels.clear();
	}

	/**
	 * Removes a channel from the channel map.
	 * 
	 * @param channel
	 *            The channel name.
	 */
	protected void removeChannel(final String channel) {
		if (channel != null && channels.containsKey(channel.toLowerCase())) {
			channels.remove(channel.toLowerCase());
		}
	}

	/**
	 * Set the local {@link User}.
	 * 
	 * @param user
	 *            The local {@code User}.
	 */
	protected void setClient(final User user) {
		client = user;
	}
}
