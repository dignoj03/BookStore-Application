/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package coe528.project;

import javafx.scene.control.CheckBox;

/**
 *
 * @author danie
 */
public class Books {
   
private String BookName;
private double BookPrice;
private CheckBox select;

public Books(String BookName, double bookPrice){
    this.BookName=BookName;
    this.BookPrice=bookPrice;
    this.select = new CheckBox();
}

public String getBookName(){
        return BookName;
}

public void setBookName(String BookName){
    this.BookName=BookName;
}

public double getBookPrice(){
    return BookPrice;
}

public void setBookPrice(double BookPrice){
    this.BookPrice=BookPrice;
}

public CheckBox getSelect(){
    return select;
}
public void setSelect(CheckBox select){
    this.select=select;
}
}

