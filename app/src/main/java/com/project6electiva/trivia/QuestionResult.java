// QuestionResult.java
package com.project6electiva.trivia;

import android.os.Parcel;
import android.os.Parcelable;

public class QuestionResult implements Parcelable {
    public String questionText;
    public boolean isCorrect;
    public String correctAnswer;

    public QuestionResult() {}

    public QuestionResult(String questionText, boolean isCorrect, String correctAnswer) {
        this.questionText = questionText;
        this.isCorrect = isCorrect;
        this.correctAnswer = correctAnswer;
    }

    protected QuestionResult(Parcel in) {
        questionText = in.readString();
        isCorrect = in.readByte() != 0;
        correctAnswer = in.readString();
    }

    public static final Creator<QuestionResult> CREATOR = new Creator<QuestionResult>() {
        @Override
        public QuestionResult createFromParcel(Parcel in) {
            return new QuestionResult(in);
        }

        @Override
        public QuestionResult[] newArray(int size) {
            return new QuestionResult[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(questionText);
        dest.writeByte((byte) (isCorrect ? 1 : 0));
        dest.writeString(correctAnswer);
    }
}