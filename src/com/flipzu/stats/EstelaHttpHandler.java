package com.flipzu.stats;

/**
 * Copyright 2011 Flipzu
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  
 *  Initial Release: Dario Rapisardi <dario@rapisardi.org>
 *  
 */

import static org.jboss.netty.buffer.ChannelBuffers.buffer;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.UUID;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.handler.codec.http.DefaultHttpResponse;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.jboss.netty.handler.codec.http.HttpVersion;

/**
 * Handles HTTP requests
 * 
 * @author Dario Rapisardi <dario@rapisardi.org>
 * 
 */
public class EstelaHttpHandler extends SimpleChannelUpstreamHandler {
	private static final String serverName = Config.getInstance()
			.getServerName();

	private static final Debug debug = Debug.getInstance();

	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e)
			throws Exception {

		HttpRequest request = (HttpRequest) e.getMessage();

		debug.logHttp("GOT METHOD " + request.getMethod());

		// quit if not GET method
		if (!request.getMethod().toString().equals("GET")) {
			e.getChannel().close();
			return;
		}

		Request req = getRequest(request.getUri());
		debug.logHttp("GOT PATH " + req.getPath() + " KEY " + req.getKey()
				+ " VALUE " + req.getValue());

		/* Broadcast stats */
		if (req.getPath().equals("stats")) {
			Broadcast bcast;
			if (req.getKey().equals("bcast_id")) {
				Integer id;
				try {
					id = Integer.parseInt(req.getValue());
				} catch (NumberFormatException err) {
					sendHttp404(e);
					return;
				}
				if ((bcast = Stats.getInstance().getBroadcast(id)) != null) {
					debug.logHttp("GOT STATS FOR " + bcast.getUsername()
							+ " LISTENERS " + bcast.getListeners());
					sendStatsResponse(e, bcast);
					return;
				} else {
					debug.logHttp("NO STATS FOR " + id);
					sendHttp404(e);
					return;
				}
			}
			if (req.getKey().equals("username")) {
				if ((bcast = Stats.getInstance().getBroadcast(req.getValue())) != null) {
					debug.logHttp("GOT STATS FOR " + bcast.getUsername()
							+ " LISTENERS " + bcast.getListeners());
					sendStatsResponse(e, bcast);
					return;
				} else {
					debug.logHttp("NO STATS FOR " + req.getValue());
					sendHttp404(e);
					return;
				}
			}
		}

		/* global stats */
		if (req.getPath().equals("status")) {
			if (req.getKey().equals("secret")) {
				if (req.getValue().equals(Config.getInstance().getSecret())) {
					sendGlobalStats(e);
					return;
				} else {
					sendHttp404(e);
					return;
				}
			}
		}

		/* servers status */
		if (req.getPath().equals("servers")) {
			if (req.getKey().equals("secret")) {
				if (req.getValue().equals(Config.getInstance().getSecret())) {
					sendServerStats(e);
					return;
				} else {
					sendHttp404(e);
					return;
				}
			}
		}

		sendHttp404(e);

		ctx.sendUpstream(e);
	}

	/** 
	 * sends 404 response
	 * @param e Netty MessageEvent
	 */
	public void sendHttp404(MessageEvent e) {
		HttpResponse response = new DefaultHttpResponse(HttpVersion.HTTP_1_1,
				HttpResponseStatus.NOT_FOUND);
		e.getChannel().write(response);
		e.getChannel().close();
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) {
		debug.logError("EstelaHttpHandler, exceptionCaught() ", e.getCause());
		e.getChannel().close();
	}

	/**
	 * resturns an http Request object from URI
	 * @param uri a URL/URI
	 * @return a Request object
	 */
	private Request getRequest(String uri) {
		Request req = new Request();

		String url = uri.substring(1);

		String[] ret;

		ret = url.split("\\?", 2);
		String path = ret[0];
		req.setPath(path);

		if (ret.length == 2) {
			String[] args = ret[1].split("\\=", 2);
			if (args.length == 2) {
				req.setKey(args[0]);
				req.setValue(args[1]);
			}
		}

		return req;
	}

	/**
	 * write global stats to web client.
	 * @param e netty MessageEvent
	 */
	public void sendGlobalStats(MessageEvent e) {
		HttpResponse response = new DefaultHttpResponse(HttpVersion.HTTP_1_1,
				HttpResponseStatus.OK);
		response.setHeader(HttpHeaders.Names.SERVER, serverName);
		response.setHeader(HttpHeaders.Names.CONTENT_TYPE, "text/html");
		response.setHeader(HttpHeaders.Names.CONNECTION, "close");

		Hashtable<UUID, Broadcast> bcasts = Stats.getInstance().getBcasts();

		Enumeration<Broadcast> en = bcasts.elements();

		String msg = "<h1>Global Stats for Estela</h1>\r\n";

		msg += "<table>\r\n";
		msg += "<tr><th>Username</th><th>Broadcast ID</th><th>Listeners</th><th>UUID</th><th>Servers</th></tr>\r\n";
		while (en.hasMoreElements()) {
			Broadcast bcast = en.nextElement();
			ArrayList<EstelaServer> servers = bcast.getServers();
			msg += "<tr>";
			msg += "<td>" + bcast.getUsername() + "</td>";
			msg += "<td>" + bcast.getId() + "</td>";
			msg += "<td>" + bcast.getListeners() + "</td>";
			msg += "<td>" + bcast.getUuid() + "</td>";
			msg += "<td>" + servers + "</td>";
			msg += "</tr>";
			msg += "\r\n";
		}
		msg += "</table>\r\n";

		ChannelBuffer buf = str2cb(msg);

		response.setContent(buf);

		e.getChannel().write(response);

		e.getChannel().close();

	}

	/**
	 * write stats for a particular broadcast, in XML format
	 * @param e netty MessageEvent
	 * @param bcast Broadcast 
	 */
	public void sendStatsResponse(MessageEvent e, Broadcast bcast) {
		HttpResponse response = new DefaultHttpResponse(HttpVersion.HTTP_1_1,
				HttpResponseStatus.OK);
		response.setHeader(HttpHeaders.Names.SERVER, serverName);
		response.setHeader(HttpHeaders.Names.CONTENT_TYPE, "text/html");
		response.setHeader(HttpHeaders.Names.CONNECTION, "close");

		String msg = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n";
		msg += "<!DOCTYPE plist PUBLIC \"-//Apple Computer//DTD PLIST 1.0//EN\"\r\n";
		msg += "\"http://www.apple.com/DTDs/PropertyList-1.0.dtd\">\r\n";
		msg += "<plist version=\"1.0\">\r\n";
		msg += "<dict>\r\n";
		msg += "<key>listening</key>\r\n";
		msg += "<string>" + bcast.getListeners() + "</string>\r\n";
		msg += "</dict>\r\n";
		msg += "</plist>\r\n";
		msg += "\r\n";

		ChannelBuffer buf = str2cb(msg);

		response.setContent(buf);

		e.getChannel().write(response);

		e.getChannel().close();

	}

	/* make a ChannelBuffer from a String */
	private ChannelBuffer str2cb(String msg) {
		ChannelBuffer buf = buffer(msg.length());
		byte[] b = new byte[msg.length()];
		ByteArrayInputStream bais = null;
		try {
			bais = new ByteArrayInputStream(msg.getBytes("ISO-8859-1"));
		} catch (UnsupportedEncodingException e2) {
			debug.logError("EstelaHttpHandler, str2cb() exception ", e2);
		}
		try {
			if (bais != null)
				bais.read(b);
		} catch (IOException e1) {
			debug.logError("EstelaHttpHandler, str2cb() exception ", e1);
		}
		buf.writeBytes(b);

		return buf;
	}

	/**
	 * write stats for every server connected to estelaStats, into web client.
	 * @param e netty MessageEvent
	 */
	public void sendServerStats(MessageEvent e) {
		HttpResponse response = new DefaultHttpResponse(HttpVersion.HTTP_1_1,
				HttpResponseStatus.OK);
		response.setHeader(HttpHeaders.Names.SERVER, serverName);
		response.setHeader(HttpHeaders.Names.CONTENT_TYPE, "text/html");
		response.setHeader(HttpHeaders.Names.CONNECTION, "close");

		ArrayList<EstelaServer> servers = Stats.getInstance().getServers();

		Hashtable<UUID, Broadcast> bcasts = Stats.getInstance().getBcasts();

		String msg = "<h1>Server Stats for Estela</h1>\r\n";

		msg += "<table>\r\n";
		msg += "<tr><th>Server</th><th>Total Shows</th><th>Total Listeners</th></tr>\r\n";
		for (EstelaServer server : servers) {
			int shows = 0;
			int listeners = 0;
			Enumeration<Broadcast> en = bcasts.elements();
			while (en.hasMoreElements()) {
				Broadcast bcast = en.nextElement();
				int l = bcast.hasServer(server);
				if (l >= 0) {
					shows++;
					listeners += l;
				}
			}
			msg += "<tr>";
			msg += "<td>" + server + "</td>";
			msg += "<td>" + shows + "</td>";
			msg += "<td>" + listeners + "</td>";
			msg += "</tr>";
			msg += "\r\n";
		}
		msg += "</table>\r\n";

		ChannelBuffer buf = str2cb(msg);

		response.setContent(buf);

		e.getChannel().write(response);

		e.getChannel().close();

	}

}
