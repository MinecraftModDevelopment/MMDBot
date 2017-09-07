package net.darkhax.cursedata;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Downloads {

    @SerializedName("monthly")
    @Expose
    private long monthly;
    @SerializedName("total")
    @Expose
    private long total;

    public long getMonthly () {

        return this.monthly;
    }

    public void setMonthly (long monthly) {

        this.monthly = monthly;
    }

    public Downloads withMonthly (long monthly) {

        this.monthly = monthly;
        return this;
    }

    public long getTotal () {

        return this.total;
    }

    public void setTotal (long total) {

        this.total = total;
    }

    public Downloads withTotal (long total) {

        this.total = total;
        return this;
    }
}