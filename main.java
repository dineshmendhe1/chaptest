import java.util.....



package org.casl.chap.chaprest;
import java.beans.PropertyVetoException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.hibernate.Session;
import org.hibernate.query.NativeQuery;
import org.hibernate.query.Query;
import model.Answer;
import model.HibernateUtil;
import model.Interview;
import model.Interviewinstance;
import model.Multipleanswer;
import model.NewInstrument;
import model.NewQuestion;
import model.User;
@Path("/")
public class MainService
{
  static HibernateUtil hibernate;
  static {
      hibernate = new HibernateUtil();
  }
  
  @Path("GetActiveInterview")
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public Object getInterview(){ 
      Session session = hibernate.getSession();
      try 
      {
          session.beginTransaction();
          String hql = "from Interview where id in (9999,948)";
          Query<Interview> query = session.createQuery(hql);
          List<Interview> interviews = query.list();
          session.close();
          return interviews;
    } catch (Exception ex) {
          session.close();
          return Response.status(500).entity(ex.getMessage()).build();
    }
  }
  
  @Path("GetAssignedParticipant")
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public Object getAssignedParticipant(@QueryParam("staffId") String staffId, @QueryParam("iid") int iid){
      Session session = hibernate.getSession();
      try {
          session.beginTransaction();        
          String query = String.format("SELECT u.* from user u join assignment a on u.id=a.participantId "
                + "left join interviewinstance ii on ii.interviewId = a.interviewId and ii.participantId = a.participantId "
                + "where a.staffId=%s and a.interviewId=%s "
                + "and (ii.percentage is null or (ii.percentage is not null and ii.percentage <> 1));", staffId, iid);
          NativeQuery queryH = session.createNativeQuery(query,User.class);
          List<User> users = queryH.list();
          session.close();
          return users;
        } catch (Exception ex) {
          session.close();
          return Response.status(500).entity(ex.getMessage()).build();
        }
  }
  
  @Path("GetParticipantInfo")
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public Object getParticipantInfo(@QueryParam("pid") String pid){
      Session session = hibernate.getSession();
      try {
          session.beginTransaction();        
          String query = String.format("SELECT u.* from user u where u.id=%s", pid);
          NativeQuery queryH = session.createNativeQuery(query,User.class);
          List<User> users = queryH.list();
          session.close();
          return users;
        } catch (Exception ex) {
          session.close();
          return Response.status(500).entity(ex.getMessage()).build();
        }
  }
  
  @Path("GetInstrument")
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public Object getInstrument(@QueryParam("id") int id){
    Session session = hibernate.getSession();
    try {
      session.beginTransaction();
      String query = String.format("SELECT i.* from form f join chap2.new_instrument i on i.id = f.instrumentId where f.interviewId=%d order by f.instrumentorder", id);
      NativeQuery<NewInstrument> queryH = session.createNativeQuery(query,NewInstrument.class);
      List<NewInstrument> instruments = (List<NewInstrument>)queryH.list();
      session.close();
      return instruments;
    } catch (Exception ex) {
      session.close();
      return Response.status(500).entity(ex.getMessage()).build();
    }
  }
  
  @Path("SaveInterview")
  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  public Object saveInterviewInstance(List<Interviewinstance> interviewInstances){
    Session session = hibernate.getSession();
      try {
          session.beginTransaction();
          for (Interviewinstance interviewInstance : interviewInstances){
              session.saveOrUpdate(interviewInstance);
          }
          session.getTransaction().commit();
          session.close();
          return Response.status(201).build();
    } catch (Exception ex) {
        session.close();
        ex.printStackTrace();
        return Response.status(500).entity(ex.getMessage()).build();
    }
  }
  
  
  @Path("GetQuestion")
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public Object getQuestion(@QueryParam("id") int id)
  {
      Session session = hibernate.getSession();
      try {
          session.beginTransaction();
          String query = String.format("select q.* from chap2.new_question q where q.instrumentid=%d order by q.id", id);
          NativeQuery<NewQuestion> queryH = session.createNativeQuery(query,NewQuestion.class);
          List<NewQuestion> questions = queryH.list();
          session.close();
          return questions;
    } catch (Exception ex) {
      session.close();
      return Response.status(500).entity(ex.getMessage()).build();
    }
  }
  
  @Path("GetAllQuestion")
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public Object getAllQuestion(@QueryParam("list") List<Integer> instrumentIds){
      Session session = hibernate.getSession();
      try {
          session.beginTransaction();
          String idSql = instrumentIds.toString().replace("[","").replace("]","");
          String query = String.format("select q.* from chap2.new_question q where q.instrumentid in (%s)",idSql);
          NativeQuery<NewQuestion> queryH = session.createNativeQuery(query,NewQuestion.class);
          List<NewQuestion> questions = queryH.list();
          session.close();
          return questions;
        } catch (Exception ex) {
          session.close();
          throw ex;
        }
  }
  
  
  @Path("SaveAnswer")
  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  public Object saveAnswer(List<Answer> answers){
      Session session = hibernate.getSession();
      try {
          session.beginTransaction();
          for (Answer answer : answers){
              session.saveOrUpdate(answer);
          }
          session.getTransaction().commit();
          return Response.status(201).entity(answers.size()+"").build();
      } catch (Exception ex) {
          return Response.status(500).entity(ex.getMessage()).build();
      }
  }
  
  @Path("SaveMulAnswer")
  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  public Object saveMulAnswer(List<Multipleanswer> answers, @QueryParam("isPerQuestion") boolean isPerQuestion){
      if (answers!=null && answers.size()>0){
          Session session = hibernate.getSession();
          String deletequery="";
          try {
              session.beginTransaction();
              Multipleanswer answerT = answers.get(0);
             
              if (!isPerQuestion){
                  deletequery = String.format("delete Multipleanswer where participantId = %s and interviewId = %d",
                      answerT.getParticipantId(),answerT.getInterviewId());
              }
              else{
                  deletequery = String.format("delete Multipleanswer where participantId = %s and interviewId = %d and questionId = %s",
                      answerT.getParticipantId(),answerT.getInterviewId(),answerT.getQuestionId());
              }
              Query query = session.createQuery(deletequery);
              query.executeUpdate();
              session.flush();
              //insertSession.beginTransaction();
              for (Multipleanswer answer : answers){            
                  session.saveOrUpdate(answer);
              }
              session.getTransaction().commit();
              session.close();
              return Response.status(201).entity(answers.size()+"").build();
          } catch (Exception ex) {
              session.close();
              return Response.status(500).entity(ex.getMessage()).build();
          }}
      return null;
  }
  
  
  @Path("Authenticate")
  @GET
  public String authenticate(){
      return "authenticated";
  }
}
