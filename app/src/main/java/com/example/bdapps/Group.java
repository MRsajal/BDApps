package com.example.bdapps;

import java.util.ArrayList;
import java.util.List;

public class Group {
    private String groupId;
    private String groupName;
    private String description;
    private String createBy;
    private long createdAt;
    private List<String> memberIds;
    private String groupImageUrl;

    public Group(){
        this.memberIds=new ArrayList<>();
    }

    public Group(String groupName,String description,String createBy){
        this.groupName=groupName;
        this.description=description;
        this.createBy=createBy;
        this.createdAt=System.currentTimeMillis();
        this.memberIds=new ArrayList<>();
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCreateBy() {
        return createBy;
    }

    public void setCreateBy(String createBy) {
        this.createBy = createBy;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public List<String> getMemberIds() {
        return memberIds;
    }

    public void setMemberIds(List<String> memberIds) {
        this.memberIds = memberIds;
    }

    public String getGroupImageUrl() {
        return groupImageUrl;
    }

    public void setGroupImageUrl(String groupImageUrl) {
        this.groupImageUrl = groupImageUrl;
    }

    public void addMember(String userId){
        if(!memberIds.contains(userId)){
            memberIds.add(userId);
        }
    }
    public void removeMember(String userId){
        memberIds.remove(userId);
    }
}
