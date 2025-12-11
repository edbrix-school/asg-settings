package com.asg.settings.service;

import com.asg.common.lib.utility.PaginationUtil;
import com.asg.settings.dto.request.FavoriteMenuRequest;
import com.asg.settings.entity.FavoriteMenuEntity;
import com.asg.settings.repository.FavoriteMenuRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

@Service
public class FavoriteMenuService {

    @Autowired
    private FavoriteMenuRepository favoriteMenuRepository;

    public List<FavoriteMenuEntity> getFavoriteList(Long userPoid, String userId) throws SQLException {
        return favoriteMenuRepository.getFavoriteMenuList(userPoid, userId);
    }

    public Map<String, Object> getUnassignedFavList(Long userPoid, String userId, String search, Pageable pageable) throws SQLException {
        List<FavoriteMenuEntity> allResults = favoriteMenuRepository.getUnassignedFavList(userId, userPoid, search);

        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), allResults.size());

        List<FavoriteMenuEntity> pagedList = allResults.subList(start, end);

        Page<FavoriteMenuEntity> page = new PageImpl<>(pagedList, pageable, allResults.size());

        return PaginationUtil.wrapPage(page,null);
    }


    public String addFavoriteMenu(FavoriteMenuRequest request) throws SQLException {
        return favoriteMenuRepository.addFavoriteMenu(
                request.getUserId(),
                request.getUserPoid(),
                request.getMenuGroup(),
                request.getSelectedDocIds()
        );
    }

    public String removeFavoriteMenus(String userId, Long userPoid, String categoryValue, String selectedDocIdList) throws SQLException {
        return favoriteMenuRepository.removeFavoriteMenuList(userId, userPoid, categoryValue, selectedDocIdList);
    }
}
