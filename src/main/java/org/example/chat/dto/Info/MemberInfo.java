package org.example.chat.dto.Info;

import java.time.LocalDateTime;

public class MemberInfo {
    private Long userId;
    private String username;
    private String name;
    private String profileImgUrl;

    private LocalDateTime joinedAt;
    public MemberInfo() { }


    public MemberInfo(Long userId, String username, String name, String profileImgUrl, LocalDateTime joinedAt) {
        this.userId = userId;
        this.username = username;
        this.name = name;
        this.profileImgUrl = profileImgUrl;
        this.joinedAt = joinedAt;

    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getProfileImgUrl() {
        return profileImgUrl;
    }

    public void setProfileImgUrl(String profileImgUrl) {
        this.profileImgUrl = profileImgUrl;
    }

    public LocalDateTime getJoinedAt() {
        return joinedAt;
    }

    public void setJoinedAt(LocalDateTime joinedAt) {
        this.joinedAt = joinedAt;
    }
}
