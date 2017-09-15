
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

    public Long getAlternateFileId() {
        return alternateFileId;
    }

    public void setAlternateFileId(Long alternateFileId) {
        this.alternateFileId = alternateFileId;
    }

    public List<Object> getDependencies() {
        return dependencies;
    }

    public void setDependencies(List<Object> dependencies) {
        this.dependencies = dependencies;
    }

    public String getDownloadURL() {
        return downloadURL;
    }

    public void setDownloadURL(String downloadURL) {
        this.downloadURL = downloadURL;
    }

    public String getFileDate() {
        return fileDate;
    }

    public void setFileDate(String fileDate) {
        this.fileDate = fileDate;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileNameOnDisk() {
        return fileNameOnDisk;
    }

    public void setFileNameOnDisk(String fileNameOnDisk) {
        this.fileNameOnDisk = fileNameOnDisk;
    }

    public String getFileStatus() {
        return fileStatus;
    }

    public void setFileStatus(String fileStatus) {
        this.fileStatus = fileStatus;
    }

    public List<String> getGameVersion() {
        return gameVersion;
    }

    public void setGameVersion(List<String> gameVersion) {
        this.gameVersion = gameVersion;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Boolean getIsAlternate() {
        return isAlternate;
    }

    public void setIsAlternate(Boolean isAlternate) {
        this.isAlternate = isAlternate;
    }

    public Boolean getIsAvailable() {
        return isAvailable;
    }

    public void setIsAvailable(Boolean isAvailable) {
        this.isAvailable = isAvailable;
    }

    public Long getPackageFingerprint() {
        return packageFingerprint;
    }

    public void setPackageFingerprint(Long packageFingerprint) {
        this.packageFingerprint = packageFingerprint;
    }

    public String getReleaseType() {
        return releaseType;
    }

    public void setReleaseType(String releaseType) {
        this.releaseType = releaseType;
    }

}
