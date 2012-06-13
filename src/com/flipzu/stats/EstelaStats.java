package com.flipzu.stats;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;


/**
 * Estela stats server
 *
 * @author Dario Rapisardi <dario@flipzu.com>
 *
 */
public class EstelaStats {
	public static void main(String[] args) throws Exception {
		ChannelFactory factory = new NioServerSocketChannelFactory(
				Executors.newCachedThreadPool(),
				Executors.newCachedThreadPool());
		

		ServerBootstrap apiBootstrap = new ServerBootstrap(factory);
		
		apiBootstrap.setPipelineFactory(new EstelaStatsApiPipelineFactory());	

		// bind a puertos
		apiBootstrap.bind(new InetSocketAddress(Config.getInstance().getApiPort()));
		
		ServerBootstrap httpBootstrap = new ServerBootstrap(factory);
		
		httpBootstrap.setPipelineFactory(new EstelaStatsHttpPipelineFactory());
		
		httpBootstrap.bind(new InetSocketAddress(Config.getInstance().getPort()));
		
	}
}
