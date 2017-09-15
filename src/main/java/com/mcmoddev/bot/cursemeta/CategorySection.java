
package com.mcmoddev.bot.cursemeta;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class CategorySection {

    @SerializedName("GameID")
    @Expose
    private Long gameID;
    @SerializedName("ID")
    @Expose
    private Long iD;
    @SerializedName("InitialInclusionPattern")
    @Expose
    private String initialInclusionPattern;
    @SerializedName("Name")
    @Expose
    private String name;
    @SerializedName("PackageType")
    @Expose
    private String packageType;
    @SerializedName("Path")
    @Expose
    private String path;

    public Long getGameID () {

        return this.gameID;
    }

    public void setGameID (Long gameID) {

        this.gameID = gameID;
    }

    public Long getID () {

        return this.iD;
    }

    public void setID (Long iD) {

        this.iD = iD;
    }

    public String getInitialInclusionPattern () {

        return this.initialInclusionPattern;
    }

    public void setInitialInclusionPattern (String initialInclusionPattern) {

        this.initialInclusionPattern = initialInclusionPattern;
    }

    public String getName () {

        return this.name;
    }

    public void setName (String name) {

        this.name = name;
    }

    public String getPackageType () {

        return this.packageType;
    }

    public void setPackageType (String packageType) {

        this.packageType = packageType;
    }

    public String getPath () {

        return this.path;
    }

    public void setPath (String path) {

        this.path = path;
    }

}
