package com.dss006.connect.api.plugin.udpp2p.handler;

import com.alibaba.fastjson.JSONObject;
import com.dss006.connect.api.base.pojo.ApiMsg;
import com.dss006.connect.api.base.pojo.P2pMessage;
import com.dss006.connect.api.base.pojo.P2pUser;
import com.dss006.connect.api.plugin.udpp2p.service.P2pService;
import com.dss006.connect.api.plugin.udpp2p.util.MessageUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;
import io.netty.util.CharsetUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author daishaoshu
 */
@Component
@AllArgsConstructor
@Slf4j
public class P2pServerHandler extends SimpleChannelInboundHandler<DatagramPacket> {

    private P2pService p2pService;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket packet) {
        P2pMessage msg = MessageUtil.getMessageFromPacket(packet);
        ApiMsg apiMessageFromPacket = MessageUtil.getApiMessageFromPacket(packet);
        assert apiMessageFromPacket != null;
        if (msg != null) {
            final int action = msg.getAction();
            log.info("客户端[{}]消息 msg = {}", MessageUtil.actionToString(action), apiMessageFromPacket);
            ApiMsg replyMsg = null;
            List<P2pUser> users;
            switch (action) {
                case P2pMessage.ACTION_TYPE_REG:
                    users = p2pService.getGroupUser(msg);
                    replyMsg = ApiMsg.apiMsgMaker().param(P2pMessage.NAME_ACTION, P2pMessage.ACTION_TYPE_GROUP_INFO).
                            param(P2pMessage.NAME_USERS, users).param(P2pMessage.NAME_GROUP_CODE, msg.getGroupCode())
                            .toApiMsg();
                    break;
                case P2pMessage.ACTION_TYPE_ACTIVE:
                    replyMsg = ApiMsg.apiMsgMaker()
                            .param(P2pMessage.NAME_ACTION, p2pService.userActive(msg) ? P2pMessage.ACTION_TYPE_ACTIVE_OK : P2pMessage.ACTION_TYPE_GROUP_NEED_REG)
                            .toApiMsg();
                    break;
                case P2pMessage.ACTION_TYPE_EXIT_GROUP:
                    String exitGroupCode = msg.getGroupCode();
                    p2pService.notifyGroupUserExit(msg, exitGroupCode);
                    break;
                case P2pMessage.ACTION_TYPE_TRANSFER_TO_USER:
                    ApiMsg transferSingleMsg = apiMessageFromPacket.getJSONObjectParam(P2pMessage.NAME_API_MSG).toJavaObject(ApiMsg.class);
                    ApiMsg transferSingleApiMsg = ApiMsg.apiMsgMaker()
                            .param(P2pMessage.NAME_USER, new P2pUser(null, packet.sender().getAddress().getHostAddress(), packet.sender().getPort(), null))
                            .param(P2pMessage.NAME_API_MSG, transferSingleMsg)
                            .param(P2pMessage.NAME_ACTION, P2pMessage.ACTION_TYPE_TRANSFER_TO_USER).toApiMsg();
                    P2pUser transferUser = apiMessageFromPacket.getJSONObjectParam(P2pMessage.NAME_USER).toJavaObject(P2pUser.class);
                    p2pService.sendTransferNormalSingleMsg(transferSingleApiMsg, transferUser);
                    break;
                case P2pMessage.ACTION_TYPE_TRANSFER_TO_GROUP:
                    String transferGroupCode1 = msg.getGroupCode();
                    ApiMsg transferMsg = apiMessageFromPacket.getJSONObjectParam(P2pMessage.NAME_API_MSG).toJavaObject(ApiMsg.class);
                    ApiMsg transferGroupApiMsg = ApiMsg.apiMsgMaker()
                            .param(P2pMessage.NAME_USER, new P2pUser(null, packet.sender().getAddress().getHostAddress(), packet.sender().getPort(), null))
                            .param(P2pMessage.NAME_GROUP_CODE, transferGroupCode1)
                            .param(P2pMessage.NAME_API_MSG, transferMsg)
                            .param(P2pMessage.NAME_ACTION, P2pMessage.ACTION_TYPE_TRANSFER_TO_GROUP).toApiMsg();
                    p2pService.sendTransferNormalGroupMsg(P2pUser.builder()
                            .ip(packet.sender().getAddress().getHostAddress())
                            .port(packet.sender().getPort()).build(), transferGroupCode1, transferGroupApiMsg);
                    break;
                case P2pMessage.ACTION_TYPE_TRANSFER_MSG_OK:
                    // 消息回执目标用户 todo
                    P2pUser toUser = apiMessageFromPacket.getJSONObjectParam(P2pMessage.NAME_USER).toJavaObject(P2pUser.class);
//                    apiMessageFromPacket.getParams().put(P2pMessage.NAME_USER, new P2pUser(null, packet.sender().getAddress().getHostAddress(), packet.sender().getPort(), null))
//                    p2pService.send
                    break;
                default:
                    break;
            }
            if (replyMsg != null) {
                this.reply(ctx, replyMsg, packet);
            }
        } else {
            ctx.close();
        }
    }

    private void reply(ChannelHandlerContext ctx, ApiMsg msg, DatagramPacket packet) {
        ByteBuf byteBuf = Unpooled.copiedBuffer(msg.toJsonString(), CharsetUtil.UTF_8);
        ctx.writeAndFlush(new DatagramPacket(byteBuf, packet.sender()));
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        ctx.close();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        P2pService.setCtx(ctx);
    }

}