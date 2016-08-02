package example.com.mpdlcamera.Model.TO;

import com.google.gson.annotations.Expose;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yingli on 7/18/16.
 */

public class StatementTO {

    @Expose
    private String id;

    @Expose
    private int pos;

    @Expose
    private String type;

    @Expose
    private List<LocalizedStringTO> labels = new ArrayList<LocalizedStringTO>();

    @Expose
    private String vocabulary;

    @Expose
    private List<LiteralConstraintTO> literalConstraints = new ArrayList<LiteralConstraintTO>();

    @Expose
    private String minOccurs;

    @Expose
    private String maxOccurs;

    @Expose
    private String parentStatementId;

    @Expose
    private boolean useInPreview;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getPos() {
        return pos;
    }

    public void setPos(int pos) {
        this.pos = pos;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }



    public List<LocalizedStringTO> getLabels() {
        return labels;
    }

    public void setLabels(List<LocalizedStringTO> labels) {
        this.labels = labels;
    }

    public String getVocabulary() {
        return vocabulary;
    }

    public void setVocabulary(String vocabulary) {
        this.vocabulary = vocabulary;
    }

    public List<LiteralConstraintTO> getLiteralConstraints() {
        return literalConstraints;
    }

    public void setLiteralConstraints(List<LiteralConstraintTO> literalConstraints) {
        this.literalConstraints = literalConstraints;
    }

    public String getMinOccurs() {
        return minOccurs;
    }

    public void setMinOccurs(String minOccurs) {
        this.minOccurs = minOccurs;
    }

    public String getMaxOccurs() {
        return maxOccurs;
    }

    public void setMaxOccurs(String maxOccurs) {
        this.maxOccurs = maxOccurs;
    }

    public String getParentStatementId() {
        return parentStatementId;
    }

    public void setParentStatementId(String parentStatementId) {
        this.parentStatementId = parentStatementId;
    }

    public boolean isUseInPreview() {
        return useInPreview;
    }

    public void setUseInPreview(boolean useInPreview) {
        this.useInPreview = useInPreview;
    }
}
