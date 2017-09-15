
package com.mcmoddev.bot.cursemeta;

import java.util.List;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class LatestFile {

    @SerializedName("AlternateFileId")
    @Expose
    private Long alternateFileId;
    @SerializedName("Dependencies")
    @Expose
    private List<Object> dependencies = null;
    @SerializedName("DownloadURL")
    @Expose
    private String downloadURL;
    @SerializedName("FileDate")
    @Expose
    private String fileDate;
    @SerializedName("FileName")
    @Expose
    private String fileName;
    @SerializedName("FileNameOnDisk")
    @Expose
    private String fileNameOnDisk;
    @SerializedName("FileStatus")
    @Expose
    private String fileStatus;
    @SerializedName("GameVersion")
    @Expose
    private List<String> gameVersion = null;
    @SerializedName("Id")
    @Expose
    private Long id;
    @SerializedName("IsAlternate")
    @Expose
    private Boolean isAlternate;
    @SerializedName("IsAvailable")
    @Expose
    private Boolean isAvailable;
    @SerializedName("PackageFingerprint")
    @Expose
    private Long packageFingerprint;
    @SerializedName("ReleaseType")
    @Expose
    private String releaseType;

    public Long getAlternateFileId () {

        return this.alternateFileId;
    }

    public void setAlternateFileId (Long alternateFileId) {

        this.alternateFileId = alternateFileId;
    }

    public List<Object> getDependencies () {

        return this.dependencies;
    }

    public void setDependencies (List<Object> dependencies) {

        this.dependencies = dependencies;
    }

    public String getDownloadURL () {

        return this.downloadURL;
    }

    public void setDownloadURL (String downloadURL) {

        this.downloadURL = downloadURL;
    }

    public String getFileDate () {

        return this.fileDate;
    }

    public void setFileDate (String fileDate) {

        this.fileDate = fileDate;
    }

    public String getFileName () {

        return this.fileName;
    }

    public void setFileName (String fileName) {

        this.fileName = fileName;
    }

    public String getFileNameOnDisk () {

        return this.fileNameOnDisk;
    }

    public void setFileNameOnDisk (String fileNameOnDisk) {

        this.fileNameOnDisk = fileNameOnDisk;
    }

    public String getFileStatus () {

        return this.fileStatus;
    }

    public void setFileStatus (String fileStatus) {

        this.fileStatus = fileStatus;
    }

    public List<String> getGameVersion () {

        return this.gameVersion;
    }

    public void setGameVersion (List<String> gameVersion) {

        this.gameVersion = gameVersion;
    }

    public Long getId () {

        return this.id;
    }

    public void setId (Long id) {

        this.id = id;
    }

    public Boolean getIsAlternate () {

        return this.isAlternate;
    }

    public void setIsAlternate (Boolean isAlternate) {

        this.isAlternate = isAlternate;
    }

    public Boolean getIsAvailable () {

        return this.isAvailable;
    }

    public void setIsAvailable (Boolean isAvailable) {

        this.isAvailable = isAvailable;
    }

    public Long getPackageFingerprint () {

        return this.packageFingerprint;
    }

    public void setPackageFingerprint (Long packageFingerprint) {

        this.packageFingerprint = packageFingerprint;
    }

    public String getReleaseType () {

        return this.releaseType;
    }

    public void setReleaseType (String releaseType) {

        this.releaseType = releaseType;
    }

}
