package org.snake.transfer.command.client.handler;


import org.snake.message.command.RegisterServer;
import org.snake.message.pool.MessagePool;
import org.snake.transfer.command.client.SingleConnection;
import org.snake.transfer.command.client.disruptor.CommandEventProcessor;
import org.snake.transfer.command.zookeeper.ServersImpl;
import org.snake.transfer.command.zookeeper.ZkDataImpl;
import org.snake.transfer.user.server.netty.server.TransferServer;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class CommandEventDispatchHandler extends ChannelInboundHandlerAdapter {
	
	private CommandEventProcessor commandEventProcessor
									= CommandEventProcessor.getInstance();

	@Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
    	
		commandEventProcessor.enqueue(ctx.channel(), (ByteBuf)msg);
		
    }
	
	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {

		RegisterServer registerServer = (RegisterServer)MessagePool.borrowMessage(RegisterServer.CMD_INTEGER);
		
		registerServer.setServerType(ZkDataImpl.SERVER_TYPE);
		registerServer.setTime(ServersImpl.START_TIME);
		
		registerServer.setIp("192.168.0.105");
		registerServer.setPort(TransferServer.PORT);
		
		ctx.writeAndFlush(registerServer);
		
		ctx.fireChannelActive();
		
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		
		log.warn("连接断开, 属性: {}", ctx.channel().attr(SingleConnection.ATTR_KEY).get());
		
		ctx.fireChannelInactive();
		
		ctx.channel().attr(SingleConnection.ATTR_KEY).get().reConnect();
	}

    
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        // Close the connection when an exception is raised.
    	
    	
        cause.printStackTrace();
        ctx.close();
        
//        ctx.channel().attr(SingleConnection.ATTR_KEY).get().reConnect();
    }
}
