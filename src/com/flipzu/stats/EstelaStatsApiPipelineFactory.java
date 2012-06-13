package com.flipzu.stats;

import static org.jboss.netty.channel.Channels.pipeline;

import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.handler.codec.frame.LengthFieldBasedFrameDecoder;
import org.jboss.netty.handler.codec.frame.LengthFieldPrepender;
import org.jboss.netty.handler.codec.protobuf.ProtobufDecoder;
import org.jboss.netty.handler.codec.protobuf.ProtobufEncoder;


/**
 * Estela stats server
 *
 * @author Dario Rapisardi <dario@flipzu.com>
 *
 */
public class EstelaStatsApiPipelineFactory implements ChannelPipelineFactory {
	public ChannelPipeline getPipeline() throws Exception {
		
		// default pipeline implementation
		ChannelPipeline p = pipeline();
		
		p.addLast("frameDecoder", new LengthFieldBasedFrameDecoder(1048576, 0, 4, 0, 4));		
		p.addLast("protobufDecoder", new ProtobufDecoder(EstelaProtocol.EstelaMessage.getDefaultInstance()));  
		p.addLast("frameEncoder", new LengthFieldPrepender(4));
		p.addLast("protobufEncoder", new ProtobufEncoder());

		// main handler
		p.addLast("handler", new EstelaApiHandler());
		
		
		return p;
	}
}
