package net.darkhax.cursedata;

import java.util.Collections;
import java.util.List;

public class Member {

    private final String username;
    private final String avatar;
    private final String joinDate;
    private final List<Project> projects;

    public Member (String username, String avatar, String joined, List<Project> projects) {
        this.username = username;
        this.avatar = avatar;
        this.joinDate = joined;
        this.projects = projects;

        this.projects.sort(Collections.reverseOrder( (Project p1, Project p2) -> Long.compare(p1.getTotalDownloads(), p2.getTotalDownloads())));
    }

    public String getUsername () {
        return this.username;
    }

    public String getAvatar () {
        return this.avatar;
    }

    public String getJoinDate () {
        return this.joinDate;
    }

    public List<Project> getProjects () {
        return this.projects;
    }
}
