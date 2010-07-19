/*
 * IrcParser.java
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

import java.util.Date;
import java.util.Iterator;

/**
 * Parses incoming messages and calls event handlers.
 * 
 * @author Sorcix
 */
final class IrcParser {
	
	/** Buffer for motd. */
	private static StringBuffer buffer = null;
	
	/**
	 * Parses normal IRC commands.
	 * 
	 * @param irc IrcConnection receiving this line.
	 * @param line The input line.
	 */
	protected static void parseCommand(final IrcConnection irc, final IrcDecoder line) {
		if (line.getCommand().equals("PRIVMSG") && (line.getArguments() != null)) {
			if (line.isCtcp()) {
				// reply to CTCP commands
				if (line.getMessage().startsWith("ACTION ")) {
					if (Channel.CHANNEL_PREFIX.indexOf(line.getArguments().charAt(0)) >= 0) {
						// to channel
						final Channel chan = irc.getState().getChannel(line.getArguments());
						for (final Iterator<MessageListener> it = irc.getMessageListeners(); it.hasNext();) {
							it.next().onAction(irc, chan.updateUser(line.getSenderUser(), true), chan, line.getMessage().substring(7));
						}
					} else {
						// to user
						for (final Iterator<MessageListener> it = irc.getMessageListeners(); it.hasNext();) {
							it.next().onAction(irc, line.getSenderUser(), line.getMessage().substring(7));
						}
					}
				} else if (line.getMessage().equals("VERSION") || line.getMessage().equals("FINGER")) {
					// send custom version string
					line.getSenderUser().sendCtcpReply("VERSION " + irc.getVersion());
				} else if (line.getMessage().equals("SIRCVERS")) {
					// send sIRC version information
					line.getSenderUser().sendCtcpReply("SIRCVERS " + IrcConnection.ABOUT);
				} else if (line.getMessage().equals("TIME")) {
					// send current date&time
					line.getSenderUser().sendCtcpReply(new Date().toString());
				} else if (line.getMessage().startsWith("PING ")) {
					// send ping reply
					line.getSenderUser().sendCtcpReply("PING " + line.getMessage().substring(5), true);
				} else if (line.getMessage().startsWith("SOURCE")) {
					// send sIRC source
					line.getSenderUser().sendCtcpReply("SOURCE http://j-sirc.googlecode.com");
				} else if (line.getMessage().equals("CLIENTINFO")) {
					// send client info
					line.getSenderUser().sendCtcpReply("CLIENTINFO VERSION TIME PING SOURCE FINGER SIRCVERS");
				} else {
					// send error message
					line.getSenderUser().sendCtcpReply("ERRMSG CTCP Command not supported. Use CLIENTINFO to list supported commands.");
				}
				return;
			} else if (line.getArguments().startsWith("#")) {
				// to channel
				final Channel chan = irc.getState().getChannel(line.getArguments());
				for (final Iterator<MessageListener> it = irc.getMessageListeners(); it.hasNext();) {
					it.next().onMessage(irc, chan.updateUser(line.getSenderUser(), true), chan, line.getMessage());
				}
				return;
			} else {
				// to user
				for (final Iterator<MessageListener> it = irc.getMessageListeners(); it.hasNext();) {
					it.next().onPrivateMessage(irc, line.getSenderUser(), line.getMessage());
				}
				return;
			}
		} else if (line.getCommand().equals("NOTICE") && (line.getArguments() != null)) {
			if (line.isCtcp()) {
				// receive CTCP replies.
				final int cmdPos = line.getMessage().indexOf(' ');
				final String command = line.getMessage().substring(0, cmdPos);
				final String args = line.getMessage().substring(cmdPos + 1);
				if (command.equals("VERSION") || command.equals("PING") || command.equals("CLIENTINFO")) {
					for (final Iterator<MessageListener> it = irc.getMessageListeners(); it.hasNext();) {
						it.next().onCtcpReply(irc, line.getSenderUser(), command, args);
					}
				}
				return;
			} else if (Channel.CHANNEL_PREFIX.indexOf(line.getArguments().charAt(0)) >= 0) {
				// to channel
				final Channel chan = irc.getState().getChannel(line.getArguments());
				for (final Iterator<MessageListener> it = irc.getMessageListeners(); it.hasNext();) {
					it.next().onNotice(irc, chan.updateUser(line.getSenderUser(), true), chan, line.getMessage());
				}
			} else {
				// to user
				for (final Iterator<MessageListener> it = irc.getMessageListeners(); it.hasNext();) {
					it.next().onNotice(irc, line.getSenderUser(), line.getMessage());
				}
			}
			return;
		} else if (line.getCommand().equals("JOIN")) {
			// some server seem to send the joined channel as message,
			// while others have it as an argument. (quakenet related)
			String channel;
			if (line.hasMessage()) {
				channel = line.getMessage();
			} else {
				channel = line.getArguments();
			}
			// someone joined a channel
			if (line.getSenderUser().isUs()) {
				// if the user joining the channel is the client
				// we need to add it to the channel list.
				irc.getState().addChannel(new Channel(channel, irc, true));
			} else {
				// add user to channel list.
				irc.getState().getChannel(channel).addUser(line.getSenderUser());
			}
			for (final Iterator<ServerListener> it = irc.getServerListeners(); it.hasNext();) {
				it.next().onJoin(irc, irc.getState().getChannel(line.getMessage()), line.getSenderUser());
			}
			return;
		} else if (line.getCommand().equals("PART")) {
			// someone left a channel
			if (line.getSenderUser().isUs()) {
				// if the user leaving the channel is the client
				// we need to remove it from the channel list
				irc.getState().removeChannel(line.getArguments());
				// run garbage collection
				irc.garbageCollection();
			} else {
				// remove user from channel list.
				irc.getState().getChannel(line.getArguments()).removeUser(line.getSenderUser());
			}
			for (final Iterator<ServerListener> it = irc.getServerListeners(); it.hasNext();) {
				it.next().onPart(irc, irc.getState().getChannel(line.getArguments()), line.getSenderUser(), line.getMessage());
			}
			return;
		} else if (line.getCommand().equals("QUIT")) {
			// someone quit the IRC server
			for (final Iterator<ServerListener> it = irc.getServerListeners(); it.hasNext();) {
				it.next().onQuit(irc, line.getSenderUser(), line.getMessage());
			}
			return;
		} else if (line.getCommand().equals("KICK")) {
			// someone was kicked from a channel
			final String[] data = line.getArgumentsArray();
			final User kicked = new User(data[1], irc);
			final Channel channel = irc.getState().getChannel(data[0]);
			if (kicked.isUs()) {
				// if the user leaving the channel is the client
				// we need to remove it from the channel list
				irc.getState().removeChannel(data[0]);
			} else {
				// remove user from channel list.
				channel.removeUser(kicked);
			}
			for (final Iterator<ServerListener> it = irc.getServerListeners(); it.hasNext();) {
				it.next().onKick(irc, channel, line.getSenderUser(), kicked);
			}
			return;
		} else if (line.getCommand().equals("MODE")) {
			IrcParser.parseMode(irc, line);
			return;
		} else if (line.getCommand().equals("TOPIC")) {
			// someone changed the topic.
			for (final Iterator<ServerListener> it = irc.getServerListeners(); it.hasNext();) {
				final Channel chan = irc.getState().getChannel(line.getArguments());
				it.next().onTopic(irc, chan, chan.updateUser(line.getSenderUser(), false), line.getMessage());
			}
			return;
		} else if (line.getCommand().equals("NICK")) {
			User newUser;
			if (line.hasMessage()) {
				newUser = new User(line.getMessage(), irc);
			} else {
				newUser = new User(line.getArguments(), irc);
			}
			// someone changed his nick
			for (final Iterator<Channel> it = irc.getState().getChannels(); it.hasNext();) {
				it.next().renameUser(line.getSenderUser().getNickLower(), newUser.getNick());
			}
			// change local user
			if (line.getSenderUser().isUs()) {
				irc.getState().getClient().setNick(newUser.getNick());
			}
			for (final Iterator<ServerListener> it = irc.getServerListeners(); it.hasNext();) {
				it.next().onNick(irc, line.getSenderUser(), newUser);
			}
			return;
		} else if (line.getCommand().equals("INVITE")) {
			// someone was invited
			final String[] args = line.getArgumentsArray();
			if ((args.length >= 2) && (line.getMessage() == null)) {
				final Channel channel = irc.createChannel(args[1]);
				for (final Iterator<ServerListener> it = irc.getServerListeners(); it.hasNext();) {
					it.next().onInvite(irc, line.getSenderUser(), new User(args[0], irc), channel);
				}
			}
			return;
		} else {
			if (irc.getAdvancedListener() != null) {
				irc.getAdvancedListener().onUnknown(irc, line);
			}
		}
	}
	
