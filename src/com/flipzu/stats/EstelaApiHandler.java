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

import java.util.List;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelEvent;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;

import com.flipzu.stats.EstelaProtocol.BroadcastQueryResponse;
import com.flipzu.stats.EstelaProtocol.EstelaMessage;
import com.flipzu.stats.EstelaProtocol.StatsMessage;

/**
 * Netty handler that takes care of API calls.
 *
 * @author Dario Rapisardi <dario@rapisardi.org>
 *
 */
public class EstelaApiHandler extends SimpleChannelUpstreamHandler {
	
	private static Debug debug = Debug.getInstance();

	@Override
	public void handleUpstream( ChannelHandlerContext ctx, ChannelEvent e) throws Exception {		 
		super.handleUpstream(ctx, e);
	}
	 
	@Override
	public void channelConnected ( ChannelHandlerContext ctx, ChannelStateEvent e ) throws Exception {
		debug.logApi("EstelaApiHandler, CONNECTED");		
	}
	
	@Override
	public void channelClosed ( ChannelHandlerContext ctx, ChannelStateEvent e ) throws Exception {
		debug.logApi("EstelaApiHandler, CLOSED");
		
		EstelaServer server = SessionAttrs.server.get(e.getChannel());
		
		Stats.getInstance().clearHost(server);
	}
	
	@Override
	public void messageReceived( ChannelHandlerContext ctx, MessageEvent e) {	

		 EstelaMessage message = (EstelaMessage) e.getMessage();
		 
		 switch ( message.getMessageType() ) {
		 case STATS_MESSAGE:
			 StatsMessage stats_message = message.getStatsMessage();
			 EstelaServer server = new EstelaServer(stats_message.getServerName(), stats_message.getServerIp(), stats_message.getReadPort(), stats_message.getWritePort());
			 if ( server.getIp().equals("0.0.0.0")) {
				 server.setIp(getRemoteAddress(ctx));
			 }
			 switch ( stats_message.getMessageType() ) {
			 case REGISTER_SERVER:
				 handleNewServer(server, e.getChannel());
				 break;
			 case UPDATE_STATS:
				 handleUpdate(server, stats_message);
				 break;
			 case CLOSE_BCAST:
				 handleCloseBcast(server, stats_message);
				 break;			 
			 }
			 break;
		 case BROADCAST_QUERY:
			 String username = message.getBcastQuery().getUsername();
			 handleBroadcastQuery(e, username);			 
			 break;
		 }	
		 
		 ctx.sendUpstream(e);
	}
	
	/**
	 * registers a new server
	 * @param server new incoming server
	 * @param channel the netty Channel connecting with this server
	 */
	public void handleNewServer( EstelaServer server, Channel channel ) {
		debug.logApi("EstelaApiHandler, handleNewServer from " + server);

		Stats.getInstance().clearHost(server);
		
		SessionAttrs.server.set(channel, server);
		
		Stats.getInstance().addServer(server);
	}
	
	/**
	 * closes a broadcast
	 * @param server the EstelaServer who fired the event
	 * @param message the message
	 */
	public void handleCloseBcast( EstelaServer server, StatsMessage message ) {		
		List<EstelaProtocol.Broadcast> bList = message.getBroadcastList();
		
		if ( bList.size() != 1 ) {
			debug.logApi("EstelaApiHandler, handleCloseBcast, size mismatch for MessageType " + message.getMessageType());
			return;
		}
		
		EstelaProtocol.Broadcast b = bList.get(0);
		
		debug.logApi("EstelaApiHandler, handleCloseBcast from " + server + " for " + b.getUsername());
		
		Stats.getInstance().closeBroadcast(server, b);
	}
		
	/**
	 * updates a broadcast status (such as listeners)
	 * @param server the server firing the event
	 * @param message the message
	 */
	public void handleUpdate ( EstelaServer server, StatsMessage message ) {
		debug.logApi("EstelaApiHandler, handleUpdate from " + server);						
		
		for ( EstelaProtocol.Broadcast b : message.getBroadcastList() ) {
			Stats.getInstance().updateBroadcast(server, b);
			debug.logApi("EstelaApiHandler, handleUpdate(), adding stats for " + b.getUsername());
		}
	}
	
	/**
	 * creates a response for a BROADCAST_QUERY api call
	 * @param e the message event.
	 * @param username
	 */
	public void handleBroadcastQuery( MessageEvent e, String username ) {
		debug.logApi("EstelaApiHandler, handleBroadcastQuery for " + username);
		
		Broadcast bcast = Stats.getInstance().getBroadcast(username);		


		EstelaServer server = null;
		Channel channel = e.getChannel();
		EstelaProtocol.BroadcastQueryResponse.Builder response_builder;
		
		if ( bcast == null ) {
			debug.logApi("EstelaApiHandler, handleBroadcastQuery, " + username + " not found");
			// build response packet
			response_builder = EstelaProtocol.BroadcastQueryResponse.newBuilder();
			response_builder
				.setBroadcast(EstelaProtocol.Broadcast.newBuilder().setUsername(username))
				.setResponseCode(BroadcastQueryResponse.ResponseCode.NOT_FOUND)
				.build();
		} else {
			for ( int i=0; i < bcast.getServers().size(); i++ ) {
				server = bcast.getServers().get(i);
				if ( server.isMainServer() ) {
					 break;
				}
			}
			
			debug.logApi("EstelaApiHandler, handleBroadcastQuery, got " + server);
			
			// build response packet
			response_builder = EstelaProtocol.BroadcastQueryResponse.newBuilder();
			response_builder.setServerIp(server.getIp())
				.setReadPort(server.getReadPort())
				.setWritePort(server.getWritePort())
				.setHostname(server.getHostname())
				.setBroadcast(EstelaProtocol.Broadcast.newBuilder()
						.setUsername(bcast.getUsername())
						.setId(bcast.getId())
						.setUuid(bcast.getUuid().toString()))
				.setResponseCode(BroadcastQueryResponse.ResponseCode.FOUND)
				.build();			
		}
				
		
		EstelaProtocol.EstelaMessage.Builder builder = EstelaProtocol.EstelaMessage.newBuilder();
		builder.setMessageType(EstelaMessage.EstelaMessageType.BROADCAST_QUERY)
			.setBcastQueryResponse(response_builder);
			
		if ( channel.isWritable() ) {
			debug.logApi("EstelaApiHandler, handleBroadcastQuery, writing packet to " + channel);
			channel.write(builder.build());	
		} else {
			debug.logApi("EstelaApiHandler, handleBroadcastQuery, can't write to " + channel);
		}
		
	}
	
	@Override
	public void exceptionCaught( ChannelHandlerContext ctx, ExceptionEvent e) {
		 debug.logError("EstelaApiHandler, exceptionCaught() ", e.getCause());
		 e.getChannel().close();
	}
	
	/**
	 * returns the remote IP address
	 * @param ctx the context for this connection
	 * @return the IP address
	 */
	private String getRemoteAddress(ChannelHandlerContext ctx) {		
		return ctx.getChannel().getRemoteAddress().toString().substring(1).split(":")[0];
	}
		 
}
