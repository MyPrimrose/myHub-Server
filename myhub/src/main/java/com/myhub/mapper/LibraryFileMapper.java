package com.myhub.mapper;

import com.myhub.entity.LibraryFile;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface LibraryFileMapper {
    LibraryFile selectById(@Param("id") Long id, @Param("ownerId") Long ownerId);

    LibraryFile selectByFolderAndName(@Param("ownerId") Long ownerId,
                                      @Param("folderId") Long folderId,
                                      @Param("name") String name);

    List<LibraryFile> selectPage(@Param("ownerId") Long ownerId,
                                 @Param("folderId") Long folderId,
                                 @Param("uncategorized") boolean uncategorized,
                                 @Param("keyword") String keyword,
                                 @Param("favorite") Boolean favorite,
                                 @Param("offset") int offset,
                                 @Param("limit") int limit);

    long countPage(@Param("ownerId") Long ownerId,
                   @Param("folderId") Long folderId,
                   @Param("uncategorized") boolean uncategorized,
                   @Param("keyword") String keyword,
                   @Param("favorite") Boolean favorite);

    List<LibraryFile> selectRecent(@Param("ownerId") Long ownerId, @Param("limit") int limit);

    List<LibraryFile> selectFavorites(@Param("ownerId") Long ownerId, @Param("limit") int limit);

    List<LibraryFile> selectByFolder(@Param("ownerId") Long ownerId, @Param("folderId") Long folderId);

    int countByOwner(@Param("ownerId") Long ownerId);

    int countByFolder(@Param("ownerId") Long ownerId, @Param("folderId") Long folderId);

    Long sumSizeByOwner(@Param("ownerId") Long ownerId);

    int insert(LibraryFile file);

    int update(LibraryFile file);

    int updateFavorite(@Param("id") Long id, @Param("ownerId") Long ownerId, @Param("favorite") int favorite);

    int updateFolder(@Param("id") Long id, @Param("ownerId") Long ownerId, @Param("folderId") Long folderId);

    int softDelete(@Param("id") Long id, @Param("ownerId") Long ownerId);

    int softDeleteBatch(@Param("ids") List<Long> ids, @Param("ownerId") Long ownerId);

    int moveFolderFilesToParent(@Param("ownerId") Long ownerId,
                                @Param("folderId") Long folderId,
                                @Param("targetFolderId") Long targetFolderId);

    int markAccessed(@Param("id") Long id, @Param("ownerId") Long ownerId);
}