	/**
	 * Parses mode changes.
	 * 
	 * @param irc IrcConnection receiving this line.
	 * @param line The mode change line.
	 */
	private static void parseMode(final IrcConnection irc, final IrcDecoder line) {
		final String[] args = line.getArgumentsArray();
		if ((args.length >= 2) && (Channel.CHANNEL_PREFIX.indexOf(args[0].charAt(0)) >= 0)) {
			// general mode event listener
			for (final Iterator<ServerListener> it = irc.getServerListeners(); it.hasNext();) {
				it.next().onMode(irc, irc.getState().getChannel(args[0]), line.getSenderUser(), line.getArguments().substring(args[0].length() + 1));
			}
			if ((args.length >= 3)) {
				final Channel channel = irc.getState().getChannel(args[0]);
				final String mode = args[1];
				final boolean enable = mode.charAt(0) == '+' ? true : false;
				char current;
				// tries all known modes.
				// this is an ugly part of sIRC, but the only way to
				// do this.
				for (int x = 2; x < args.length; x++) {
					current = mode.charAt(x - 1);
					if (current == User.MODE_VOICE) {
						// voice or devoice
						irc.askNames(channel);
						if (enable) {
							for (final Iterator<ModeListener> it = irc.getModeListeners(); it.hasNext();) {
								it.next().onVoice(irc, channel, line.getSenderUser(), irc.createUser(args[x]));
							}
						} else {
							for (final Iterator<ModeListener> it = irc.getModeListeners(); it.hasNext();) {
								it.next().onDeVoice(irc, channel, line.getSenderUser(), irc.createUser(args[x]));
							}
						}
					} else if (current == User.MODE_ADMIN) {
						// admin or deadmin
						irc.askNames(channel);
						if (enable) {
							for (final Iterator<ModeListener> it = irc.getModeListeners(); it.hasNext();) {
								it.next().onAdmin(irc, channel, line.getSenderUser(), irc.createUser(args[x]));
							}
						} else {
							for (final Iterator<ModeListener> it = irc.getModeListeners(); it.hasNext();) {
								it.next().onDeAdmin(irc, channel, line.getSenderUser(), irc.createUser(args[x]));
							}
						}
					} else if (current == User.MODE_OPERATOR) {
						// op or deop
						irc.askNames(channel);
						if (enable) {
							for (final Iterator<ModeListener> it = irc.getModeListeners(); it.hasNext();) {
								it.next().onOp(irc, channel, line.getSenderUser(), irc.createUser(args[x]));
							}
						} else {
							for (final Iterator<ModeListener> it = irc.getModeListeners(); it.hasNext();) {
								it.next().onDeOp(irc, channel, line.getSenderUser(), irc.createUser(args[x]));
							}
						}
					} else if (current == User.MODE_HALF_OP) {
						// halfop or dehalfop
						irc.askNames(channel);
						if (enable) {
							for (final Iterator<ModeListener> it = irc.getModeListeners(); it.hasNext();) {
								it.next().onHalfop(irc, channel, line.getSenderUser(), irc.createUser(args[x]));
							}
						} else {
							for (final Iterator<ModeListener> it = irc.getModeListeners(); it.hasNext();) {
								it.next().onDeHalfop(irc, channel, line.getSenderUser(), irc.createUser(args[x]));
							}
						}
					} else if (current == User.MODE_FOUNDER) {
						// founder or defounder
						irc.askNames(channel);
						if (enable) {
							for (final Iterator<ModeListener> it = irc.getModeListeners(); it.hasNext();) {
								it.next().onFounder(irc, channel, line.getSenderUser(), irc.createUser(args[x]));
							}
						} else {
							for (final Iterator<ModeListener> it = irc.getModeListeners(); it.hasNext();) {
								it.next().onDeFounder(irc, channel, line.getSenderUser(), irc.createUser(args[x]));
							}
						}
					}
				}
			}
		}
	}
	
