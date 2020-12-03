package com.neu.edu.assignment2.controller;


import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.neu.edu.assignment2.dao.UserDao;
import com.neu.edu.assignment2.model.*;
import com.neu.edu.assignment2.service.AmazonSNSClient;
import com.timgroup.statsd.NonBlockingStatsDClient;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.commons.CommonsMultipartFile;
import org.springframework.web.server.ResponseStatusException;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.PublishRequest;
import software.amazon.awssdk.services.sns.model.PublishResponse;
import software.amazon.awssdk.services.sns.model.SnsException;

import javax.activation.MimetypesFileTypeMap;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

@RestController
@RequestMapping(value = "/v1")
public class UserController {
    @Autowired
    private UserDao userDao;
    @Autowired
    private Environment env;
    @Autowired
    private AmazonSNSClient amazonSNSClient;

    private final static Logger logger = LoggerFactory.getLogger(UserController.class);
    private final NonBlockingStatsDClient client = new NonBlockingStatsDClient("webapp", "localhost", 8125);
    @PostMapping(value = "/user")
    @ApiOperation(value="Create a user")
    public Object addUser(@RequestBody User user){
        long startTime = System.currentTimeMillis();
        logger.info("Starting addUser method");
        client.incrementCounter("/user");
        try{
            String regex = "^[\\w-_\\.+]*[\\w-_\\.]\\@([\\w]+\\.)+[\\w]+[\\w]$";
            String password = "^([a-zA-Z0-9@*#]{8,15})$";
            if((user.getUsername()!=null&&!user.getUsername().matches(regex))){
                return new ResponseEntity<>("Please enter valid Username",HttpStatus.BAD_REQUEST);
            }
            if(user.getPassword()!=null&&!user.getPassword().matches(password)){
                return new ResponseEntity<>("Please enter valid password",HttpStatus.BAD_REQUEST);
            }
            long endTime = System.currentTimeMillis();

            long duration = endTime - startTime;
            client.recordExecutionTime("/user",duration);
            return userDao.addUser(user);
        }catch(Exception ex){
            logger.error("Bad Request");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Bad Request",ex);
        }
    }


