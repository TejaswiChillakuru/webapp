package com.neu.edu.assignment2.controller;


import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.neu.edu.assignment2.dao.UserDao;
import com.neu.edu.assignment2.model.*;
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
//    @Autowired
//    private StatsDClient statsDClient;

    private final static Logger logger = LoggerFactory.getLogger(UserController.class);
    private final NonBlockingStatsDClient client = new NonBlockingStatsDClient("webapp", "localhost", 8125);
    @PostMapping(value = "/user")
    @ApiOperation(value="Create a user")
    public Object addUser(@RequestBody User user){
        long startTime = System.currentTimeMillis();
        logger.info("This is information message");
        logger.warn("This is Warning message");
        logger.error("This is Error message");
        client.incrementCounter("/user");
        //client.incrementCounter("endpoint.homepage.http.get.version");
        try{
            String regex = "^[\\w-_\\.+]*[\\w-_\\.]\\@([\\w]+\\.)+[\\w]+[\\w]$";
            String password = "^([a-zA-Z0-9@*#]{8,15})$";
            if((user.getUsername()!=null&&!user.getUsername().matches(regex))){
                return new ResponseEntity<>("Please enter valid Username",HttpStatus.BAD_REQUEST);
            }
            if(user.getPassword()!=null&&!user.getPassword().matches(password)){
                return new ResponseEntity<>("Please enter valid password",HttpStatus.BAD_REQUEST);
            }
            long endTime = System.nanoTime();

            long duration = endTime - startTime;
            client.recordExecutionTime("/user",duration);
            return userDao.addUser(user);
        }catch(Exception ex){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Bad Request",ex);
        }
    }


    @GetMapping(value="/user/self")
    @ApiOperation(value="Get User Information")
    public Object getUser(@AuthenticationPrincipal User user){
        client.incrementCounter("GET    /user/self");
        User u = userDao.getUser(user.getUserId());
        try {
            if (!u.getPassword().equalsIgnoreCase(user.getPassword())) {
                return new ResponseEntity<>("Invalid Credentials",HttpStatus.FORBIDDEN);
            }
        }catch(Exception ex){
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Invalid Credentials",ex);
        }
        return u;
    }
    @PutMapping(value="/user/self")
    @ApiOperation(value="Update User Information")
    public Object updateUser(@AuthenticationPrincipal User loggedUser , @RequestBody User user){
        client.incrementCounter("POST   /user/self");
        try {

            if (user.getUsername() != null || user.getAccountCreated() != null || user.getAccountUpdated() != null) {
                return new ResponseEntity<>("Cannot update username/accountupdated/account created fields",HttpStatus.BAD_REQUEST);
            }
            String password = "^([a-zA-Z0-9@*#]{8,15})$";
            if(user.getPassword()!=null&&!user.getPassword().matches(password)){
                return new ResponseEntity<>("Please enter valid password",HttpStatus.BAD_REQUEST);
            }
            userDao.updateUser(loggedUser.getUserId(), user);
            return HttpStatus.NO_CONTENT;
        }catch(Exception e){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Bad Request",e);
        }
    }
    @GetMapping("/user/{id}")
    public Object getUserDetails(@PathVariable String id){
        client.incrementCounter("/user/{id}");
        try {
            return userDao.getUser(id);
        }catch(Exception e){
            return new ResponseEntity<>("User not found",HttpStatus.NOT_FOUND);
        }
    }
    @PostMapping(value="/question")
    public Object addQuestions(@AuthenticationPrincipal User loggedUser,@RequestBody Question question){
        try {
            client.incrementCounter("/question");
            question.setUserId(loggedUser.getUserId());
            Question q = userDao.addQuestion(question);;
            return new ResponseEntity<Question>(q,HttpStatus.CREATED);
        }catch(Exception e){
            return new ResponseEntity<>("Bad Request",HttpStatus.BAD_REQUEST);
        }
    }
    @PostMapping(value="/question/{question_id}/answer")
    public Object addAnswers(@AuthenticationPrincipal User loggedUser, @RequestBody Answers answer, @PathVariable String question_id){
        try {
            client.incrementCounter("/question/{question_id}/answer");
            answer.setUserId(loggedUser.getUserId());
            answer.setQuestionId(question_id);
            return userDao.addAnswer(answer);
        }catch(Exception e){
            return new ResponseEntity<>("Bad Request",HttpStatus.BAD_REQUEST);
        }
    }
    @PutMapping(value="/question/{question_id}/answer/{answer_id}")
    public Object updateAnswer(@AuthenticationPrincipal User loggedUser,@RequestBody Answers answer, @PathVariable String question_id, @PathVariable String answer_id){
        client.incrementCounter("PUT    /question/{question_id}/answer/{answer_id}");
        Answers ans = userDao.getAnswer(answer_id);
        if(ans==null)
            return new ResponseEntity<>("Id Not Found",HttpStatus.NOT_FOUND);
        if(!ans.getUserId().equals(loggedUser.getUserId()))
            return new ResponseEntity<>("Cannot Update Answer",HttpStatus.UNAUTHORIZED);
        answer.setUserId(loggedUser.getUserId());
        answer.setQuestionId(question_id);
        answer.setAnswerId(answer_id);
        Answers a = (Answers)userDao.updateAnswer(answer);
        if(a==null)
            return new ResponseEntity<>("Please enter valid input",HttpStatus.BAD_REQUEST);
        return new ResponseEntity<Void>(HttpStatus.NO_CONTENT);

    }
    @GetMapping(value="/question/{question_id}/answer/{answer_id}")
    public Object getAnswer(@PathVariable String question_id, @PathVariable String answer_id){
        try {
            client.incrementCounter("GET    /question/{question_id}/answer/{answer_id}");
            return userDao.getAnswer(answer_id);
        }catch(Exception e){
            return new ResponseEntity<>("Id not found",HttpStatus.NOT_FOUND);
        }
    }
    @GetMapping(value="/questions")
    public List<Question> getAllQuestions(){
        client.incrementCounter("/questions");
        return userDao.getAllQuestions();
    }
    @GetMapping(value="/question/{questionId}")
    public Object getQuestion(@PathVariable String questionId){
        client.incrementCounter("/question/{questionId}");
        Question q =  userDao.getQuestion(questionId);
        if(q==null)
            return new ResponseEntity<>("Id not found",HttpStatus.NOT_FOUND);
        return q;

    }
    @DeleteMapping(value="/question/{question_id}/answer/{answer_id}")
    public Object deleteAnswer(@AuthenticationPrincipal User loggedUser, @PathVariable String question_id, @PathVariable String answer_id){
        client.incrementCounter("DELETE    /question/{question_id}/answer/{answer_id}");
        Answers ans = userDao.getAnswer(answer_id);
        if(!ans.getUserId().equals(loggedUser.getUserId()))
            return new ResponseEntity<>("Cannot Delete Answer",HttpStatus.UNAUTHORIZED);
        try {
            userDao.deleteAnswer(question_id, answer_id);
            return new ResponseEntity<Void>(HttpStatus.NO_CONTENT);
        }catch(Exception e){
            return new ResponseEntity<>("Id not found",HttpStatus.NOT_FOUND);
        }
    }
    @DeleteMapping(value="/question/{question_id}")
    public Object deleteQuestion(@AuthenticationPrincipal User loggedUser,@PathVariable String question_id){
        client.incrementCounter("DELETE   /question/{question_id}");
        Question q = (Question)getQuestion(question_id);
        if(q==null)
            return new ResponseEntity<>("Id not found",HttpStatus.NOT_FOUND);
        if(!q.getAnswers().isEmpty())
            return new ResponseEntity<>("Question cannot be deleted",HttpStatus.BAD_REQUEST);
        if(!loggedUser.getUserId().equals(q.getUserId()))
            return new ResponseEntity<>("User Cannot Update/delete question",HttpStatus.UNAUTHORIZED);
        try {
            userDao.deleteQuestion(question_id, loggedUser.getUserId());
            return new ResponseEntity<Void>(HttpStatus.NO_CONTENT);
        }catch(Exception e){
            return new ResponseEntity<>("Id not found",HttpStatus.NOT_FOUND);
        }
    }
    @PutMapping(value="/question/{question_id}")
    public Object updateQuestion(@AuthenticationPrincipal User loggedUser, @RequestBody Question question, @PathVariable String question_id){
        client.incrementCounter("PUT   /question/{question_id}");
        question.setQuestionId(question_id);
        Question q = (Question) userDao.getQuestion(question_id);
        if(q==null)
            return new ResponseEntity<>("Id not found",HttpStatus.NOT_FOUND);
        if(!loggedUser.getUserId().equals(q.getUserId()))
            return new ResponseEntity<>("User Cannot Update/delete question",HttpStatus.UNAUTHORIZED);
        try {
            q = (Question)userDao.updateQuestion(question, q,loggedUser.getUserId());
        } catch (Exception e) {
            return new ResponseEntity<>("Unable to update question",HttpStatus.BAD_REQUEST);
        }
        if(q==null)
            return new ResponseEntity<>("Unable to update question",HttpStatus.BAD_REQUEST);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
    @PostMapping(value="/question/{question_id}/file",consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Object uploadQuestionFile(@AuthenticationPrincipal User loggedUser, @PathVariable String question_id , @RequestParam(value = "file") MultipartFile file){
        client.incrementCounter("/question/{question_id}/file");
        //        String accessKey="AKIASVHASTC5EPAU5TKX";
//        String secretKey="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";
        String accessKey=env.getProperty("aws-access-key-id");
        String secretKey=env.getProperty("aws-secret-access-key");
        Question q = (Question) userDao.getQuestion(question_id);
        if(q==null)
            return new ResponseEntity<>("Id not found",HttpStatus.NOT_FOUND);
        if(!loggedUser.getUserId().equals(q.getUserId()))
            return new ResponseEntity<>("User Cannot Update/delete question",HttpStatus.UNAUTHORIZED);
        BasicAWSCredentials creds = new BasicAWSCredentials(accessKey, secretKey);
        AWSStaticCredentialsProvider provider = new AWSStaticCredentialsProvider(creds);
        final AmazonS3 s3 = AmazonS3ClientBuilder.standard().withCredentials(provider).withRegion("us-east-1").withForceGlobalBucketAccessEnabled(true).build();
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
            QuestionFiles output = userDao.saveFile(f);

            File convFile = new File(file.getOriginalFilename());
            convFile.createNewFile();
            FileOutputStream fos = new FileOutputStream(convFile);
            fos.write(file.getBytes());
            fos.close();


            s3.putObject(bucket_name,keyName,convFile);
            return output;

        }catch(AmazonServiceException | IOException e){
            System.out.println(e.getMessage());
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }
    @DeleteMapping(value="/question/{question_id}/file/{file_id}")
    public Object deleteFile(@AuthenticationPrincipal User loggedUser, @PathVariable String question_id, @PathVariable String file_id ){
        //        String accessKey="AKIASVHASTC5EPAU5TKX";
//        String secretKey="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";
        client.incrementCounter("/question/{question_id}/file/{file_id");
        String accessKey=env.getProperty("aws-access-key-id");
        String secretKey=env.getProperty("aws-secret-access-key");
        QuestionFiles files = userDao.getFile(file_id);
        if(files==null)
            return new ResponseEntity<>("Id not found",HttpStatus.NOT_FOUND);
        if(!loggedUser.getUserId().equals(files.getUserId()))
            return new ResponseEntity<>("User Cannot Update/delete question",HttpStatus.UNAUTHORIZED);
        BasicAWSCredentials creds = new BasicAWSCredentials(accessKey, secretKey);
        AWSStaticCredentialsProvider provider = new AWSStaticCredentialsProvider(creds);
        final AmazonS3 s3 = AmazonS3ClientBuilder.standard().withCredentials(provider).withRegion("us-east-1").withForceGlobalBucketAccessEnabled(true).build();
        String bucket_name = "webapp.tejaswi.chillakuru";
        try {
            s3.deleteObject(bucket_name, files.getS3objectName());
        } catch (AmazonServiceException e) {
            System.err.println(e.getErrorMessage());
            System.exit(1);
            return new ResponseEntity<>("Id not found",HttpStatus.NOT_FOUND);
        }
        if(!files.getUserId().equals(loggedUser.getUserId()))
            return new ResponseEntity<>("Cannot Delete File",HttpStatus.UNAUTHORIZED);
        try {
            System.out.println(file_id);
            userDao.deleteFile(question_id, file_id);
            return new ResponseEntity<Void>(HttpStatus.NO_CONTENT);
        }catch(Exception e){
            System.out.println(e.getMessage());
            return new ResponseEntity<>("Id not found",HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping(value="/question/{question_id}/answer/{answer_id}/file",consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Object uploadAnswerFile(@AuthenticationPrincipal User loggedUser, @PathVariable String question_id ,@PathVariable String answer_id , @RequestParam(value = "file") MultipartFile file){
        //        String accessKey="AKIASVHASTC5EPAU5TKX";
//        String secretKey="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";
        client.incrementCounter("/question/{question_id}/answer/{answer_id}/file");
        String accessKey=env.getProperty("aws-access-key-id");
        String secretKey=env.getProperty("aws-secret-access-key");
        Answers ans = userDao.getAnswer(answer_id);
        if(ans==null)
            return new ResponseEntity<>("Id Not Found",HttpStatus.NOT_FOUND);
        if(!ans.getUserId().equals(loggedUser.getUserId()))
            return new ResponseEntity<>("Cannot Upload File",HttpStatus.UNAUTHORIZED);
        BasicAWSCredentials creds = new BasicAWSCredentials(accessKey, secretKey);
        AWSStaticCredentialsProvider provider = new AWSStaticCredentialsProvider(creds);
        final AmazonS3 s3 = AmazonS3ClientBuilder.standard().withCredentials(provider).withRegion("us-east-1").withForceGlobalBucketAccessEnabled(true).build();
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
            AnswerFiles output = userDao.saveAnswerFile(f);

            File convFile = new File(file.getOriginalFilename());
            convFile.createNewFile();
            FileOutputStream fos = new FileOutputStream(convFile);
            fos.write(file.getBytes());
            fos.close();


            s3.putObject(bucket_name,keyName,convFile);
            return output;

        }catch(AmazonServiceException | IOException e){
            System.out.println(e.getMessage());
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @DeleteMapping(value="/question/{question_id}/answer/{answer_id}/file/{file_id}")
    public Object deleteAnswerFile(@AuthenticationPrincipal User loggedUser, @PathVariable String question_id,@PathVariable String answer_id, @PathVariable String file_id ){
        //        String accessKey="AKIASVHASTC5EPAU5TKX";
//        String secretKey="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";
        client.incrementCounter("/question/{question_id}/answer/{answer_id}/file/{file_id}");
        String accessKey=env.getProperty("aws-access-key-id");
        String secretKey=env.getProperty("aws-secret-access-key");
        AnswerFiles files = userDao.getAnswerFile(file_id);
        if(files==null)
            return new ResponseEntity<>("Id Not Found",HttpStatus.NOT_FOUND);
        if(!files.getUserId().equals(loggedUser.getUserId()))
            return new ResponseEntity<>("Cannot Delete File",HttpStatus.UNAUTHORIZED);
        BasicAWSCredentials creds = new BasicAWSCredentials(accessKey, secretKey);
        AWSStaticCredentialsProvider provider = new AWSStaticCredentialsProvider(creds);
        final AmazonS3 s3 = AmazonS3ClientBuilder.standard().withCredentials(provider).withRegion("us-east-1").withForceGlobalBucketAccessEnabled(true).build();
        String bucket_name = "webapp.tejaswi.chillakuru";
        try {
            s3.deleteObject(bucket_name, files.getS3objectName());
        } catch (AmazonServiceException e) {
            System.err.println(e.getErrorMessage());
            System.exit(1);
            return new ResponseEntity<>("Id not found",HttpStatus.NOT_FOUND);
        }
        if(!files.getUserId().equals(loggedUser.getUserId()))
            return new ResponseEntity<>("Cannot Delete File",HttpStatus.UNAUTHORIZED);
        try {
            System.out.println(file_id);
            userDao.deleteAnswerFile(answer_id, file_id);
            return new ResponseEntity<Void>(HttpStatus.NO_CONTENT);
        }catch(Exception e){
            System.out.println(e.getMessage());
            return new ResponseEntity<>("Id not found",HttpStatus.NOT_FOUND);
        }
    }

}