	/**
	 * Parses numeric IRC replies.
	 * 
	 * @param irc IrcConnection receiving this line.
	 * @param line The input line.
	 */
	protected static void parseNumeric(final IrcConnection irc, final IrcDecoder line) {
		switch (line.getNumericCommand()) {
			case IrcDecoder.RPL_TOPIC:
				for (final Iterator<ServerListener> it = irc.getServerListeners(); it.hasNext();) {
					it.next().onTopic(irc, irc.getState().getChannel(line.getArgumentsArray()[1]), null, line.getMessage());
				}
				break;
			case IrcDecoder.RPL_NAMREPLY:
				final String[] arguments = line.getArgumentsArray();
				final Channel channel = irc.getState().getChannel(arguments[arguments.length - 1]);
				if (channel != null) {
					final String[] users = line.getMessage().split(" ");
					User buffer;
					for (final String user : users) {
						buffer = new User(user, irc);
						/*
						 * if (channel.hasUser(buffer)) {
						 * channel.addUser(buffer); }
						 * channel.addUser(buffer);
						 */
						channel.updateUser(buffer, true);
					}
				}
				break;
			case IrcDecoder.RPL_MOTD:
				if (IrcParser.buffer == null) {
					IrcParser.buffer = new StringBuffer();
				}
				IrcParser.buffer.append(line.getMessage());
				IrcParser.buffer.append(IrcConnection.ENDLINE);
				break;
			case IrcDecoder.RPL_ENDOFMOTD:
				if (IrcParser.buffer != null) {
					final String motd = IrcParser.buffer.toString();
					IrcParser.buffer = null;
					for (final Iterator<ServerListener> it = irc.getServerListeners(); it.hasNext();) {
						it.next().onMotd(irc, motd);
					}
				}
				break;
			case IrcDecoder.RPL_BOUNCE:
				// redirect to another server.
				if (irc.isBounceAllowed()) {
					irc.disconnect();
					irc.setServer(new IrcServer(line.getArgumentsArray()[0], line.getArgumentsArray()[1]));
					try {
						irc.connect();
					} catch (final Exception ex) {
						// TODO: exception while connecting to new
						// server?
					}
				}
				break;
			default:
				if (irc.getAdvancedListener() != null) {
					irc.getAdvancedListener().onUnknown(irc, line);
				}
		}
	}
}