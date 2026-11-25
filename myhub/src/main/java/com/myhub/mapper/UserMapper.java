package com.myhub.mapper;

import com.myhub.entity.UserAccount;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserMapper {
    UserAccount selectByUsername(@Param("username") String username);

    int countUsers();

    int insertInitialUser(@Param("username") String username, @Param("password") String password, @Param("nickname") String nickname);

    int updateLastLogin(@Param("id") long id, @Param("loginTime") java.time.LocalDateTime loginTime);
}
