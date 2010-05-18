/*
 * UserListTest.java
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
package com.sorcix.sirc.test;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Iterator;
import com.sorcix.sirc.Channel;
import com.sorcix.sirc.IrcAdaptor;
import com.sorcix.sirc.IrcConnection;
import com.sorcix.sirc.IrcDebug;
import com.sorcix.sirc.NickNameException;
import com.sorcix.sirc.User;
import com.sorcix.sirc.examples.ExampleBot;

/**
 * Joins a few channels and prints the user list when someone sends
 * !test. Based on {@link ExampleBot}.
 * 
 * @author Sorcix
 */
public class UserListTest extends IrcAdaptor {
	
	/**
	 * Main method. We just launch our bot class.
	 * 
	 * @param args
	 */
	public static void main(final String[] args) {
		new UserListTest();
	}
	
	/** An instance of the IrcConnection we use. */
	private final IrcConnection irc;
	
	/**
	 * In our constructor, basically need to set up sIRC and connect
	 * to the server.
	 */
	public UserListTest() {
		IrcDebug.setEnabled(true);
		// The server details can be given through the constructor.
		this.irc = new IrcConnection("irc.sorcix.com");
		// Choose the nick for our bot.
		this.irc.setNick("UserListTest");
		// We are using a serverlistener and messagelistener in this
		// example. As we extend IrcAdaptor, both these classes are
		// already implemented.
		this.irc.addMessageListener(this);
		this.irc.addServerListener(this);
		try {
			// Attempt to connect to the server.
			this.irc.connect();
			// If anything goes wrong here, we just stop our
			// application.
		} catch (final UnknownHostException e) {
			e.printStackTrace();
			System.exit(1);
		} catch (final IOException e) {
			e.printStackTrace();
			System.exit(1);
		} catch (final NickNameException e) {
			// When this exception occurs,
			// you could change the nickname and try again.
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	@Override
	public void onConnect(final IrcConnection irc) {
		// The onConnect event is called when sIRC established
		// a connection with the IRC server.
		irc.createChannel("#sirc").join();
		irc.createChannel("#general").join();
	}
	
	@Override
	public void onMessage(final IrcConnection irc, final User sender, final Channel target, final String message) {
		if (message.equalsIgnoreCase("!test")) {
			final Iterator<Channel> ch = irc.getState().getChannels();
			Iterator<User> us;
			Channel current;
			User u;
			while (ch.hasNext()) {
				current = ch.next();
				System.out.println("======================");
				System.out.println(current.getName());
				System.out.println("----------------------");
				us = current.getUsers();
				while (us.hasNext()) {
					u = us.next();
					System.out.println(u.getNick() + " (Prefix: " + u.getPrefix() + ")");
				}
			}
		}
	}
}
