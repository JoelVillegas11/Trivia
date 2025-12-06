// GameRecord.java
package com.project6electiva.trivia;

import com.google.firebase.Timestamp;
import java.util.List;

public class GameRecord {
    public String id;
    public String difficulty;
    public String category;
    public long score;
    public long totalQuestions;
    public Timestamp timestamp;
    public List<QuestionResult> details;

    public GameRecord() {}
}