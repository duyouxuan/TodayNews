package me.weyye.todaynews.base;

import me.weyye.todaynews.utils.ToastUtils;

/**
 * Created by Administrator
 * on 2016/5/18.
 */
public abstract class SubscriberCallBack<T> extends BaseCallBack<ResultResponse<T>> {


    @Override
    public void onNext(ResultResponse response) {

        if (response.message.equals("success")) {
            onSuccess((T) response.data);
        } else {
            ToastUtils.showToast(response.message);
            onFailure(response);
        }
    }

    protected abstract void onSuccess(T response);
}
