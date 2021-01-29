package com.dss006.connect.api.plugin.udpp2p.util;

import com.alibaba.fastjson.JSON;
import com.dss006.connect.api.base.pojo.ApiMsg;
import com.dss006.connect.api.base.pojo.P2pMessage;
import io.netty.channel.socket.DatagramPacket;
import io.netty.util.CharsetUtil;

/**
 * @author daishaoshu
 */
public class MessageUtil {

    public static P2pMessage getMessageFromPacket(DatagramPacket packet) {
        String udpDataString = packet.content().toString(CharsetUtil.UTF_8);
        try {
            ApiMsg message = JSON.parseObject(udpDataString, ApiMsg.class);
            return P2pMessage.builder().action(message.getIntegerParam(P2pMessage.NAME_ACTION))
                    .ip(packet.sender().getAddress().getHostAddress())
                    .groupCode(message.getStringParam(P2pMessage.NAME_GROUP_CODE))
                    .port(packet.sender().getPort()).build();
        } catch (Exception e) {
            return null;
        }
    }

    public static ApiMsg getApiMessageFromPacket(DatagramPacket packet) {
        String udpDataString = packet.content().toString(CharsetUtil.UTF_8);
        try {
            return JSON.parseObject(udpDataString, ApiMsg.class);
        } catch (Exception e) {
            return null;
        }
    }

    public static String actionToString(int action) {
        String name;
        switch (action) {
            case P2pMessage.ACTION_TYPE_EXIT_GROUP:
                name = "用户退出群组";
                break;
            case P2pMessage.ACTION_TYPE_TRANSFER_TO_GROUP:
                name = "群组中转";
                break;
            case P2pMessage.ACTION_TYPE_TRANSFER_TO_USER:
                name = "用户中转";
                break;
            case P2pMessage.ACTION_TYPE_ACTIVE:
                name = "保活";
                break;
            case P2pMessage.ACTION_TYPE_REG:
                name = "群组注册";
                break;
            default:
                name = "未知";
                break;
        }
        return name;
    }
}
