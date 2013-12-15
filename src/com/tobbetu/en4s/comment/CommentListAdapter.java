package com.tobbetu.en4s.comment;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.tobbetu.en4s.R;
import com.tobbetu.en4s.cache.Cache;

public class CommentListAdapter extends ArrayAdapter<Comment> {

    private Context context;
    private List<Comment> comments;

    public CommentListAdapter(Context context, List<Comment> comments) {
        super(context, R.layout.comment_list_item, comments);

        this.context = context;
        this.comments = comments;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        LayoutInflater myInflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View myRowView = myInflater.inflate(R.layout.comment_list_item, parent,
                false);

        TextView tvComment = (TextView) myRowView
                .findViewById(R.id.tvCommentListComment);
        TextView tvCommentUser = (TextView) myRowView
                .findViewById(R.id.tvCommentUser);
        ImageView ivAvatar = (ImageView) myRowView
                .findViewById(R.id.ivCommentListAvatar);

        /*
         * for future 1. User avatart 2. User name 3. Up Vote and Down Vote
         * count
         */

        Comment aComment = comments.get(position);
        tvComment.setText(aComment.getText().trim());
        tvCommentUser.setText(aComment.getAuthor().getName().trim());

        Cache.getInstance()
                .getImage(aComment.getAuthor().getAvatar(), ivAvatar);

        return myRowView;
    }
}
