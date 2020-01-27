package me.weyye.todaynews.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import me.weyye.todaynews.R;
import me.weyye.todaynews.base.BaseFragment;
import me.weyye.todaynews.base.BaseMvpFragment;
import me.weyye.todaynews.presenter.HomePresenter;
import me.weyye.todaynews.ui.activity.ChannelActivity;
import me.weyye.todaynews.ui.adapter.TitlePagerAdapter;
import me.weyye.todaynews.ui.view.colortrackview.ColorTrackTabViewIndicator;
import me.weyye.todaynews.ui.view.colortrackview.ColorTrackView;
import me.weyye.todaynews.utils.ConstanceValue;
import me.weyye.todaynews.view.IHomeView;

/**
 * Created by Administrator on 2016/11/17 0017.
 */
public class HomeFragment extends BaseMvpFragment<HomePresenter> implements IHomeView {
    @BindView(R.id.feed_top_search_hint)
    TextView feedTopSearchHint;
    @BindView(R.id.tab)
    ColorTrackTabViewIndicator tab;
    @BindView(R.id.icon_category)
    ImageView iconCategory;
    @BindView(R.id.vp)
    ViewPager vp;
    private String[] titles = new String[]{"推荐", "视频", "热点", "社会", "娱乐", "科技", "汽车", "体育", "财经", "军事", "国际", "时尚", "游戏", "旅游", "历史", "探索", "美食", "育儿", "养生", "故事", "美文"};
    private String[] titlesCode = new String[]{"__all__", "video", "news_hot", "news_society", "news_entertainment", "news_tech", "news_car", "news_sports", "news_finance", "news_military", "news_world", "news_fashion", "news_game", "news_travel", "news_history", "news_discovery", "news_food", "news_baby", "news_regimen", "news_story", "news_essay"};

    @Override
    protected HomePresenter createPresenter() {
        return new HomePresenter(this);
    }

    @Override
    protected View loadViewLayout(LayoutInflater inflater, ViewGroup container) {
        return inflater.inflate(R.layout.fragment_home, null);
    }

    @Override
    protected void bindViews(View view) {
        ButterKnife.bind(this, rootView);
    }

    @Override
    protected void processLogic() {
        List<BaseFragment> fragments = new ArrayList<>();
        for (int i = 0; i < titles.length; i++) {
            NewsListFragment fragment = new NewsListFragment();
            Bundle bundle = new Bundle();
            bundle.putString(ConstanceValue.DATA, titlesCode[i]);
            fragment.setArguments(bundle);
            fragments.add(fragment);
        }
        vp.setAdapter(new TitlePagerAdapter(getChildFragmentManager(), fragments, titles));
        tab.setTitles(titles, new ColorTrackTabViewIndicator.CorlorTrackTabBack() {
            @Override
            public void onClickButton(Integer position, ColorTrackView colorTrackView) {
                vp.setCurrentItem(position, false);
            }
        });
        final View tabChild = tab.getChildAt(0);
        int w = View.MeasureSpec.makeMeasureSpec(0,
                View.MeasureSpec.UNSPECIFIED);
        int h = View.MeasureSpec.makeMeasureSpec(0,
                View.MeasureSpec.UNSPECIFIED);
        //重新测量
        tabChild.measure(w, h);
        //设置最小宽度，使其可以在滑动一部分距离
        tabChild.setMinimumWidth(tabChild.getMeasuredWidth() + tab.getTabWidth());

        vp.setOffscreenPageLimit(titles.length);
        tab.setupViewPager(vp);
    }

    @Override
    protected void setListener() {

    }

    static final int REQUEST_CHANNEL = 111;

    @OnClick({R.id.feed_top_search_hint, R.id.icon_category})
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.icon_category:
//                intent2Activity(ChannelActivity.class);
                Intent intent = new Intent(mContext, ChannelActivity.class);
                startActivityForResult(intent,REQUEST_CHANNEL);
                break;
        }
    }
}
