package com.wondersgroup.healthcloud.services.article.impl;

import com.wondersgroup.healthcloud.jpa.entity.article.ArticleFavorite;
import com.wondersgroup.healthcloud.services.article.ManageArticleFavoriteService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Service("manageArticleFavoriteService")
public class ManageArticleFavoriteServiceImpl implements ManageArticleFavoriteService {


    @Override
    public Integer addFavorite(ArticleFavorite favorite) {
        return null;
    }

    @Override
    public Integer updateFavorite(ArticleFavorite favorite) {
        return null;
    }

    @Override
    public Integer deleteFavorite(int id) {
        return null;
    }

    @Override
    public List<ArticleFavorite> queryArticleFavoriteByUserId(Map<String, Object> parm) {
        return null;
    }

    @Override
    public ArticleFavorite queryArticleFavoriteById(int id) {
        return null;
    }

    @Override
    public List<ArticleFavorite> queryAllArticleFavListByUserId(String uid) {
        return null;
    }

    @Override
    public List<Integer> getUserArticleFavIds(String uid) {
        return null;
    }

    @Override
    public List<Integer> getUserArticleFavIds(String uid, Integer type) {
        return null;
    }

    @Override
    public int getCountOfFavoriteList(String uid) {
        return 0;
    }

    @Override
    public ArticleFavorite queryArticleFavoriteByObj(ArticleFavorite favorite) {
        return null;
    }

    @Override
    public ArticleFavorite queryArticleFavoriteByArticleId(int articleId) {
        return null;
    }
}
