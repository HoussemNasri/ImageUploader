package com.example.imageuploader.Adapters;

import android.content.Context;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.imageuploader.Models.Upload;
import com.example.imageuploader.R;


/**
 * A custom adapter to use with the RecyclerView widget.
 */
public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context mContext;
    private ArrayList<Upload> uploadsList;

    private OnItemClickListener mItemClickListener;


    public RecyclerViewAdapter(Context context, ArrayList<Upload> uploadsList) {
        this.mContext = context;
        this.uploadsList = uploadsList;
    }

    public void updateList(ArrayList<Upload> uploadsList) {
        this.uploadsList = uploadsList;
        notifyDataSetChanged();

    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {

        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.image_item_layout, viewGroup, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {

        //Here you can fill your row view
        if (holder instanceof ViewHolder) {
            final Upload model = getItem(position);
            ViewHolder genericViewHolder = (ViewHolder) holder;
            String imageUrl = model.getImageUrl();
            String imageName = model.getName();
            genericViewHolder.image_title.setText(imageName);
            Glide.with(mContext)
                    .load(imageUrl)
                    .placeholder(R.drawable.image_place_holder)
                    .fitCenter()
                    .centerCrop()
                    .into(genericViewHolder.uploaded_image);

        }
    }


    @Override
    public int getItemCount() {

        return uploadsList.size();
    }

    public void SetOnItemClickListener(final OnItemClickListener mItemClickListener) {
        this.mItemClickListener = mItemClickListener;
    }

    private Upload getItem(int position) {
        return uploadsList.get(position);
    }


    public interface OnItemClickListener {
        void onItemClick(int position);

        void onDeleteClick(int position);

        void onWhateverClick(int position);

    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnCreateContextMenuListener, MenuItem.OnMenuItemClickListener {

        private ImageView uploaded_image;
        private TextView image_title;

        public ViewHolder(final View itemView) {
            super(itemView);
            uploaded_image = itemView.findViewById(R.id.image_upload);
            image_title = itemView.findViewById(R.id.image_title);

            itemView.setOnClickListener(this);
            itemView.setOnCreateContextMenuListener(this);
        }

        @Override
        public void onClick(View view) {
            if(mItemClickListener != null)
                 mItemClickListener.onItemClick(getAdapterPosition());
        }

        @Override
        public void onCreateContextMenu(ContextMenu contextMenu, View view, ContextMenu.ContextMenuInfo contextMenuInfo) {
            contextMenu.setHeaderTitle("select action");
            MenuItem doWhatever = contextMenu.add(Menu.NONE ,1, 1 ,"doWhatever");
            MenuItem delete = contextMenu.add(Menu.NONE,2,2,"Delete");

            doWhatever.setOnMenuItemClickListener(this);
            delete.setOnMenuItemClickListener(this);
        }

        @Override
        public boolean onMenuItemClick(MenuItem menuItem) {
            if(mItemClickListener != null){
                int position = getAdapterPosition();
                switch (menuItem.getItemId()){
                    case 1:
                        mItemClickListener.onWhateverClick(position);
                        return true;
                    case 2:
                        mItemClickListener.onDeleteClick(position);
                        return true;
                    default:
                        return false;
                }
            }

            return false;
        }
    }

}

