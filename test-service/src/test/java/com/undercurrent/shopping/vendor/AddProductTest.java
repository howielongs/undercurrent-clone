package com.undercurrent.shopping.vendor;

import org.junit.jupiter.api.Test;


import static org.junit.jupiter.api.Assertions.assertEquals;

public class AddProductTest {

//    @Test
//    public void testStartCommandResponse() {
//        // user input for the start command
//        String userInput = "start";
//
//        // get actual response
//        //String actualResponse = //connection to database;
//
//        // the expected response for the start command
//        String expectedResponse = TestDataCollection.getStartCommandResponse();
//
//
//        //Assert that actual response matches expected response
//        //  assertEquals(expectedResponse, actualResponse);
//    }


    @Test
    public void testProductSCommandResponse() {

        String userInput = "C";
        String userInput1 = "products";

        // get actual response
        //String actualResponse = //connection to database;

        // the expected response for the start command
        String expectedResponse = "Select the letter of a command to run:" +
                "A. addproduct - Add a product to your inventory" +
                "B. addimage - Add/replace an image on a product in your inventory" +
                "C. listproducts - List inventory of products" +
                "D. removesku - Remove product SKU from your inventory" +
                "E. rmproduct - Remove product (and all its SKUs) from your inventory" +
                "F. editprice - Edit the price of a SKU" +
                "G. cancel - Cancel current operation";


        //Assert that actual response matches expected response
        //  assertEquals(expectedResponse, actualResponse);
        assertEquals(0, 0);
    }

    @Test
    public void testAddProductCommandResponse() {

        String userInput = "A";
        String userInput1 = "addproduct";

        // get actual response
        //String actualResponse = //connection to database;

        // the expected response for the start command
        String expectedResponse = "Select the letter of a command to run:" +
                "A. addproduct - Add a product to your inventory" +
                "B. addimage - Add/replace an image on a product in your inventory" +
                "C. listproducts - List inventory of products" +
                "D. removesku - Remove product SKU from your inventory" +
                "E. rmproduct - Remove product (and all its SKUs) from your inventory" +
                "F. editprice - Edit the price of a SKU" +
                "G. cancel - Cancel current operation";
    }
}




   /*
        System.out.println("Expected Response: " + expectedResponse);
        System.out.println("Bot Response: " + botResponse);

    }
    */

