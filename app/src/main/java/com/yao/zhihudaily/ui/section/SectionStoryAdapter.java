package com.yao.zhihudaily.ui.section;

import android.app.Activity;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.yao.zhihudaily.R;
import com.yao.zhihudaily.model.SectionStory;
import com.yao.zhihudaily.tool.Constant;
import com.yao.zhihudaily.tool.OnItemClickListener;
import com.yao.zhihudaily.ui.NewsDetailActivity;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;

/**
 *
 * @author Yao
 * @date 2016/9/10
 */
public class SectionStoryAdapter extends RecyclerView.Adapter<SectionStoryAdapter.StoryHolder> {

    private Activity mActivity;
    private ArrayList<SectionStory> mSectionStories = new ArrayList<>();

    private OnItemClickListener listener = new OnItemClickListener() {
        @Override
        public void onItemClick(int pos) {
            SectionStory sectionStory = mSectionStories.get(pos);
            Intent intent = new Intent(mActivity, NewsDetailActivity.class);
            intent.putExtra(Constant.ID, sectionStory.getId());
            mActivity.startActivity(intent);
        }
    };

    public SectionStoryAdapter(Activity aty) {
        this.mActivity = aty;
    }

    public void addList(ArrayList<SectionStory> stories) {
        this.mSectionStories.addAll(stories);
    }

    @NonNull
    @Override
    public StoryHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new StoryHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_section_story_with_image, parent, false));
    }

    @Override
    public void onBindViewHolder(StoryHolder holder, int position) {
        SectionStory sectionStory = mSectionStories.get(position);
        holder.tvTitle.setText(sectionStory.getTitle());
        holder.tvDescription.setText(sectionStory.getDate());
        if (sectionStory.getImages() != null) {
            Glide.with(mActivity).load(sectionStory.getImages().get(0)).placeholder(R.mipmap.ic_launcher).into(holder.iv);
        }
        holder.itemView.setTag(position);
        holder.itemView.setOnClickListener(listener);

    }

    @Override
    public int getItemCount() {
        return mSectionStories.size();
    }

    class StoryHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.iv_splash)
        ImageView iv;
        @BindView(R.id.tv_title)
        TextView tvTitle;
        @BindView(R.id.tv_description)
        TextView tvDescription;

        public StoryHolder(View view) {
            super(view);
            ButterKnife.bind(this, itemView);
        }
    }


}
