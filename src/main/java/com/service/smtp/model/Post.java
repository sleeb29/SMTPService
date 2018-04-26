package com.service.smtp.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.service.smtp.handler.MessageQueueServiceDataDeserializer;

public class Post {

    String title;
    String text;
    String siteType;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getSiteType() {
        return siteType;
    }

    public void setSiteType(String siteType) {
        this.siteType = siteType;
    }
}
