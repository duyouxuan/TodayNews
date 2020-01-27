package me.weyye.todaynews.view;

import me.weyye.todaynews.model.CommentList;

/**
 * Created by Administrator on 2016/11/24 0024.
 */

public interface IBaseDetailView {
    void onGetCommentSuccess(CommentList response);
}
