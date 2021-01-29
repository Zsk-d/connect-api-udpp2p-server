package com.dss006.connect.api.plugin.udpp2p.mapper;

import com.dss006.connect.api.base.pojo.P2pGroup;
import com.dss006.connect.api.base.pojo.P2pUser;
import com.dss006.connect.api.base.pojo.P2pUserGroup;
import org.apache.ibatis.annotations.*;

import java.sql.Timestamp;
import java.util.List;

/**
 * @author daishaoshu
 */
@Mapper
public interface P2pMapper {

   /* @Insert("INSERT INTO p2p_group (code,create_time) VALUES (#{code},#{createTime})")
    void insertGroup(P2pGroup group);

    @Insert("INSERT INTO p2p_user (`code`,ip,port,update_time) VALUES (#{code},#{ip},#{port},#{updateTime})")
    void insertUser(P2pUser user);





    @Select("SELECT * FROM p2p_group WHERE `code` = #{code}")
    @Results({
            @Result(property = "createTime", column = "create_time")
    })
    P2pGroup getGroup(String code);


    @Select("SELECT * FROM p2p_user WHERE `code` = #{code} AND ip = #{ip}")
    @Results({
            @Result(property = "updateTime", column = "update_time")
    })
    P2pUser getUserByIpAndGroup(String groupCode, String ip);

    @Select("SELECT * FROM p2p_user WHERE `code` = #{code} AND ip != #{ip}")
    @Results({
            @Result(property = "updateTime", column = "update_time")
    })
    List<P2pUser> getGroupUsersWithoutIp(String groupCode, String ip);

    @Select("SELECT * FROM p2p_user WHERE `code` IN (SELECT `code` FROM p2p_user WHERE ip = #{ip}) AND ip != #{ip}")
    @Results({
            @Result(property = "updateTime", column = "update_time")
    })
    List<P2pUser> getUsersSimGroupWithoutIp(String ip);
*/

    /**
     * insertUser
     *
     * @param user
     */
    @Insert("INSERT INTO p2p_user (ip,port,update_time) VALUES (#{ip},#{port},#{updateTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    void insertUser(P2pUser user);

    /**
     * @param group
     */
    @Insert("INSERT INTO p2p_group (code,create_time) VALUES (#{code},#{createTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    void insertGroup(P2pGroup group);

    /**
     * @param userGroup
     */
    @Insert("INSERT INTO p2p_user_group (user_id,group_id,create_time) VALUES (#{userId},#{groupId},#{createTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    void insertUserGroup(P2pUserGroup userGroup);

    /**
     * updateUser
     *
     * @param ip
     * @param port
     * @param updateTime
     */
    @Update("UPDATE p2p_user SET update_time = #{updateTime} WHERE ip = #{ip} AND port = #{port}")
    void updateUser(String ip, Integer port, Timestamp updateTime);

    @Delete("DELETE FROM p2p_user_group WHERE group_id = (SELECT id FROM p2p_group WHERE `code`=#{groupCode}) AND user_id = (SELECT id FROM p2p_user WHERE ip = #{ip} AND `port` = #{port})")
    void deleteGroupUser(String ip, Integer port, String groupCode);


    @Delete("DELETE FROM p2p_user_group WHERE user_id = (SELECT id FROM p2p_user WHERE ip = #{ip} AND `port` = #{port})")
    void deleteAllGroupUser(String ip, Integer port);

    @Delete(("DELETE p2p_user_group,p2p_user " +
            "FROM p2p_user LEFT JOIN p2p_user_group ON p2p_user.id = p2p_user_group.user_id " +
            "WHERE UNIX_TIMESTAMP(update_time) < (UNIX_TIMESTAMP(NOW()) - #{minutes}*60)"))
    int deleteInactiveUserGroup(Integer minutes);

    /**
     * getUser
     *
     * @param ip
     * @param port
     * @return
     */
    @Select("SELECT * FROM p2p_user WHERE ip = #{ip} AND port = #{port}")
    @Results({
            @Result(property = "updateTime", column = "update_time")
    })
    P2pUser getUser(String ip, Integer port);

    @Select("SELECT * FROM p2p_group WHERE `code` = #{code}")
    @Results({
            @Result(property = "createTime", column = "create_time")
    })
    P2pGroup getGroup(String code);

    @Select("SELECT * FROM p2p_user_group WHERE user_id = #{userId} AND group_id  = #{groupId}")
    @Results({
            @Result(property = "createTime", column = "create_time")
    })
    P2pUserGroup getUserGroup(Integer userId, Integer groupId);

    @Select("SELECT * FROM p2p_user WHERE id IN (SELECT user_id FROM p2p_user_group WHERE group_id = #{groupId} AND user_id != #{userId}) AND UNIX_TIMESTAMP(update_time) > (UNIX_TIMESTAMP(NOW()) - 3*60)")
    @Results({
            @Result(property = "updateTime", column = "update_time")
    })
    List<P2pUser> getSimGroupWithoutUser(Integer userId, Integer groupId);

    @Select("SELECT * FROM p2p_user WHERE id IN (SELECT user_id " +
            "FROM p2p_user_group " +
            "WHERE group_id IN (" +
            "SELECT group_id " +
            "FROM p2p_user_group " +
            "WHERE user_id = (" +
            "SELECT id " +
            "FROM p2p_user " +
            "WHERE ip = #{ip} " +
            "AND `port` = #{port})) " +
            "AND user_id != (SELECT id FROM p2p_user WHERE ip = #{ip} AND `port` = #{port})) " +
            "AND UNIX_TIMESTAMP(update_time) > (UNIX_TIMESTAMP(NOW()) - 3*60)")
    @Results({
            @Result(property = "updateTime", column = "update_time")
    })
    List<P2pUser> getAllSimGroupWithoutUser(String ip, Integer port);

    @Select("SELECT * FROM p2p_user " +
            "WHERE id IN (SELECT user_id FROM p2p_user_group WHERE group_id = (SELECT id FROM p2p_group WHERE `code` = #{groupCode}) " +
            "AND user_id != (SELECT id FROM p2p_user WHERE ip = #{ip} AND `port` = #{port})) " +
            "AND UNIX_TIMESTAMP(update_time) > (UNIX_TIMESTAMP(NOW()) - 3*60)")
    @Results({
            @Result(property = "updateTime", column = "update_time")
    })
    List<P2pUser> getSimGroupWithoutUserInfo(String ip, Integer port, String groupCode);
}
