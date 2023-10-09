/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package coe528.project;

/**
 *
 * @author danie
 */
public class SilverCustomer extends CustomerStatus{
private String stat;

public SilverCustomer(){
    stat="Silver";
}
   
@Override
public void changeStatus(Customer c){
    c.setState(new GoldCustomer());
}

@Override
public String currentStatus(){
    return stat;
}
}

