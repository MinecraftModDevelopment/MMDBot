
package com.mcmoddev.bot.cursemeta;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class GameVersionLatestFile {

    @SerializedName("FileType")
    @Expose
    private String fileType;
    @SerializedName("GameVesion")
    @Expose
    private String gameVesion;
    @SerializedName("ProjectFileID")
    @Expose
    private Long projectFileID;
    @SerializedName("ProjectFileName")
    @Expose
    private String projectFileName;

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public String getGameVesion() {
        return gameVesion;
    }

    public void setGameVesion(String gameVesion) {
        this.gameVesion = gameVesion;
    }

    public Long getProjectFileID() {
        return projectFileID;
    }

    public void setProjectFileID(Long projectFileID) {
        this.projectFileID = projectFileID;
    }

    public String getProjectFileName() {
        return projectFileName;
    }

    public void setProjectFileName(String projectFileName) {
        this.projectFileName = projectFileName;
    }

}
