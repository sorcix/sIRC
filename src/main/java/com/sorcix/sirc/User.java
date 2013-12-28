/*
 * User.java
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

/**
 * Represents a user on the IRC server.
 * 
 * @author Sorcix
 */
public final class User {
	
	/** Hostname of this user (or null if unknown). */
	private final String hostName;
	/** IrcConnection used to contact this user. */
	private final IrcConnection irc;
	/** Nickname of this user. */
	private String nick;
	/** Lowercase nickname of this user. */
	private String nickLower;
	/** The prefix. */
	private char prefix;
	/** Custom address to send messages to. */
	private String address = null;
	/** Username of this user (or null if unknown). */
	private final String userName;
	/** Mode character for voice. */
	protected static final char MODE_VOICE = 'v';
	/** Mode character for operator. */
	protected static final char MODE_OPERATOR = 'o';
	/** Mode character for half-op. (Not supported by RFC!) */
	protected static final char MODE_HALF_OP = 'h';
	/** Mode character for founder. (Not supported by RFC!) */
	protected static final char MODE_FOUNDER = 'q';
	/** Mode character for admin. (Not supported by RFC!) */
	protected static final char MODE_ADMIN = 'a';
	/** Prefix character for half-op. (Not supported by RFC!) */
	protected static final char PREFIX_HALF_OP = '%';
	/** Prefix character for founder. (Not supported by RFC!) */
	protected static final char PREFIX_FOUNDER = '~';
	/** Prefix character for admin. (Not supported by RFC!) */
	protected static final char PREFIX_ADMIN = '&';
	/** Prefix character for voice. */
	protected static final char PREFIX_VOICE = '+';
	/** Prefix character for operator. */
	protected static final char PREFIX_OPERATOR = '@';
	/** Possible user prefixes. */
	protected static final String USER_PREFIX = "~@%+&";

	private final String realName;

	/**
	 * Creates a new {@code User}.
	 * 
	 * @param nick The nickname.
	 * @param irc The IrcConnection used to send messages to this
	 *            user.
	 */
	public User(final String nick, final IrcConnection irc) {
		this(nick, null, null, null, irc);
	}
	
	/**
	 * Creates a new {@code User}.
	 * 
	 * @param nick The nickname.
	 * @param user The username.
	 * @param host The hostname.
	 * @param realName The 'real name'.
	 * @param irc The IrcConnection used to send messages to this
	 *            user.
	 */
	protected User(final String nick, final String user, final String host, final String realName, final IrcConnection irc) {
		setNick(nick);
		this.realName = realName;
		this.irc = irc;
		userName = user;
		hostName = host;
		address = getNick();
	}
	
	@Override
	public boolean equals(final Object user) {
		try {
			return ((User) user).getNick().equalsIgnoreCase(nick);
		} catch (final Exception ex) {
			return false;
		}
	}
	
	/**
	 * Returns the address sIRC uses to send messages to this user.
	 * @return The address used to send messages to this user.
	 */
	private String getAddress() {
		return address;
	}
	
	/**
	 * Returns the hostname for this user.
	 * 
	 * @return The hostname, or null if unknown.
	 */
	public String getHostName() {
		return hostName;
	}
	
	/**
	 * Returns the nickname for this user.
	 * 
	 * @return The nickname.
	 */
	public String getNick() {
		return nick;
	}
	
	/**
	 * Returns the lowercase nickname for this user.
	 * 
	 * @return Lowercase nickname.
	 */
	public String getNickLower() {
		return nickLower;
	}
	
	/**
	 * Returns this user's prefix.
	 * 
	 * @return The prefix.
	 */
	public char getPrefix() {
		return prefix;
	}
	
	/**
	 * Returns the username for this user.
	 * 
	 * @return The username.
	 */
	public String getUserName() {
		return userName;
	}

	public String getRealName() {
		return realName != null ? realName : nick;
	}

	/**
	 * Checks whether this user has Admin privileges.
	 * 
	 * @return True if this user is an admin.
	 * @since 1.1.0
	 */
	public boolean hasAdmin() {
		return getPrefix() == User.PREFIX_ADMIN;
	}
	
	/**
	 * Checks whether this user has Founder privileges.
	 * 
	 * @return True if this user is a founder.
	 * @since 1.1.0
	 */
	public boolean hasFounder() {
		return getPrefix() == User.PREFIX_FOUNDER;
	}
	
	/**
	 * Checks whether this user has Halfop privileges.
	 * 
	 * @return True if this user is a half operator.
	 * @since 1.1.0
	 */
	public boolean hasHalfOp() {
		return getPrefix() == User.PREFIX_HALF_OP;
	}
	
	/**
	 * Checks whether this user has Operator privileges.
	 * 
	 * @return True if this user is an operator.
	 * @since 1.1.0
	 */
	public boolean hasOperator() {
		return getPrefix() == User.PREFIX_OPERATOR;
	}
	
	/**
	 * Checks whether this user has Voice privileges.
	 * 
	 * @return True if this user has voice.
	 * @since 1.1.0
	 */
	public boolean hasVoice() {
		return getPrefix() == User.PREFIX_VOICE;
	}
	
	/**
	 * Checks if this {@code User} represents us.
	 * 
	 * @return True if this {@code User} represents us, false
	 *         otherwise.
	 * @see IrcConnection#isUs(User)
	 */
	public boolean isUs() {
		return irc.isUs(this);
	}
	