    @GetMapping(value="/user/self")
    @ApiOperation(value="Get User Information")
    public Object getUser(@AuthenticationPrincipal User user){
        logger.info("Starting getUser method");
        long startTime = System.currentTimeMillis();
        client.incrementCounter("GET    /user/self");
        User u = userDao.getUser(user.getUserId());
        long dbEndTime = System.currentTimeMillis();
        long dbDuration = dbEndTime - startTime;
        client.recordExecutionTime("DB call GET   /user/self",dbDuration);
        try {
            if (!u.getPassword().equalsIgnoreCase(user.getPassword())) {
                long endTime = System.currentTimeMillis();
                long duration = endTime - startTime;
                client.recordExecutionTime("GET   /user/self",duration);

                return new ResponseEntity<>("Invalid Credentials",HttpStatus.FORBIDDEN);
            }
        }catch(Exception ex){
            logger.error("Invalid Credentials");
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Invalid Credentials",ex);
        }
        return u;
    }
    @PutMapping(value="/user/self")
    @ApiOperation(value="Update User Information")
    public Object updateUser(@AuthenticationPrincipal User loggedUser , @RequestBody User user){
        logger.info("Starting updateUser method");
        long startTime = System.currentTimeMillis();
        client.incrementCounter("POST   /user/self");
        try {

            if (user.getUsername() != null || user.getAccountCreated() != null || user.getAccountUpdated() != null) {
                return new ResponseEntity<>("Cannot update username/accountupdated/account created fields",HttpStatus.BAD_REQUEST);
            }
            String password = "^([a-zA-Z0-9@*#]{8,15})$";
            if(user.getPassword()!=null&&!user.getPassword().matches(password)){
                return new ResponseEntity<>("Please enter valid password",HttpStatus.BAD_REQUEST);
            }
            long dbStartTime = System.currentTimeMillis();
            userDao.updateUser(loggedUser.getUserId(), user);


            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;
            client.recordExecutionTime("DB call POST   /user/self",endTime-dbStartTime);
            client.recordExecutionTime("POST   /user/self",duration);

            return HttpStatus.NO_CONTENT;
        }catch(Exception e){
            logger.error("Bad Request");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Bad Request",e);
        }
    }
    @GetMapping("/user/{id}")
    public Object getUserDetails(@PathVariable String id){
        logger.info("starting getUserDetails method");
        long startTime = System.currentTimeMillis();
        client.incrementCounter("/user/{id}");
        try {

            long dbStartTime = System.currentTimeMillis();
            Object ob = userDao.getUser(id);
            long endTime = System.currentTimeMillis();
            client.recordExecutionTime("Db call /user/{id}",endTime-dbStartTime);
            long duration = endTime - startTime;
            client.recordExecutionTime("/user/{id}",duration);
            return ob;
        }catch(Exception e){
            logger.error("User not found");
            return new ResponseEntity<>("User not found",HttpStatus.NOT_FOUND);
        }
    }
    @PostMapping(value="/question")
    public Object addQuestions(@AuthenticationPrincipal User loggedUser,@RequestBody Question question){
        logger.info("starting addQuestions method");
        long startTime = System.currentTimeMillis();
        try {
            client.incrementCounter("/question");
            question.setUserId(loggedUser.getUserId());
            Question q = userDao.addQuestion(question);
            long dbEndTime = System.currentTimeMillis();
            long dbDuration = dbEndTime - startTime;
            client.recordExecutionTime("DB Call /question",dbDuration);


            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;
            client.recordExecutionTime("/question",duration);
            amazonSNSClient.sendEmailToUser(loggedUser.getUsername(),q.getQuestionId());
//            SnsClient snsClient = SnsClient.builder()
//                    .region(Region.US_EAST_1)
//                    .build();
//            pubTopic(snsClient,q.getQuestionId(),"user-updates-topic");
            return new ResponseEntity<Question>(q,HttpStatus.CREATED);
        }catch(Exception e){
            logger.error("Bad Request");
            return new ResponseEntity<>("Bad Request",HttpStatus.BAD_REQUEST);
        }
    }
    @PostMapping(value="/question/{question_id}/answer")
    public Object addAnswers(@AuthenticationPrincipal User loggedUser, @RequestBody Answers answer, @PathVariable String question_id){
        logger.info("starting addAnswers method");
        long startTime = System.currentTimeMillis();
        try {
            client.incrementCounter("/question/{question_id}/answer");
            answer.setUserId(loggedUser.getUserId());
            answer.setQuestionId(question_id);

            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;
            client.recordExecutionTime("DB call /question/{question_id}/answer",duration);
            client.recordExecutionTime("/question/{question_id}/answer",duration);
            return userDao.addAnswer(answer);

        }catch(Exception e){
            logger.error("Bad Request");
            return new ResponseEntity<>("Bad Request",HttpStatus.BAD_REQUEST);
        }
    }
    @PutMapping(value="/question/{question_id}/answer/{answer_id}")
    public Object updateAnswer(@AuthenticationPrincipal User loggedUser,@RequestBody Answers answer, @PathVariable String question_id, @PathVariable String answer_id){
        logger.info("starting updateAnswer method");
        long startTime = System.currentTimeMillis();
        client.incrementCounter("PUT    /question/{question_id}/answer/{answer_id}");
        Answers ans = userDao.getAnswer(answer_id);
        long dbEndTime = System.currentTimeMillis();
        client.recordExecutionTime("DB call GET    /question/{question_id}/answer/{answer_id}",dbEndTime-startTime);
        if(ans==null)
            return new ResponseEntity<>("Id Not Found",HttpStatus.NOT_FOUND);
        if(!ans.getUserId().equals(loggedUser.getUserId()))
            return new ResponseEntity<>("Cannot Update Answer",HttpStatus.UNAUTHORIZED);
        answer.setUserId(loggedUser.getUserId());
        answer.setQuestionId(question_id);
        answer.setAnswerId(answer_id);
        Answers a = (Answers)userDao.updateAnswer(answer);

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        client.recordExecutionTime("PUT    /question/{question_id}/answer/{answer_id}",duration);

        if(a==null)
            return new ResponseEntity<>("Please enter valid input",HttpStatus.BAD_REQUEST);
        return new ResponseEntity<Void>(HttpStatus.NO_CONTENT);

    }
    @GetMapping(value="/question/{question_id}/answer/{answer_id}")
    public Object getAnswer(@PathVariable String question_id, @PathVariable String answer_id){
        logger.info("starting getAnswer method");
        long startTime = System.currentTimeMillis();
        try {
            client.incrementCounter("GET    /question/{question_id}/answer/{answer_id}");

            long dbStartTime = System.currentTimeMillis();
            Object ob = userDao.getAnswer(answer_id);
            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;
            client.recordExecutionTime("DB call GET    /question/{question_id}/answer/{answer_id}",endTime-dbStartTime);
            client.recordExecutionTime("GET    /question/{question_id}/answer/{answer_id}",duration);
            return ob;

        }catch(Exception e){
            logger.error("Id not found");
            return new ResponseEntity<>("Id not found",HttpStatus.NOT_FOUND);
        }
    }
    @GetMapping(value="/questions")
    public List<Question> getAllQuestions(){
        logger.info("starting getAllQuestions method");
        long startTime = System.currentTimeMillis();
        client.incrementCounter("/questions");

        long endTime = System.currentTimeMillis();


        long dbStartTime = System.currentTimeMillis();
        List<Question> list = userDao.getAllQuestions();
        long dbEndTime = System.currentTimeMillis();
        long dbDuration = dbEndTime - dbStartTime;
        client.recordExecutionTime("DB call duration /questions",dbDuration);
        long duration = endTime - startTime;
        client.recordExecutionTime("/questions",duration);
        return list;
    }
    @GetMapping(value="/question/{questionId}")
    public Object getQuestion(@PathVariable String questionId){
        logger.info("starting getQuestion method");
        long startTime = System.currentTimeMillis();
        client.incrementCounter("/question/{questionId}");
        Question q =  userDao.getQuestion(questionId);

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        client.recordExecutionTime("db call duration /question/{questionId}",duration);
        client.recordExecutionTime("/question/{questionId}",duration);

        if(q==null)
            return new ResponseEntity<>("Id not found",HttpStatus.NOT_FOUND);
        return q;

    }
    @DeleteMapping(value="/question/{question_id}/answer/{answer_id}")
    public Object deleteAnswer(@AuthenticationPrincipal User loggedUser, @PathVariable String question_id, @PathVariable String answer_id){
        logger.info("starting deleteAnswer method");
        long startTime = System.currentTimeMillis();
        client.incrementCounter("DELETE    /question/{question_id}/answer/{answer_id}");
        Answers ans = userDao.getAnswer(answer_id);
        if(!ans.getUserId().equals(loggedUser.getUserId()))
            return new ResponseEntity<>("Cannot Delete Answer",HttpStatus.UNAUTHORIZED);
        try {
            long dbStartTime = System.currentTimeMillis();
            userDao.deleteAnswer(question_id, answer_id);
            long dbEndTime = System.currentTimeMillis();
            long dbDuration = dbEndTime-dbStartTime;
            client.recordExecutionTime("DB call DELETE    /question/{question_id}/answer/{answer_id}",dbDuration);


            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;
            client.recordExecutionTime("DELETE    /question/{question_id}/answer/{answer_id}",duration);

            return new ResponseEntity<Void>(HttpStatus.NO_CONTENT);
        }catch(Exception e){
            logger.error("Id not found");
            return new ResponseEntity<>("Id not found",HttpStatus.NOT_FOUND);
        }
    }
    @DeleteMapping(value="/question/{question_id}")
    public Object deleteQuestion(@AuthenticationPrincipal User loggedUser,@PathVariable String question_id){
        logger.info("starting deleteQuestion method");
        long startTime = System.currentTimeMillis();
        client.incrementCounter("DELETE   /question/{question_id}");
        Question q = (Question)getQuestion(question_id);
        if(q==null)
            return new ResponseEntity<>("Id not found",HttpStatus.NOT_FOUND);
        if(!q.getAnswers().isEmpty())
            return new ResponseEntity<>("Question cannot be deleted",HttpStatus.BAD_REQUEST);
        if(!loggedUser.getUserId().equals(q.getUserId()))
            return new ResponseEntity<>("User Cannot Update/delete question",HttpStatus.UNAUTHORIZED);
        try {

            long dbStartTime = System.currentTimeMillis();
            userDao.deleteQuestion(question_id, loggedUser.getUserId());
            long dbEndTime = System.currentTimeMillis();
            long dbDuration = dbEndTime-dbStartTime;
            client.recordExecutionTime("DB call DELETE   /question/{question_id}",dbDuration);


            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;
            client.recordExecutionTime("DELETE   /question/{question_id}",duration);

            return new ResponseEntity<Void>(HttpStatus.NO_CONTENT);
        }catch(Exception e){
            logger.error("Id not found");
            return new ResponseEntity<>("Id not found",HttpStatus.NOT_FOUND);
        }
    }
    @PutMapping(value="/question/{question_id}")
    public Object updateQuestion(@AuthenticationPrincipal User loggedUser, @RequestBody Question question, @PathVariable String question_id){
        logger.info("starting updateQuestion method");
        long startTime = System.currentTimeMillis();
        client.incrementCounter("PUT   /question/{question_id}");
        question.setQuestionId(question_id);
        Question q = (Question) userDao.getQuestion(question_id);
        if(q==null)
            return new ResponseEntity<>("Id not found",HttpStatus.NOT_FOUND);
        if(!loggedUser.getUserId().equals(q.getUserId()))
            return new ResponseEntity<>("User Cannot Update/delete question",HttpStatus.UNAUTHORIZED);
        try {
            long dbStartTime = System.currentTimeMillis();
            q = (Question)userDao.updateQuestion(question, q,loggedUser.getUserId());
            long dbEndTime = System.currentTimeMillis();
            long dbDuration = dbEndTime-dbStartTime;
            client.recordExecutionTime("db call PUT   /question/{question_id}",dbDuration);
        } catch (Exception e) {
            logger.error("Unable to update question");
            return new ResponseEntity<>("Unable to update question",HttpStatus.BAD_REQUEST);
        }

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        client.recordExecutionTime("PUT   /question/{question_id}",duration);

        if(q==null)
            return new ResponseEntity<>("Unable to update question",HttpStatus.BAD_REQUEST);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
    @PostMapping(value="/question/{question_id}/file",consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Object uploadQuestionFile(@AuthenticationPrincipal User loggedUser, @PathVariable String question_id , @RequestParam(value = "file") MultipartFile file){
        logger.info("starting uploadQuestionFile method");
        long startTime = System.currentTimeMillis();
        client.incrementCounter("/question/{question_id}/file");
        //        String accessKey="AKIASVHASTC5EPAU5TKX";
//        String secretKey="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";
//        String accessKey=env.getProperty("aws-access-key-id");
//        String secretKey=env.getProperty("aws-secret-access-key");
        Question q = (Question) userDao.getQuestion(question_id);
        if(q==null)
            return new ResponseEntity<>("Id not found",HttpStatus.NOT_FOUND);
        if(!loggedUser.getUserId().equals(q.getUserId()))
            return new ResponseEntity<>("User Cannot Update/delete question",HttpStatus.UNAUTHORIZED);
//        BasicAWSCredentials creds = new BasicAWSCredentials(accessKey, secretKey);
//        AWSStaticCredentialsProvider provider = new AWSStaticCredentialsProvider(creds);
        final AmazonS3 s3 = AmazonS3ClientBuilder.standard().withRegion("us-east-1").withForceGlobalBucketAccessEnabled(true).build();
        String bucket_name = "webapp.tejaswi.chillakuru";
        UUID uuid = UUID.randomUUID();
        String keyName = question_id+"/"+uuid.toString()+"/"+file.getOriginalFilename();
        String extension = FilenameUtils.getExtension(file.getOriginalFilename());

        if(!extension.equals("png")&&!extension.equals("jpg")&&!extension.equals("jpeg")){
            return new ResponseEntity<>("Invalid Image Type",HttpStatus.BAD_REQUEST);
        }
        try{
            QuestionFiles f = new QuestionFiles();
            f.setFileId(uuid.toString());
            f.setUserId(loggedUser.getUserId());
            f.setMime(extension);
            f.setQuestionId(question_id);
            f.setFileName(file.getName());
            f.setSize(String.valueOf(file.getSize()));
            f.setS3objectName(keyName);

            long dbStartTime = System.currentTimeMillis();
            QuestionFiles output = userDao.saveFile(f);
            long dbEndTime = System.currentTimeMillis();
            long dbDuration = dbEndTime-dbStartTime;
            client.recordExecutionTime("db call Duration /question/{question_id}/file",dbDuration);

            File convFile = new File(file.getOriginalFilename());
            convFile.createNewFile();
            FileOutputStream fos = new FileOutputStream(convFile);
            fos.write(file.getBytes());
            fos.close();

            long s3StartTime = System.currentTimeMillis();
            s3.putObject(bucket_name,keyName,convFile);

            long s3EndTime = System.currentTimeMillis();
            long s3Duration = s3EndTime-s3StartTime;
            client.recordExecutionTime("s3 Duration /question/{question_id}/file",s3Duration);

            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;
            client.recordExecutionTime("/question/{question_id}/file",duration);

            return output;

        }catch(AmazonServiceException | IOException e){
            logger.error(e.getMessage());
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }
    @DeleteMapping(value="/question/{question_id}/file/{file_id}")
    public Object deleteFile(@AuthenticationPrincipal User loggedUser, @PathVariable String question_id, @PathVariable String file_id ){
        logger.info("starting deleteFile method");
        long startTime = System.currentTimeMillis();
        client.incrementCounter("/question/{question_id}/file/{file_id}");

        QuestionFiles files = userDao.getFile(file_id);
        if(files==null)
            return new ResponseEntity<>("Id not found",HttpStatus.NOT_FOUND);
        if(!loggedUser.getUserId().equals(files.getUserId()))
            return new ResponseEntity<>("User Cannot Update/delete question",HttpStatus.UNAUTHORIZED);

        final AmazonS3 s3 = AmazonS3ClientBuilder.standard().withRegion("us-east-1").withForceGlobalBucketAccessEnabled(true).build();
        String bucket_name = "webapp.tejaswi.chillakuru";
        try {
            long s3StartTime = System.currentTimeMillis();
            s3.deleteObject(bucket_name, files.getS3objectName());
            long s3EndTime = System.currentTimeMillis();
            long s3Duration = s3EndTime-s3StartTime;
            client.recordExecutionTime("s3 Duration /question/{question_id}/file/{file_id}",s3Duration);
        } catch (AmazonServiceException e) {
            logger.error(e.getErrorMessage());
            System.exit(1);
            return new ResponseEntity<>("Id not found",HttpStatus.NOT_FOUND);
        }
        if(!files.getUserId().equals(loggedUser.getUserId())) {
            logger.warn("Cannot Delete File");
            return new ResponseEntity<>("Cannot Delete File", HttpStatus.UNAUTHORIZED);
        }
        try {

            long dbStartTime = System.currentTimeMillis();
            userDao.deleteFile(question_id, file_id);
            long dbEndTime = System.currentTimeMillis();
            long dbDuration = dbEndTime-dbStartTime;
            client.recordExecutionTime("db call Duration /question/{question_id}/file/{file_id}",dbDuration);

            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;
            client.recordExecutionTime("/question/{question_id}/file/{file_id",duration);

            return new ResponseEntity<Void>(HttpStatus.NO_CONTENT);
        }catch(Exception e){
            logger.error("Id not found");
            return new ResponseEntity<>("Id not found",HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping(value="/question/{question_id}/answer/{answer_id}/file",consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Object uploadAnswerFile(@AuthenticationPrincipal User loggedUser, @PathVariable String question_id ,@PathVariable String answer_id , @RequestParam(value = "file") MultipartFile file){
        logger.info("starting uploadAnswerFile method");
        long startTime = System.currentTimeMillis();
        client.incrementCounter("/question/{question_id}/answer/{answer_id}/file");

        Answers ans = userDao.getAnswer(answer_id);
        if(ans==null)
            return new ResponseEntity<>("Id Not Found",HttpStatus.NOT_FOUND);
        if(!ans.getUserId().equals(loggedUser.getUserId()))
            return new ResponseEntity<>("Cannot Upload File",HttpStatus.UNAUTHORIZED);

        final AmazonS3 s3 = AmazonS3ClientBuilder.standard().withRegion("us-east-1").withForceGlobalBucketAccessEnabled(true).build();
        String bucket_name = "webapp.tejaswi.chillakuru";
        UUID uuid = UUID.randomUUID();
        String keyName = answer_id+"/"+uuid.toString()+"/"+file.getOriginalFilename();
        String extension = FilenameUtils.getExtension(file.getOriginalFilename());

        if(!extension.equals("png")&&!extension.equals("jpg")&&!extension.equals("jpeg")){
            return new ResponseEntity<>("Invalid Image Type",HttpStatus.BAD_REQUEST);
        }
        try{
            AnswerFiles f = new AnswerFiles();
            f.setFileId(uuid.toString());
            f.setUserId(loggedUser.getUserId());
            f.setMime(extension);
            f.setAnswerId(answer_id);
            f.setFileName(file.getName());
            f.setSize(String.valueOf(file.getSize()));
            f.setS3objectName(keyName);


            long dbStartTime = System.currentTimeMillis();
            AnswerFiles output = userDao.saveAnswerFile(f);
            long dbEndTime = System.currentTimeMillis();
            long dbDuration = dbEndTime-dbStartTime;
            client.recordExecutionTime("db call Duration /question/{question_id}/answer/{answer_id}/file",dbDuration);

            File convFile = new File(file.getOriginalFilename());
            convFile.createNewFile();
            FileOutputStream fos = new FileOutputStream(convFile);
            fos.write(file.getBytes());
            fos.close();

            long s3StartTime = System.currentTimeMillis();
            s3.putObject(bucket_name,keyName,convFile);
            long s3EndTime = System.currentTimeMillis();
            long s3Duration = s3EndTime-s3StartTime;
            client.recordExecutionTime("s3 Duration /question/{question_id}/answer/{answer_id}/file",s3Duration);

            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;
            client.recordExecutionTime("/question/{question_id}/answer/{answer_id}/file",duration);

            return output;

        }catch(AmazonServiceException | IOException e){
            logger.error(e.getMessage());
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @DeleteMapping(value="/question/{question_id}/answer/{answer_id}/file/{file_id}")
    public Object deleteAnswerFile(@AuthenticationPrincipal User loggedUser, @PathVariable String question_id,@PathVariable String answer_id, @PathVariable String file_id ){
        logger.info("starting deleteAnswerFile method");
        long startTime = System.currentTimeMillis();
        client.incrementCounter("/question/{question_id}/answer/{answer_id}/file/{file_id}");

        AnswerFiles files = userDao.getAnswerFile(file_id);
        if(files==null)
            return new ResponseEntity<>("Id Not Found",HttpStatus.NOT_FOUND);
        if(!files.getUserId().equals(loggedUser.getUserId()))
            return new ResponseEntity<>("Cannot Delete File",HttpStatus.UNAUTHORIZED);

        final AmazonS3 s3 = AmazonS3ClientBuilder.standard().withRegion("us-east-1").withForceGlobalBucketAccessEnabled(true).build();
        String bucket_name = "webapp.tejaswi.chillakuru";

        try {
            long s3StartTime = System.currentTimeMillis();
            s3.deleteObject(bucket_name, files.getS3objectName());
            long s3EndTime = System.currentTimeMillis();
            long s3Duration = s3EndTime-s3StartTime;
            client.recordExecutionTime("s3 Duration /question/{question_id}/answer/{answer_id}/file/{file_id}",s3Duration);
        } catch (AmazonServiceException e) {
            System.err.println(e.getErrorMessage());
            System.exit(1);
            return new ResponseEntity<>("Id not found",HttpStatus.NOT_FOUND);
        }
        if(!files.getUserId().equals(loggedUser.getUserId()))
            return new ResponseEntity<>("Cannot Delete File",HttpStatus.UNAUTHORIZED);
        try {
            long dbStartTime = System.currentTimeMillis();
            userDao.deleteAnswerFile(answer_id, file_id);
            long dbEndTime = System.currentTimeMillis();
            long dbDuration = dbEndTime-dbStartTime;
            client.recordExecutionTime("db call Duration /question/{question_id}/answer/{answer_id}/file/{file_id}",dbDuration);

            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;
            client.recordExecutionTime("/question/{question_id}/answer/{answer_id}/file/{file_id}",duration);

            return new ResponseEntity<Void>(HttpStatus.NO_CONTENT);
        }catch(Exception e){
            logger.error("Id not found");
            logger.error(e.getMessage());
            return new ResponseEntity<>("Id not found",HttpStatus.NOT_FOUND);
        }
    }


}
