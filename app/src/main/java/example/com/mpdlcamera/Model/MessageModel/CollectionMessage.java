package example.com.mpdlcamera.Model.MessageModel;


import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

import example.com.mpdlcamera.Model.ImejiFolder;

/**
 * Created by yingli on 1/11/16.
 */
public class CollectionMessage {

    @Expose
    @SerializedName("totalNumberOfResults")
    private int totalNumberOfResults;

    @Expose
    @SerializedName("numberOfResults")
    private int numberOfResults;

    @Expose
    @SerializedName("offset")
    private int offset;

    @Expose
    @SerializedName("size")
    private int size;

    @Expose
    @SerializedName("results")
    private List<ImejiFolder> results = new ArrayList<>();


    public CollectionMessage(int totalNumberOfResults, int numberOfResults, int offset, int size, String query, List<ImejiFolder> results) {
        this.totalNumberOfResults = totalNumberOfResults;
        this.numberOfResults = numberOfResults;
        this.offset = offset;
        this.size = size;
        this.results = results;
    }

    public int getTotalNumberOfResults() {
        return totalNumberOfResults;
    }

    public void setTotalNumberOfResults(int totalNumberOfResults) {
        this.totalNumberOfResults = totalNumberOfResults;
    }

    public int getNumberOfResults() {
        return numberOfResults;
    }

    public void setNumberOfResults(int numberOfResults) {
        this.numberOfResults = numberOfResults;
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public List<ImejiFolder> getResults() {
        return results;
    }

    public void setResults(List<ImejiFolder> results) {
        this.results = results;
    }
}
