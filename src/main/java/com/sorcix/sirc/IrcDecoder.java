/*
 * IrcDecoder.java
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
 * Parses a raw server response into a more readable format.
 * 
 * <pre>
 * :&lt;prefix&gt; &lt;command&gt; &lt;receiver&gt; [&lt;arguments&gt;] [:&lt;message&gt;]
 * </pre>
 * 
 * @author Sorcix
 * @see AdvancedListener
 */
public final class IrcDecoder {
	
	/** Arguments separated by a space */
	private String arguments = null;
	/** The numeric server reply. */
	private int cmdNumeric = -1;
	/** The IRC command. */
	private String command = null;
	/** Whether this is a CTCP command or not. */
	private boolean ctcp = false;
	/** The message (basically anything behind the colon). */
	private String message = null;
	/** Whether this is a numeric server reply. */
	private boolean numeric;
	/** The sender. */
	private String sender = null;
	/** The sender user object. */
	private User senderUser = null;
	/** Unknown command sent to IRC server. */
	protected static final int ERR_UNKNOWNCOMMAND = 421;
	/** Termination of an RPL_MOTD list. */
	protected static final int RPL_ENDOFMOTD = 376;
	/** Reply to MOTD. (message of the day) */
	protected static final int RPL_MOTD = 372;
	/** Response to TOPIC with the set topic. */
	protected static final int RPL_TOPIC = 332;
	/** Termination of an RPL_NAMREPLY list. */
	protected static final int RPL_ENDOFNAMES = 366;
	/** Reply to NAMES (See RFC). */
	protected static final int RPL_NAMREPLY = 353;
	/** Sent to the client to redirect it to another server. */
	protected static final int RPL_BOUNCE = 10;
	/** CTCP message mark. */
	protected static final String CTCP = "\u0001";
	
	/**
	 * Creates a new IrcParser object for given raw server line.
	 * 
	 * @param line Raw server line.
	 * @param irc The IrcConnection used to send messages.
	 */
	protected IrcDecoder(String line, final IrcConnection irc) {
		line = IrcColors.remove(line);
		final int locLineStart = line.indexOf(':') + 1;
		int locCommand;
		// some messages don't have a prefix
		if ((locLineStart > 1) || (locLineStart < 0)) {
			locCommand = 0;
			this.sender = null;
		} else {
			// space between sender and command
			locCommand = line.indexOf(' ', locLineStart + 1);
			// retrieve sender
			this.sender = line.substring(locLineStart, locCommand);
		}
		// space between command and receiver
		final int locArgs = line.indexOf(' ', locCommand + 1);
		// retrieve command
		this.command = line.substring(locCommand + 1, locArgs);
		// colon between arguments and message
		final int locMsg = line.indexOf(':', locArgs);
		// if there are arguments, save them
		if (locMsg - locArgs > 1) {
			this.arguments = line.substring(locArgs + 1, locMsg - 1);
		} else if (locMsg < 0) {
			// there is no message, so arguments go to the end
			this.arguments = line.substring(locArgs + 1);
		}
		// If there is a message, save it
		if (locMsg > 0) {
			this.message = line.substring(locMsg + 1);
			// check if this message is a CTCP request
			if (this.message.startsWith(IrcDecoder.CTCP) && this.message.endsWith(IrcDecoder.CTCP)) {
				this.ctcp = true;
				this.message = this.message.substring(1, this.message.length() - 1);
			}
		}
		// check if the command is a server reply
		this.cmdNumeric = this.getInteger(this.command);
		if (this.cmdNumeric != -1) {
			// numeric server response
			this.numeric = true;
		} else {
			// normal message
			this.numeric = false;
		}
		// if possible, parse the sender into a user object
		if ((this.sender != null) && (this.sender.indexOf('!') > 0)) {
			final String[] stuff = this.sender.split("@|!");
			if (stuff.length == 3) {
				this.senderUser = new User(stuff[0], stuff[1], stuff[2], irc);
			}
		}
	}
	
	/**
	 * Gives the arguments parsed from this raw server line.
	 * 
	 * @return Arguments string, or {@code null} if there were none.
	 */
	public String getArguments() {
		return this.arguments;
	}
	
	/**
	 * Gives the arguments as an array.
	 * 
	 * @return Arguments array, or {@code null} if there were none.
	 */
	public String[] getArgumentsArray() {
		if (this.arguments == null) {
			return null;
		}
		return this.arguments.split(" ");
	}
	
	/**
	 * Gives the command parsed from this raw server line.
	 * 
	 * @return The command string.
	 */
	public String getCommand() {
		return this.command;
	}
	
	/**
	 * Tries to parse given string to an integer.
	 * 
	 * @param parse String to parse.
	 * @return Integer value, or -1 if the string is not an integer.
	 */
	private int getInteger(final String parse) {
		try {
			return Integer.parseInt(parse);
		} catch (final NumberFormatException ex) {
			return -1;
		}
	}
	
	/**
	 * Gives the message parsed from this raw server line.
	 * 
	 * @return Message string, or {@code null} if there was none.
	 */
	public String getMessage() {
		return this.message;
	}
	
	/**
	 * Gives the command parsed from this raw server line.
	 * 
	 * @return The command integer.
	 */
	public int getNumericCommand() {
		return this.cmdNumeric;
	}
	
	/**
	 * Gives the sender parsed from this raw server line. For commands
	 * that return users, it might be better to use
	 * {@link #getSenderUser()} instead.
	 * 
	 * @return The sender string.
	 */
	public String getSender() {
		return this.sender;
	}
	
	/**
	 * Gives the Sender {@link User} from this raw server line.
	 * 
	 * @return Sender {@link User}, or {@code null} if the sender was
	 *         not a user.
	 */
	public User getSenderUser() {
		return this.senderUser;
	}
	
	/**
	 * Checks whether this line had arguments.
	 * 
	 * @return True if there were arguments.
	 */
	public boolean hasArguments() {
		return (this.arguments != null) && (this.arguments.length() > 0);
	}
	
	/**
	 * Checks whether this line had a message.
	 * 
	 * @return True if there was a message.
	 */
	public boolean hasMessage() {
		return (this.message != null) && (this.message.trim().length() > 0);
	}
	
	/**
	 * Checks whether this command is a CTCP command.
	 * 
	 * @return True if this message was sent using CTCP, false
	 *         otherwise.
	 */
	public boolean isCtcp() {
		return this.ctcp;
	}
	
	/**
	 * Checks if this server line is a numeric reply.
	 * 
	 * @return True if this line is a numeric reply.
	 */
	public boolean isNumeric() {
		return this.numeric;
	}
}