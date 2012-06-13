package com.flipzu.stats;

import static org.jboss.netty.channel.Channels.pipeline;

import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.handler.codec.http.HttpRequestDecoder;
import org.jboss.netty.handler.codec.http.HttpResponseEncoder;

/**
 * Estela stats server
 *
 * @author Dario Rapisardi <dario@flipzu.com>
 *
 */
public class EstelaStatsHttpPipelineFactory implements ChannelPipelineFactory {
	public ChannelPipeline getPipeline() throws Exception {
		
		// default pipeline implementation
		ChannelPipeline p = pipeline();

		p.addLast("http-decoder", new HttpRequestDecoder());
		p.addLast("http-encoder", new HttpResponseEncoder());
		
		// main handler
		p.addLast("handler", new EstelaHttpHandler());
		
		
		return p;
	}
}
