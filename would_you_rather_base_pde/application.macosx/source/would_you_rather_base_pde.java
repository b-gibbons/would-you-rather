import processing.core.*; 
import processing.data.*; 
import processing.event.*; 
import processing.opengl.*; 

import java.awt.event.KeyEvent; 
import de.voidplus.redis.*; 
import development.*; 
import redis.clients.jedis.*; 
import redis.clients.jedis.exceptions.*; 
import redis.clients.util.*; 

import java.util.HashMap; 
import java.util.ArrayList; 
import java.io.File; 
import java.io.BufferedReader; 
import java.io.PrintWriter; 
import java.io.InputStream; 
import java.io.OutputStream; 
import java.io.IOException; 

public class would_you_rather_base_pde extends PApplet {








ArrayList<String> QuestionGUIDs = new ArrayList();
String CurrentQuestionGUID = "";
String Question = "Would you rather...?";
String Option1 = "";
String Option2 = "";
String Server = "::1";
float[] questionPercentages = { 0, 0 };
Redis redis;

public void setup() {
  redis = new Redis(this, Server, 6379);
  
  for (String name : redis.keys("Question.GUIDs.*")) {
    QuestionGUIDs.add(redis.get(name));
    println(redis.get(name));
  }
  drawQuestion();
}
public void drawQuestion() {
  CurrentQuestionGUID = QuestionGUIDs.get(PApplet.parseInt(random(0, QuestionGUIDs.size())));
  Option1 = redis.get("Question." + CurrentQuestionGUID + ".Option1");
  Option2 = redis.get("Question." + CurrentQuestionGUID + ".Option2");
  fill(255, 255, 255);
  background(150, 150, 250);
  rect(50, 400, 700, 300);
  line(400, 400, 400, 700);
  fill(0, 0, 0);
  text(Option1, 75, 475);
  text(Option2, 425, 475);
  scale(3);
  text(Question, 75, 75);
}
public void draw() {}

public void mouseClicked() {
  boolean optClicked = true;
  if (inSquare(50, 400, 400, 700)) {
    println("Option 1");
    redis.incr("Question." + CurrentQuestionGUID + ".Option1.Count");
  } else if (inSquare(400, 400, 750, 700)) {
    println("Option 2");
    redis.incr("Question." + CurrentQuestionGUID + ".Option2.Count");
  }
  if (optClicked) {
  try {
    questionPercentages[0] = PApplet.parseInt(redis.get("Question." + CurrentQuestionGUID + ".Option1.Count"));
  } catch (NullPointerException e) {
    questionPercentages[0] = 0;
  }
    try {
    questionPercentages[1] = PApplet.parseInt(redis.get("Question." + CurrentQuestionGUID + ".Option2.Count"));
  } catch (NullPointerException e) {
    questionPercentages[1] = 0;
  }
    float totalCount = questionPercentages[0] + questionPercentages[1];
    questionPercentages[0] = (questionPercentages[0]/totalCount)*360;
    questionPercentages[1] = (questionPercentages[1]/totalCount)*360;
  drawPieChart(500, questionPercentages);
}
}

public boolean inSquare(float x1, float y1, float x2, float y2) {
  return (mouseX>x1)&&(mouseX<x2)&&(mouseY>y1)&&(mouseY<y2);
}

public void drawPieChart(float diameter, float[] data) {
  background(150, 150, 250);
  if (data[0] > data[1]) {
    text("Most people picked Option 1! Press 'n' for a new question!", 50, 50);
  } else if (data[0] < data[1]) {
    text("Most people picked Option 2! Press 'n' for a new question!", 50, 50);
  } else {
    text("It was a tie!", 50, 50);
  }
  float lastAngle = 0;
  for (int i = 0; i < data.length; i++) {
    float gray = map(i, 0, data.length, 0, 255);
    fill(gray);
    arc(width/2, height/2, diameter, diameter, lastAngle, lastAngle+radians(data[i]));
    lastAngle += radians(data[i]);
  }
}

public void keyPressed() {
  switch (key) {
  case 'n':
    drawQuestion();
  }
}
  public void settings() {  size(800, 800); }
  static public void main(String[] passedArgs) {
    String[] appletArgs = new String[] { "would_you_rather_base_pde" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }
}
