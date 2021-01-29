package com.dss006.connect.api.plugin.udpp2p.service;

import com.dss006.connect.api.base.pojo.*;
import com.dss006.connect.api.plugin.udpp2p.mapper.P2pMapper;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;
import io.netty.util.CharsetUtil;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.net.InetSocketAddress;
import java.sql.Timestamp;
import java.util.List;

/**
 * @author daishaoshu
 */
@Service
@AllArgsConstructor
public class P2pService {

    private static ChannelHandlerContext ctx;

    private P2pMapper mapper;

    public static void setCtx(ChannelHandlerContext ctx) {
        P2pService.ctx = ctx;
    }

    public List<P2pUser> getGroupUser(P2pMessage msg) {
        List<P2pUser> groupUsers;
        String ip = msg.getIp();
        int port = msg.getPort();
        String groupCode = msg.getGroupCode();

        P2pUser user = mapper.getUser(ip, port);
        P2pGroup group = mapper.getGroup(groupCode);

        Integer userId, groupId;
        if (user == null) {
            user = P2pUser.builder().ip(ip).port(port).updateTime(new Timestamp(System.currentTimeMillis())).build();
            mapper.insertUser(user);
        } else {
            this.updateUser(msg);
        }
        userId = user.getId();
        if (group == null) {
            group = P2pGroup.builder().code(groupCode).createTime(new Timestamp(System.currentTimeMillis())).build();
            mapper.insertGroup(group);
        }
        groupId = group.getId();
        groupUsers = this.notifyGroupNewUser(ip, port, userId, group.getId(), groupCode);
        P2pUserGroup userGroup = mapper.getUserGroup(userId, groupId);
        if (userGroup == null) {
            userGroup = P2pUserGroup.builder().groupId(groupId).userId(userId).build();
            mapper.insertUserGroup(userGroup);
        }
        return groupUsers;
    }

    public boolean userActive(P2pMessage msg) {
        String ip = msg.getIp();
        int port = msg.getPort();
        P2pUser user = mapper.getUser(ip, port);
        if (user == null) {
            return false;
        } else {
            this.updateUser(msg);
            return true;
        }
    }

    public void notifyGroupUserExit(P2pMessage msg, String groupCode) {
        String ip = msg.getIp();
        Integer port = msg.getPort();
        List<P2pUser> users;
        if (groupCode == null) {
            mapper.deleteAllGroupUser(ip, port);
            users = mapper.getAllSimGroupWithoutUser(ip, port);
        } else {
            mapper.deleteGroupUser(ip, port, groupCode);
            users = mapper.getSimGroupWithoutUserInfo(ip, port, groupCode);
        }
        if (users != null) {
            users.forEach(user -> this.sendExitGroupMsg(user, groupCode));
        }
    }

    public void sendTransferNormalSingleMsg(ApiMsg msg, P2pUser user) {
        byte[] data = msg.toJsonString().getBytes(CharsetUtil.UTF_8);
        this.sendTransferNormalSingleMsg(data, user);
    }

    private void updateUser(P2pMessage msg) {
        this.updateUser(msg.getIp(), msg.getPort());
    }

    private void updateUser(String ip, int port) {
        mapper.updateUser(ip, port, new Timestamp(System.currentTimeMillis()));
    }

    private List<P2pUser> notifyGroupNewUser(String ip, int port, Integer userId, Integer groupId, String groupCode) {
        List<P2pUser> usersSimGroupWithoutIp = mapper.getSimGroupWithoutUser(userId, groupId);
        return usersSimGroupWithoutIp.size() == 0 ? null : this.notifyGroupNewUser(ip, port, groupCode, usersSimGroupWithoutIp);
    }

    private List<P2pUser> notifyGroupNewUser(String ip, int port, String groupCode, List<P2pUser> users) {
        if (users == null || users.size() == 0) {
            return null;
        }
        ApiMsg msg = ApiMsg.apiMsgMaker()
                .param(P2pMessage.NAME_ACTION, P2pMessage.ACTION_TYPE_ADD_USER)
                .param(P2pMessage.NAME_IP, ip)
                .param(P2pMessage.NAME_PORT, port)
                .param(P2pMessage.NAME_GROUP_CODE, groupCode)
                .toApiMsg();
        byte[] data = msg.toJsonString().getBytes(CharsetUtil.UTF_8);
        for (P2pUser user : users) {
            ByteBuf byteBuf = Unpooled.copiedBuffer(data);
            sendMsg(user.getIp(), user.getPort(), byteBuf);
        }
        return users;
    }

    private void sendExitGroupMsg(P2pUser user, String groupCode) {
        byte[] data = ApiMsg.apiMsgMaker().param(P2pMessage.NAME_ACTION, P2pMessage.ACTION_TYPE_EXIT_GROUP)
                .param(P2pMessage.NAME_GROUP_CODE, groupCode).toApiMsg().toJsonString().getBytes(CharsetUtil.UTF_8);
        this.sendMsg(user.getIp(), user.getPort(), Unpooled.copiedBuffer(data));
    }

    private void sendTransferNormalSingleMsg(byte[] data, P2pUser user) {
        ByteBuf byteBuf = Unpooled.copiedBuffer(data);
        sendMsg(user.getIp(), user.getPort(), byteBuf);
    }

    public void sendTransferNormalGroupMsg(P2pUser user, String groupCode, ApiMsg msg) {
        List<P2pUser> simGroupWithoutUserInfo = mapper.getSimGroupWithoutUserInfo(user.getIp(), user.getPort(), groupCode);
        if (simGroupWithoutUserInfo.size() > 0) {
            final byte[] data = msg.toJsonString().getBytes(CharsetUtil.UTF_8);
            simGroupWithoutUserInfo.forEach(otherUser -> sendTransferNormalSingleMsg(data, otherUser));
        }
    }

    private void sendMsg(String ip, int port, ByteBuf byteBuf) {
        ctx.writeAndFlush(new DatagramPacket(byteBuf, new InetSocketAddress(ip, port)));
    }
}
