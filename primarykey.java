package org.casl.chap.model;
import java.io.Serializable;
import javax.persistence.Embeddable;
/**
 * JPA data model for primary keys.
 * 
  * 
 */
@Embeddable
public class ParticipantSuperset2PK implements Serializable {
    private static final long serialVersionUID = -4606255317872824321L;
    private String participantid;
    private String interviewid;
    public String getParticipantid() {
        return participantid;
    }
    public void setParticipantid(String participantid) {
        this.participantid = participantid;
    }
    public String getInterviewid() {
        return interviewid;
    }
    public void setInterviewid(String interviewid) {
        this.interviewid = interviewid;
    }
        
}
