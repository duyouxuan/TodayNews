package me.weyye.todaynews.ui.activity;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.OnClick;
import me.weyye.todaynews.R;
import me.weyye.todaynews.model.CommentList;
import me.weyye.todaynews.presenter.NewsDetailPresenter;
import me.weyye.todaynews.view.INewsDetailView;


public class NewsDetailActivity extends BaseNewsActivity<NewsDetailPresenter> implements INewsDetailView {

    @BindView(R.id.back_btn)
    ImageView backBtn;
    @BindView(R.id.title)
    TextView title;
    @BindView(R.id.share_btn)
    ImageView shareBtn;

    @Override
    protected void loadViewLayout() {
        setContentView(R.layout.activity_news_detail);
        super.loadViewLayout();
    }

    @OnClick(R.id.back_btn)
    public void onBackClick(View view) {
        finish();
    }

    @Override
    protected NewsDetailPresenter createPresenter() {
        return new NewsDetailPresenter(this);
    }

    @Override
    public void onGetCommentSuccess(CommentList response) {
        getCommentSuccess(response);
    }


}
