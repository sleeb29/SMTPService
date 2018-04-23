package com.service.smtp.model;

import java.util.Set;

public class MessageQueueServiceData {

    String service;
    String topic;
    Set<String> userIds;
    Post post;

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public Set<String> getUserIds() {
        return userIds;
    }

    public void setUserIds(Set<String> userIds) {
        this.userIds = userIds;
    }

    public Post getPost() {
        return post;
    }

    public void setPost(Post post) {
        this.post = post;
    }

}