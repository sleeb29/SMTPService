package com.service.smtp.handler;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.service.smtp.model.MessageQueueServiceData;
import com.service.smtp.model.Post;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class MessageQueueServiceDataDeserializer extends StdDeserializer<MessageQueueServiceData> {

    public MessageQueueServiceDataDeserializer() {
        this(null);
    }

    public MessageQueueServiceDataDeserializer(Class<?> vc) {
        super(vc);
    }

    @Override
    public MessageQueueServiceData deserialize(JsonParser jsonParser, DeserializationContext context) throws IOException {

        JsonNode node = jsonParser.getCodec().readTree(jsonParser);
        MessageQueueServiceData messageQueueServiceData = new MessageQueueServiceData();
        messageQueueServiceData.setUserIds(deserializeUserIds(node.get("userIds")));
        messageQueueServiceData.setService(formatTreeNodeToString(node.get("service")));
        messageQueueServiceData.setTopic(formatTreeNodeToString(node.get("topic")));
        messageQueueServiceData.setPost(deserializePost(node.get("post")));

        return messageQueueServiceData;
    }

    private Set<String> deserializeUserIds(TreeNode userIdsNode){

        Set<String> userIds = new HashSet<>();

        int userIdsSize = userIdsNode.size();
        int i = 0;
        while(i < userIdsSize){
            userIds.add(formatTreeNodeToString(userIdsNode.get(i)));
            i++;
        }

        return userIds;

    }

    private Post deserializePost(TreeNode postNode){

        Post post = new Post();
        post.setTitle(formatTreeNodeToString(postNode.get("title")));
        post.setText(formatTreeNodeToString(postNode.get("text")));

        if(postNode.get("thread") != null && postNode.get("thread").get("siteType") != null){
            post.setSiteType(formatTreeNodeToString(postNode.get("thread").get("siteType")));
        }

        return post;

    }

    private String formatTreeNodeToString(TreeNode sourceNode) {
        return sourceNode.toString().replace("\"", "");
    }


}
