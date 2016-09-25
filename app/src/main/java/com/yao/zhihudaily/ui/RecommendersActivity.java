package com.yao.zhihudaily.ui;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.google.gson.Gson;
import com.orhanobut.logger.Logger;
import com.yao.zhihudaily.R;
import com.yao.zhihudaily.model.RecommendsJson;
import com.yao.zhihudaily.net.OkHttpSync;
import com.yao.zhihudaily.net.UrlConstants;
import com.yao.zhihudaily.net.ZhihuHttp;
import com.yao.zhihudaily.tool.DividerItemDecoration;

import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.Response;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by Administrator on 2016/9/16.
 */
public class RecommendersActivity extends Activity {

    private static final String TAG = "RecommendersActivity";
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.rvRecommenders)
    RecyclerView rvRecommenders;

    private RecommendsJson recommendsJson;
    private int id;
    private RecommenderAdapter recommenderAdapter;
    private LinearLayoutManager linearLayoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recommenders);
        ButterKnife.bind(this);

        id = getIntent().getIntExtra("id", 0);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        recommenderAdapter = new RecommenderAdapter(this);
        rvRecommenders.setAdapter(recommenderAdapter);
        rvRecommenders.setLayoutManager(linearLayoutManager = new LinearLayoutManager(this));
        rvRecommenders.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL_LIST));


        //获取推荐者
        getRecommenders(String.valueOf(id));
    }

    private void getRecommenders(String id) {
        Subscriber subscriber = new Subscriber<RecommendsJson>() {

            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {
                Logger.e(e, "Subscriber onError()");
            }

            @Override
            public void onNext(RecommendsJson recommendsJson) {
                recommenderAdapter.addList(recommendsJson.getItems().get(0).getRecommenders());
                recommenderAdapter.notifyDataSetChanged();
            }
        };

        ZhihuHttp.getZhihuHttp().getRecommends(subscriber, id);
    }

    @Deprecated
    private void getRecommendersOld() {
        Observable.create(new Observable.OnSubscribe<RecommendsJson>() {

            @Override
            public void call(Subscriber<? super RecommendsJson> subscriber) {
                try {
                    Response response = OkHttpSync.get(String.format(UrlConstants.RECOMMENDERS, id));
                    if (response.isSuccessful()) {
                        String json = response.body().string();
                        recommendsJson = new Gson().fromJson(json, RecommendsJson.class);
                        recommenderAdapter.addList(recommendsJson.getItems().get(0).getRecommenders());
                        subscriber.onCompleted();
                    } else {
                        subscriber.onError(new Exception("error"));
                    }
                } catch (IOException e) {
                    subscriber.onError(e);
                }
            }
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<RecommendsJson>() {

                    @Override
                    public void onCompleted() {
                        recommenderAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onError(Throwable e) {
                        Logger.e(e, "Subscriber onError()");
                    }

                    @Override
                    public void onNext(RecommendsJson recommendsJson) {
                    }
                });
    }
}
