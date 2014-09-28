//=============================================================================
// Uses the GiftSearcher class to find product combination that fit the user's
// parameters.
//
// VIGNESH NARAYANAN
//=============================================================================
package vignesh_zappos_challenge_solution;

import java.io.*;
import org.json.simple.parser.*;

public class GiftSearch {

	/**
	 * Takes user inputs from System.in and finds gift combinations that fit
	 * @param args None
	 */
	public static void main(String[] args) {
		
		//---------------------------------------------------------------------
		// get inputs from user
		//---------------------------------------------------------------------
		boolean noInput = true;
		int numItems = 0;
		double totalPrice = 0;
		
		//keep running if user had incorrect inputs
		while(noInput){
			String numItemsRequest = "How many items would you like to search for? ";
			String numItemsString = Parsing.prompt(numItemsRequest);
			String totalPriceRequest = "How much should the total price be (in dollars)? $";
			String totalPriceString = Parsing.prompt(totalPriceRequest);
			
			boolean errors = false;
			
			//check to make sure number of items is an integer
			try {
				numItems = Integer.parseInt(numItemsString);
			} catch (NumberFormatException e){
				System.err.println("Number of items must be an integer greater than 0. Restarting: ");
				errors = true;
			}
			
			//check to make sure total price is a double
			try {
				totalPrice = Double.parseDouble(totalPriceString);
			} catch (NumberFormatException e){
				System.err.println("Total price must be a number greater than 0. Restarting: ");
				errors = true;
			}
			
			//check to make sure number of items is a valid integer
			if(numItems < 1 && !errors) {
				System.out.println("Number of items must be greater than 0. Restarting:");
			} 
			//check to make sure total price is a valid double
			else if(totalPrice <= 0 && !errors) {
				System.out.println("Total price must be greater than 0. Restarting:");
			} 
			//otherwise, if no errors, then have valid inputs and can stop loop
			else if(!errors){
				noInput = false;
			}
		}
		
		//---------------------------------------------------------------------
		// have usable inputs, so run search
		//---------------------------------------------------------------------
		try {
			GiftSearcher searcher = new GiftSearcher(numItems, totalPrice);
			System.out.println(searcher.getGiftCombos());
			
		} catch (ParseException e) {
			// occurs if parsed incorrectly
			System.err.println("Sorry, we couldn't parse the server's response.");
			e.printStackTrace();
		} catch (IOException e) {
			// occurs if response code wasn't 200
			System.err.println("Sorry, something went wrong with our search.");
			e.printStackTrace();
		}
	}
}
