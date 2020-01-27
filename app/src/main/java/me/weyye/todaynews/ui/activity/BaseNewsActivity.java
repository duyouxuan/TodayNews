package me.weyye.todaynews.ui.activity;

import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import me.weyye.todaynews.R;
import me.weyye.todaynews.base.BaseMvpActivity;
import me.weyye.todaynews.model.Comment;
import me.weyye.todaynews.model.CommentList;
import me.weyye.todaynews.presenter.BaseDetailPresenter;
import me.weyye.todaynews.ui.adapter.CommentAdapter;
import me.weyye.todaynews.ui.view.ProgressWebView;
import me.weyye.todaynews.utils.ConstanceValue;

import static me.weyye.todaynews.R.id.action_comment_count;

/**
 * Created by Administrator on 2016/11/24 0024.
 */

public abstract class BaseNewsActivity<P extends BaseDetailPresenter> extends BaseMvpActivity<P> {
    @BindView(R.id.recyclerView)
    protected RecyclerView recyclerView;
    @BindView(R.id.write_comment_layout)
    TextView writeCommentLayout;
    @BindView(R.id.action_view_up)
    ImageView actionViewUp;
    @BindView(R.id.action_view_comment)
    ImageView actionViewComment;
    @BindView(action_comment_count)
    TextView actionCommentCount;
    @BindView(R.id.action_commont_layout)
    FrameLayout actionCommontLayout;
    @BindView(R.id.view_comment_layout)
    FrameLayout viewCommentLayout;
    @BindView(R.id.action_favor)
    ImageView actionFavor;
    @BindView(R.id.action_repost)
    ImageView actionRepost;
    private ProgressWebView web;
    private String url;
    private CommentAdapter mAdapter;
    private View headerView;

    @Override
    protected void loadViewLayout() {
        ButterKnife.bind(this);
    }

    @Override
    protected void bindViews() {

    }

    protected int mPageNow = 1;
    protected List<Comment> mDatas = new ArrayList<>();
    protected String groupId;
    protected String itemId;

    @Override
    protected void processLogic(Bundle savedInstanceState) {
        initCommonRecyclerView(mAdapter = new CommentAdapter(mDatas), null);
        headerView = View.inflate(this, R.layout.layout_webview, null);
        headerView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        web = (ProgressWebView) headerView.findViewById(R.id.web);
        mAdapter.addHeaderView(headerView);
        web.setOnHtmlEventListener(new ProgressWebView.OnHtmlEventListener() {
            @Override
            public void onFinished(String html) {
                Pattern pattern = Pattern.compile("var.+group_id.+=.+\"([0-9]+)\";\\n.+var.+item_id.+=.+\"([0-9]+)\"");
                Matcher matcher = pattern.matcher(html);
                if (matcher.find()) {
                    groupId = matcher.group(1);
                    itemId = matcher.group(2);
                    mvpPresenter.getComment(groupId, itemId, mPageNow);
                }


            }

            @Override
            public void onUriLoading(Uri uri) {
                onUriLoad(uri);
            }
        });
        mAdapter.openLoadMore(10, true);
        View emptyView = View.inflate(this, R.layout.subscribe_list_empty_view, null);
        emptyView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        mAdapter.setEmptyView(true, emptyView);
        url = getIntent().getStringExtra(ConstanceValue.URL);
        web.loadUrl(url);
    }

    protected void onUriLoad(Uri uri) {
    }

    @Override
    protected void setListener() {
        mAdapter.setOnLoadMoreListener(new BaseQuickAdapter.RequestLoadMoreListener() {
            @Override
            public void onLoadMoreRequested() {
                if (!TextUtils.isEmpty(groupId) && !TextUtils.isEmpty(itemId))
                    mvpPresenter.getComment(groupId, itemId, ++mPageNow);
            }
        });
    }

    protected void getCommentSuccess(CommentList response) {
        if (response.total > 0) {
            actionCommentCount.setVisibility(View.VISIBLE);
            actionCommentCount.setText(response.total + "");
        }
        if (response.comments.size() > 0) {
            mAdapter.notifyDataChangedAfterLoadMore(response.comments, response.has_more);
        } else {
            mAdapter.notifyDataChangedAfterLoadMore(false);
        }
    }

    @OnClick({R.id.action_commont_layout, R.id.action_favor, R.id.action_repost})
    public void onClick(View view) {
        switch (view.getId()) {

            case R.id.action_commont_layout:
//                LinearLayoutManager linearLayoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
//                linearLayoutManager.scrollToPositionWithOffset(1, 0);
                recyclerView.smoothScrollToPosition(1);
                break;
            case R.id.action_favor:
                break;
            case R.id.action_repost:
                break;
        }
    }
}
