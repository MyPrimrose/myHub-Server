package com.myhub.mapper;

import com.myhub.entity.LibraryFolder;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface LibraryFolderMapper {
    LibraryFolder selectById(@Param("id") Long id, @Param("ownerId") Long ownerId);

    List<LibraryFolder> selectByOwner(@Param("ownerId") Long ownerId);

    LibraryFolder selectByParentAndName(@Param("ownerId") Long ownerId,
                                        @Param("parentId") Long parentId,
                                        @Param("name") String name);

    int countChildren(@Param("ownerId") Long ownerId, @Param("parentId") Long parentId);

    int countByOwner(@Param("ownerId") Long ownerId);

    int insert(LibraryFolder folder);

    int update(LibraryFolder folder);

    int softDelete(@Param("id") Long id, @Param("ownerId") Long ownerId);

    int updateParent(@Param("id") Long id, @Param("ownerId") Long ownerId, @Param("parentId") Long parentId);
}
