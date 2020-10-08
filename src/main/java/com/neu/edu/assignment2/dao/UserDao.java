package com.neu.edu.assignment2.dao;

import com.neu.edu.assignment2.model.Answers;
import com.neu.edu.assignment2.model.Categories;
import com.neu.edu.assignment2.model.Question;
import com.neu.edu.assignment2.model.User;
import com.neu.edu.assignment2.repository.AnswerRepository;
import com.neu.edu.assignment2.repository.CategoryRepository;
import com.neu.edu.assignment2.repository.QuestionRepository;
import com.neu.edu.assignment2.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
public class UserDao {
    @Autowired
    UserRepository userRepository;
    @Autowired
    private PasswordEncoder bcryptEncoder;
    @Autowired
    QuestionRepository questionRepository;
    @Autowired
    CategoryRepository categoriesRepository;
    @Autowired
    AnswerRepository answerRepository;

    public User addUser(User user){
        user.setPassword(bcryptEncoder.encode(user.getPassword()));
        String date = String.valueOf(java.time.LocalDateTime.now());
        user.setAccountUpdated(date);
        user.setAccountCreated(date);
        return userRepository.save(user);
    }

    public User getUser(String userId){
        return userRepository.findById(userId).get();
    }
    public User updateUser(String userId, User user){

        User u = getUser(userId);
        if(u==null)
            return null;
        if(user.getFirstName()!=null)
        u.setFirstName(user.getFirstName());
        if(user.getLastName()!=null)
        u.setLastName(user.getLastName());
        if(user.getPassword()!=null)
        u.setPassword(bcryptEncoder.encode(user.getPassword()));
        String date = String.valueOf(java.time.LocalDateTime.now());
        u.setAccountUpdated(date);
        return userRepository.save(u);
    }

    public Question addQuestion(Question question) throws Exception {
//        List<Categories> list = question.getCategories();
//        int i=0;
//        for(Categories c : list){
//            //c.setQuestionId(question.getQuestionId());
//            Categories cat = categoriesRepository.findByCategory(c.getCategory());
//            if(cat!=null)
//                c.setCategoryId(cat.getCategoryId());
//            addCategory(c);
//        }
//        question.setCategories(list);
        String date = String.valueOf(java.time.LocalDateTime.now());
        question.setCreatedTimestamp(date);
        question.setUpdatedTimestamp(date);
        List<Categories> categoryList = new ArrayList<>();
        List<String> categories = new ArrayList<>();
        if(question.getCategories()!=null&&question.getCategories().size()>0){
            for(Categories cat:question.getCategories()){
                String catLower=cat.getCategory().toLowerCase().trim();
                if(catLower.equals(""))
                    throw new Exception();
                if(!categories.contains(catLower)){
                    Categories existingCat= categoriesRepository.findByCategory(catLower);
                    if(existingCat==null){
                        cat.setCategory(catLower);
                        existingCat=cat;
                        existingCat=categoriesRepository.save(existingCat);
                    }
                    categories.add(catLower);
                    categoryList.add(existingCat);
                }
            }
            question.setCategories(null);
            question.setCategories(categoryList);
        }
        Question q = questionRepository.save(question);

        return q;
    }
    public Categories addCategory(Categories category){
        try {
            return categoriesRepository.save(category);
        }catch(Exception e){
            return null;
        }
    }
    public Answers addAnswer(Answers answer){
        String date = String.valueOf(java.time.LocalDateTime.now());
        answer.setCreatedTimestamp(date);
        answer.setUpdatedTimestamp(date);
        return answerRepository.save(answer);
    }
    public Object updateAnswer(Answers answer){
        try {
            String date = String.valueOf(java.time.LocalDateTime.now());
            Answers ans = getAnswer(answer.getAnswerId());
            if (ans == null)
                return null;
            answer.setCreatedTimestamp(ans.getCreatedTimestamp());
            answer.setUpdatedTimestamp(date);
            return answerRepository.save(answer);
        }catch(Exception e){
            return null;
        }
    }
    public Answers getAnswer(String answer_id){
        try {
            return answerRepository.findById(answer_id).get();
        }catch(Exception e){
            return null;
        }
    }
    public List<Question> getAllQuestions(){
        return questionRepository.findAll();
    }
    public Question getQuestion(String questionId){
        try {
            return questionRepository.findById(questionId).get();
        }catch(Exception e){
            return null;
        }
    }
    public void deleteAnswer(String question_id, String answer_id){
        answerRepository.deleteById(answer_id);
    }
    public void deleteQuestion(String questionId, String userId){
        questionRepository.deleteById(questionId);
    }
    public Object updateQuestion(Question question, Question q, String userId) throws Exception {
        question.setCreatedTimestamp(q.getCreatedTimestamp());
        question.setUserId(userId);
        String date = String.valueOf(java.time.LocalDateTime.now());
        question.setUpdatedTimestamp(date);
        question.setAnswers(q.getAnswers());
        List<Categories> categoryList = new ArrayList<>();
        List<String> categories = new ArrayList<>();
        if(question.getCategories()!=null&&question.getCategories().size()>0){
            for(Categories cat:question.getCategories()){
                String catLower=cat.getCategory().toLowerCase().trim();
                if(catLower.equals(""))
                    throw new Exception();
                if(!categories.contains(catLower)){
                    Categories existingCat= categoriesRepository.findByCategory(catLower);
                    if(existingCat==null){
                        cat.setCategory(catLower);
                        existingCat=cat;
                        existingCat=categoriesRepository.save(existingCat);
                    }
                    categories.add(catLower);
                    categoryList.add(existingCat);
                }
            }
            question.setCategories(null);
            question.setCategories(categoryList);
        }
        try {
            return questionRepository.save(question);
        }catch(Exception e){
            return null;
        }
    }

}
