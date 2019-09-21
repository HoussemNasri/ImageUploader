package com.example.imageuploader.Models;

import com.google.firebase.database.Exclude;

public class Upload {
    private String mName;
    private String mImageUrl;
    private String Key;
    private boolean mCompressed;

    public Upload() {
        //empty constructor needed
    }

    public Upload(String name, String imageUrl,boolean compressed) {
        if (name.trim().equals("")) {
            name = "No Name";
        }

        mName = name;
        mImageUrl = imageUrl;
        mCompressed = compressed;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    public String getImageUrl() {
        return mImageUrl;
    }

    public void setImageUrl(String imageUrl) {
        mImageUrl = imageUrl;
    }

    public boolean isCompressed() {
        return mCompressed;
    }

    public void setCompressed(boolean Compressed) {
        this.mCompressed = Compressed;
    }

    @Exclude
    public String getKey() {
        return Key;
    }
    @Exclude
    public void setKey(String key) {
        Key = key;
    }
}