package example.com.mpdlcamera.Model.TO;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by yingli on 7/18/16.
 */

public class StatementTO {

    private String id;

    private int pos;

    private URI type;

    private List<String> labels = new ArrayList<String>();

    private URI vocabulary;

    private List<LiteralConstraintTO> literalConstraints = new ArrayList<LiteralConstraintTO>();

    private String minOccurs;

    private String maxOccurs;

    private String parentStatementId;

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

    public URI getType() {
        return type;
    }

    public void setType(URI type) {
        this.type = type;
    }



    public List<String> getLabels() {
        return labels;
    }

    public void setLabels(List<String> labels) {
        this.labels = labels;
    }

    public URI getVocabulary() {
        return vocabulary;
    }

    public void setVocabulary(URI vocabulary) {
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
