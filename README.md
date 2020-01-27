# TodayNews
一个仿今日头条的开源项目，是基于MVP+[RxJava](https://github.com/ReactiveX/RxJava)+[Retrofit](https://github.com/square/retrofit)

# Blog

[Android仿今日头条的开源项目](http://www.weyye.me/detail/my-project-today-news/)

#Apk

[点击下载](http://fir.im/np8w)

# 项目截图

![](/screenshot/01.png)

![](/screenshot/02.png)

![](/screenshot/03.png)

![](/screenshot/04.png)

![](/screenshot/05.png)

![](/screenshot/06.png)

![](/screenshot/07.png)

![](/screenshot/08.jpg)



# 第三方库
* [BaseRecyclerViewAdapterHelper](https://github.com/CymChad/BaseRecyclerViewAdapterHelper)
* [ImageLoader](https://github.com/nostra13/Android-Universal-Image-Loader)
* [Retrofit](https://github.com/square/retrofit)
* [RxJava](https://github.com/ReactiveX/RxJava)
* [ButterKnife](https://github.com/JakeWharton/butterknife)
* [MultipleTheme](https://github.com/dersoncheng/MultipleTheme)
* [Gson](https://github.com/google/gson)
* [JieCaoVideoPlayer](https://github.com/lipangit/JieCaoVideoPlayer)

技术要点

主要是一些第三方库的使用
首页顶部导航使用的hongyang大神的ColorTrackView然后做了一下封装来实现滑动渐变效果
多种Item布局展示->BaseRecyclerViewAdapterHelper
日夜间模式切换->MultipleTheme
个人中心 自定义ScrollView实现下拉图片放大
新闻详情我采用的是RecyclerView添加头的方式添加WebView（当然是Adapter里面添加）,加载页面成功之后获取评论信息，点击评论图标滑动至评论第一条，这里我是调用recyclerView.smoothScrollToPosition(1);
视频播放我使用的是JieCaoVideoPlayer,一群大牛封装的代码，底层实际使用ijkplayer,视频源均使用非正常手段获取，视频源地址分析请看我的另一篇博客手撸一个今日头条视频下载器
问题1
在使用MultipleTheme的时候唯一的缺陷就是需要在布局里面大量使用到自定义控件，这对于我们的项目而言，布局看着很冗余，也有点恶心。。我有时候就在想，那我可不可以写原生控件，然后在特定的时机来个偷梁换柱换成我们的自定义控件呢？(比如我们布局写RelativeLayout—转换成MyRelativeLayout),似乎好像是可以的。

思路1

当时想到一个最简单最快实现的方法，也就是替换，我在布局里面写原生控件，然后在用工具全局替换成我们的自定义控件，但是假如我们换了包名，那就需要重新替换，这无疑是不易扩展的，所以这个方法放弃掉

思路2

不知道大家有木有发现就是，我们在布局里面写上Button、ImageView、TextView等这些控件的时候，在5.0以上运行的时候实际变成了AppCompatButton、AppCompatImageView、AppCompatTextView(debug或者打印对象就可以看到实际的类型),在当我们运行的时候就这样悄无声息的给替换了，那系统又是怎么做到的？那只要找到它的实现方法，我们的问题不就迎刃而解了吗？



于是我找到系统替换的代码（以下代码全部基于Api23）

AppCompatViewInflater.java
```
public final View createView(View parent, final String name, @NonNull Context context,
        @NonNull AttributeSet attrs, boolean inheritContext,
        boolean readAndroidTheme, boolean readAppTheme, boolean wrapContext) {
    final Context originalContext = context;
    // We can emulate Lollipop's android:theme attribute propagating down the view hierarchy
    // by using the parent's context
    if (inheritContext && parent != null) {
        context = parent.getContext();
    }
    if (readAndroidTheme || readAppTheme) {
        // We then apply the theme on the context, if specified
        context = themifyContext(context, attrs, readAndroidTheme, readAppTheme);
    }
    if (wrapContext) {
        context = TintContextWrapper.wrap(context);
    }
    View view = null;
    // We need to 'inject' our tint aware Views in place of the standard framework versions
    switch (name) {
        case "TextView":
            view = new AppCompatTextView(context, attrs);
            break;
        case "ImageView":
            view = new AppCompatImageView(context, attrs);
            break;
        case "Button":
            view = new AppCompatButton(context, attrs);
            break;
        case "EditText":
            view = new AppCompatEditText(context, attrs);
            break;
        case "Spinner":
            view = new AppCompatSpinner(context, attrs);
            break;
        case "ImageButton":
            view = new AppCompatImageButton(context, attrs);
            break;
        case "CheckBox":
            view = new AppCompatCheckBox(context, attrs);
            break;
        case "RadioButton":
            view = new AppCompatRadioButton(context, attrs);
            break;
        case "CheckedTextView":
            view = new AppCompatCheckedTextView(context, attrs);
            break;
        case "AutoCompleteTextView":
            view = new AppCompatAutoCompleteTextView(context, attrs);
            break;
        case "MultiAutoCompleteTextView":
            view = new AppCompatMultiAutoCompleteTextView(context, attrs);
            break;
        case "RatingBar":
            view = new AppCompatRatingBar(context, attrs);
            break;
        case "SeekBar":
            view = new AppCompatSeekBar(context, attrs);
            break;
    }
    if (view == null && originalContext != context) {
        // If the original context does not equal our themed context, then we need to manually
        // inflate it using the name so that android:theme takes effect.
        view = createViewFromTag(context, name, attrs);
    }
    if (view != null) {
        // If we have created a view, check it's android:onClick
        checkOnClickListener(view, attrs);
    }
    return view;
}```
当我们在xml写的那些布局映射成对象的时候，都会调用到这里来转换成对应的AppCompat。

偷梁换柱的关键点我们找到了，那如何找到这个入口呢？

其实当我们加载布局的时候最终都会用LayoutInflater来加载，所以我打算从这里入手，看源码我发现有一个接口可以利用->Factory,这个接口有一个方法

```
public interface Factory {
    /**
     * Hook you can supply that is called when inflating from a LayoutInflater.
     * You can use this to customize the tag names available in your XML
     * layout files.
     * 
     * <p>
     * Note that it is good practice to prefix these custom names with your
     * package (i.e., com.coolcompany.apps) to avoid conflicts with system
     * names.
     * 
     * @param name Tag name to be inflated.
     * @param context The context the view is being created in.
     * @param attrs Inflation attributes as specified in XML file.
     * 
     * @return View Newly created view. Return null for the default
     *         behavior.
     */
    public View onCreateView(String name, Context context, AttributeSet attrs);
}```
果然功夫不负有心人，如果我们实现了这个接口，最终加载布局的时候那么就会调用onCreateView在这里面来实现偷梁换柱替换成我们的自定义控件

ok，入口和关键代码都找到了，剩下的就是撸代码了

```
public class SkinFactory implements LayoutInflaterFactory {
    private AppCompatActivity mActivity;
    public SkinFactory(AppCompatActivity activity) {
        mActivity = activity;
    }
    
@Override
    public View onCreateView(View parent, String name, Context context, AttributeSet attrs) {
        View view = null;
		//是否需要替换成自定义View
        boolean isColorUi = attrs.getAttributeBooleanValue("http://schemas.android.com/apk/res-auto", "isColorUi", false);
        if (!isColorUi) return delegateCreateView(parent, name, context, attrs);
        switch (name) {
            case "TextView":
                view = new ColorTextView(context, attrs);
                break;
            case "ImageView":
                view = new ColorImageView(context, attrs);
                Logger.i("ImageView 转换成"+view.getClass().getSimpleName());
                break;
            case "RelativeLayout":
                view = new ColorRelativeLayout(context, attrs);
                break;
            case "LinearLayout":
                view = new ColorLinearLayout(context, attrs);
                break;
            case "View":
                view = new ColorView(context, attrs);
                break;
        }
        if (view == null) {
            view = delegateCreateView(parent, name, context, attrs);
        }
        return view;
    }
    private View delegateCreateView(View parent, String name, Context context, AttributeSet attrs) {
        AppCompatDelegate delegate = mActivity.getDelegate();
        return delegate.createView(parent, name, context, attrs);
    }
}```
这里isColorUi做了一个标示，因为有的是不需要转换的，如果不转换，直接走系统的创建View流程

关键代码写好了，下面是入口

BaseActivity.java

```
   protected void onCreate(Bundle savedInstanceState) {
super.onCreate(savedInstanceState);
LayoutInflaterCompat.setFactory(layoutInflater, new SkinFactory(this));
}```
原本以为这样就完美的解决了，没想到又引出了下一个问题
```
Caused by: java.lang.IllegalStateException: A factory has already been set on this LayoutInflater
        at android.view.LayoutInflater.setFactory2(LayoutInflater.java:317)
        at android.support.v4.view.LayoutInflaterCompatLollipop.setFactory(LayoutInflaterCompatLollipop.java:28)
        at android.support.v4.view.LayoutInflaterCompat$LayoutInflaterCompatImplV21.setFactory(LayoutInflaterCompat.java:55)
        at android.support.v4.view.LayoutInflaterCompat.setFactory(LayoutInflaterCompat.java:85)
        at me.weyye.todaynews.base.BaseActivity.setLayoutInflaterFactory(BaseActivity.java:70)
        at me.weyye.todaynews.base.BaseActivity.onCreate(BaseActivity.java:60)
        at android.app.Activity.performCreate(Activity.java:6910)
        at android.app.Instrumentation.callActivityOnCreate(Instrumentation.java:1123)
        at android.app.ActivityThread.performLaunchActivity(ActivityThread.java:2746)
        at android.app.ActivityThread.handleLaunchActivity(ActivityThread.java:2864) 
        at android.app.ActivityThread.-wrap12(ActivityThread.java) 
        at android.app.ActivityThread$H.handleMessage(ActivityThread.java:1567)```
于是找到这个方法
```
public void setFactory(Factory factory) {
        if (mFactorySet) {
            throw new IllegalStateException("A factory has already been set on this LayoutInflater");
        }
        if (factory == null) {
            throw new NullPointerException("Given factory can not be null");
        }
        mFactorySet = true;
        if (mFactory == null) {
            mFactory = factory;
        } else {
            mFactory = new FactoryMerger(factory, null, mFactory, mFactory2);
        }
    }```
当mFactorySet=true的时候就会抛出这个错误，可是我并没有去set，那么可能是系统set了，对，没错，不然它怎么转换成AppCompat呢。

那么我只需要用反射把mFactorySet改成false就可以了

于是乎我修改了下原来的代码

BaseActivity.java
```
@Override
protected void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	setLayoutInflaterFactory();
}
   public void setLayoutInflaterFactory() {
       LayoutInflater layoutInflater = getLayoutInflater();
       try {
           Field mFactorySet = LayoutInflater.class.getDeclaredField("mFactorySet");
           mFactorySet.setAccessible(true);
           mFactorySet.set(layoutInflater, false);
           LayoutInflaterCompat.setFactory(layoutInflater, new SkinFactory(this));
       } catch (Exception e) {
           e.printStackTrace();
       }
   }```
先利用反射改成false然后在设置上去，这样就不会报错了

问题2
好吧，几经周折终于完成了日夜间切换，但是当我滑动新闻列表的时候，又一个问题出来了…

当点击切换成夜间主题后，列表滑动后有的还是白天的主题，这很明显是RecyclerView复用的问题，我的思路是当点击切换主题后清除掉复用的View,这样就不会出现这种问题。怎么清除呢？好像RecyclerView没有直接给我们方法，所以我得去源码好好看看，发现RecyclerView里面有个内部类Recycler用来管理复用和回收的类，而且有clear方法，

```
public final class Recycler {
	...
	/**
    * Clear scrap views out of this recycler. Detached views contained within a
    * recycled view pool will remain.
    */
    public void clear() {
        mAttachedScrap.clear();
        recycleAndClearCachedViews();
    }
	...
}```
看代码好像是我所需要的，于是找到这个类对应的变量mRecycler,可惜的是private并且没有get方法，那就只好反射咯~
```
RecyclerView recyclerView = (RecyclerView) rootView;
try {
    Field mRecyclerField = RecyclerView.class.getDeclaredField("mRecycler");
    mRecyclerField.setAccessible(true);
    Method clearMethod = RecyclerView.Recycler.class.getDeclaredMethod("clear");
    clearMethod.setAccessible(true);
    clearMethod.invoke(mRecyclerField.get(recyclerView));
} catch (Exception e) {
    e.printStackTrace();
}```
ok,成功解决!
果然功夫不负有心人，如果我们实现了这个接口，最终加载布局的时候那么就会调用`onCreateView`在这里面来实现偷梁换柱替换成我们的自定义控件

# TODO

* 加入未写界面以及功能
* 逻辑代码的整理

# 声明

这个属于个人开发作品，仅做学习交流使用，如用到实际项目还需多考虑其他因素如并发等，请多多斟酌。**诸位勿传播于非技术人员，拒绝用于商业用途，数据均属于非正常渠道获取，原作公司拥有所有权利。**

# License

	Copyright (C) 2017 WeyYe
	
	Licensed under the Apache License, Version 2.0 (the "License");
	you may not use this file except in compliance with the License.
	You may obtain a copy of the License at
	
	http://www.apache.org/licenses/LICENSE-2.0
	
	Unless required by applicable law or agreed to in writing, software
	distributed under the License is distributed on an "AS IS" BASIS,
	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
	See the License for the specific language governing permissions and
	limitations under the License.