	/**
	 * Send message to user.
	 * 
	 * @param message The message to send.
	 * @see #sendMessage(String)
	 */
	public void send(final String message) {
		sendMessage(message);
	}
	
	/**
	 * Sends an action.
	 * 
	 * @param action The action to send.
	 */
	public void sendAction(final String action) {
		sendCtcpAction(action);
	}
	
	/**
	 * Sends CTCP request. This is a very primitive way to send CTCP
	 * commands, other methods are preferred.
	 * 
	 * @param command Command to send.
	 */
	public void sendCtcp(final String command) {
		irc.getOutput().send(new IrcPacket(null, "PRIVMSG", getAddress(), IrcPacket.CTCP + command + IrcPacket.CTCP));
	}
	
	/**
	 * Sends a CTCP ACTION command.
	 * 
	 * @param action The action to send.
	 * @see #sendCtcp(String)
	 */
	protected void sendCtcpAction(final String action) {
		if ((action != null) && (action.length() != 0)) {
			sendCtcp("ACTION " + action);
		}
	}
	
	/**
	 * Sends a CTCP CLIENTINFO command.
	 */
	public void sendCtcpClientInfo() {
		sendCtcp("CLIENTINFO");
	}
	
	/**
	 * Sends a CTCP PING command.
	 * 
	 * @return The timestamp sent to this user.
	 */
	public long sendCtcpPing() {
		final Long time = System.currentTimeMillis();
		sendCtcp("PING " + time.toString());
		return time;
	}
	
	/**
	 * Sends CTCP reply using notices. Replies to CTCP requests should
	 * be sent using a notice.
	 * 
	 * @param command Command to send.
	 */
	protected void sendCtcpReply(final String command) {
		sendCtcpReply(command, false);
	}
	
	/**
	 * Sends CTCP reply using notices. Replies to CTCP requests should
	 * be sent using a notice.
	 * 
	 * @param command Command to send.
	 * @param skipQueue Whether to skip the outgoing message queue.
	 */
	protected void sendCtcpReply(final String command, final boolean skipQueue) {
		if (skipQueue) {
			irc.getOutput().sendNow(new IrcPacket(null, "NOTICE", getAddress(), IrcPacket.CTCP + command + IrcPacket.CTCP));
		} else {
			irc.getOutput().send(new IrcPacket(null, "NOTICE", getAddress(), IrcPacket.CTCP + command + IrcPacket.CTCP));
		}
	}
	
	/**
	 * Sends a CTCP VERSION command to this user.
	 */
	public void sendCtcpVersion() {
		sendCtcp("VERSION");
	}
	
	/**
	 * Send message to this user.
	 * 
	 * @param message The message to send.
	 */
	public void sendMessage(final String message) {
		irc.getOutput().send(new IrcPacket(null, "PRIVMSG", getAddress(), message));
	}
	
	/**
	 * Send notice to this user.
	 * 
	 * @param message The notice to send.
	 */
	public void sendNotice(final String message) {
		irc.getOutput().send(new IrcPacket(null, "NOTICE", getAddress(), message));
	}
	
	/**
	 * Sets a custom address for this user. This address will be used
	 * to send messages to instead of simply the nickname. Use an
	 * address like {@code nick@server}. Setting the address to
	 * {@code @server} will prepend the nick automatically.
	 * 
	 * @param address The address to use.
	 */
	public void setCustomAddress(final String address) {
		if (address == null) {
			this.address = getNick();
		} else if (address.startsWith("@")) {
			this.address = getNick() + address;
		} else {
			this.address = address;
		}
	}
	
	/**
	 * Changes a user mode for given user.
	 * 
	 * @param mode The mode character.
	 * @param toggle True to enable the mode, false to disable.
	 */
	public void setMode(final char mode, final boolean toggle) {
		if (toggle) {
			setMode("+" + mode);
		} else {
			setMode("-" + mode);
		}
	}
	
	/**
	 * Changes a user mode. The address is automatically added.
	 * 
	 * <pre>
	 * setMode(&quot;+m&quot;);
	 * </pre>
	 * 
	 * @param mode The mode to change.
	 */
	public void setMode(final String mode) {
		irc.getOutput().send(new IrcPacket(null, "MODE", getAddress() + mode, null));
	}
	
	/**
	 * Changes the nickname of this user.
	 * 
	 * @param nick The new nickname.
	 */
	protected void setNick(String nick) {
		if (nick == null)
			return;
		if (User.USER_PREFIX.indexOf(nick.charAt(0)) >= 0) {
			prefix = nick.charAt(0);
			nick = nick.substring(1);
		}
		this.nick = nick;
		nickLower = nick.toLowerCase();
		// TODO: Check whether addresses like nick!user@server are
		// allowed
		if ((address != null) && address.contains("@")) {
			address = this.nick + "@" + address.split("@", 2)[1];
		} else {
			address = this.nick;
		}
	}
	
	@Override
	public String toString() {
		return getNick();
	}
	
	/**
	 * Updates this User object with data from given User.
	 * 
	 * @param user The fresh User object.
	 */
	protected void updateUser(final User user) {
		//TODO: Unfinished method?
	}
}
