package com.yao.zhihudaily.ui.daily;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.gson.Gson;
import com.yao.zhihudaily.R;
import com.yao.zhihudaily.model.Comment;
import com.yao.zhihudaily.model.CommentJson;
import com.yao.zhihudaily.model.DailyExtra;
import com.yao.zhihudaily.net.OkHttpSync;
import com.yao.zhihudaily.tool.DividerItemDecoration;

import java.io.IOException;
import java.util.List;

import okhttp3.Response;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by Administrator on 2016/9/4.
 */
public class CommentsFragment extends Fragment {

    private RecyclerView rvComments;
    private List<Comment> comments;
    private CommentAdapter commentAdapter;
    private LinearLayoutManager linearLayoutManager;

    private int id;
    private DailyExtra dailyExtra;
    private String url;
    private int count;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_short_comments, null);

        Bundle bundle = getArguments();
        id = bundle.getInt("id", 0);
        dailyExtra = (DailyExtra) bundle.getSerializable("dailyExtra");
        url = bundle.getString("url");
        count = bundle.getInt("count");



        rvComments = (RecyclerView) view.findViewById(R.id.rvComments);
        rvComments.setLayoutManager(linearLayoutManager = new LinearLayoutManager(getActivity()));
        rvComments.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL_LIST));
        rvComments.setAdapter(commentAdapter = new CommentAdapter(getActivity()));

        if (count > 20) {
            rvComments.addOnScrollListener(new RecyclerView.OnScrollListener() {
                private int lastVisibleItemPosition;
                private int visibleItemCount;
                private int totalItemCount;
                @Override
                public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                    super.onScrollStateChanged(recyclerView, newState);
                    RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
                    visibleItemCount = layoutManager.getChildCount();
                    lastVisibleItemPosition = ((LinearLayoutManager) layoutManager).findLastVisibleItemPosition();
                    totalItemCount = layoutManager.getItemCount();
                    if (visibleItemCount > 0 && newState == RecyclerView.SCROLL_STATE_IDLE
                            && lastVisibleItemPosition >= totalItemCount - 1) {
                        //加载更多
                        final Snackbar snackbar = Snackbar.make(rvComments, "如想查看更多评论\n    请下载正版知乎日报.", Snackbar.LENGTH_SHORT);
                        snackbar.setAction("关闭", new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        snackbar.dismiss();
                                    }
                                });
                        snackbar.show();
                    }
                }
                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);
                }
            });
        }

        Subscription subscription = Observable.create(new Observable.OnSubscribe<Boolean>() {

            @Override
            public void call(Subscriber<? super Boolean> subscriber) {
                try {
                    Response response = OkHttpSync.get(String.format(url, String.valueOf(id)));
                    if (response.isSuccessful()) {
                        String json = response.body().string();
                        CommentJson commentJson = new Gson().fromJson(json, CommentJson.class);
                        comments = commentJson.getComments();
                        commentAdapter.addList(comments);
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
                .subscribe(new Subscriber<Boolean>() {

                    @Override
                    public void onCompleted() {
                        commentAdapter.notifyDataSetChanged();
//                        toolbar.setTitle(toolbar.getTitle() + "(以下展示" + comments.size() + "条)");
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e("YAO", "onError: " + e.toString());
                    }

                    @Override
                    public void onNext(Boolean isRefreshing) {
                    }
                });

        return view;
    }
}